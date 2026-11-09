package dev.estudos.springcore.minidi;

import java.util.ArrayList;
import java.util.List;
import org.aopalliance.intercept.MethodInterceptor;
import org.springframework.aop.framework.ProxyFactory;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;

/**
 * SC06-12 · O mesmo mini-container, com o Spring de verdade: um BPP que embrulha em {@code ProxyFactory}
 * (JDK se há interface, CGLIB se não há) e um listener de {@code ContextRefreshedEvent}.
 * Documento: 02-spring-core/06-como-java-e-spring-conversam.md §5.2.
 *
 * <p>Saída real (Spring 7.0.10, Java 25; o número do {@code $Proxy} varia e é normalizado):
 * <pre>
 * antes do refresh: 6 definições
 * [ctor] OrderService recebeu repo=Trace$OrderRepository$$SpringCGLIB$$0 gw=proxy JDK
 * [event] ContextRefreshedEvent
 * depois: 11
 *   trace.Cfg -&gt; dev.estudos.springcore.minidi.Trace$Cfg
 *   trace.CardGateway -&gt; jdk.proxyN.$ProxyN
 *   trace.Listener -&gt; dev.estudos.springcore.minidi.Trace$Listener
 *   trace.OrderRepository -&gt; ...Trace$OrderRepository$$SpringCGLIB$$0
 *   trace.OrderService -&gt; ...Trace$OrderService
 *   trace.ProxyingBpp -&gt; ...Trace$ProxyingBpp
 *   [advice] save
 *   [advice] charge
 * saved o-1/ok 10
 * </pre>
 */
public class Trace {
    static final List<String> out = new ArrayList<>();

    public interface PaymentGateway { String charge(long cents); }

    @Component static class CardGateway implements PaymentGateway {
        public String charge(long c) { return "ok " + c; }
    }
    @Repository static class OrderRepository {
        public String save(String id) { return "saved " + id; }
    }
    @Service static class OrderService {
        private final OrderRepository repo; private final PaymentGateway gw;
        OrderService(OrderRepository repo, PaymentGateway gw) {
            this.repo = repo; this.gw = gw;
            out.add("[ctor] OrderService recebeu repo=" + repo.getClass().getSimpleName()
                    + " gw=" + (gw.getClass().getSimpleName().startsWith("$Proxy") ? "proxy JDK" : gw.getClass().getSimpleName()));
        }
        public String place(String id) { return repo.save(id) + "/" + gw.charge(10); }
    }

    /** BPP: ProxyFactory escolhe JDK (há interface) ou CGLIB (não há). */
    @Component static class ProxyingBpp implements BeanPostProcessor {
        public Object postProcessAfterInitialization(Object bean, String name) {
            if (!(bean instanceof PaymentGateway || bean instanceof OrderRepository)) return bean;
            var pf = new ProxyFactory(bean);
            pf.addAdvice((MethodInterceptor) inv -> {
                out.add("  [advice] " + inv.getMethod().getName());
                return inv.proceed();
            });
            return pf.getProxy();
        }
    }

    @Component static class Listener {
        @EventListener void on(ContextRefreshedEvent e) { out.add("[event] ContextRefreshedEvent"); }
    }

    @Configuration @ComponentScan(basePackageClasses = Trace.class,
        useDefaultFilters = true) static class Cfg {}

    public static List<String> run() {
        out.clear();
        var ctx = new AnnotationConfigApplicationContext();
        ctx.register(Cfg.class);
        out.add("antes do refresh: " + ctx.getBeanDefinitionCount() + " definições");
        ctx.refresh();
        out.add("depois: " + ctx.getBeanDefinitionCount());
        for (String n : ctx.getBeanDefinitionNames())
            if (n.startsWith("dev.estudos") || n.startsWith("trace"))
                out.add("  " + n + " -> " + ctx.getBean(n).getClass().getName().replaceAll("\\$Proxy\\d+", "\\$ProxyN"));
        out.add(ctx.getBean(OrderService.class).place("o-1"));
        ctx.close();
        return new ArrayList<>(out);
    }

    public static void main(String[] a) { run().forEach(System.out::println); }
}
