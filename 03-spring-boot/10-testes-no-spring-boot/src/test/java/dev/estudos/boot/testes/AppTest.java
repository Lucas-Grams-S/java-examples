package dev.estudos.boot.testes;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;

/** SB10-11: contexto completo + MockMvc. */
@SpringBootTest
@AutoConfigureMockMvc
class AppTest {
    @Autowired MockMvc mvc;
    @Autowired PriceService prices;

    @Test void sb1011_contextoCompletoECache() throws Exception {
        mvc.perform(get("/orders/999")).andExpect(status().isNotFound());
        prices.price("A"); prices.price("A");
        assertThat(prices.calls()).isEqualTo(1);              // 2ª chamada veio do cache
        assertThat(prices.getClass()).isNotEqualTo(PriceService.class);   // é um proxy CGLIB
        assertThat(prices.calls).isNull();                     // campo herdado do proxy NÃO é inicializado
    }
}
