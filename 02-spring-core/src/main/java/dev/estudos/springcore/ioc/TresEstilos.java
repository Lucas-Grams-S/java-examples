package dev.estudos.springcore.ioc;

import java.util.ArrayList;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

/**
 * SC02-01 · Os três estilos de injeção: construtor (recomendado), setter (opcional) e campo (evite).
 * Documento: 02-spring-core/02-ioc-e-injecao-de-dependencia.md §2.1.
 *
 * <p>Saída real:
 * <pre>
 * construtor -&gt; repo injetado=true (campo final)
 * setter opcional (nenhum Notifier no contexto) -&gt; null
 * campo -&gt; audit injetado=true
 * setter opcional (com Notifier no contexto) -&gt; email
 * </pre>
 */
public class TresEstilos {
    interface Notifier { String name(); }
    @Component("email") static class EmailNotifier implements Notifier { public String name() { return "email"; } }
    @Component static class OrderRepository {}
    @Component static class AuditLog {}

    @Service
    static class OrderService {
        // 1) Construtor: único construtor dispensa @Autowired
        private final OrderRepository repo;
        OrderService(OrderRepository repo) { this.repo = repo; }

        // 2) Setter: dependência opcional
        private Notifier notifier;
        @Autowired(required = false)
        void setNotifier(Notifier notifier) { this.notifier = notifier; }

        // 3) Campo (evite): reflection grava direto no campo
        @Autowired private AuditLog audit;
    }

    public static List<String> run() {
        List<String> out = new ArrayList<>();
        try (var ctx = new AnnotationConfigApplicationContext(OrderService.class, OrderRepository.class, AuditLog.class)) {
            var s = ctx.getBean(OrderService.class);
            out.add("construtor -> repo injetado=" + (s.repo != null) + " (campo final)");
            out.add("setter opcional (nenhum Notifier no contexto) -> " + (s.notifier == null ? null : s.notifier.name()));
            out.add("campo -> audit injetado=" + (s.audit != null));
        }
        try (var ctx = new AnnotationConfigApplicationContext(OrderService.class, OrderRepository.class, AuditLog.class, EmailNotifier.class)) {
            out.add("setter opcional (com Notifier no contexto) -> " + ctx.getBean(OrderService.class).notifier.name());
        }
        return out;
    }

    public static void main(String[] args) { run().forEach(System.out::println); }
}
