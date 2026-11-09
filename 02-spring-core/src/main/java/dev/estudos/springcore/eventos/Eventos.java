package dev.estudos.springcore.eventos;

import java.util.ArrayList;
import java.util.List;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.event.EventListener;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * SC08-08 · Eventos: ouvintes síncronos na thread do publicador, ordem com {@code @Order}, retorno que vira
 * outro evento e {@code condition} em SpEL (requer {@code -parameters} para {@code #e}).
 * Documento: 02-spring-core/08-eventos-validacao-e-conversao.md §5.1.
 *
 * <p>Saída real:
 * <pre>
 * publicando
 * [mail] first
 * [chained] xo-1
 * [billing] OrderPlaced[orderId=o-1, cents=1500] thread=main
 * [big] o-1
 * publicou
 * </pre>
 * (Rodando via {@code exec:java} a thread se chama {@code ...Eventos.main()}; no shell, {@code main}.)
 */
public class Eventos {
    static final List<String> out = new ArrayList<>();

    public record OrderPlaced(String orderId, long cents) {}

    @Component static class Billing {
        @EventListener void on(OrderPlaced e) { out.add("[billing] " + e + " thread=" + Thread.currentThread().getName()); }
    }

    @Component static class Mail {
        @EventListener @Order(1) String on(OrderPlaced e) {      // retorno vira OUTRO evento
            out.add("[mail] first");
            return "x" + e.orderId();
        }
        @EventListener void chained(String s) { out.add("[chained] " + s); }
        @EventListener(condition = "#e.cents > 1000")
        void big(OrderPlaced e) { out.add("[big] " + e.orderId()); }
    }

    @Component static class OrderService {
        private final ApplicationEventPublisher publisher;
        OrderService(ApplicationEventPublisher publisher) { this.publisher = publisher; }
        void place(String id, long cents) {
            out.add("publicando");
            publisher.publishEvent(new OrderPlaced(id, cents));
            out.add("publicou");
        }
    }

    public static List<String> run() {
        out.clear();
        try (var ctx = new AnnotationConfigApplicationContext(Billing.class, Mail.class, OrderService.class)) {
            ctx.getBean(OrderService.class).place("o-1", 1500);
        }
        return new ArrayList<>(out);
    }

    public static void main(String[] args) { run().forEach(System.out::println); }
}
