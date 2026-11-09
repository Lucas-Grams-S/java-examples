package dev.estudos.java.testes;

import static org.assertj.core.api.Assertions.*;

import java.math.BigDecimal;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** J15-11 · Teste de um record com validação. */
class MoneyTest {
    @Test @DisplayName("soma valores")
    void sums() {
        assertThat(new Money(BigDecimal.ONE).plus(new Money(BigDecimal.TEN)).amount()).isEqualByComparingTo("11");
    }

    @Test void rejectsNegative() {
        assertThatThrownBy(() -> new Money(new BigDecimal("-1")))
            .isInstanceOf(IllegalArgumentException.class).hasMessage("negative");
    }

    @Test void zeroEPermitido() { assertThat(new Money(BigDecimal.ZERO).amount()).isEqualByComparingTo("0"); }

    @Test void recordTemEqualsPorComponente() {
        assertThat(new Money(new BigDecimal("1"))).isEqualTo(new Money(new BigDecimal("1")));
        assertThat(new Money(new BigDecimal("1.0"))).isNotEqualTo(new Money(new BigDecimal("1.00")));   // BigDecimal.equals vê a escala
    }
}
