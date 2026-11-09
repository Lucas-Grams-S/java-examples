package dev.estudos.java.oo;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * J04-12 · Domínio de pedidos com polimorfismo via interface.
 * Documento: java-docs/01-java/04-orientacao-a-objetos.md §5.1.
 *
 * <p>Saída real (JDK 25):
 * <pre>
 * sem desconto (lambda) -&gt; 120.00
 * 10% -&gt; 108.00
 * pagar duas vezes -&gt; só pedidos CREATED podem ser pagos
 * </pre>
 *
 * <p>Variações: crie um {@code Discount} novo (ex.: valor fixo) sem tocar em {@code Order}.
 */
public class DiscountDomain {
    public enum OrderStatus { CREATED, PAID }

    public interface Discount { BigDecimal apply(BigDecimal total); }

    public static class PercentageDiscount implements Discount {
        private final BigDecimal percent;
        public PercentageDiscount(BigDecimal percent) { this.percent = percent; }
        @Override public BigDecimal apply(BigDecimal total) {
            return total.subtract(total.multiply(percent).divide(BigDecimal.valueOf(100)));
        }
    }

    record OrderItemLine(String sku, int quantity, BigDecimal unitPrice) {
        BigDecimal subtotal() { return unitPrice.multiply(BigDecimal.valueOf(quantity)); }
    }

    public static class Order {
        private final List<OrderItemLine> lines;
        private OrderStatus status = OrderStatus.CREATED;
        public Order(List<OrderItemLine> lines) { this.lines = List.copyOf(lines); }

        public BigDecimal total(Discount discount) {
            BigDecimal sum = lines.stream().map(OrderItemLine::subtotal).reduce(BigDecimal.ZERO, BigDecimal::add);
            return discount.apply(sum);
        }

        public void pay() {
            if (status != OrderStatus.CREATED) throw new IllegalStateException("só pedidos CREATED podem ser pagos");
            status = OrderStatus.PAID;
        }
    }

    public static List<String> run() {
        List<String> out = new ArrayList<>();
        var order = new Order(List.of(new OrderItemLine("A", 2, new BigDecimal("50.00")),
                new OrderItemLine("B", 1, new BigDecimal("20.00"))));
        out.add("sem desconto (lambda) -> " + order.total(t -> t));
        out.add("10% -> " + order.total(new PercentageDiscount(BigDecimal.TEN)));
        order.pay();
        try {
            order.pay();
        } catch (IllegalStateException e) {
            out.add("pagar duas vezes -> " + e.getMessage());
        }
        return out;
    }

    public static void main(String[] args) { run().forEach(System.out::println); }
}
