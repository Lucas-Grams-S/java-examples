package com.example.lab;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.testcontainers.k3s.K3sContainer;
import org.testcontainers.utility.DockerImageName;
import org.testcontainers.images.builder.Transferable;
import org.testcontainers.utility.MountableFile;

/**
 * PP13: os dois serviços numa k3s real. ConfigMap/Secret por variáveis de ambiente, Deployments com 3 probes e
 * Services (a descoberta é o DNS do cluster: {@code http://catalog}). Verificação: um pedido criado em orders
 * (via proxy da API) chama o catalog por DNS e devolve o preço em BRL, que veio do ConfigMap.
 */
class Pp13KubernetesIT {
    @TempDir Path tmp;

    static String sh(Path dir, String... cmd) throws Exception {
        var p = new ProcessBuilder(cmd).directory(dir.toFile()).redirectErrorStream(true).start();
        String out = new String(p.getInputStream().readAllBytes());
        assertThat(p.waitFor()).as(String.join(" ", cmd) + "\n" + out).isZero();
        return out;
    }

    static String k(K3sContainer k3s, String... args) throws Exception {
        var cmd = new String[args.length + 1];
        cmd[0] = "kubectl";
        System.arraycopy(args, 0, cmd, 1, args.length);
        var r = k3s.execInContainer(cmd);
        assertThat(r.getExitCode()).as("kubectl %s: %s", String.join(" ", args), r.getStderr()).isZero();
        return r.getStdout();
    }

    @Test void pp13_doisServicosNoCluster() throws Exception {
        if (new ProcessBuilder("docker", "info").redirectErrorStream(true).start().waitFor() != 0) return;
        for (String svc : List.of("catalog", "orders")) {
            Path ctx = tmp.resolve("ctx-" + svc); Files.createDirectories(ctx);
            Files.copy(Lab.jarPp(svc + "-service"), ctx.resolve("app.jar"));
            Files.copy(Path.of("../docker/Dockerfile"), ctx.resolve("Dockerfile"));
            sh(ctx, "docker", "build", "-t", "pp-" + svc + "-service:k8s", ".");
            sh(ctx, "docker", "save", "-o", tmp.resolve(svc + ".tar").toString(), "pp-" + svc + "-service:k8s");
        }
        try (var k3s = new K3sContainer(DockerImageName.parse("rancher/k3s:latest"))) {
            k3s.start();
            for (String svc : List.of("catalog", "orders")) {
                k3s.copyFileToContainer(MountableFile.forHostPath(tmp.resolve(svc + ".tar")), "/tmp/" + svc + ".tar");
                var imp = k3s.execInContainer("ctr", "images", "import", "/tmp/" + svc + ".tar");
                assertThat(imp.getExitCode()).as(imp.getStderr()).isZero();
            }
            for (String f : List.of("platform", "catalog", "orders"))
                k3s.copyFileToContainer(MountableFile.forHostPath("../k8s/" + f + ".yaml"), "/tmp/" + f + ".yaml");
            for (int i = 0; i < 60; i++) {
                if (k3s.execInContainer("kubectl", "get", "serviceaccount", "default").getExitCode() == 0) break;
                Thread.sleep(2000);
            }
            // a descoberta é o DNS do cluster: sem o CoreDNS pronto, o catalog não resolve e o circuit breaker do orders abre
            assertThat(k(k3s, "-n", "kube-system", "rollout", "status", "deployment/coredns", "--timeout=240s")).contains("successfully rolled out");
            k3s.copyFileToContainer(Transferable.of("{\"productId\":1,\"quantity\":2}"), "/tmp/pedido.json");
            k(k3s, "apply", "-f", "/tmp/platform.yaml", "-f", "/tmp/catalog.yaml", "-f", "/tmp/orders.yaml");
            for (String svc : List.of("catalog", "orders"))
                assertThat(k(k3s, "rollout", "status", "deployment/" + svc, "--timeout=240s")).contains("successfully rolled out");

            // um pedido de orders (a API proxy fala com um pod) -> catalog por DNS -> preço em BRL (ConfigMap)
            String pod = k(k3s, "get", "pod", "-l", "app=orders", "-o", "jsonpath={.items[0].metadata.name}");
            var r = k3s.execInContainer("sh", "-c",
                "kubectl get --raw /api/v1/namespaces/default/services/orders:80/proxy/orders/stats");
            assertThat(r.getExitCode()).as(r.getStderr()).isZero();
            // criar pedido via POST: o proxy da API aceita POST em services/<nome>:<porta>/proxy/...
            // aquece o catalog (JVM fria + CPU limitada): senão as primeiras chamadas estouram o timeout de 1 s do orders
            // e o circuit breaker abre, que é exatamente o comportamento desejado, mas não o que este teste quer observar
            for (int i = 0; i < 30; i++) {
                var w = k3s.execInContainer("sh", "-c", "wget -qO- http://127.0.0.1:8001/api/v1/namespaces/default/services/catalog:80/proxy/products/1 || "
                    + "((kubectl proxy --port=8001 >/dev/null 2>&1 &) ; sleep 2; false)");
                if (w.getExitCode() == 0 && w.getStdout().contains("Teclado")) break;
                Thread.sleep(2000);
            }
            // o primeiro pedido pode estourar o timeout de 1 s do cliente (JVM fria sob carga): repete até aquecer, como um cliente real
            org.testcontainers.containers.Container.ExecResult post = null;
            for (int i = 0; i < 10; i++) {
                post = k3s.execInContainer("sh", "-c",
                    "(pgrep -f 'kubectl proxy' >/dev/null || (kubectl proxy --port=8001 >/dev/null 2>&1 &)) ; sleep 2; wget -qO- --header='Content-Type: application/json' "
                    + "--post-data='{\"productId\":1,\"quantity\":2}' "
                    + "http://127.0.0.1:8001/api/v1/namespaces/default/services/orders:80/proxy/orders");
                if (post.getExitCode() == 0) break;
                Thread.sleep(3000);
            }
            if (post.getExitCode() != 0) {   // diagnóstico: o que os pods dizem
                String diag = k3s.execInContainer("sh", "-c", "kubectl logs deploy/orders --tail=40; echo ---; kubectl logs deploy/catalog --tail=20; echo ---; kubectl get pods -o wide; kubectl get endpoints").getStdout();
                assertThat(post.getExitCode()).as(post.getStderr() + "\n" + diag).isZero();
            }
            assertThat(post.getExitCode()).as(post.getStderr()).isZero();
            assertThat(post.getStdout()).contains("\"totalCents\":30000").contains("\"currency\":\"BRL\"");
            // as duas réplicas do catalog respondem pelo Service
            assertThat(k(k3s, "get", "deployment", "catalog", "-o", "jsonpath={.status.readyReplicas}")).isEqualTo("2");
            assertThat(k(k3s, "get", "pod", pod, "-o", "jsonpath={.status.containerStatuses[0].restartCount}")).isEqualTo("0");
            // o Secret chegou ao contêiner como variável de ambiente (sem estar no jar nem na imagem)
            assertThat(k(k3s, "exec", pod, "--", "printenv", "SPRING_DATASOURCE_PASSWORD").trim()).isEqualTo("super-secreta");
        } finally {
            for (String svc : List.of("catalog", "orders")) sh(tmp, "docker", "rmi", "-f", "pp-" + svc + "-service:k8s");
        }
    }
}
