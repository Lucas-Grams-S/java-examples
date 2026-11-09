package com.example.orders.shared;

/** Violação de regra de negócio (diferente de validação de entrada). Vira 422. */
public class DomainException extends RuntimeException {
    public DomainException(String message) { super(message); }
}
