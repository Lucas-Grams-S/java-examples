package dev.estudos.padroes.intro;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * DP01-07 · SOLID, um princípio por vez (SRP, OCP, LSP, ISP, DIP).
 * Documento: java-docs/06-padroes-de-projeto/01-introducao.md §5.1.
 *
 * <p>Saída real (JDK 25):
 * <pre>
 * SRP: total=15.50, salvos=1, nota=&lt;h1&gt;pedido total=15.50&lt;/h1&gt;
 * OCP: sem desconto=100, 10%=90.00, BlackFriday=70.00
 * LSP: Square no lugar de Rectangle, área esperada 10, obtida 4
 * ISP: relatório de pedido-1
 * DIP: CheckoutService só conhece DiscountPolicy -&gt; 50.00
 * </pre>
 *
 * <p>Variações: crie um {@code BlackFridayDiscount} e passe ao {@code CheckoutService}; nada mais muda (OCP).
 */
public class SolidPrincipios {

    // ---- S: três atores, três classes ----
    static class PricingService {
        BigDecimal calculateTotal(List<BigDecimal> items) { return items.stream().reduce(BigDecimal.ZERO, BigDecimal::add); }
    }
    static class OrderRepository {
        final List<String> saved = new ArrayList<>();
        void save(String order) { saved.add(order); }
    }
    static class InvoiceRenderer {
        String toInvoiceHtml(String order) { return "<h1>" + order + "</h1>"; }
    }

    // ---- O / D: política injetada, abstração pertence ao consumidor ----
    interface DiscountPolicy { BigDecimal apply(BigDecimal total); }
    record NoDiscount() implements DiscountPolicy { public BigDecimal apply(BigDecimal t) { return t; } }
    record PercentDiscount(int pct) implements DiscountPolicy {
        public BigDecimal apply(BigDecimal t) { return t.multiply(BigDecimal.valueOf(100 - pct)).movePointLeft(2); }
    }
    record BlackFridayDiscount() implements DiscountPolicy {      // extensão: nenhuma classe existente muda
        public BigDecimal apply(BigDecimal t) { return t.multiply(new BigDecimal("0.70")); }
    }
    static class CheckoutService {
        private final DiscountPolicy policy;
        CheckoutService(DiscountPolicy policy) { this.policy = policy; }
        BigDecimal total(BigDecimal base) { return policy.apply(base); }
    }

    // ---- L: a violação clássica ----
    static class Rectangle {
        protected int w, h;
        void setW(int w) { this.w = w; }
        void setH(int h) { this.h = h; }
        int area() { return w * h; }
    }
    static class Square extends Rectangle {
        @Override void setW(int w) { this.w = w; this.h = w; }
        @Override void setH(int h) { this.w = h; this.h = h; }
    }

    // ---- I: interfaces pequenas ----
    interface OrderReader { String find(long id); }
    interface OrderWriter { void save(long id, String order); }
    static class InMemoryOrders implements OrderReader, OrderWriter {
        private final Map<Long, String> data = new HashMap<>();
        public String find(long id) { return data.get(id); }
        public void save(long id, String order) { data.put(id, order); }
    }
    static class ReportJob {
        private final OrderReader reader;                 // só lê: não consegue gravar
        ReportJob(OrderReader reader) { this.reader = reader; }
        String report(long id) { return "relatório de " + reader.find(id); }
    }

    public static List<String> run() {
        List<String> out = new ArrayList<>();
        var pricing = new PricingService();
        var repo = new OrderRepository();
        var invoice = new InvoiceRenderer();
        var total = pricing.calculateTotal(List.of(new BigDecimal("10.00"), new BigDecimal("5.50")));
        repo.save("pedido total=" + total);
        out.add("SRP: total=" + total + ", salvos=" + repo.saved.size() + ", nota=" + invoice.toInvoiceHtml(repo.saved.get(0)));

        var cem = new BigDecimal("100");
        out.add("OCP: sem desconto=" + new CheckoutService(new NoDiscount()).total(cem)
                + ", 10%=" + new CheckoutService(new PercentDiscount(10)).total(cem)
                + ", BlackFriday=" + new CheckoutService(new BlackFridayDiscount()).total(cem).setScale(2));

        Rectangle r = new Square();
        r.setW(5); r.setH(2);
        out.add("LSP: Square no lugar de Rectangle, área esperada 10, obtida " + r.area());

        var orders = new InMemoryOrders();
        orders.save(1, "pedido-1");
        out.add("ISP: " + new ReportJob(orders).report(1));

        out.add("DIP: CheckoutService só conhece DiscountPolicy -> " + new CheckoutService(new PercentDiscount(50)).total(cem));
        return out;
    }

    public static void main(String[] args) { run().forEach(System.out::println); }
}
