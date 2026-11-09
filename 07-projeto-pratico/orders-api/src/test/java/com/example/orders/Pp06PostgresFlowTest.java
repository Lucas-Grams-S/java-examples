package com.example.orders;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.orders.catalog.CatalogService;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

/** PP06-01 (fluxo completo): PostgreSQL real, Flyway e {@code ddl-auto=validate} contra o Postgres. Exige Docker. */
@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
class Pp06PostgresFlowTest {
    @Container @ServiceConnection
    static PostgreSQLContainer pg = new PostgreSQLContainer("postgres:17-alpine");

    @Autowired MockMvc mvc;
    @Autowired JdbcClient jdbc;
    @Autowired CatalogService catalog;

    @Test void fluxoCompleto_noPostgres() throws Exception {
        assertThat(jdbc.sql("select version()").query(String.class).single()).contains("PostgreSQL");
        var email = "pg@x.com";
        var auth = jwt().jwt(j -> j.subject(email)).authorities(new SimpleGrantedAuthority("ROLE_USER"));
        var body = mvc.perform(post("/customers").with(auth).contentType(MediaType.APPLICATION_JSON).content("{\"name\":\"Pg\",\"email\":\"" + email + "\"}"))
                .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();
        long customer = Long.parseLong(body.replaceAll(".*\"id\":(\\d+).*", "$1"));
        var p = catalog.create("Item", new BigDecimal("9.99"), 3);
        var created = mvc.perform(post("/orders").with(auth).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"customerId\":%d,\"items\":[{\"productId\":%d,\"quantity\":2}]}".formatted(customer, p.id())))
                .andExpect(status().isCreated()).andExpect(jsonPath("$.total").value(19.98)).andReturn().getResponse().getContentAsString();
        String id = created.replaceAll(".*?\"id\":(\\d+).*", "$1");
        mvc.perform(post("/orders/" + id + "/cancel").with(auth)).andExpect(status().isOk()).andExpect(jsonPath("$.status").value("CANCELED"));
    }
}
