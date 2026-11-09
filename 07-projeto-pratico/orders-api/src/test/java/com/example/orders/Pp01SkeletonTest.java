package com.example.orders;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionEvaluationReport;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.test.web.servlet.MockMvc;

/** PP01-01: o esqueleto sobe, o health responde e o relatório de condições explica as auto-configurações. */
@SpringBootTest
@AutoConfigureMockMvc
class Pp01SkeletonTest {
    @Autowired MockMvc mvc;
    @Autowired ConfigurableApplicationContext ctx;

    @Test void health_up_semAutenticacao() throws Exception {
        mvc.perform(get("/actuator/health")).andExpect(status().isOk()).andExpect(jsonPath("$.status").value("UP"));
    }

    /** Verificação: três auto-configurações e a condição que as ativou (o que o --debug imprime). */
    @Test void relatorioDeCondicoes_explicaTresAutoConfiguracoes() {
        var report = ConditionEvaluationReport.get(ctx.getBeanFactory());
        var positivas = report.getConditionAndOutcomesBySource().entrySet().stream()
                .filter(e -> e.getValue().isFullMatch()).map(e -> e.getKey()).toList();
        assertThat(positivas).anyMatch(s -> s.endsWith("DataSourceAutoConfiguration"));
        assertThat(positivas).anyMatch(s -> s.endsWith("DispatcherServletAutoConfiguration"));
        assertThat(positivas).anyMatch(s -> s.endsWith("JacksonAutoConfiguration"));
        assertThat(ctx.getBeansOfType(javax.sql.DataSource.class)).isNotEmpty();
    }
}
