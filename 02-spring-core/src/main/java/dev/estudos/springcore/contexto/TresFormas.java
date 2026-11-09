package dev.estudos.springcore.contexto;

import java.time.Clock;
import java.util.ArrayList;
import java.util.List;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.core.annotation.MergedAnnotations;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Controller;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;

/**
 * SC03-03 · Três formas de declarar um bean: estereótipo + scanning (código seu), {@code @Bean} (código de
 * terceiros ou montagem) e registro programático. Também confirma que todo estereótipo é {@code @Component}
 * por meta-anotação.
 * Documento: 02-spring-core/03-application-context-e-beans.md §2.3.
 *
 * <p>Saída real:
 * <pre>
 * estereótipo -&gt; orderService (classe OrderService)
 * &#64;Bean -&gt; clock (factoryMethod=clock, classe SystemClock)
 * programático -&gt; relogio2 (classe SystemClock)
 * &#64;Service, &#64;Repository, &#64;Controller, &#64;Configuration são &#64;Component -&gt; true
 * </pre>
 */
public class TresFormas {
    @Service static class OrderService {}

    @Configuration(proxyBeanMethods = false)
    static class InfraConfig {
        @Bean Clock clock() { return Clock.systemUTC(); }
    }

    public static List<String> run() {
        List<String> out = new ArrayList<>();
        try (var ctx = new AnnotationConfigApplicationContext(OrderService.class, InfraConfig.class)) {
            out.add("estereótipo -> orderService (classe " + ctx.getBean(OrderService.class).getClass().getSimpleName() + ")");
            BeanDefinition bd = ctx.getBeanDefinition("clock");
            out.add("@Bean -> clock (factoryMethod=" + bd.getFactoryMethodName() + ", classe " + ctx.getBean(Clock.class).getClass().getSimpleName() + ")");
        }
        try (var ctx = new GenericApplicationContext()) {
            ctx.registerBean("relogio2", Clock.class, Clock::systemUTC);
            ctx.refresh();
            out.add("programático -> relogio2 (classe " + ctx.getBean("relogio2").getClass().getSimpleName() + ")");
        }
        boolean todos = List.of(Service.class, Repository.class, Controller.class, Configuration.class).stream()
            .allMatch(a -> MergedAnnotations.from(a).isPresent(Component.class));
        out.add("@Service, @Repository, @Controller, @Configuration são @Component -> " + todos);
        return out;
    }

    public static void main(String[] args) { run().forEach(System.out::println); }
}
