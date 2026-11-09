package dev.estudos.boot.jpa;

import java.math.BigDecimal;

public record OrderDto(Long id, String customer, BigDecimal total) {}
