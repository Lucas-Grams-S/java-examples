package dev.estudos.boot.actuator;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest(properties = "management.defaults.metrics.export.enabled=true")
@AutoConfigureMockMvc
class ActuatorTest {
    @Autowired MockMvc mvc;
    @Autowired StockHealth stock;
    @Autowired OrderService orders;

    @Test void sb0910_info_e_exposicao_restritiva() throws Exception {
        mvc.perform(get("/actuator/info")).andExpect(status().isOk())
           .andExpect(jsonPath("$.app.name").value("orders"));
        // não exposto => 404 (SB09-02)
        mvc.perform(get("/actuator/env")).andExpect(status().isNotFound());
        mvc.perform(get("/actuator/beans")).andExpect(status().isNotFound());
    }

    @Test void sb0903_0911_health_composto_e_503() throws Exception {
        stock.setOk(true);
        mvc.perform(get("/actuator/health")).andExpect(status().isOk())
           .andExpect(jsonPath("$.status").value("UP"))
           .andExpect(jsonPath("$.components.stock.details.items").value(42));
        mvc.perform(get("/actuator/health/liveness")).andExpect(status().isOk());
        mvc.perform(get("/actuator/health/readiness")).andExpect(status().isOk());
        stock.setOk(false);
        try {
            mvc.perform(get("/actuator/health")).andExpect(status().isServiceUnavailable())
               .andExpect(jsonPath("$.status").value("DOWN"));
        } finally { stock.setOk(true); }
    }

    @Test void sb0912_metrica_de_negocio() throws Exception {
        orders.place(); orders.place();
        mvc.perform(get("/actuator/metrics/orders.placed")).andExpect(status().isOk())
           .andExpect(jsonPath("$.measurements[0].value").value(2.0))
           .andExpect(jsonPath("$.availableTags[0].tag").value("channel"));
    }

    @Test void sb0913_flyway_caches_scheduledtasks() throws Exception {
        mvc.perform(get("/actuator/flyway")).andExpect(status().isOk())
           .andExpect(jsonPath("$.contexts.orders.flywayBeans.flyway.migrations[0].script").value("V1__create_orders.sql"))
           .andExpect(jsonPath("$.contexts.orders.flywayBeans.flyway.migrations[1].state").value("SUCCESS"));
        orders.price("abc");   // o ConcurrentMapCacheManager cria o cache sob demanda
        mvc.perform(get("/actuator/caches")).andExpect(status().isOk())
           .andExpect(jsonPath("$.cacheManagers.cacheManager.caches.prices").exists());
        mvc.perform(get("/actuator/scheduledtasks")).andExpect(status().isOk())
           .andExpect(jsonPath("$.fixedRate[0].interval").value(200));
    }

    @Test void sb0914_nivel_de_log_em_runtime() throws Exception {
        mvc.perform(post("/actuator/loggers/dev.estudos.boot.actuator").contentType(MediaType.APPLICATION_JSON)
           .content("{\"configuredLevel\":\"DEBUG\"}")).andExpect(status().isNoContent());
        mvc.perform(get("/actuator/loggers/dev.estudos.boot.actuator"))
           .andExpect(jsonPath("$.configuredLevel").value("DEBUG"));
    }
}
