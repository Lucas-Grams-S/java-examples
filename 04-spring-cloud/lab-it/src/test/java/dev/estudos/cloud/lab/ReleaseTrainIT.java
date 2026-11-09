package dev.estudos.cloud.lab;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Test;

/** CL01-01: o release train 2025.1.4 fixa as versoes dos modulos (nenhum starter declara versao). */
class ReleaseTrainIT {
    static List<String> libs(String app) throws Exception {
        Process p = new ProcessBuilder("unzip", "-Z1", Lab.jar(app).toString()).redirectErrorStream(true).start();
        String out = new String(p.getInputStream().readAllBytes());
        assertThat(p.waitFor(60, TimeUnit.SECONDS)).isTrue();
        return out.lines().filter(l -> l.startsWith("BOOT-INF/lib/") && l.endsWith(".jar")).map(l -> Path.of(l).getFileName().toString()).toList();
    }

    @Test void cl0101_releaseTrainFixaAsVersoes() throws Exception {
        List<String> catalog = libs("catalog");
        // Boot 4.1.2 (parent) + modulos Cloud da linha 5.0.x escolhidos pelo BOM 2025.1.4: cada modulo tem versao propria
        // (Commons 5.0.4, Config 5.0.6, Eureka/Netflix 5.0.3). O doc diz Config 5.0.4: ver DIVERGENCIAS.md.
        assertThat(catalog).contains("spring-boot-4.1.2.jar", "spring-cloud-commons-5.0.4.jar", "spring-cloud-config-client-5.0.6.jar",
            "spring-cloud-netflix-eureka-client-5.0.3.jar");
        assertThat(libs("config-server")).anyMatch(n -> n.startsWith("spring-cloud-config-server-5.0."));
        assertThat(libs("gateway")).anyMatch(n -> n.startsWith("spring-cloud-gateway-server-webflux-5.0."));
        assertThat(libs("gateway-mvc")).anyMatch(n -> n.startsWith("spring-cloud-gateway-server-webmvc-5.0."));
        assertThat(libs("eureka-server")).anyMatch(n -> n.startsWith("spring-cloud-netflix-eureka-server-5.0."));
    }
}
