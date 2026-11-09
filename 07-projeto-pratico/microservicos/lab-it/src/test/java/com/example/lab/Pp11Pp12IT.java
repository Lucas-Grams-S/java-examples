package com.example.lab;

import static com.example.lab.Lab.freePort;
import static com.example.lab.Lab.get;
import static com.example.lab.Lab.post;
import static com.example.lab.Lab.ok;
import static com.example.lab.Lab.newGitRepo;
import static com.example.lab.Lab.jarCl;
import static com.example.lab.Lab.jarPp;
import static com.example.lab.Lab.defaultZone;
import static com.example.lab.Lab.registrado;
import static com.example.lab.Lab.RAPIDO;
import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

import java.nio.file.Path;
import java.time.Duration;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.io.TempDir;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.kafka.KafkaContainer;
import tools.jackson.databind.json.JsonMapper;

/**
 * PP11 (dois serviços: Config Server, Eureka, Gateway, HTTP Service Client, banco por serviço) e
 * PP12 (timeout, retry, circuit breaker; Outbox + Kafka com consumidor idempotente; tracing no Zipkin).
 * Um laboratório só, em subprocessos, com Kafka e Zipkin em contêineres (Docker + imagens apache/kafka e openzipkin/zipkin).
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class Pp11Pp12IT {
    @TempDir static Path tmp;
    static final KafkaContainer KAFKA = new KafkaContainer("apache/kafka:latest");
    static final GenericContainer<?> ZIPKIN = new GenericContainer<>("openzipkin/zipkin:latest")
        .withExposedPorts(9411).waitingFor(Wait.forHttp("/health").forStatusCode(200));
    static Lab lab;
    static int eureka, gw, orders, catalog;
    static final JsonMapper JSON = JsonMapper.builder().build();

    @BeforeAll static void up() throws Exception {
        KAFKA.start(); ZIPKIN.start();
        lab = new Lab(tmp);
        Path repo = newGitRepo(tmp.resolve("config-repo"), Map.of("application.yml", "shop.currency: BRL\n"));
        int cfg = freePort();
        lab.start("config-server", jarCl("config-server"), Map.of(), "--server.port=" + cfg, "--CONFIG_REPO=file://" + repo);
        Lab.await("config server", 90, () -> ok(cfg, "/catalog/default"));
        eureka = lab.eureka();
        Map<String, String> env = Map.of("KAFKA_BOOTSTRAP", KAFKA.getBootstrapServers(), "TRACING_SAMPLING", "1.0",
            "ZIPKIN_URL", "http://localhost:" + ZIPKIN.getMappedPort(9411) + "/api/v2/spans",
            "CONFIG_SERVER_URL", "http://localhost:" + cfg);
        catalog = freePort(); orders = freePort(); gw = freePort();
        var c = new java.util.ArrayList<>(List.of("--server.port=" + catalog, defaultZone(eureka))); c.addAll(RAPIDO);
        lab.start("catalog", jarPp("catalog-service"), env, c.toArray(String[]::new));
        var o = new java.util.ArrayList<>(List.of("--server.port=" + orders, defaultZone(eureka))); o.addAll(RAPIDO);
        lab.start("orders", jarPp("orders-service"), env, o.toArray(String[]::new));
        var g = new java.util.ArrayList<>(List.of("--server.port=" + gw, defaultZone(eureka))); g.addAll(RAPIDO);
        lab.start("gateway", jarCl("gateway"), Map.of(), g.toArray(String[]::new));
        Lab.await("registro", 120, () -> registrado(eureka, "catalog", 1) && registrado(eureka, "orders", 1) && registrado(eureka, "gateway", 1));
        Lab.await("gateway enxerga catalog e orders", 90, () -> ok(gw, "/api/catalog/products/1") && ok(gw, "/api/orders/stats"));
    }

    @AfterAll static void down() { if (lab != null) lab.close(); KAFKA.stop(); ZIPKIN.stop(); }

    static long catalogCalls() throws Exception { return JSON.readTree(get(orders, "/orders/stats").body()).get("catalogCalls").asLong(); }
    static int reservations() throws Exception { return JSON.readTree(get(catalog, "/reservations/count").body()).get("reservations").asInt(); }
    /** Espera as reservas pararem de mudar (eventos de pedidos anteriores ainda podem estar a caminho). */
    static int reservasEstaveis() throws Exception {
        int ultimo = -1, igual = 0;
        for (int i = 0; i < 60 && igual < 6; i++) { int n = reservations(); igual = n == ultimo ? igual + 1 : 0; ultimo = n; Thread.sleep(500); }
        return ultimo;
    }
    static final String PEDIDO = "{\"productId\":1,\"quantity\":2}";

    // ---------------- PP11 ----------------------------------------------------------------------------------

    @Test @Order(1) void pp11_pedidoPeloGatewayUsaCatalogDescobertoPeloEurekaEConfigDoConfigServer() throws Exception {
        var r = post(gw, "/api/orders", PEDIDO);
        assertThat(r.status()).as(r.body()).isEqualTo(201);
        var o = JSON.readTree(r.body());
        assertThat(o.get("totalCents").asLong()).isEqualTo(30000);        // 2 x 15000, preço vem do catalog
        assertThat(o.get("currency").asText()).isEqualTo("BRL");           // veio do Config Server -> catalog -> orders
        assertThat(get(gw, "/api/orders/" + o.get("id").asLong()).status()).isEqualTo(200);
        assertThat(get(gw, "/api/catalog/products/1").body()).contains("Teclado");
        assertThat(post(gw, "/api/orders", "{\"productId\":999,\"quantity\":1}").status()).isEqualTo(404);   // 404 do catalog não é 503
        assertThat(post(gw, "/api/orders", "{\"productId\":3,\"quantity\":50}").status()).isEqualTo(409);    // estoque insuficiente
    }

    @Test @Order(2) void pp11_bancoPorServico() throws Exception {
        // cada serviço tem o próprio H2 em memória; orders não tem tabela de produtos e o catalog não tem pedidos
        assertThat(get(catalog, "/orders/1").status()).isEqualTo(404);
        assertThat(get(orders, "/products/1").status()).isEqualTo(404);
    }

    // ---------------- PP12: mensageria ----------------------------------------------------------------------

    @Test @Order(3) void pp12_outboxPublicaEConsumidorEIdempotente() throws Exception {
        int antes = reservasEstaveis();
        var o = JSON.readTree(post(orders, "/orders", PEDIDO).body());
        long id = o.get("id").asLong();
        await().atMost(Duration.ofSeconds(60)).untilAsserted(() -> assertThat(reservations()).isEqualTo(antes + 1));
        assertThat(JSON.readTree(get(orders, "/orders/stats").body()).get("outboxPending").asInt()).isZero();
        // at-least-once: o MESMO evento chegando de novo não reserva outra vez
        Properties p = new Properties();
        p.put("bootstrap.servers", KAFKA.getBootstrapServers());
        p.put("key.serializer", StringSerializer.class.getName());
        p.put("value.serializer", StringSerializer.class.getName());
        try (var prod = new KafkaProducer<String, String>(p)) {
            String evento = "{\"orderId\":" + id + ",\"productId\":1,\"quantity\":2}";
            prod.send(new ProducerRecord<>("order-created", "dup", evento)).get();
            prod.send(new ProducerRecord<>("order-created", "dup", evento)).get();
            Thread.sleep(3000);
        }
        assertThat(reservations()).isEqualTo(antes + 1);
    }

    @Test @Order(4) void pp12_brokerForaDoArNaoPerdeOEvento() throws Exception {
        int antes = reservasEstaveis();
        var docker = KAFKA.getDockerClient();
        docker.pauseContainerCmd(KAFKA.getContainerId()).exec();
        try {
            assertThat(post(orders, "/orders", PEDIDO).status()).isEqualTo(201);   // o pedido NÃO depende do broker
            await().during(Duration.ofSeconds(2)).atMost(Duration.ofSeconds(10)).untilAsserted(() ->
                assertThat(JSON.readTree(get(orders, "/orders/stats").body()).get("outboxPending").asInt()).isEqualTo(1));
        } finally { docker.unpauseContainerCmd(KAFKA.getContainerId()).exec(); }
        await().atMost(Duration.ofSeconds(90)).untilAsserted(() -> {
            assertThat(JSON.readTree(get(orders, "/orders/stats").body()).get("outboxPending").asInt()).isZero();
            assertThat(reservations()).isEqualTo(antes + 1);
        });
    }

    // ---------------- PP12: tracing -------------------------------------------------------------------------

    @Test @Order(5) void pp12_umTraceComOsDoisServicos() throws Exception {
        assertThat(post(orders, "/orders", PEDIDO).status()).isEqualTo(201);
        String api = "http://localhost:" + ZIPKIN.getMappedPort(9411) + "/api/v2/traces?serviceName=orders&limit=50";
        await().atMost(Duration.ofSeconds(30)).untilAsserted(() -> {
            var traces = JSON.readTree(java.net.http.HttpClient.newHttpClient().send(
                java.net.http.HttpRequest.newBuilder(java.net.URI.create(api)).build(),
                java.net.http.HttpResponse.BodyHandlers.ofString()).body());
            boolean achou = false;
            for (var t : traces) {
                var servicos = new HashSet<String>();
                for (var s : t) servicos.add(s.path("localEndpoint").path("serviceName").asText());
                achou |= servicos.containsAll(List.of("orders", "catalog"));
            }
            assertThat(achou).as("trace com orders e catalog no Zipkin").isTrue();
        });
    }

    // ---------------- PP12: resiliência ---------------------------------------------------------------------

    @Test @Order(6) void pp12_retryTrataFalhaTransitoria() throws Exception {
        assertThat(post(catalog, "/admin/falhar/2", null).status()).isEqualTo(204);
        long antes = catalogCalls();
        assertThat(post(orders, "/orders", PEDIDO).status()).isEqualTo(201);   // 2 falhas + 1 sucesso, sem o cliente perceber
        assertThat(catalogCalls() - antes).isEqualTo(3);
    }

    @Test @Order(7) void pp12_timeoutViraRespostaDeIndisponibilidade() throws Exception {
        post(catalog, "/admin/atraso/2500", null);           // timeout do cliente é 1 s
        try {
            long t = System.nanoTime();
            var r = post(orders, "/orders", PEDIDO);
            assertThat(r.status()).as(r.body()).isEqualTo(503);
            assertThat((System.nanoTime() - t) / 1_000_000).as("3 tentativas x ~1 s").isLessThan(10_000);
        } finally { post(catalog, "/admin/atraso/0", null); }
    }

    @Test @Order(8) void pp12_circuitBreakerAbreECortaAsChamadas() throws Exception {
        lab.kill("catalog");                                 // catalog some de vez
        int status = 0;
        for (int i = 0; i < 3; i++) status = post(orders, "/orders", PEDIDO).status();
        assertThat(status).isEqualTo(503);
        long antes = catalogCalls();
        var r = post(orders, "/orders", PEDIDO);
        assertThat(r.status()).isEqualTo(503);
        assertThat(r.body()).contains("CallNotPermittedException");   // circuito ABERTO: falha rápida
        assertThat(catalogCalls()).as("com o circuito aberto nem tenta chamar").isEqualTo(antes);
    }
}
