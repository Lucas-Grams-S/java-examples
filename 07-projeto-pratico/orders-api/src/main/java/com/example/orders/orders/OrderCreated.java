package com.example.orders.orders;

/** Evento de aplicação publicado dentro da transação; os ouvintes AFTER_COMMIT só o veem após o commit. */
public record OrderCreated(Long orderId, Long customerId) {}
