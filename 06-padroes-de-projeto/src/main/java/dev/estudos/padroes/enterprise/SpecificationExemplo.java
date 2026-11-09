package dev.estudos.padroes.enterprise;

import dev.estudos.padroes.enterprise.DominioPedidos.*;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

/**
 * DP05-13 · Specification: critério de negócio com nome, combinável com and/or/not.
 * Documento: java-docs/06-padroes-de-projeto/05-padroes-enterprise.md §5.4.
 *
 * <p>Saída real (JDK 25):
 * <pre>
 * (pago e &gt;20) ou (vip e não pago) -&gt; [1, 3]
 * Predicate 'pago' -&gt; [1, 2]
 * </pre>
 */
public class SpecificationExemplo {
    @FunctionalInterface
    interface Spec<T> {
        boolean isSatisfiedBy(T t);
        default Spec<T> and(Spec<T> o) { return t -> isSatisfiedBy(t) && o.isSatisfiedBy(t); }
        default Spec<T> or(Spec<T> o)  { return t -> isSatisfiedBy(t) || o.isSatisfiedBy(t); }
        default Spec<T> not()          { return t -> !isSatisfiedBy(t); }
    }

    static Spec<Order> paid()               { return o -> o.status().equals("PAID"); }
    static Spec<Order> totalAbove(String v) { return o -> o.total().compareTo(new BigDecimal(v)) > 0; }
    static Spec<Order> vipCustomer()        { return o -> o.customer().vip(); }

    private static Order order(long id, Customer c, String price, boolean pay) {
        var o = new Order(id, c);
        o.add(new OrderItem("A", 1, new BigDecimal(price)));
        if (pay) o.pay();
        return o;
    }

    public static List<String> run() {
        var ana = new Customer(1, "Ana", true);
        var bob = new Customer(2, "Bob", false);
        var orders = List.of(order(1, ana, "50", true), order(2, bob, "10", true), order(3, ana, "20", false));
        // (pago E acima de 20) OU (VIP E NÃO pago)
        var spec = paid().and(totalAbove("20")).or(vipCustomer().and(paid().not()));
        List<Long> ids = orders.stream().filter(spec::isSatisfiedBy).map(Order::id).toList();
        // a mesma coisa com java.util.function.Predicate
        Predicate<Order> p = o -> o.status().equals("PAID");
        List<Long> soPagos = orders.stream().filter(p).map(Order::id).toList();
        List<String> out = new ArrayList<>();
        out.add("(pago e >20) ou (vip e não pago) -> " + ids);
        out.add("Predicate 'pago' -> " + soPagos);
        return out;
    }

    public static void main(String[] args) { run().forEach(System.out::println); }
}
