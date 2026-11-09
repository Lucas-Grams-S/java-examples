package dev.estudos.cloud.tracingotlp;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

import com.sun.net.httpserver.HttpServer;
import java.io.ByteArrayOutputStream;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.HexFormat;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.zip.GZIPInputStream;
import org.junit.jupiter.api.Test;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;

/**
 * CL08-10: dois serviços exportam spans por OTLP/HTTP (protobuf) para um coletor simulado (JDK HttpServer).
 * O {@code traceparent} W3C enviado pelo teste deve aparecer, como traceId binário, nos spans dos DOIS serviços.
 */
class OtlpTracingTest {
    static final String TRACE_ID = "4bf92f3577b34da6a3ce929d0e0e4736";

    record Export(byte[] corpo) {
        boolean contem(byte[] trecho) { return indexOf(corpo, trecho) >= 0; }
        boolean contem(String texto) { return contem(texto.getBytes(StandardCharsets.UTF_8)); }
    }

    static int indexOf(byte[] h, byte[] n) {
        outer: for (int i = 0; i <= h.length - n.length; i++) {
            for (int j = 0; j < n.length; j++) if (h[i + j] != n[j]) continue outer;
            return i;
        }
        return -1;
    }

    private ConfigurableApplicationContext sobe(String perfil, int porta, int coletor) {
        return new SpringApplicationBuilder(TracingOtlpApp.class).profiles(perfil)
            .run("--server.port=" + porta, "--catalog.url=http://localhost:" + (porta + 1),
                "--management.opentelemetry.tracing.export.otlp.endpoint=http://127.0.0.1:" + coletor + "/v1/traces",
                "--management.otlp.metrics.export.enabled=false",
                "--management.opentelemetry.tracing.export.schedule-delay=200ms");
    }

    @Test void cl0810_doisServicosExportamOtlpComOMesmoTraceId() throws Exception {
        List<Export> recebidos = new CopyOnWriteArrayList<>();
        HttpServer coletor = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        coletor.createContext("/v1/traces", ex -> {
            byte[] b = ex.getRequestBody().readAllBytes();
            if ("gzip".equals(ex.getRequestHeaders().getFirst("Content-Encoding"))) {
                try (var in = new GZIPInputStream(new java.io.ByteArrayInputStream(b)); var out = new ByteArrayOutputStream()) {
                    in.transferTo(out); b = out.toByteArray();
                }
            }
            recebidos.add(new Export(b));
            ex.sendResponseHeaders(200, -1); ex.close();
        });
        coletor.start();
        int base = 18091;
        try (var catalog = sobe("catalog", base + 1, coletor.getAddress().getPort());
             var orders = sobe("orders", base, coletor.getAddress().getPort())) {
            var req = HttpRequest.newBuilder(URI.create("http://localhost:" + base + "/orders/7"))
                .header("traceparent", "00-" + TRACE_ID + "-00f067aa0ba902b7-01").build();
            assertThat(HttpClient.newHttpClient().send(req, HttpResponse.BodyHandlers.ofString()).body()).contains("produto-7");
            byte[] id = HexFormat.of().parseHex(TRACE_ID);
            await().atMost(Duration.ofSeconds(30)).untilAsserted(() -> {
                var doTrace = recebidos.stream().filter(e -> e.contem(id)).toList();
                assertThat(doTrace).anyMatch(e -> e.contem("service.name") && e.contem("orders") && e.contem("http get /orders/{id}"));
                assertThat(doTrace).anyMatch(e -> e.contem("catalog.lookup"));   // span manual (Observation API) saiu por OTLP
            });
        } finally { coletor.stop(0); }
    }
}
