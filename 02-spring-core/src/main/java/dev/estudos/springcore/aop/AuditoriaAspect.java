package dev.estudos.springcore.aop;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

/**
 * SC07-12 · Aspecto realista: auditoria de métodos de serviço ({@code place*}) com {@code @AfterReturning},
 * usando {@code execution(...)} e {@code @Order}. O log vai para uma lista (sem dependência de SLF4J).
 * Documento: 02-spring-core/07-aop-e-proxies.md §5.4.
 *
 * <p>Saída real:
 * <pre>
 * AUDIT OrderService.place(..) executado com [o-1, 1500] =&gt; placed o-1
 * (cancel não casa com place*: sem auditoria)
 * </pre>
 */
public class AuditoriaAspect {
    static final List<String> log = new ArrayList<>();

    @Aspect @Component @Order(Ordered.HIGHEST_PRECEDENCE + 10)
    static class Audit {
        @AfterReturning(pointcut = "execution(* dev.estudos.springcore.aop.AuditoriaAspect.*Service.place*(..))",
                        returning = "result")
        void audit(JoinPoint jp, Object result) {
            log.add("AUDIT " + jp.getSignature().toShortString() + " executado com "
                + Arrays.toString(jp.getArgs()) + " => " + result);
        }
    }

    @Service static class OrderService {
        public String place(String id, long cents) { return "placed " + id; }
        public String cancel(String id) { return "cancelled " + id; }
    }

    @Configuration @EnableAspectJAutoProxy static class Cfg {}

    public static List<String> run() {
        log.clear();
        try (var ctx = new AnnotationConfigApplicationContext(Cfg.class, Audit.class, OrderService.class)) {
            var svc = ctx.getBean(OrderService.class);
            svc.place("o-1", 1500);
            svc.cancel("o-1");
        }
        var out = new ArrayList<>(log);
        out.add("(cancel não casa com place*: sem auditoria)");
        return out;
    }

    public static void main(String[] args) { run().forEach(System.out::println); }
}
