package dev.estudos.boot.web;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

/** Teste de SB04-15: versionamento por cabeçalho X-Version. */
@SpringBootTest(properties = "spring.profiles.active=versao")
@AutoConfigureMockMvc
class VersaoTest {
    @Autowired MockMvc mvc;

    @Test void sb0415_versao_por_cabecalho() throws Exception {
        mvc.perform(get("/v/orders")).andExpect(jsonPath("$.version").value("1.0"));
        mvc.perform(get("/v/orders").header("X-Version", "1.1")).andExpect(jsonPath("$.version").value("1.1"));
        // versão que nenhum mapeamento declara: 400 (não há "1.1 ou superior")
        mvc.perform(get("/v/orders").header("X-Version", "1.2")).andExpect(status().isBadRequest())
           .andExpect(jsonPath("$.detail").value("Invalid API version: '1.2.0'."));
    }
}
