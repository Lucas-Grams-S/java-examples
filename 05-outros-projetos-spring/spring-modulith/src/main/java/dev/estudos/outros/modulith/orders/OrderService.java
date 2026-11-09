package dev.estudos.outros.modulith.orders;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class OrderService {
    private final ApplicationEventPublisher events;

    public OrderService(ApplicationEventPublisher events) { this.events = events; }

    @Transactional   // obrigatório para o listener pós-commit rodar
    public void place(String id, String sku, int q) { events.publishEvent(new OrderPlaced(id, sku, q)); }

    /** Variante SEM transação: o evento é descartado pelo listener AFTER_COMMIT. */
    public void placeSemTx(String id, String sku, int q) { events.publishEvent(new OrderPlaced(id, sku, q)); }
}
