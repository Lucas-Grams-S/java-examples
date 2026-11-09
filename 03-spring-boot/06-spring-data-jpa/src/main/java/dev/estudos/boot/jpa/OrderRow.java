package dev.estudos.boot.jpa;

import java.math.BigDecimal;

/** SB06-07: DTO (record) para projeção com select new. */
public record OrderRow(Long id, String customerName, BigDecimal total) {}
