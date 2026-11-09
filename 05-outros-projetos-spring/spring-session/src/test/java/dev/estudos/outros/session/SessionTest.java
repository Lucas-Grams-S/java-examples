package dev.estudos.outros.session;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;

/** OP07-02. */
@SpringBootTest
@AutoConfigureMockMvc
class SessionTest {
    @Autowired MockMvc mvc;
    @Autowired JdbcTemplate jdbc;

    @Test void op0702_sessaoFicaNaTabelaSpringSession() throws Exception {
        mvc.perform(get("/cart/add")).andExpect(content().string("n=1"));
        assertThat(jdbc.queryForObject("select count(*) from spring_session", Integer.class)).isGreaterThan(0);
        assertThat(jdbc.queryForObject("select count(*) from spring_session_attributes", Integer.class)).isGreaterThan(0);
    }

    /** O cookie SESSION (id do repositório) mantém o estado entre requisições, sem HttpSession do Tomcat. */
    @Test void op0702_cookieSession_mantemOEstado() throws Exception {
        var r = mvc.perform(get("/cart/add")).andExpect(content().string("n=1")).andReturn();
        Cookie c = r.getResponse().getCookie("SESSION");
        assertThat(c).as("cookie SESSION do Spring Session").isNotNull();
        mvc.perform(get("/cart/add").cookie(c)).andExpect(content().string("n=2"));
        mvc.perform(get("/cart/add").cookie(c)).andExpect(content().string("n=3"));
    }
}
