package dev.estudos.boot.actuator;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

/** SB09-15: EndpointRequest protege o Actuator; health/info abertos, o resto só para ROLE_OPS. */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("seguranca")
class ActuatorSegurancaTest {
    @Autowired MockMvc mvc;

    @Test void sb0915_health_e_info_abertos() throws Exception {
        mvc.perform(get("/actuator/health")).andExpect(status().isOk());
        mvc.perform(get("/actuator/info")).andExpect(status().isOk());
    }

    @Test void sb0915_demais_endpoints_exigem_ops() throws Exception {
        mvc.perform(get("/actuator/metrics")).andExpect(status().isUnauthorized());
        mvc.perform(get("/actuator/metrics").with(httpBasic("dev", "dev"))).andExpect(status().isForbidden());
        mvc.perform(get("/actuator/metrics").with(httpBasic("ops", "ops"))).andExpect(status().isOk());
    }
}
