package dev.estudos.boot.web;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

/** Testes de SB04-10, 11, 13 (fluxo, erros padrão em ProblemDetail) e 14 (CORS e estáticos com hash). */
@SpringBootTest
@AutoConfigureMockMvc    // org.springframework.boot.webmvc.test.autoconfigure (Boot 4)
class OrdersTest {
    @Autowired MockMvc mvc;

    @Test void sb0413_fluxo_criar_e_buscar() throws Exception {
        mvc.perform(post("/orders").contentType(MediaType.APPLICATION_JSON)
                .content("{\"customerId\":\"c1\",\"total\":10.5}"))
           .andExpect(status().isCreated()).andExpect(header().exists("Location"))
           .andExpect(jsonPath("$.customerId").value("c1")).andExpect(jsonPath("$.total").value(10.5))
           .andExpect(jsonPath("$.currency").value("BRL"));
        mvc.perform(get("/orders/99")).andExpect(status().isNotFound());
    }
    @Test void sb0411_nao_encontrado_vira_problem_detail() throws Exception {
        mvc.perform(get("/orders/99")).andExpect(status().isNotFound())
           .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
           .andExpect(jsonPath("$.detail").value("Pedido 99 não encontrado"))
           .andExpect(jsonPath("$.title").value("Not Found")).andExpect(jsonPath("$.instance").value("/orders/99"));
    }
    @Test void sb0411_validacao_padrao_nao_lista_campos() throws Exception {
        mvc.perform(post("/orders").contentType(MediaType.APPLICATION_JSON).content("{\"customerId\":\"\",\"total\":-1}"))
           .andExpect(status().isBadRequest()).andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
           .andExpect(jsonPath("$.detail").value("Invalid request content.")).andExpect(jsonPath("$.errors").doesNotExist());
    }
    @Test void sb0411_parametro_invalido() throws Exception {
        mvc.perform(get("/orders").param("min", "abc")).andExpect(status().isBadRequest())
           .andExpect(jsonPath("$.detail").value("Failed to convert 'min' with value: 'abc'"));
    }
    @Test void sb0410_list_filtra_por_min() throws Exception {
        mvc.perform(post("/orders").contentType(MediaType.APPLICATION_JSON).content("{\"customerId\":\"a\",\"total\":5}"));
        mvc.perform(get("/orders").param("min", "1000")).andExpect(jsonPath("$.length()").value(0));
    }
    @Test void sb0414_cors_permitido_e_negado() throws Exception {
        mvc.perform(options("/orders/1").header("Origin", "https://app.example.com").header("Access-Control-Request-Method", "GET"))
           .andExpect(status().isOk()).andExpect(header().string("Access-Control-Allow-Origin", "https://app.example.com"));
        mvc.perform(options("/orders/1").header("Origin", "https://evil.example").header("Access-Control-Request-Method", "GET"))
           .andExpect(status().isForbidden());
    }
    @Test void sb0414_estatico_com_hash_do_conteudo() throws Exception {
        String md5 = org.springframework.util.DigestUtils.md5DigestAsHex(
            new org.springframework.core.io.ClassPathResource("static/app.js").getContentAsByteArray());
        mvc.perform(get("/app-" + md5 + ".js")).andExpect(status().isOk()).andExpect(content().string("console.log(\"app\");\n"));
        mvc.perform(get("/app.js")).andExpect(status().isOk());
    }
}
