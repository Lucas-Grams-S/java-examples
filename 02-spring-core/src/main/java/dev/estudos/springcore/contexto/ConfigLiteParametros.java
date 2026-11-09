package dev.estudos.springcore.contexto;

import java.time.Clock;
import java.util.ArrayList;
import java.util.List;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * SC03-11 · {@code @Configuration(proxyBeanMethods = false)} combinada com dependências por parâmetro: o
 * {@code repo} chega ao {@code OrderService} como argumento do método {@code @Bean}, e é o mesmo singleton.
 * Documento: 02-spring-core/03-application-context-e-beans.md §5.3.
 *
 * <p>Saída real:
 * <pre>
 * service.repo == repo do contêiner -&gt; true
 * service.clock == clock do contêiner -&gt; true
 * repo.ds == ds do contêiner -&gt; true
 * </pre>
 */
public class ConfigLiteParametros {
    static class DataSource {}
    static class OrderRepository { final DataSource ds; OrderRepository(DataSource ds) { this.ds = ds; } }
    static class OrderService {
        final OrderRepository repo; final Clock clock;
        OrderService(OrderRepository repo, Clock clock) { this.repo = repo; this.clock = clock; }
    }

    @Configuration(proxyBeanMethods = false)
    static class OrderConfig {
        @Bean DataSource dataSource() { return new DataSource(); }
        @Bean Clock clock() { return Clock.systemUTC(); }
        @Bean OrderRepository orderRepository(DataSource ds) { return new OrderRepository(ds); }
        @Bean OrderService orderService(OrderRepository repo, Clock clock) { return new OrderService(repo, clock); }
    }

    public static List<String> run() {
        List<String> out = new ArrayList<>();
        try (var ctx = new AnnotationConfigApplicationContext(OrderConfig.class)) {
            var s = ctx.getBean(OrderService.class);
            out.add("service.repo == repo do contêiner -> " + (s.repo == ctx.getBean(OrderRepository.class)));
            out.add("service.clock == clock do contêiner -> " + (s.clock == ctx.getBean(Clock.class)));
            out.add("repo.ds == ds do contêiner -> " + (s.repo.ds == ctx.getBean(DataSource.class)));
        }
        return out;
    }

    public static void main(String[] args) { run().forEach(System.out::println); }
}
