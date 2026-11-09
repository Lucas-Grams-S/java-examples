package dev.estudos.cloud.k8s;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.k3s.K3sContainer;
import org.testcontainers.utility.DockerImageName;
import org.testcontainers.utility.MountableFile;

/**
 * CL09-08/09/10: um k3s real (contêiner privilegiado, imagem {@code rancher/k3s}). Os manifests de
 * {@code k8s/} são aplicados com {@code kubectl}; o app roda FORA do cluster (subprocesso com KUBECONFIG), apontando para a API.
 * (Dentro do cluster valem os mesmos beans: a diferença é só de onde vêm as credenciais, ver doc §3.1.)
 * Requer Docker; leva ~1 min (partida do k3s).
 */
@Testcontainers
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class K8sLabIT {
    @Container static final K3sContainer K3S = new K3sContainer(DockerImageName.parse("rancher/k3s:latest"));

    private final HttpClient http = HttpClient.newHttpClient();

    private String kubectl(String... args) throws Exception {
        var cmd = new String[args.length + 1];
        cmd[0] = "kubectl";
        System.arraycopy(args, 0, cmd, 1, args.length);
        var r = K3S.execInContainer(cmd);
        assertThat(r.getExitCode()).as("kubectl %s: %s", String.join(" ", args), r.getStderr()).isZero();
        return r.getStdout();
    }

    /** Sobe o app em subprocesso com KUBECONFIG apontando para o k3s (o cliente de discovery lê o kubeconfig padrão). */
    private Process sobe(int porta, String... extra) throws Exception {
        var kubeconfig = Files.createTempFile("kubeconfig", ".yaml");
        Files.writeString(kubeconfig, K3S.getKubeConfigYaml());
        var cmd = new ArrayList<>(List.of(
            Path.of(System.getProperty("java.home"), "bin", "java").toString(),
            "-cp", System.getProperty("java.class.path"), K8sApp.class.getName(),
            "--server.port=" + porta,
            "--spring.main.cloud-platform=kubernetes",   // fora do cluster o Boot não detecta a plataforma sozinho
            "--spring.cloud.kubernetes.client.namespace=default",
            "--spring.cloud.kubernetes.discovery.namespaces=default",
            "--spring.cloud.kubernetes.reload.period=2000"));
        cmd.addAll(List.of(extra));
        var pb = new ProcessBuilder(cmd).redirectErrorStream(true)
            .redirectOutput(ProcessBuilder.Redirect.to(Files.createTempFile("k8sapp-" + porta, ".log").toFile()));
        pb.environment().put("KUBECONFIG", kubeconfig.toString());
        return pb.start();
    }

    private String get(int porta, String path) throws Exception {
        return http.send(HttpRequest.newBuilder(URI.create("http://localhost:" + porta + path)).build(),
            HttpResponse.BodyHandlers.ofString()).body();
    }

    @Test @Order(1)
    void configReloadDiscoverySecretEManifests() throws Exception {
        for (var f : List.of("orders.yaml", "rbac.yaml", "deployment.yaml"))
            K3S.copyFileToContainer(MountableFile.forHostPath("k8s/" + f), "/tmp/" + f);
        await().atMost(Duration.ofMinutes(2)).untilAsserted(() -> kubectl("get", "serviceaccount", "default"));

        // CL09-10: manifests válidos (aqui o Deployment só é validado; a imagem real roda no teste seguinte, CL09-12)
        assertThat(kubectl("apply", "-f", "/tmp/orders.yaml", "-f", "/tmp/rbac.yaml")).contains("configmap/orders created");
        assertThat(kubectl("apply", "--dry-run=server", "-f", "/tmp/deployment.yaml")).contains("deployment.apps/orders");

        // CL09-09: ConfigMap lido; Secret NÃO lido por padrão (pwdLength 9 = "sem-senha")
        var app1 = sobe(18090);
        try {
            await().atMost(Duration.ofSeconds(60)).ignoreExceptions().untilAsserted(() ->
                assertThat(get(18090, "/cfg")).contains("ola-do-configmap").contains("\"pwdLength\":\"9\""));
            // discovery pela API: inclui o Service "kubernetes" do próprio cluster
            assertThat(get(18090, "/services")).contains("orders", "catalog", "kubernetes");
            // reload por polling, sem reiniciar o app
            kubectl("patch", "configmap", "orders", "--type=merge", "-p",
                "{\"data\":{\"application.yaml\":\"greeting: mudou-sem-reiniciar\\n\"}}");
            await().atMost(Duration.ofSeconds(40)).untilAsserted(() ->
                assertThat(get(18090, "/cfg")).contains("mudou-sem-reiniciar"));
        } finally { app1.destroy(); }
        // Secret só com spring.cloud.kubernetes.secrets.enabled=true (13 = "super-secreta")
        var app2 = sobe(18091, "--spring.cloud.kubernetes.secrets.enabled=true");
        try {
            await().atMost(Duration.ofSeconds(60)).ignoreExceptions().untilAsserted(() ->
                assertThat(get(18091, "/cfg")).contains("\"pwdLength\":\"13\""));
        } finally { app2.destroy(); }
    }

    private static String sh(Path dir, String... cmd) throws Exception {
        var p = new ProcessBuilder(cmd).directory(dir.toFile()).redirectErrorStream(true).start();
        String out = new String(p.getInputStream().readAllBytes());
        assertThat(p.waitFor()).as(String.join(" ", cmd) + "\n" + out).isZero();
        return out;
    }

    /**
     * CL09-12: Dockerfile do doc -> {@code docker build} -> {@code docker save} -> {@code ctr images import} -> Deployment
     * (2 réplicas, imagePullPolicy Never). DENTRO do cluster o Boot detecta a plataforma sozinho e usa a ServiceAccount.
     */
    @Test @Order(2)
    void imagemRodaDentroDoCluster() throws Exception {
        Path ctx = Files.createTempDirectory("ctx-k8s");
        Files.copy(Path.of("target/cl-09-kubernetes-1.0.0-SNAPSHOT-exec.jar"), ctx.resolve("app.jar"));
        Files.copy(Path.of("docker/Dockerfile"), ctx.resolve("Dockerfile"));
        sh(ctx, "docker", "build", "-t", "orders-k8s:1", ".");
        Path tar = ctx.resolve("img.tar");
        sh(ctx, "docker", "save", "-o", tar.toString(), "orders-k8s:1");
        try {
            K3S.copyFileToContainer(MountableFile.forHostPath(tar), "/tmp/img.tar");
            var imp = K3S.execInContainer("ctr", "images", "import", "/tmp/img.tar");
            assertThat(imp.getExitCode()).as(imp.getStderr()).isZero();
            for (var f : List.of("orders.yaml", "rbac.yaml", "deployment.yaml"))
                K3S.copyFileToContainer(MountableFile.forHostPath("k8s/" + f), "/tmp/" + f);
            kubectl("apply", "-f", "/tmp/orders.yaml", "-f", "/tmp/rbac.yaml", "-f", "/tmp/deployment.yaml");
            assertThat(kubectl("rollout", "status", "deployment/orders", "--timeout=240s")).contains("successfully rolled out");
            assertThat(kubectl("get", "deployment", "orders", "-o", "jsonpath={.status.readyReplicas}")).isEqualTo("2");
            String pod = kubectl("get", "pod", "-l", "app=orders", "-o", "jsonpath={.items[0].metadata.name}");
            // plataforma detectada sozinha (perfil "kubernetes") e config/discovery pela ServiceAccount do pod
            assertThat(kubectl("logs", pod)).contains("kubernetes");
            String base = "/api/v1/namespaces/default/pods/" + pod + ":8080/proxy";
            assertThat(kubectl("get", "--raw", base + "/cfg")).contains("greeting").contains(pod);
            assertThat(kubectl("get", "--raw", base + "/services")).contains("orders", "catalog", "kubernetes");
        } finally {
            sh(ctx, "docker", "rmi", "-f", "orders-k8s:1");
        }
    }
}
