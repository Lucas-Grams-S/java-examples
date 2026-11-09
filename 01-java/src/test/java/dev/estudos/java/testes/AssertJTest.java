package dev.estudos.java.testes;

import static org.assertj.core.api.Assertions.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.Test;

/** J15-05 · AssertJ: encadeamento, BigDecimal, extracting, exceções, Map, isCloseTo e SoftAssertions. */
class AssertJTest {
    record Order(String id, BigDecimal total) {}

    @Test void bigDecimal_compara_valor_ignorando_escala() {
        BigDecimal total = new BigDecimal("11.0");
        assertThat(total).isEqualByComparingTo("11.00");
        assertThat(total.equals(new BigDecimal("11.00"))).isFalse();   // equals considera a escala
    }

    @Test void colecoes_e_extracting() {
        var orders = List.of(new Order("o1", BigDecimal.ONE), new Order("o2", BigDecimal.TEN));
        assertThat(orders).hasSize(2).extracting(Order::id).containsExactly("o1", "o2");
        assertThat(orders).filteredOn(o -> o.total().compareTo(BigDecimal.ONE) > 0).extracting(Order::id).containsExactly("o2");
    }

    @Test void strings() { assertThat("Ana").startsWith("An").endsWith("a").isNotBlank().hasSize(3); }

    @Test void excecoes() {
        assertThatThrownBy(() -> { throw new IllegalStateException("duplicado: dup"); })
            .isInstanceOf(IllegalStateException.class).hasMessageContaining("dup");
        assertThatIllegalStateException().isThrownBy(() -> { throw new IllegalStateException("x"); }).withMessage("x");
        assertThatNoException().isThrownBy(() -> { });
    }

    @Test void mapa() { assertThat(Map.of("k", 1)).containsEntry("k", 1).containsKey("k").doesNotContainKey("z"); }

    @Test void double_com_tolerancia() {
        assertThat(0.1 + 0.2).isNotEqualTo(0.3);              // armadilha de ponto flutuante (ver 03-tipos-e-memoria)
        assertThat(0.1 + 0.2).isCloseTo(0.3, within(1e-9));
    }

    @Test void descricao_na_falha() {
        assertThatThrownBy(() -> assertThat(2 + 2).as("soma básica").isEqualTo(5))
            .hasMessageContaining("[soma básica]").hasMessageContaining("expected: 5");
    }

    @Test void soft_assertions_coletam_varias_falhas() {
        var soft = new SoftAssertions();
        soft.assertThat(1).isEqualTo(2);
        soft.assertThat("a").isEqualTo("b");
        assertThatThrownBy(soft::assertAll).hasMessageContaining("Multiple Failures (2 failures)");
    }
}
