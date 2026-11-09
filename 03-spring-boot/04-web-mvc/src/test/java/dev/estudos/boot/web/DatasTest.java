package dev.estudos.boot.web;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

/** Teste de SB04-03: o ServerHttpMessageConvertersCustomizer troca o formato de data do JSON. */
@SpringBootTest(properties = "spring.profiles.active=datas")
@AutoConfigureMockMvc
class DatasTest {
    @Autowired MockMvc mvc;

    @Test void sb0403_conversor_json_customizado() throws Exception {
        mvc.perform(get("/dates/sample")).andExpect(status().isOk()).andExpect(content().json("{\"date\":\"1970-02\"}"));
    }
}
