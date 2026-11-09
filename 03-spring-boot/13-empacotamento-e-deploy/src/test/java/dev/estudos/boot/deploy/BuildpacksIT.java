package dev.estudos.boot.deploy;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 * SB13-11: a imagem {@code sb13-buildpacks:it} é gerada sem Dockerfile pelo perfil {@code buildpacks}
 * ({@code spring-boot:build-image-no-fork}, builder Paketo). Sem Docker, o perfil não ativa e o teste retorna.
 */
class BuildpacksIT {
    static final String TAG = "sb13-buildpacks:it";
    @TempDir Path tmp;

    @Test void sb1311_imagemDoPaketoSobeEExpoeOsMetadados() throws Exception {
        if (new ProcessBuilder("docker", "image", "inspect", TAG).redirectErrorStream(true).start().waitFor() != 0) return;
        try {
            // o Paketo grava rótulos com a versão do Boot e usa usuário não root
            String labels = PackagingIT.run(tmp, "docker", "image", "inspect", "-f", "{{json .Config.Labels}}", TAG);
            assertThat(labels).contains("org.springframework.boot.version\":\"4.1.2")
                .contains("io.buildpacks.build.metadata");
            assertThat(PackagingIT.run(tmp, "docker", "image", "inspect", "-f", "{{.Config.User}}", TAG).trim())
                .isNotEqualTo("root").isNotEmpty();
            int port = PackagingIT.freePort();
            String id = PackagingIT.run(tmp, "docker", "run", "-d", "--rm", "-p", port + ":8080", TAG).trim();
            try {
                PackagingIT.awaitUp(port);
                assertThat(PackagingIT.get(port, "/ping")).isEqualTo("pong");
                // o memory calculator do Paketo calcula -Xmx a partir do limite do contêiner
                assertThat(PackagingIT.run(tmp, "docker", "logs", id)).contains("Memory Configuration");
            } finally {
                PackagingIT.run(tmp, "docker", "stop", id);
            }
        } finally {
            PackagingIT.run(tmp, "docker", "rmi", "-f", TAG);
        }
    }
}
