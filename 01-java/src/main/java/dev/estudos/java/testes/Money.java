package dev.estudos.java.testes;

import java.math.BigDecimal;

/** Código sob teste (J15-11): record com validação no construtor compacto. */
public record Money(BigDecimal amount) {
    public Money {
        if (amount.signum() < 0) throw new IllegalArgumentException("negative");
    }
    public Money plus(Money o) { return new Money(amount.add(o.amount)); }
}
