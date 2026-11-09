package com.example.orders.orders;

import com.example.orders.shared.DomainException;
import java.math.BigDecimal;

/** Imutável; o preço é copiado no momento da compra. */
public record OrderItem(Long productId, int quantity, BigDecimal unitPrice) {
    public OrderItem {
        if (quantity <= 0) throw new DomainException("quantity must be > 0");
        if (unitPrice == null || unitPrice.signum() < 0) throw new DomainException("unitPrice must be >= 0");
    }
    public BigDecimal subtotal() { return unitPrice.multiply(BigDecimal.valueOf(quantity)); }
}
