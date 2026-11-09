package dev.estudos.padroes.arquiteturais;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * DP06-13 · Hexagonal em Java puro: domínio, portas, caso de uso e adaptadores.
 * Documento: java-docs/06-padroes-de-projeto/06-padroes-arquiteturais.md §5.2.
 *
 * <p>Saída real (JDK 25):
 * <pre>
 * cobrando 50
 * pago? true total=50
 * Money negativo -&gt; negativo
 * cobrando 0
 * pedido vazio -&gt; pedido vazio
 * </pre>
 */
public class HexagonalJavaPuro {
    // ----- domínio (sem frameworks)
    record OrderId(long value) {}
    record Money(BigDecimal amount) {
        Money { if (amount.signum() < 0) throw new IllegalArgumentException("negativo"); }
        Money plus(Money o) { return new Money(amount.add(o.amount)); }
    }
    static final class Order {
        private final OrderId id; private Money total = new Money(BigDecimal.ZERO); private boolean paid;
        Order(OrderId id) { this.id = id; }
        OrderId id() { return id; }
        void addItem(Money price) { if (paid) throw new IllegalStateException("já pago"); total = total.plus(price); }
        void pay() { if (total.amount().signum() == 0) throw new IllegalStateException("pedido vazio"); paid = true; }
        boolean paid() { return paid; }
        Money total() { return total; }
    }

    // ----- aplicação: portas
    interface PlaceOrderUseCase { OrderId place(List<BigDecimal> prices); }        // entrada
    interface OrderStore { void save(Order o); Optional<Order> find(OrderId id); } // saída
    interface PaymentGateway { boolean charge(OrderId id, Money m); }              // saída

    // ----- aplicação: caso de uso, que só conhece as portas
    static class PlaceOrderService implements PlaceOrderUseCase {
        private final OrderStore store; private final PaymentGateway pay; private long seq = 0;
        PlaceOrderService(OrderStore s, PaymentGateway p) { store = s; pay = p; }
        public OrderId place(List<BigDecimal> prices) {
            var o = new Order(new OrderId(++seq));
            prices.forEach(p -> o.addItem(new Money(p)));
            if (pay.charge(o.id(), o.total())) o.pay();
            store.save(o);
            return o.id();
        }
    }

    // ----- adaptadores (nas bordas)
    static class MemoryOrderStore implements OrderStore {
        private final Map<OrderId, Order> map = new HashMap<>();
        public void save(Order o) { map.put(o.id(), o); }
        public Optional<Order> find(OrderId id) { return Optional.ofNullable(map.get(id)); }
    }
    static class FakeGateway implements PaymentGateway {
        final List<String> log;
        FakeGateway(List<String> log) { this.log = log; }
        public boolean charge(OrderId id, Money m) { log.add("cobrando " + m.amount().stripTrailingZeros().toPlainString()); return true; }
    }

    public static List<String> run() {
        List<String> out = new ArrayList<>();
        var store = new MemoryOrderStore();
        PlaceOrderUseCase uc = new PlaceOrderService(store, new FakeGateway(out));
        var id = uc.place(List.of(new BigDecimal("20"), new BigDecimal("30")));
        var o = store.find(id).orElseThrow();
        out.add("pago? " + o.paid() + " total=" + o.total().amount());
        try { new Money(new BigDecimal("-1")); }
        catch (IllegalArgumentException e) { out.add("Money negativo -> " + e.getMessage()); }
        try { uc.place(List.of()); out.add("pedido vazio foi pago?! (não deveria)"); }
        catch (IllegalStateException e) { out.add("pedido vazio -> " + e.getMessage()); }
        return out;
    }

    public static void main(String[] args) { run().forEach(System.out::println); }
}
