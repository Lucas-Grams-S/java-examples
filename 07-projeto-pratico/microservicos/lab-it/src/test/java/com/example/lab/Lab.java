package com.example.lab;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.function.BooleanSupplier;

/** Infraestrutura do laboratório: sobe os jars -exec em subprocessos com portas livres e os encerra no fim. */
final class Lab implements AutoCloseable {
    static final String JAVA = Path.of(System.getProperty("java.home"), "bin", "java").toString();
    static final HttpClient HTTP = HttpClient.newHttpClient();
    /** Relógios do Eureka/LoadBalancer acelerados para o teste (os padrões do doc continuam valendo fora daqui). */
    static final List<String> RAPIDO = List.of("--eureka.instance.lease-renewal-interval-in-seconds=1",
        "--eureka.client.registry-fetch-interval-seconds=1", "--spring.cloud.loadbalancer.cache.ttl=1s");

    record Resp(int status, String body) {}

    private final Path dir;
    private final Map<String, Process> procs = new LinkedHashMap<>();
    private final Map<String, Path> logs = new HashMap<>();

    Lab(Path dir) { this.dir = dir; }

    static int freePort() {
        try (var s = new ServerSocket(0)) { return s.getLocalPort(); } catch (IOException e) { throw new IllegalStateException(e); }
    }

    /** {@code modulo}: caminho relativo ao módulo lab-it, ex. {@code ../orders-service/target/pp-orders-service-...-exec.jar}. */
    static Path jar(String relativo) {
        Path p = Path.of(relativo).toAbsolutePath().normalize();
        assertThat(p).as("jar %s (rode com -am para construir)", p).exists();
        return p;
    }
    static Path jarPp(String nome) { return jar("../" + nome + "/target/pp-" + nome + "-1.0.0-SNAPSHOT-exec.jar"); }
    static Path jarCl(String nome) { return jar("../../../04-spring-cloud/" + nome + "/target/cl-" + nome + "-1.0.0-SNAPSHOT-exec.jar"); }

    Process start(String id, Path jar, Map<String, String> env, String... args) throws IOException {
        List<String> cmd = new ArrayList<>(List.of(JAVA, "-Xmx256m", "-jar", jar.toString()));
        cmd.addAll(Arrays.asList(args));
        Path log = dir.resolve(id + ".log");
        ProcessBuilder pb = new ProcessBuilder(cmd).redirectErrorStream(true).redirectOutput(log.toFile());
        pb.environment().putAll(env);
        Process p = pb.start();
        procs.put(id, p); logs.put(id, log);
        return p;
    }

    String log(String id) { try { return Files.readString(logs.get(id)); } catch (IOException e) { return ""; } }
    void kill(String id) throws InterruptedException { Process p = procs.get(id); p.destroyForcibly(); p.waitFor(30, TimeUnit.SECONDS); }

    static Resp send(String method, int port, String path, String body) throws Exception {
        var rb = HttpRequest.newBuilder(URI.create("http://localhost:" + port + path)).timeout(java.time.Duration.ofSeconds(30))
            .header("Content-Type", "application/json");
        rb.method(method, body == null ? HttpRequest.BodyPublishers.noBody() : HttpRequest.BodyPublishers.ofString(body));
        var r = HTTP.send(rb.build(), HttpResponse.BodyHandlers.ofString());
        return new Resp(r.statusCode(), r.body());
    }
    static Resp get(int port, String path) throws Exception { return send("GET", port, path, null); }
    static Resp post(int port, String path, String body) throws Exception { return send("POST", port, path, body); }

    static void await(String what, int seconds, BooleanSupplier cond) throws InterruptedException {
        long end = System.nanoTime() + seconds * 1_000_000_000L;
        while (System.nanoTime() < end) {
            try { if (cond.getAsBoolean()) return; } catch (RuntimeException ignored) { }
            Thread.sleep(300);
        }
        throw new AssertionError("timeout esperando: " + what);
    }
    static boolean ok(int port, String path) { try { return get(port, path).status() == 200; } catch (Exception e) { return false; } }

    static Path newGitRepo(Path repo, Map<String, String> files) throws Exception {
        Files.createDirectories(repo);
        for (var e : files.entrySet()) Files.writeString(repo.resolve(e.getKey()), e.getValue());
        git(repo, "init", "-b", "main");
        git(repo, "add", "-A");
        git(repo, "-c", "user.name=lab", "-c", "user.email=lab@example.com", "commit", "-m", "inicial");
        return repo;
    }
    static void git(Path repo, String... args) throws Exception {
        List<String> cmd = new ArrayList<>(List.of("git")); cmd.addAll(Arrays.asList(args));
        Process p = new ProcessBuilder(cmd).directory(repo.toFile()).redirectErrorStream(true).start();
        String out = new String(p.getInputStream().readAllBytes());
        assertThat(p.waitFor()).as(out).isZero();
    }

    static String defaultZone(int eureka) { return "--eureka.client.service-url.defaultZone=http://localhost:" + eureka + "/eureka/"; }

    int eureka() throws Exception {
        int p = freePort();
        start("eureka", jarCl("eureka-server"), Map.of(), "--server.port=" + p,
            "--eureka.server.response-cache-update-interval-ms=1000", "--eureka.server.use-read-only-response-cache=false");
        await("eureka server", 90, () -> ok(p, "/eureka/apps"));
        return p;
    }

    static boolean registrado(int eureka, String app, int n) {
        try {
            var r = HTTP.send(HttpRequest.newBuilder(URI.create("http://localhost:" + eureka + "/eureka/apps/" + app.toUpperCase()))
                .header("Accept", "application/json").build(), HttpResponse.BodyHandlers.ofString());
            String body = r.body();
            return body.split("\"status\":\"UP\"", -1).length - 1 >= n;
        } catch (Exception e) { return false; }
    }

    @Override public void close() {
        List<Process> all = new ArrayList<>(procs.values());
        Collections.reverse(all);
        all.forEach(Process::destroy);
        for (Process p : all) {
            try { if (!p.waitFor(20, TimeUnit.SECONDS)) p.destroyForcibly(); } catch (InterruptedException e) { p.destroyForcibly(); }
        }
    }
}
