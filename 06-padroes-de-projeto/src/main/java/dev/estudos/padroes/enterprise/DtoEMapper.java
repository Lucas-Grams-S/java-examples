package dev.estudos.padroes.enterprise;

import dev.estudos.padroes.enterprise.DominioPedidos.*;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * DP05-11 · DTO de entrada e saída distintos e Mapper estático.
 * Documento: java-docs/06-padroes-de-projeto/05-padroes-enterprise.md §5.2.
 *
 * <p>Saída real (JDK 25):
 * <pre>
 * OrderResponse[id=7, customer=Ana, status=NEW, total=25.50, items=[OrderItemResponse[sku=A, qty=2, subtotal=20.00], OrderItemResponse[sku=B, qty=1, subtotal=5.50]]]
 * entrada: CreateOrderRequest[customerId=1, items=[ItemRequest[sku=A, qty=2]]]
 * request tem id/status? false
 * </pre>
 */
public class DtoEMapper {
    public record CreateOrderRequest(long customerId, List<ItemRequest> items) {
        public record ItemRequest(String sku, int qty) {}
    }
    public record OrderItemResponse(String sku, int qty, BigDecimal subtotal) {}
    public record OrderResponse(long id, String customer, String status, BigDecimal total,
                                List<OrderItemResponse> items) {}

    public static final class OrderMapper {
        public static OrderResponse toResponse(Order o) {
            return new OrderResponse(o.id(), o.customer().name(), o.status(), o.total(),
                o.items().stream().map(i -> new OrderItemResponse(i.sku(), i.qty(), i.subtotal())).toList());
        }
    }

    public static List<String> run() {
        List<String> out = new ArrayList<>();
        var o = new Order(7, new Customer(1, "Ana", false));
        o.add(new OrderItem("A", 2, new BigDecimal("10.00")));
        o.add(new OrderItem("B", 1, new BigDecimal("5.50")));
        out.add(OrderMapper.toResponse(o).toString());
        var req = new CreateOrderRequest(1, List.of(new CreateOrderRequest.ItemRequest("A", 2)));
        out.add("entrada: " + req);
        boolean temIdOuStatus = java.util.Arrays.stream(CreateOrderRequest.class.getRecordComponents())
            .anyMatch(c -> c.getName().equals("id") || c.getName().equals("status"));
        out.add("request tem id/status? " + temIdOuStatus);
        return out;
    }

    public static void main(String[] args) { run().forEach(System.out::println); }
}
