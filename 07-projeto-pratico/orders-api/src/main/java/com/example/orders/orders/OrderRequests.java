package com.example.orders.orders;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

/** DTOs da API (nenhuma entidade JPA é exposta). Validação de entrada aqui; regra de negócio no domínio. */
public final class OrderRequests {
    private OrderRequests() {}

    public record ItemRequest(@NotNull Long productId, @Positive int quantity) {}
    public record CreateOrderRequest(@NotNull Long customerId, @NotEmpty List<@Valid ItemRequest> items) {}
    public record ItemResponse(Long productId, int quantity, BigDecimal unitPrice) {}
    public record OrderResponse(Long id, Long customerId, OrderStatus status, Instant createdAt, BigDecimal total, List<ItemResponse> items) {
        static OrderResponse of(Order o) {
            return new OrderResponse(o.id(), o.customerId(), o.status(), o.createdAt(), o.total(),
                    o.items().stream().map(i -> new ItemResponse(i.productId(), i.quantity(), i.unitPrice())).toList());
        }
    }
}
