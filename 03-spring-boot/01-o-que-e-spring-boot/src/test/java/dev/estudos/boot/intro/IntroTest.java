package dev.estudos.boot.intro;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;

/** Testes de SB01-01, 07, 09 e 10. */
@SpringBootTest
class IntroTest {
    @Autowired ApplicationContext ctx;
    @Autowired StartupLogger logger;

    @Test void sb0101_meta_anotacoes() {
        var a = OrdersApplication.class;
        assertThat(AnnotatedElementUtils.hasAnnotation(a, SpringBootConfiguration.class)).isTrue();
        assertThat(AnnotatedElementUtils.hasAnnotation(a, EnableAutoConfiguration.class)).isTrue();
        assertThat(AnnotatedElementUtils.hasAnnotation(a, ComponentScan.class)).isTrue();
    }
    @Test void sb0107_contexto_sobe_com_webmvc() {
        assertThat(ctx.containsBean("dispatcherServlet")).isTrue();
        assertThat(ctx.getBean(OrdersProperties.class).currency()).isEqualTo("BRL");
    }
    @Test void sb0109_application_runner_rodou() {
        assertThat(logger.messages()).containsExactly("Moeda padrão: BRL", "Opções: [] / não-opções: []");
    }
    @Test void sb0110_customizar_spring_application() {
        assertThat(CustomizeSpringApplication.run()).containsExactly(
            "perfis ativos = [local]", "tipo de contexto = AnnotationConfigApplicationContext");
    }
}
