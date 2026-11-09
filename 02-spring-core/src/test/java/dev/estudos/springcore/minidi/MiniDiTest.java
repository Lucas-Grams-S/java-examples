package dev.estudos.springcore.minidi;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

/** Testes de SC06-11 e SC06-12. */
class MiniDiTest {
    @Test void sc0611_mini_container() throws Exception {
        assertThat(MiniContainerDemo.run()).containsExactly("[init] OrderRepository pronto", "salvo o-1 / cobrado 1500",
            "[proxy] CardGateway.charge chamado", "gateway é proxy JDK -> true", "mesma instância (singleton) -> true");
    }
    @Test void sc0611_ciclo() throws Exception {
        assertThat(MiniContainerDemo.ciclo()).startsWith("Dependência circular:");
    }
    @Test void sc0612_spring_de_verdade() {
        var out = Trace.run();
        assertThat(out).contains("antes do refresh: 6 definições", "depois: 11", "[event] ContextRefreshedEvent",
            "  [advice] save", "  [advice] charge", "saved o-1/ok 10");
        assertThat(out).anyMatch(l -> l.startsWith("[ctor]") && l.contains("SpringCGLIB") && l.contains("proxy JDK"));
        assertThat(out).anyMatch(l -> l.contains("trace.CardGateway -> jdk.proxy"));
        assertThat(out).anyMatch(l -> l.contains("trace.OrderRepository ->") && l.contains("$$SpringCGLIB$$"));
    }
}
