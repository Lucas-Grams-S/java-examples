package dev.estudos.padroes.enterprise;

import dev.estudos.padroes.enterprise.DominioPedidos.*;
import dev.estudos.padroes.enterprise.DtoEMapper.*;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;

/**
 * DP05-12 · Service Layer fina: coordena, não decide.
 * Documento: java-docs/06-padroes-de-projeto/05-padroes-enterprise.md §5.3.
 *
 * <p>Saída real (JDK 25):
 * <pre>
 * criado: OrderResponse[id=1, customer=Ana, status=NEW, total=20.00, items=[OrderItemResponse[sku=A, qty=2, subtotal=20.00]]]
 * pago: PAID
 * erro: cliente 9
 * erro: pedido vazio
 * </pre>
 */
public class ServiceLayerFina {
    static class OrderService {
        private final OrderRepository repo;
        private final Map<Long, Customer> customers;   // no real: CustomerRepository
        OrderService(OrderRepository r, Map<Long, Customer> c) { repo = r; customers = c; }

        OrderResponse create(CreateOrderRequest req) {
            Customer c = Optional.ofNullable(customers.get(req.customerId()))
                .orElseThrow(() -> new NoSuchElementException("cliente " + req.customerId()));
            Order o = new Order(repo.findAll().size() + 1L, c);
            req.items().forEach(i -> o.add(new OrderItem(i.sku(), i.qty(), new BigDecimal("10.00"))));
            repo.save(o);
            return OrderMapper.toResponse(o);
        }

        OrderResponse pay(long id) {
            Order o = repo.findById(id).orElseThrow();
            o.pay();                       // regra no domínio
            repo.save(o);
            return OrderMapper.toResponse(o);
        }
    }

    public static List<String> run() {
        List<String> out = new ArrayList<>();
        var svc = new OrderService(new InMemoryOrderRepository(), Map.of(1L, new Customer(1, "Ana", false)));
        out.add("criado: " + svc.create(new CreateOrderRequest(1, List.of(new CreateOrderRequest.ItemRequest("A", 2)))));
        out.add("pago: " + svc.pay(1).status());
        try { svc.create(new CreateOrderRequest(9, List.of())); }
        catch (NoSuchElementException e) { out.add("erro: " + e.getMessage()); }
        svc.create(new CreateOrderRequest(1, List.of()));
        try { svc.pay(2); }
        catch (IllegalStateException e) { out.add("erro: " + e.getMessage()); }
        return out;
    }

    public static void main(String[] args) { run().forEach(System.out::println); }
}
