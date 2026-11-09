package com.example.orders.orders;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/** PP8: só roda DEPOIS do commit (rollback => nunca roda). Aqui só registra; num sistema real publicaria numa fila (Outbox). */
@Component
public class OrderCreatedListener {
    private static final Logger log = LoggerFactory.getLogger(OrderCreatedListener.class);
    private final List<Long> seen = new CopyOnWriteArrayList<>();
    private volatile boolean failing;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    void on(OrderCreated e) {
        seen.add(e.orderId());
        log.info("pedido {} criado (após commit)", e.orderId());
        if (failing) throw new IllegalStateException("falha no listener (a transação já foi confirmada)");
    }

    public List<Long> seen() { return List.copyOf(seen); }
    public void clear() { seen.clear(); failing = false; }
    public void failNext(boolean f) { this.failing = f; }
}
