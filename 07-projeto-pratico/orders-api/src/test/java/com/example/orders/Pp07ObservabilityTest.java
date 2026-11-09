package com.example.orders;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.micrometer.metrics.test.autoconfigure.AutoConfigureMetrics;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.MockMvc;

/** PP07-01: probes de liveness/readiness públicas; métricas só para ADMIN; {@code http.server.requests} mede POST /orders. */
@SpringBootTest
@AutoConfigureMockMvc
@AutoConfigureMetrics
class Pp07ObservabilityTest {
    @Autowired MockMvc mvc;

    @Test void probes_publicas() throws Exception {
        mvc.perform(get("/actuator/health/liveness")).andExpect(status().isOk()).andExpect(jsonPath("$.status").value("UP"));
        mvc.perform(get("/actuator/health/readiness")).andExpect(status().isOk()).andExpect(jsonPath("$.status").value("UP"));
    }

    @Test void metricas_exigemAdmin() throws Exception {
        mvc.perform(get("/actuator/metrics")).andExpect(status().isUnauthorized());
        mvc.perform(get("/actuator/metrics").with(jwt().authorities(new SimpleGrantedAuthority("ROLE_USER")))).andExpect(status().isForbidden());
    }

    /** Verificação do roteiro: qual métrica mostra o tempo de POST /orders? {@code http.server.requests}, tag {@code uri=/orders}. */
    @Test void httpServerRequests_temOPostDeOrders() throws Exception {
        var admin = jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN"));
        mvc.perform(post("/orders").with(admin).contentType(MediaType.APPLICATION_JSON).content("{}")).andExpect(status().isBadRequest());
        mvc.perform(get("/actuator/metrics/http.server.requests").param("tag", "uri:/orders").param("tag", "method:POST").with(admin))
                .andExpect(status().isOk()).andExpect(jsonPath("$.name").value("http.server.requests"))
                .andExpect(jsonPath("$.measurements[?(@.statistic=='COUNT')].value").exists());
    }
}
