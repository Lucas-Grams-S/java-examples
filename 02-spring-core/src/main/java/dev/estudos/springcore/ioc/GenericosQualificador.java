package dev.estudos.springcore.ioc;

import java.util.ArrayList;
import java.util.List;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

/**
 * SC02-07 · Genéricos como qualificador: {@code Repository<Order,Long>} e {@code Repository<Customer,Long>}
 * são tipos distintos para o Spring, mesmo com <em>type erasure</em> (a assinatura genérica fica no
 * atributo {@code Signature} da classe).
 * Documento: 02-spring-core/02-ioc-e-injecao-de-dependencia.md §3.2.
 *
 * <p>Saída real:
 * <pre>
 * Repository&lt;Order,Long&gt; injetado -&gt; OrderRepo
 * Repository&lt;Customer,Long&gt; injetado -&gt; CustomerRepo
 * </pre>
 */
public class GenericosQualificador {
    record Order(long id) {}
    record Customer(long id) {}
    interface Repository<T, ID> { String kind(); }
    @Component static class OrderRepo implements Repository<Order, Long> { public String kind() { return "OrderRepo"; } }
    @Component static class CustomerRepo implements Repository<Customer, Long> { public String kind() { return "CustomerRepo"; } }

    @Service
    static class OrderService {
        final Repository<Order, Long> orders;
        final Repository<Customer, Long> customers;
        OrderService(Repository<Order, Long> orders, Repository<Customer, Long> customers) {
            this.orders = orders; this.customers = customers;
        }
    }

    public static List<String> run() {
        List<String> out = new ArrayList<>();
        try (var ctx = new AnnotationConfigApplicationContext(OrderRepo.class, CustomerRepo.class, OrderService.class)) {
            var s = ctx.getBean(OrderService.class);
            out.add("Repository<Order,Long> injetado -> " + s.orders.kind());
            out.add("Repository<Customer,Long> injetado -> " + s.customers.kind());
        }
        return out;
    }

    public static void main(String[] args) { run().forEach(System.out::println); }
}
