package dev.estudos.cloud.lab;

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

/** Infraestrutura do laboratorio: sobe os jars -exec em subprocessos, com portas livres, e os encerra no fim. */
final class Lab implements AutoCloseable {
    static final String JAVA = Path.of(System.getProperty("java.home"), "bin", "java").toString();
    static final HttpClient HTTP = HttpClient.newHttpClient();

    /** Aceleracao dos relogios do Eureka/LoadBalancer para o teste nao esperar 30 s (os padroes sao os do doc). */
    static final List<String> RAPIDO = List.of(
        "--eureka.instance.lease-renewal-interval-in-seconds=1",
        "--eureka.client.registry-fetch-interval-seconds=1",
        "--spring.cloud.loadbalancer.cache.ttl=1s");

    record Resp(int status, String body, HttpHeaders headers) {
        String header(String n) { return headers.firstValue(n).orElse(null); }
    }
    private record HttpHeaders(java.net.http.HttpHeaders h) {
        Optional<String> firstValue(String n) { return h.firstValue(n); }
    }

    final Path dir;
    private final Map<String, Process> procs = new LinkedHashMap<>();
    private final Map<String, Path> logs = new HashMap<>();

    Lab(Path dir) { this.dir = dir; }

    static int freePort() {
        try (var s = new ServerSocket(0)) { return s.getLocalPort(); } catch (IOException e) { throw new IllegalStateException(e); }
    }

    static Path jar(String app) {
        Path p = Path.of("..", app, "target", "cl-" + app + "-1.0.0-SNAPSHOT-exec.jar").toAbsolutePath().normalize();
        assertThat(p).as("jar de %s (rode com -am para construir)", app).exists();
        return p;
    }

    /** Sobe um app; devolve o processo. {@code id} identifica o log/processo. */
    Process start(String id, String app, Map<String, String> env, String... args) throws IOException {
        List<String> cmd = new ArrayList<>(List.of(JAVA, "-Xmx256m", "-jar", jar(app).toString()));
        cmd.addAll(Arrays.asList(args));
        Path log = dir.resolve(id + ".log");
        ProcessBuilder pb = new ProcessBuilder(cmd).redirectErrorStream(true).redirectOutput(log.toFile());
        pb.environment().putAll(env);
        Process p = pb.start();
        procs.put(id, p); logs.put(id, log);
        return p;
    }

    String log(String id) {
        try { return Files.readString(logs.get(id)); } catch (IOException e) { return ""; }
    }

    void kill(String id, boolean force) throws InterruptedException {
        Process p = procs.get(id);
        if (force) p.destroyForcibly(); else p.destroy();
        p.waitFor(30, TimeUnit.SECONDS);
    }

    boolean alive(String id) { return procs.get(id).isAlive(); }

    static Resp get(int port, String path, String... headers) throws Exception {
        var rb = HttpRequest.newBuilder(URI.create("http://localhost:" + port + path)).timeout(java.time.Duration.ofSeconds(15));
        for (int i = 0; i < headers.length; i += 2) rb.header(headers[i], headers[i + 1]);
        var r = HTTP.send(rb.build(), HttpResponse.BodyHandlers.ofString());
        return new Resp(r.statusCode(), r.body(), new HttpHeaders(r.headers()));
    }

    static Resp post(int port, String path) throws Exception {
        var r = HTTP.send(HttpRequest.newBuilder(URI.create("http://localhost:" + port + path))
            .POST(HttpRequest.BodyPublishers.noBody()).build(), HttpResponse.BodyHandlers.ofString());
        return new Resp(r.statusCode(), r.body(), new HttpHeaders(r.headers()));
    }

    /** Espera a condicao (ate {@code seconds}); falha com o log do processo se nao ocorrer. */
    static void await(String what, int seconds, BooleanSupplier cond) throws InterruptedException {
        long end = System.nanoTime() + seconds * 1_000_000_000L;
        while (System.nanoTime() < end) {
            try { if (cond.getAsBoolean()) return; } catch (RuntimeException ignored) { }
            Thread.sleep(300);
        }
        throw new AssertionError("timeout esperando: " + what);
    }

    static boolean ok(int port, String path) {
        try { return get(port, path).status() == 200; } catch (Exception e) { return false; }
    }

