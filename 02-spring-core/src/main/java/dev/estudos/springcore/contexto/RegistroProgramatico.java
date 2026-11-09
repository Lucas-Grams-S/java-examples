package dev.estudos.springcore.contexto;

import java.time.Clock;
import java.util.ArrayList;
import java.util.List;
import org.springframework.context.support.GenericApplicationContext;

/**
 * SC03-10 · Registro programático em {@link GenericApplicationContext}: {@code registerBean} com
 * {@code Supplier} e com customizador da definição (aqui, {@code lazyInit}). Útil em testes e frameworks.
 * Documento: 02-spring-core/03-application-context-e-beans.md §5.2.
 *
 * <p>Saída real:
 * <pre>
 * antes do refresh, 0 singletons de aplicação criados
 * OrderService (lazy) criado após refresh? -&gt; false
 * Clock (supplier, não lazy) criado após refresh? -&gt; true
 * após getBean, OrderService criado? -&gt; true
 * </pre>
 */
public class RegistroProgramatico {
    public static class OrderService {
        static final List<String> LOG = new ArrayList<>();
        public OrderService() { LOG.add("OrderService criado"); }
    }

    public static List<String> run() {
        List<String> out = new ArrayList<>();
        OrderService.LOG.clear();
        try (var ctx = new GenericApplicationContext()) {
            ctx.registerBean("clock", Clock.class, Clock::systemUTC);                // com Supplier
            ctx.registerBean(OrderService.class, bd -> bd.setLazyInit(true));        // customiza a definição
            out.add("antes do refresh, 0 singletons de aplicação criados");
            ctx.refresh();
            out.add("OrderService (lazy) criado após refresh? -> " + !OrderService.LOG.isEmpty());
            out.add("Clock (supplier, não lazy) criado após refresh? -> " + ctx.getBeanFactory().containsSingleton("clock"));
            ctx.getBean(OrderService.class);
            out.add("após getBean, OrderService criado? -> " + !OrderService.LOG.isEmpty());
        }
        return out;
    }

    public static void main(String[] args) { run().forEach(System.out::println); }
}
