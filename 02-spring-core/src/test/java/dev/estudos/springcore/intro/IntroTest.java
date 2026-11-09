package dev.estudos.springcore.intro;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

/** Testes dos exemplos SC01-02, 04, 05 e 06. */
class IntroTest {
    @Test void sc0102_modulos() throws Exception {
        var out = ModulosSpring.run();
        assertThat(out.getFirst()).startsWith("Spring Framework -> 7.0");
        assertThat(out).anyMatch(l -> l.matches("spring-core -> spring-core-7\\.0\\.\\d+\\.jar"));
        assertThat(out).anyMatch(l -> l.startsWith("spring-aop -> spring-aop-7.0"));
        assertThat(out).hasSize(6);
    }
    @Test void sc0104_sem_spring() {
        assertThat(SemSpring.run()).containsExactly("cartão cobrou 1500 centavos de p-1", "pedidos salvos -> 1");
    }
    @Test void sc0105_com_spring() {
        assertThat(ComSpring.run()).containsExactly("cartão cobrou 1500 centavos de p-1", "pedidos salvos -> 1",
            "repo injetado é InMemoryOrderRepository -> true");
    }
    @Test void sc0106_proxy() {
        assertThat(ProxyEnriquecido.run()).containsExactly("eventos -> [begin, commit]", "é proxy do Spring AOP -> true",
            "classe real -> OrderService", "classe em runtime contém $$SpringCGLIB$$ -> true");
    }
}
