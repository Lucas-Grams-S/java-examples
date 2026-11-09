package dev.estudos.boot.jpa;

import java.math.BigDecimal;

/** SB06-07: projeção baseada em interface (closed). */
public interface OrderSummary { Long getId(); BigDecimal getTotal(); }