    static Path newGitRepo(Path repo, Map<String, String> files) throws Exception {
        Files.createDirectories(repo);
        for (var e : files.entrySet()) Files.writeString(repo.resolve(e.getKey()), e.getValue());
        git(repo, "init", "-b", "main");
        commit(repo, "inicial");
        return repo;
    }

    static void commit(Path repo, String msg) throws Exception {
        git(repo, "add", "-A");
        git(repo, "-c", "user.name=lab", "-c", "user.email=lab@example.com", "commit", "-m", msg);
    }

    static String git(Path repo, String... args) throws Exception {
        List<String> cmd = new ArrayList<>(List.of("git"));
        cmd.addAll(Arrays.asList(args));
        Process p = new ProcessBuilder(cmd).directory(repo.toFile()).redirectErrorStream(true).start();
        String out = new String(p.getInputStream().readAllBytes());
        assertThat(p.waitFor()).as(out).isZero();
        return out;
    }


    // ---- pecas do laboratorio -------------------------------------------------------------------------

    static String defaultZone(int eureka) { return "--eureka.client.service-url.defaultZone=http://localhost:" + eureka + "/eureka/"; }

    /** Sobe o Eureka Server (caches do servidor encurtados para o teste) e devolve a porta. */
    int eureka() throws Exception {
        int p = freePort();
        start("eureka", "eureka-server", Map.of(), "--server.port=" + p,
            "--eureka.server.response-cache-update-interval-ms=1000", "--eureka.server.use-read-only-response-cache=false");
        await("eureka server", 90, () -> ok(p, "/eureka/apps"));
        return p;
    }

    /** catalog sem Config Server (valores por argumento), registrado no Eureka. Devolve a porta. */
    int catalogSemConfig(String id, int eureka) throws Exception {
        int p = freePort();
        List<String> a = new ArrayList<>(List.of("--server.port=" + p, "--spring.cloud.config.enabled=false",
            "--spring.config.import=", "--catalog.discount=5", "--shop.currency=BRL"));
        if (eureka > 0) { a.add(defaultZone(eureka)); a.addAll(RAPIDO); } else a.add("--eureka.client.enabled=false");
        start(id, "catalog", Map.of(), a.toArray(String[]::new));
        await(id, 90, () -> ok(p, "/products/1"));
        return p;
    }

    /** orders registrado no Eureka (perfil opcional). Devolve a porta. */
    int orders(String id, int eureka, String profile, Map<String, String> env) throws Exception {
        int p = freePort();
        List<String> a = new ArrayList<>(List.of("--server.port=" + p));
        if (profile != null) a.add("--spring.profiles.active=" + profile);
        if (eureka > 0) { a.add(defaultZone(eureka)); a.addAll(RAPIDO); }
        start(id, "orders", env, a.toArray(String[]::new));
        return p;
    }

    /** Instancias de uma aplicacao no registro do Eureka (JSON): ids com status UP. */
    static List<String> registro(int eureka, String app) throws Exception {
        String body = get(eureka, "/eureka/apps/" + app.toUpperCase(), "Accept", "application/json").body();
        List<String> ids = new ArrayList<>();
        var m = java.util.regex.Pattern.compile("\"instanceId\":\"([^\"]+)\"[^{}]*?\"status\":\"UP\"").matcher(body);
        while (m.find()) ids.add(m.group(1));
        return ids;
    }

    static boolean registrado(int eureka, String app, int n) {
        try { return registro(eureka, app).size() >= n; } catch (Exception e) { return false; }
    }

    /** Chama GET path n vezes e conta a ocorrencia de cada "instancePort". */
    static Map<String, Integer> distribuicao(int port, String path, int n) throws Exception {
        Map<String, Integer> c = new TreeMap<>();
        var pat = java.util.regex.Pattern.compile("\"instancePort\":\"(\\d+)\"");
        for (int i = 0; i < n; i++) {
            var r = get(port, path);
            assertThat(r.status()).as(r.body()).isEqualTo(200);
            var m = pat.matcher(r.body());
            assertThat(m.find()).as(r.body()).isTrue();
            c.merge(m.group(1), 1, Integer::sum);
        }
        return c;
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
