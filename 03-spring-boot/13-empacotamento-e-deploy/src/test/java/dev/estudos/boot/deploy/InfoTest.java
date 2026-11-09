package dev.estudos.boot.deploy;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;

/** SB13-14: o goal {@code build-info} grava META-INF/build-info.properties; o /actuator/info o expõe. */
@SpringBootTest
@AutoConfigureMockMvc
class InfoTest {
    @Autowired MockMvc mvc;

    @Test void sb1314_versaoNoInfo() throws Exception {
        mvc.perform(get("/actuator/info")).andExpect(status().isOk())
           .andExpect(jsonPath("$.build.artifact").value("sb-13-deploy"))
           .andExpect(jsonPath("$.build.version").value("1.0.0-SNAPSHOT"));
    }
}
