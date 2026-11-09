package com.example.orders.orders;

/** Resultado de uma operação que pode falhar por regra, sem exceção. */
public sealed interface CancelResult {
    record Canceled(Order order) implements CancelResult {}
    record Rejected(String reason) implements CancelResult {}
}
