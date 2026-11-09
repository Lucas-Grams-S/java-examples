package dev.estudos.boot.testes;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

/** SB10-12 (e SB10-05/08): slice web com {@code @MockitoBean}; passa porque a classe principal não tem {@code @EnableCaching}. */
@WebMvcTest(Api.class)
class ApiSliceTest {
    @Autowired MockMvc mvc;
    @MockitoBean OrderService orders;

    @Test void sb1012_retornaPedido() throws Exception {
        when(orders.find(1L)).thenReturn(new OrderDto(1L, "Ana"));
        mvc.perform(get("/orders/1")).andExpect(status().isOk()).andExpect(jsonPath("$.customer").value("Ana"));
    }

    @Test void sb1012_erroVira404() throws Exception {
        when(orders.find(2L)).thenThrow(new IllegalArgumentException("x"));
        mvc.perform(get("/orders/2")).andExpect(status().isNotFound());
    }
}
