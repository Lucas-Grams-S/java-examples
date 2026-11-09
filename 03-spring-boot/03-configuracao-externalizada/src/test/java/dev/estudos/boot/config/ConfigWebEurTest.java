package dev.estudos.boot.config;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;

/** Teste de SB03-11: uma propriedade externa (aqui, do teste) vence o YAML. */
@SpringBootTest(properties = "orders.currency=EUR")
@AutoConfigureMockMvc
class ConfigWebEurTest {
    @Autowired MockMvc mvc;

    @Test void sb0311_propriedade_externa_vence_o_yaml() throws Exception {
        mvc.perform(post("/orders")).andExpect(jsonPath("$.currency").value("EUR"));
    }
}
