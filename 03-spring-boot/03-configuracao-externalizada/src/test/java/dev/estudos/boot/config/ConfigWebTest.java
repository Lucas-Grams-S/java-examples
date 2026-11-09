package dev.estudos.boot.config;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;

/** Teste de SB03-11: o controlador usa as propriedades tipadas. */
@SpringBootTest
@AutoConfigureMockMvc
class ConfigWebTest {
    @Autowired MockMvc mvc;

    @Test void sb0311_controlador_usa_as_propriedades() throws Exception {
        mvc.perform(post("/orders")).andExpect(status().isOk())
           .andExpect(jsonPath("$.currency").value("BRL")).andExpect(jsonPath("$.maxItems").value(50));
    }
}
