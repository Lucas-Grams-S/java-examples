package dev.estudos.springcore.ioc;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.jspecify.annotations.Nullable;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.stereotype.Component;

/**
 * SC02-05 e SC02-10 · Dependências opcionais: {@code Optional}, {@code ObjectProvider} e {@code @Nullable}
 * (JSpecify no Spring 7). Sem o bean, o contexto sobe normalmente.
 * Documento: 02-spring-core/02-ioc-e-injecao-de-dependencia.md §2.5 e §5.2.
 *
 * <p>Saída real:
 * <pre>
 * sem bean: Optional.isPresent -&gt; false
 * sem bean: provider.getIfAvailable -&gt; null
 * sem bean: @Nullable -&gt; null
 * sem bean: provider.stream().count -&gt; 0
 * com bean: Optional.isPresent -&gt; true
 * com bean: provider.getIfAvailable -&gt; auditado
 * com bean: @Nullable -&gt; auditado
 * </pre>
 */
public class Opcionais {
    interface AuditLog { String name(); }
    @Component static class RealAudit implements AuditLog { public String name() { return "auditado"; } }

    @Component
    static class OrderService {
        final Optional<AuditLog> opt;
        final ObjectProvider<AuditLog> provider;
        final @Nullable AuditLog nullable;
        OrderService(Optional<AuditLog> opt, ObjectProvider<AuditLog> provider, @Nullable AuditLog nullable) {
            this.opt = opt; this.provider = provider; this.nullable = nullable;
        }
    }

    private static void descrever(String prefixo, OrderService s, List<String> out) {
        out.add(prefixo + "Optional.isPresent -> " + s.opt.isPresent());
        AuditLog a = s.provider.getIfAvailable();
        out.add(prefixo + "provider.getIfAvailable -> " + (a == null ? null : a.name()));
        out.add(prefixo + "@Nullable -> " + (s.nullable == null ? null : s.nullable.name()));
        if (a == null) out.add(prefixo + "provider.stream().count -> " + s.provider.stream().count());
    }

    public static List<String> run() {
        List<String> out = new ArrayList<>();
        try (var ctx = new AnnotationConfigApplicationContext(OrderService.class)) {
            descrever("sem bean: ", ctx.getBean(OrderService.class), out);
        }
        try (var ctx = new AnnotationConfigApplicationContext(OrderService.class, RealAudit.class)) {
            descrever("com bean: ", ctx.getBean(OrderService.class), out);
        }
        return out;
    }

    public static void main(String[] args) { run().forEach(System.out::println); }
}
