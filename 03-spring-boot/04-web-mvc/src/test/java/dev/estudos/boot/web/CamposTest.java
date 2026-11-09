package dev.estudos.boot.web;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

/** Teste de SB04-12: com o perfil 'campos' o erro de validação traz os campos. */
@SpringBootTest(properties = "spring.profiles.active=campos")
@AutoConfigureMockMvc
class CamposTest {
    @Autowired MockMvc mvc;

    @Test void sb0412_erros_por_campo() throws Exception {
        mvc.perform(post("/orders").contentType(MediaType.APPLICATION_JSON).content("{\"customerId\":\"\",\"total\":-1}"))
           .andExpect(status().isBadRequest()).andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
           .andExpect(jsonPath("$.detail").value("Dados inválidos"))
           .andExpect(jsonPath("$.errors.customerId").value("must not be blank"))
           .andExpect(jsonPath("$.errors.total").value("must be greater than 0"));
    }
}
