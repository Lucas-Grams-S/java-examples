package com.example.orders;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.example.orders.catalog.CatalogService;
import java.math.BigDecimal;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

/** PP02-01: API REST ponta a ponta (MockMvc); tokens simulados com {@code jwt()}. */
@SpringBootTest
@AutoConfigureMockMvc
class Pp02ApiTest {
    @Autowired MockMvc mvc;
    @Autowired CatalogService catalog;

    static RequestPostProcessor user(String email) { return jwt().jwt(j -> j.subject(email)).authorities(new SimpleGrantedAuthority("ROLE_USER")); }

    long newCustomer(String email) throws Exception {
        var body = mvc.perform(post("/customers").with(user(email)).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Ana\",\"email\":\"" + email + "\"}"))
                .andExpect(status().isCreated()).andExpect(header().exists("Location")).andReturn().getResponse().getContentAsString();
        return Long.parseLong(body.replaceAll(".*\"id\":(\\d+).*", "$1"));
    }

    @Test void semToken_401() throws Exception {
        mvc.perform(get("/customers/1")).andExpect(status().isUnauthorized());
    }

    @Test void clienteCriaEBusca_emailRepetido409_emailInvalido400() throws Exception {
        var email = "a-" + UUID.randomUUID() + "@x.com";
        long id = newCustomer(email);
        mvc.perform(get("/customers/" + id).with(user(email))).andExpect(status().isOk()).andExpect(jsonPath("$.email").value(email));
        mvc.perform(post("/customers").with(user(email)).contentType(MediaType.APPLICATION_JSON).content("{\"name\":\"Outra\",\"email\":\"" + email + "\"}"))
                .andExpect(status().isConflict()).andExpect(jsonPath("$.status").value(409));
        mvc.perform(post("/customers").with(user(email)).contentType(MediaType.APPLICATION_JSON).content("{\"name\":\"\",\"email\":\"nao-eh-email\"}"))
                .andExpect(status().isBadRequest());
        mvc.perform(get("/customers/999999").with(user(email))).andExpect(status().isNotFound()).andExpect(jsonPath("$.title").value("Not Found"));
    }

    @Test void pedido_criado201_comTotal_eBuscado() throws Exception {
        var email = "b-" + UUID.randomUUID() + "@x.com";
        long customer = newCustomer(email);
        var p = catalog.create("Caneta", new BigDecimal("2.50"), 10);
        var body = mvc.perform(post("/orders").with(user(email)).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"customerId\":%d,\"items\":[{\"productId\":%d,\"quantity\":4}]}".formatted(customer, p.id())))
                .andExpect(status().isCreated()).andExpect(jsonPath("$.status").value("NEW"))
                .andExpect(jsonPath("$.total").value(10.00)).andExpect(jsonPath("$.items[0].unitPrice").value(2.50))
                .andReturn().getResponse().getContentAsString();
        var got = mvc.perform(get("/orders/" + body.replaceAll(".*?\"id\":(\\d+).*", "$1")).with(user(email)))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        assertThat(got).contains("\"status\":\"NEW\"");
    }

    /** Verificação: quantity negativo => 400 (Bean Validation); estoque insuficiente => 422 (regra de domínio). */
    @Test void quantidadeNegativa400_estoqueInsuficiente422() throws Exception {
        var email = "c-" + UUID.randomUUID() + "@x.com";
        long customer = newCustomer(email);
        var p = catalog.create("Lápis", new BigDecimal("1.00"), 1);
        mvc.perform(post("/orders").with(user(email)).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"customerId\":%d,\"items\":[{\"productId\":%d,\"quantity\":-1}]}".formatted(customer, p.id())))
                .andExpect(status().isBadRequest());
        mvc.perform(post("/orders").with(user(email)).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"customerId\":%d,\"items\":[]}".formatted(customer))).andExpect(status().isBadRequest());
        mvc.perform(post("/orders").with(user(email)).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"customerId\":%d,\"items\":[{\"productId\":%d,\"quantity\":5}]}".formatted(customer, p.id())))
                .andExpect(status().isUnprocessableContent()).andExpect(jsonPath("$.detail").value("insufficient stock for product " + p.id()));
    }
}
