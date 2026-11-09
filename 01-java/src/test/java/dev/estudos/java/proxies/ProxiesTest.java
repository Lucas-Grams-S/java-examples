package dev.estudos.java.proxies;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

/** Testes dos exemplos J11-01 a J11-09. */
class ProxiesTest {
    @Test void j1101_jdk_proxy() {
        assertThat(JdkProxy.run()).contains("place -> placed 42", "default -> hi", "toString passa pelo handler -> proxy@toString",
                "chamadas vistas pelo handler -> [place, hi, toString, fail]", "isProxyClass -> true",
                "cast para a classe concreta -> ClassCastException",
                "checked não declarada -> UndeclaredThrowableException(IOException)");
    }

    @Test void j1102_proxy_de_classe() {
        assertThat(ClassProxy.run()).contains("é subtipo da classe -> true", "interceptados até aqui -> [place, audit]",
                "método final -> interceptado? false");
    }

    @Test void j1107_proxy_de_log() {
        assertThat(LoggingProxy.run()).containsExactly(">> place", "resultado placed 7", ">> send", "!! IllegalStateException",
                "propagou smtp fora");
    }

    @Test void j1108_proxy_tx() {
        assertThat(TxProxy.run()).containsExactly("BEGIN", "COMMIT", "depositado 10", "BEGIN", "ROLLBACK",
                "erro saldo inválido", "lido");
    }

    @Test void j1109_class_file_api() throws Exception {
        assertThat(ClassFileApi.run()).contains("invocação -> ola gerado", "bytes gerados começam com CAFEBABE -> CAFEBABE",
                "métodos da classe gerada (via parse) -> [greet]");
    }
}
