package dev.estudos.padroes.antipadroes;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

/** Testes dos exemplos DP08-11 a DP08-14. */
class AntiPadroesTest {
    @Test void dp0811_fieldInjection() {
        assertThat(FieldInjectionVsConstrutor.run()).contains("field injection sem container: NPE",
            "mail: oi", "construtor: falha cedo (NPE no new)");
    }

    @Test void dp0812_anemicoVsRico() {
        assertThat(AnemicVsRico.run()).contains("anêmico via forcePaid -> status=PAID total=0 (pago e vazio)",
            "rico pay() vazio -> IllegalStateException: vazio");
    }

    @Test void dp0813_excecaoEngolida() {
        assertThat(ExcecaoEngolida.run()).contains("swallow: seguiu como se nada",
            "linhas após engolir + corrigir -> [engolido] (engolido: commit; corrigido: rollback)");
    }

    @Test void dp0814_singletonComEstado() throws Exception {
        int[] r = BeanSingletonComEstado.contar(8, 100_000);
        assertThat(r[1]).isEqualTo(800_000);          // atômico: sempre exato
        assertThat(r[0]).isLessThanOrEqualTo(800_000); // sem sincronizar: nunca passa, quase sempre perde
    }
}
