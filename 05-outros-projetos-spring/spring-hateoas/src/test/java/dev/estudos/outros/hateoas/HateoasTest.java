package dev.estudos.outros.hateoas;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;

/** OP08-02. */
@SpringBootTest
@AutoConfigureMockMvc
class HateoasTest {
    @Autowired MockMvc mvc;

    @Test void op0802_hal_comLinksSelfEPay() throws Exception {
        mvc.perform(get("/orders/9").accept("application/hal+json"))
           .andExpect(status().isOk())
           .andExpect(content().contentType("application/hal+json"))
           .andExpect(jsonPath("$.id").value("9"))
           .andExpect(jsonPath("$.status").value("PAID"))
           .andExpect(jsonPath("$._links.self.href").value("http://localhost/orders/9"))
           .andExpect(jsonPath("$._links.pay.href").value("http://localhost/orders/9/pay"));
    }
}
