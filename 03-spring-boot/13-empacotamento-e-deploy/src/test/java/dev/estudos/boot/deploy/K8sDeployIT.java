package dev.estudos.boot.deploy;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.testcontainers.containers.Container.ExecResult;
import org.testcontainers.k3s.K3sContainer;
import org.testcontainers.utility.DockerImageName;
import org.testcontainers.utility.MountableFile;

/**
 * SB13-12: o Deployment do doc (§5.4: probes, startupProbe, requests/limits, graceful) aplicado num k3s real.
 * A imagem do Dockerfile (SB13-10) é importada no nó com {@code ctr}. Requer Docker; sem Docker o teste retorna.
 */
class K8sDeployIT {
    @TempDir Path tmp;

    static String k(K3sContainer k3s, String... args) throws Exception {
        var cmd = new String[args.length + 1];
        cmd[0] = "kubectl";
        System.arraycopy(args, 0, cmd, 1, args.length);
        ExecResult r = k3s.execInContainer(cmd);
        assertThat(r.getExitCode()).as("kubectl %s: %s", String.join(" ", args), r.getStderr()).isZero();
        return r.getStdout();
    }

    @Test void sb1312_deploymentComProbesFicaPronto() throws Exception {
        if (new ProcessBuilder("docker", "info").redirectErrorStream(true).start().waitFor() != 0) return;
        Path ctx = tmp.resolve("ctx"); Files.createDirectories(ctx);
        Files.copy(PackagingIT.JAR, ctx.resolve("app.jar"));
        Files.copy(Path.of("docker/Dockerfile"), ctx.resolve("Dockerfile"));
        String tag = "sb13-deploy:k8s";
        PackagingIT.run(ctx, "docker", "build", "-t", tag, ".");
        Path tar = tmp.resolve("img.tar");
        PackagingIT.run(tmp, "docker", "save", "-o", tar.toString(), tag);
        try (var k3s = new K3sContainer(DockerImageName.parse("rancher/k3s:latest"))) {
            k3s.start();
            k3s.copyFileToContainer(MountableFile.forHostPath(tar), "/tmp/img.tar");
            k3s.copyFileToContainer(MountableFile.forHostPath("k8s/deployment.yaml"), "/tmp/deployment.yaml");
            for (int i = 0; i < 60; i++) {
                if (k3s.execInContainer("kubectl", "get", "serviceaccount", "default").getExitCode() == 0) break;
                Thread.sleep(2000);
            }
            ExecResult imp = k3s.execInContainer("ctr", "images", "import", "/tmp/img.tar");
            assertThat(imp.getExitCode()).as(imp.getStderr()).isZero();
            assertThat(k(k3s, "apply", "-f", "/tmp/deployment.yaml")).contains("deployment.apps/orders created");
            // startup/readiness/liveness reais: o rollout só termina se as 3 probes passarem
            assertThat(k(k3s, "rollout", "status", "deployment/orders", "--timeout=180s")).contains("successfully rolled out");
            assertThat(k(k3s, "get", "deployment", "orders", "-o", "jsonpath={.status.readyReplicas}")).isEqualTo("1");
            String pod = k(k3s, "get", "pod", "-l", "app=orders", "-o", "jsonpath={.items[0].metadata.name}");
            assertThat(k(k3s, "get", "pod", pod, "-o", "jsonpath={.status.containerStatuses[0].restartCount}")).isEqualTo("0");
            // o perfil prod chegou por variável de ambiente
            assertThat(k(k3s, "logs", pod)).contains("The following 1 profile is active: \"prod\"");
            // delete respeita o graceful shutdown: o pod termina sem forçar (sem restart, sem kill)
            assertThat(k(k3s, "delete", "pod", pod, "--wait=true")).contains("deleted");
        } finally {
            PackagingIT.run(tmp, "docker", "rmi", "-f", tag);
        }
    }
}
