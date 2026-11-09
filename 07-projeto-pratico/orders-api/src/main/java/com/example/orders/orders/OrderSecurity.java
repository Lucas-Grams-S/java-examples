package com.example.orders.orders;

import com.example.orders.customers.CustomerRepository;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/** Referenciado por SpEL ({@code @orderSecurity}) nos {@code @PreAuthorize}. O "subject" do JWT é o e-mail do cliente. */
@Component("orderSecurity")
class OrderSecurity {
    private final OrderRepository orders;
    private final CustomerRepository customers;
    OrderSecurity(OrderRepository orders, CustomerRepository customers) { this.orders = orders; this.customers = customers; }

    @Transactional(readOnly = true)
    public boolean isOwner(Long orderId, Authentication auth) {
        return orders.customerIdOf(orderId)
                .flatMap(customers::findById)
                .map(c -> c.getEmail().equals(auth.getName()))
                .orElse(false);   // pedido inexistente: nega (vira 403, não vaza existência)
    }
}
