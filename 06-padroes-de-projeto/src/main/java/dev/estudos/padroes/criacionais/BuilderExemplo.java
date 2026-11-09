package dev.estudos.padroes.criacionais;

import java.math.BigDecimal;
import java.net.URI;
import java.net.http.HttpRequest;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import org.springframework.web.util.UriComponentsBuilder;

/**
 * DP02-09 · Builder: construção em etapas com invariante, e builders do JDK e do Spring.
 * Documento: java-docs/06-padroes-de-projeto/02-criacionais.md §5.4.
 *
 * <p>Saída real (JDK 25):
 * <pre>
 * Order[c-1, 2 itens, total=25.50, coupon=PROMO10, note=null]
 * builder reutilizado depois do build, o 1º pedido continua com 2 itens
 * sem itens -&gt; IllegalStateException: order needs at least one item
 * JDK HttpRequest.newBuilder -&gt; GET http://localhost/orders
 * Spring UriComponentsBuilder -&gt; http://localhost:8080/orders/42?expand=items
 * </pre>
 *
 * <p>Variações: acrescente um campo opcional {@code deliveryDate} ao builder sem mudar quem já o usa.
 */
public class BuilderExemplo {

    record OrderItem(String sku, int quantity, BigDecimal unitPrice) {
        OrderItem {
            Objects.requireNonNull(sku);
            if (quantity <= 0) throw new IllegalArgumentException("quantity must be > 0");
        }
        BigDecimal total() { return unitPrice.multiply(BigDecimal.valueOf(quantity)); }
    }

    static final class Order {
        private final String customerId;
        private final List<OrderItem> items;
        private final String couponCode;
        private final String note;

        private Order(Builder b) {
            this.customerId = b.customerId;
            this.items = List.copyOf(b.items);
            this.couponCode = b.couponCode;
            this.note = b.note;
        }
        static Builder builder(String customerId) { return new Builder(customerId); }
        BigDecimal total() { return items.stream().map(OrderItem::total).reduce(BigDecimal.ZERO, BigDecimal::add); }
        @Override public String toString() {
            return "Order[" + customerId + ", " + items.size() + " itens, total=" + total() + ", coupon=" + couponCode + ", note=" + note + "]";
        }

        static final class Builder {
            private final String customerId;
            private final List<OrderItem> items = new ArrayList<>();
            private String couponCode, note;

            private Builder(String customerId) { this.customerId = Objects.requireNonNull(customerId, "customerId"); }
            Builder item(String sku, int qty, String price) { items.add(new OrderItem(sku, qty, new BigDecimal(price))); return this; }
            Builder coupon(String code) { this.couponCode = code; return this; }
            Builder note(String note) { this.note = note; return this; }
            Order build() {
                if (items.isEmpty()) throw new IllegalStateException("order needs at least one item");
                return new Order(this);
            }
        }
    }

    public static List<String> run() {
        List<String> out = new ArrayList<>();
        var b = Order.builder("c-1").item("SKU-1", 2, "10.00").item("SKU-2", 1, "5.50").coupon("PROMO10");
        var o = b.build();
        b.item("SKU-3", 1, "1.00");                         // o builder não vaza estado para o Order já criado
        out.add(o.toString());
        out.add("builder reutilizado depois do build, o 1º pedido continua com " + o.items.size() + " itens");
        try {
            Order.builder("c-1").build();
        } catch (IllegalStateException e) {
            out.add("sem itens -> IllegalStateException: " + e.getMessage());
        }
        var req = HttpRequest.newBuilder(URI.create("http://localhost/orders")).header("Accept", "application/json").GET().build();
        out.add("JDK HttpRequest.newBuilder -> " + req.method() + " " + req.uri());
        out.add("Spring UriComponentsBuilder -> " + UriComponentsBuilder.fromUriString("http://localhost:8080")
                .path("/orders/{id}").queryParam("expand", "items").build(42));
        return out;
    }

    public static void main(String[] args) { run().forEach(System.out::println); }
}
