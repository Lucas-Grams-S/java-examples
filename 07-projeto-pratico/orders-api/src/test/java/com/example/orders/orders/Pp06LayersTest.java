package com.example.orders.orders;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.orders.catalog.CatalogService;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

/** PP06-01 (camada web): {@code @WebMvcTest} carrega só o MVC; os {@code @Service} não entram, por isso o serviço é um mock. */
@WebMvcTest(OrderController.class)
@AutoConfigureMockMvc(addFilters = false)   // a segurança é testada em Pp05
class Pp06LayersTest {
    @Autowired MockMvc mvc;
    @Autowired ApplicationContext ctx;
    @MockitoBean OrderService service;   // variar @MockitoBean entre classes cria outro contexto (cache de contextos por configuração)

    @Test void controller_traduzHttp_comServicoSimulado() throws Exception {
        var order = Order.restore(7L, 1L, List.of(new OrderItem(1L, 2, new BigDecimal("1.50"))),
                OrderStatus.NEW, Instant.parse("2026-11-05T12:00:00Z"));
        when(service.find(7L)).thenReturn(order);
        mvc.perform(get("/orders/7")).andExpect(status().isOk()).andExpect(jsonPath("$.total").value(3.00));
    }

    @Test void servicosReaisNaoForamCarregados() {
        assertThat(ctx.getBeanNamesForType(CatalogService.class)).isEmpty();
        assertThat(ctx.getBeanNamesForType(com.example.orders.customers.CustomerRepository.class)).isEmpty();
    }
}
