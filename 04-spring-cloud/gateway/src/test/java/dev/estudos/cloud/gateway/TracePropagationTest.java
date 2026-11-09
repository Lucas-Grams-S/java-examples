package dev.estudos.cloud.gateway;

import static org.assertj.core.api.Assertions.assertThat;

import com.sun.net.httpserver.HttpServer;
import java.net.InetSocketAddress;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webtestclient.autoconfigure.AutoConfigureWebTestClient;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.reactive.server.WebTestClient;

/**
 * CL08-11: o trace CONTINUA através do gateway. O upstream simulado devolve o {@code traceparent} que recebeu:
 * mesmo traceId do cliente, mas com span id novo (o do span do gateway).
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    properties = {"eureka.client.enabled=false", "spring.cloud.discovery.enabled=false"})
@AutoConfigureWebTestClient
@ActiveProfiles("cb")   // reaproveita a rota apontada por CATALOG_URI
class TracePropagationTest {
    static final AtomicReference<String> recebido = new AtomicReference<>();
    static final HttpServer upstream = subir();

    static HttpServer subir() {
        try {
            var s = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
            s.createContext("/", ex -> {
                recebido.set(ex.getRequestHeaders().getFirst("traceparent"));
                byte[] b = "ok".getBytes();
                try { ex.sendResponseHeaders(200, b.length); ex.getResponseBody().write(b); } catch (Exception ignored) { } finally { ex.close(); }
            });
            s.start();
            return s;
        } catch (Exception e) { throw new IllegalStateException(e); }
    }

    @DynamicPropertySource static void props(DynamicPropertyRegistry r) {
        r.add("CATALOG_URI", () -> "http://127.0.0.1:" + upstream.getAddress().getPort());
    }

    @AfterAll static void parar() { upstream.stop(0); }

    @Autowired WebTestClient web;

    @Test void cl0811_traceparentAtravessaOGateway() {
        String traceId = "4bf92f3577b34da6a3ce929d0e0e4736", spanCliente = "00f067aa0ba902b7";
        web.get().uri("/cb/catalog/products/1").header("traceparent", "00-" + traceId + "-" + spanCliente + "-01")
           .exchange().expectStatus().isOk();
        assertThat(recebido.get()).as("o upstream recebeu traceparent").isNotNull()
            .matches("00-" + traceId + "-[0-9a-f]{16}-01")
            .doesNotContain(spanCliente);   // span id novo: o gateway abriu o próprio span
    }
}
