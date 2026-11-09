package dev.estudos.cloud.gateway;

import static org.assertj.core.api.Assertions.assertThat;

import com.sun.net.httpserver.HttpServer;
import java.net.InetSocketAddress;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webtestclient.autoconfigure.AutoConfigureWebTestClient;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.reactive.server.WebTestClient;

/** CL06-15: filtro CircuitBreaker do Gateway com upstream simulado (sem Eureka): 5xx, timeout e circuito aberto -> fallback. */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    properties = {"eureka.client.enabled=false", "spring.cloud.discovery.enabled=false"})
@AutoConfigureWebTestClient
@ActiveProfiles("cb")
class CircuitBreakerRouteTest {
    static final AtomicInteger chamadas = new AtomicInteger();
    static volatile int modo;   // 0 = ok, 1 = 500, 2 = lento
    static final HttpServer upstream = subir();

    static HttpServer subir() {
        try {
            var s = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
            s.createContext("/", ex -> {
                chamadas.incrementAndGet();
                try {
                    if (modo == 2) Thread.sleep(1500);
                    byte[] b = (modo == 1 ? "erro" : "produto").getBytes();
                    ex.sendResponseHeaders(modo == 1 ? 500 : 200, b.length);
                    ex.getResponseBody().write(b);
                } catch (Exception ignored) { } finally { ex.close(); }
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

    @Test void cl0615_okFalhaTimeoutECircuitoAberto() {
        modo = 0;
        web.get().uri("/cb/catalog/products/1").exchange().expectStatus().isOk().expectBody(String.class).isEqualTo("produto");

        modo = 2;   // acima do timelimiter de 500 ms -> fallback
        web.get().uri("/cb/catalog/products/1").exchange().expectStatus().isEqualTo(503)
           .expectBody(String.class).value(b -> assertThat(b).contains("fallback do gateway"));

        modo = 1;   // 500 do upstream: no Gateway 5.0 sem "statusCodes" a resposta 5xx também cai no fallback (ver DIVERGENCIAS.md)
        web.get().uri("/cb/catalog/products/1").exchange().expectStatus().isEqualTo(503)
           .expectBody(String.class).value(b -> assertThat(b).contains("fallback do gateway"));

        // completa a janela de 4 chamadas com falhas (timeouts) para abrir o circuito
        modo = 2;
        for (int i = 0; i < 3; i++) web.get().uri("/cb/catalog/products/1").exchange().expectStatus().isEqualTo(503);
        modo = 0;
        int antes = chamadas.get();
        web.get().uri("/cb/catalog/products/1").exchange().expectStatus().isEqualTo(503);   // circuito aberto: nem chega ao upstream
        assertThat(chamadas.get()).isEqualTo(antes);
    }
}
