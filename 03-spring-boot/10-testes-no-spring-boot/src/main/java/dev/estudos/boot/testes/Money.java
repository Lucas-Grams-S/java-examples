package dev.estudos.boot.testes;

import java.math.BigDecimal;

/** SB10-10: lógica em record, testável sem Spring. */
public record Money(BigDecimal amount) {
    public Money { if (amount.signum() < 0) throw new IllegalArgumentException("negativo"); }
}
