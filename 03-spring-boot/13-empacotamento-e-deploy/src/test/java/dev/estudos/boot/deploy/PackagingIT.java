package dev.estudos.boot.deploy;

import static org.assertj.core.api.Assertions.assertThat;

import java.net.ServerSocket;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 * Roda depois do {@code package} (Failsafe): usa o jar real, como o doc (SB13-02/08/09/10).
 * O JAVA da JVM atual é usado para os subprocessos.
 */
class PackagingIT {
    static final Path JAR = Path.of("target/sb-13-deploy-1.0.0-SNAPSHOT.jar").toAbsolutePath();
    static final String JAVA = Path.of(System.getProperty("java.home"), "bin", "java").toString();
    @TempDir Path tmp;

    static String run(Path dir, String... cmd) throws Exception {
        Process p = new ProcessBuilder(cmd).directory(dir.toFile()).redirectErrorStream(true).start();
        String out = new String(p.getInputStream().readAllBytes());
        assertThat(p.waitFor(180, TimeUnit.SECONDS)).isTrue();
        assertThat(p.exitValue()).as(out).isZero();
        return out;
    }

    static int freePort() throws Exception { try (var s = new ServerSocket(0)) { return s.getLocalPort(); } }

    static String get(int port, String path) throws Exception {
        return HttpClient.newHttpClient().send(HttpRequest.newBuilder(URI.create("http://localhost:" + port + path)).build(),
            HttpResponse.BodyHandlers.ofString()).body();
    }

    static void awaitUp(int port) throws Exception {
        for (int i = 0; i < 100; i++) {
            try { get(port, "/ping"); return; } catch (Exception e) { Thread.sleep(200); }
        }
        throw new AssertionError("não subiu");
    }

    @Test void sb1302_estruturaDoFatJar() throws Exception {
        assertThat(Files.exists(JAR)).isTrue();
        String manifest = run(tmp, "unzip", "-p", JAR.toString(), "META-INF/MANIFEST.MF");
        assertThat(manifest).contains("Main-Class: org.springframework.boot.loader.launch.JarLauncher")
            .contains("Start-Class: dev.estudos.boot.deploy.DeployApplication");
        String list = run(tmp, "unzip", "-l", JAR.toString());
        assertThat(list).contains("BOOT-INF/lib/", "BOOT-INF/classpath.idx", "BOOT-INF/layers.idx", "BOOT-INF/classes/application.yaml");
    }

    @Test void sb1309_executaJarEEncerraGracioso() throws Exception {
        int port = freePort();
        Process p = new ProcessBuilder(JAVA, "-jar", JAR.toString(), "--server.port=" + port)
            .redirectErrorStream(true).redirectOutput(tmp.resolve("out.log").toFile()).start();
        try {
            awaitUp(port);
            assertThat(get(port, "/ping")).isEqualTo("pong");
            assertThat(get(port, "/actuator/info")).contains("\"artifact\":\"sb-13-deploy\"");
            assertThat(get(port, "/actuator/health/readiness")).contains("UP");
        } finally {
            p.destroy();                                   // SIGTERM
            assertThat(p.waitFor(30, TimeUnit.SECONDS)).isTrue();
        }
        assertThat(p.exitValue()).isEqualTo(143);           // 128 + SIGTERM(15): a JVM encerrou pelo shutdown hook
        assertThat(Files.readString(tmp.resolve("out.log"))).contains("Commencing graceful shutdown");
    }

    @Test void sb1302_camadasExtract() throws Exception {
        String layers = run(tmp, JAVA, "-Djarmode=tools", "-jar", JAR.toString(), "list-layers");
        assertThat(layers.lines().map(String::trim).toList())
            .containsExactly("dependencies", "spring-boot-loader", "snapshot-dependencies", "application");
        run(tmp, JAVA, "-Djarmode=tools", "-jar", JAR.toString(), "extract", "--layers", "--destination", "ex");
        assertThat(Files.isDirectory(tmp.resolve("ex/application"))).isTrue();
        assertThat(Files.isDirectory(tmp.resolve("ex/dependencies"))).isTrue();
        long dep = size(tmp.resolve("ex/dependencies")), app = size(tmp.resolve("ex/application"));
        assertThat(app).as("a camada que muda a cada commit é muito menor").isLessThan(dep / 20);
    }

    static long size(Path d) throws Exception {
        try (var s = Files.walk(d)) { return s.filter(Files::isRegularFile).mapToLong(f -> f.toFile().length()).sum(); }
    }

    @Test void sb1308_aotCache() throws Exception {
        Path cache = tmp.resolve("app.aot");
        run(tmp, JAVA, "-XX:AOTCacheOutput=" + cache, "-Dspring.context.exit=onRefresh", "-jar", JAR.toString(), "--server.port=0");
        assertThat(Files.exists(cache)).isTrue();
        long sem = tempo(JAVA, "-Dspring.context.exit=onRefresh", "-jar", JAR.toString(), "--server.port=0");
        long com = tempo(JAVA, "-XX:AOTCache=" + cache, "-Dspring.context.exit=onRefresh", "-jar", JAR.toString(), "--server.port=0");
        System.out.println("AOT cache: sem=" + sem + " ms, com=" + com + " ms");
        assertThat(com).as("o cache não deve piorar o startup").isLessThan(sem);
    }

    long tempo(String... cmd) throws Exception {
        long t = System.nanoTime(); run(tmp, cmd); return (System.nanoTime() - t) / 1_000_000;
    }

    @Test void sb1310_dockerfileMultiEstagio() throws Exception {
        if (new ProcessBuilder("docker", "info").redirectErrorStream(true).start().waitFor() != 0) return;   // sem Docker, pula
        Path ctx = tmp.resolve("ctx"); Files.createDirectories(ctx);
        Files.copy(JAR, ctx.resolve("app.jar"));
        Files.copy(Path.of("docker/Dockerfile"), ctx.resolve("Dockerfile"));
        String tag = "sb13-deploy:it";
        run(ctx, "docker", "build", "-t", tag, ".");
        int port = freePort();
        String id = run(ctx, "docker", "run", "-d", "--rm", "-p", port + ":8080", tag).trim();
        try {
            awaitUp(port);
            assertThat(get(port, "/ping")).isEqualTo("pong");
            String user = run(ctx, "docker", "exec", id, "id", "-un").trim();
            assertThat(user).isEqualTo("app");                          // não roda como root
        } finally {
            run(ctx, "docker", "stop", id);
            run(ctx, "docker", "rmi", "-f", tag);
        }
    }
}
