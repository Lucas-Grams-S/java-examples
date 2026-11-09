package dev.estudos.cloud.tracing;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.HashSet;
import org.junit.jupiter.api.Test;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

/** Requer Docker e a imagem {@code openzipkin/zipkin}. Sobe orders e catalog em 2 contextos na mesma JVM. */
@Testcontainers
class TracingTest {
    @Container static final GenericContainer<?> ZIPKIN = new GenericContainer<>("openzipkin/zipkin:latest")
        .withExposedPorts(9411).waitingFor(Wait.forHttp("/health").forStatusCode(200));

    private final HttpClient http = HttpClient.newHttpClient();
    private final ObjectMapper json = new ObjectMapper();

    private String get(String url) throws Exception {
        return http.send(HttpRequest.newBuilder(URI.create(url)).build(), HttpResponse.BodyHandlers.ofString()).body();
    }

    private ConfigurableApplicationContext sobe(String perfil, int porta) {
        return new SpringApplicationBuilder(TracingApp.class).profiles(perfil)
            .run("--server.port=" + porta,   // argumentos têm precedência sobre o application.yaml
                "--catalog.url=http://localhost:" + (porta + 1),
                "--management.tracing.export.zipkin.endpoint=http://localhost:" + ZIPKIN.getMappedPort(9411) + "/api/v2/spans");
    }

    /** CL08-08 + CL08-09: um traceId só, com spans dos dois serviços e o span manual como filho do SERVER catalog. */
    @Test
    void umTraceDoisServicosESpanManual() throws Exception {
        int base = 18081;
        try (var catalog = sobe("catalog", base + 1); var orders = sobe("orders", base)) {
            assertThat(get("http://localhost:" + base + "/orders/7")).contains("produto-7");
            String api = "http://localhost:" + ZIPKIN.getMappedPort(9411) + "/api/v2/traces?serviceName=orders";
            JsonNode[] trace = new JsonNode[1];
            await().atMost(Duration.ofSeconds(30)).untilAsserted(() -> {
                var traces = json.readTree(get(api));
                assertThat(traces.size()).isGreaterThan(0);
                for (var t : traces) {
                    var servicos = new HashSet<String>();
                    var nomes = new HashSet<String>();
                    for (var s : t) {
                        servicos.add(s.path("localEndpoint").path("serviceName").asText());
                        nomes.add(s.path("name").asText());
                    }
                    if (servicos.containsAll(java.util.List.of("orders", "catalog")) && nomes.contains("catalog.lookup")) { trace[0] = t; return; }
                }
                throw new AssertionError("trace completo ainda não chegou ao Zipkin");
            });
            var ids = new HashSet<String>();
            for (var s : trace[0]) ids.add(s.get("traceId").asText());
            assertThat(ids).hasSize(1);
            // catalog.lookup é filho de um span do catalog
            JsonNode lookup = null;
            for (var s : trace[0]) if (s.get("name").asText().equals("catalog.lookup")) lookup = s;
            String pai = lookup.get("parentId").asText();
            for (var s : trace[0]) if (s.get("id").asText().equals(pai))
                assertThat(s.path("localEndpoint").path("serviceName").asText()).isEqualTo("catalog");
            assertThat(lookup.get("duration").asLong()).isGreaterThanOrEqualTo(50_000L);   // sleep(50) em µs
        }
    }
}
