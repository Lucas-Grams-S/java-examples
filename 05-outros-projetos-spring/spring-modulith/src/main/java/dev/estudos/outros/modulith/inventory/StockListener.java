package dev.estudos.outros.modulith.inventory;

import dev.estudos.outros.modulith.orders.OrderPlaced;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import org.springframework.modulith.events.ApplicationModuleListener;
import org.springframework.stereotype.Component;

/** Módulo {@code inventory}: depende só do evento público de {@code orders}. */
@Component
class StockListener {
    static final List<String> BAIXAS = new CopyOnWriteArrayList<>();

    @ApplicationModuleListener
    void on(OrderPlaced e) { BAIXAS.add(e.sku() + "x" + e.qty()); }
}
