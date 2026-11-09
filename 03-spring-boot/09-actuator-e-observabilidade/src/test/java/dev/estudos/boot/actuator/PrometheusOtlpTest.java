package dev.estudos.boot.actuator;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.sun.net.httpserver.HttpServer;
import java.net.InetSocketAddress;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;

/** SB09-16: /actuator/prometheus em texto e exportação OTLP de métricas para um coletor local simulado. */
@SpringBootTest(properties = {
    "management.endpoints.web.exposure.include=health,prometheus",
    "management.defaults.metrics.export.enabled=true",
    "management.otlp.metrics.export.enabled=true",
    "management.otlp.metrics.export.step=1s",
    "management.opentelemetry.resource-attributes.service.name=orders-otlp"})
@AutoConfigureMockMvc
class PrometheusOtlpTest {
    static final BlockingQueue<String> recebidos = new LinkedBlockingQueue<>();
    static final HttpServer coletor = iniciar();

    static HttpServer iniciar() {
        try {
            HttpServer s = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
            s.createContext("/v1/metrics", ex -> {
                recebidos.add(ex.getRequestHeaders().getFirst("Content-Type") + " " + ex.getRequestBody().readAllBytes().length);
                ex.sendResponseHeaders(200, -1); ex.close();
            });
            s.start();
            return s;
        } catch (Exception e) { throw new IllegalStateException(e); }
    }

    @DynamicPropertySource
    static void props(DynamicPropertyRegistry r) {
        r.add("management.otlp.metrics.export.url", () -> "http://127.0.0.1:" + coletor.getAddress().getPort() + "/v1/metrics");
    }

    @AfterAll static void parar() { coletor.stop(0); }

    @Autowired MockMvc mvc;

    @Test void sb0916_prometheus_em_texto() throws Exception {
        mvc.perform(get("/actuator/prometheus")).andExpect(status().isOk())
           .andExpect(content().string(org.hamcrest.Matchers.containsString("# TYPE jvm_memory_used_bytes gauge")));
    }

    @Test void sb0916_otlp_envia_ao_coletor() throws Exception {
        String req = recebidos.poll(15, TimeUnit.SECONDS);
        assertThat(req).as("coletor OTLP recebeu métricas").isNotNull().startsWith("application/x-protobuf");
    }
}
