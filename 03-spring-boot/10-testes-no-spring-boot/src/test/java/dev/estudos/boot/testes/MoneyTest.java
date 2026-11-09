package dev.estudos.boot.testes;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
import org.junit.jupiter.api.Test;

/** SB10-10: sem Spring, milissegundos. */
class MoneyTest {
    @Test void sb1010_rejeitaNegativo() {
        assertThatThrownBy(() -> new Money(new BigDecimal("-1"))).isInstanceOf(IllegalArgumentException.class);
    }
}
