package com.example.orders.orders;

import com.example.orders.catalog.CatalogService;
import com.example.orders.customers.CustomerRepository;
import com.example.orders.orders.OrderRequests.CreateOrderRequest;
import com.example.orders.shared.ConflictException;
import com.example.orders.shared.NotFoundException;
import java.time.Clock;
import java.time.Duration;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** PP3: a transação mora no serviço, nunca no controller. */
@Service
public class OrderService {
    private final OrderRepository orders;
    private final CustomerRepository customers;
    private final CatalogService catalog;
    private final ApplicationEventPublisher events;
    private final Clock clock;

    OrderService(OrderRepository orders, CustomerRepository customers, CatalogService catalog,
                 ApplicationEventPublisher events, Clock clock) {
        this.orders = orders; this.customers = customers; this.catalog = catalog; this.events = events; this.clock = clock;
    }

    @Transactional
    public Order create(CreateOrderRequest req) {
        customers.findById(req.customerId()).orElseThrow(() -> new NotFoundException("customer " + req.customerId()));
        var items = req.items().stream().map(i -> {
            var product = catalog.find(i.productId());         // preço copiado agora (cache de leitura)
            catalog.reserve(i.productId(), i.quantity());       // DomainException => rollback de tudo
            return new OrderItem(product.id(), i.quantity(), product.price());
        }).toList();
        var saved = orders.save(new OrderEntity(Order.create(req.customerId(), items, clock.instant())));
        events.publishEvent(new OrderCreated(saved.getId(), saved.getCustomerId()));
        return saved.toDomain();
    }

    @Transactional(readOnly = true)
    @PreAuthorize("hasRole('ADMIN') or @orderSecurity.isOwner(#id, authentication)")
    public Order find(Long id) {
        return orders.findById(id).map(OrderEntity::toDomain).orElseThrow(() -> new NotFoundException("order " + id));
    }

    /** ADMIN cancela qualquer pedido; USER só os próprios. A regra é aplicada por proxy (por isso só vale em chamadas vindas de fora). */
    @Transactional
    @PreAuthorize("hasRole('ADMIN') or @orderSecurity.isOwner(#id, authentication)")
    public Order cancel(Long id) {
        var entity = orders.findById(id).orElseThrow(() -> new NotFoundException("order " + id));
        return switch (entity.toDomain().cancel()) {
            case CancelResult.Canceled c -> { entity.setStatus(c.order().status()); yield c.order(); }
            case CancelResult.Rejected r -> throw new ConflictException(r.reason());
        };
    }

    /** CP8: expira pedidos NEW antigos; devolve quantos foram expirados. */
    @Transactional
    public int expireOlderThan(Duration age) {
        return orders.expireNewBefore(clock.instant().minus(age));
    }

}
