package dev.estudos.springcore.aop;

import java.lang.annotation.*;
import java.util.ArrayList;
import java.util.List;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.aop.framework.AopContext;
import org.springframework.aop.support.AopUtils;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

/**
 * SC07-09 · Aspecto próprio + self-invocation: {@code notify()} chamado de dentro de {@code place()} NÃO passa
 * pelo proxy; via {@code AopContext.currentProxy()} passa; método {@code final} fica sem aspecto.
 * Documento: 02-spring-core/07-aop-e-proxies.md §5.1.
 *
 * <p>Saída real:
 * <pre>
 * é proxy CGLIB: aop=true cglib=true
 * -- place (chama notify internamente)
 *   [aspect] antes place
 *   [aspect] depois
 * placed o-1 / notified o-1
 * -- viaProxy (AopContext)
 *   [aspect] antes viaProxy
 *   [aspect] antes notify
 *   [aspect] depois
 *   [aspect] depois
 * notified o-2
 * -- final
 * final
 * </pre>
 */
public class Aop {
    static final List<String> out = new ArrayList<>();

    @Retention(RetentionPolicy.RUNTIME) @Target(ElementType.METHOD)
    public @interface Timed {}

    @Aspect @Component
    static class TimingAspect {
        @Around("@annotation(dev.estudos.springcore.aop.Aop.Timed)")
        Object time(ProceedingJoinPoint pjp) throws Throwable {
            out.add("  [aspect] antes " + pjp.getSignature().getName());
            try { return pjp.proceed(); }
            finally { out.add("  [aspect] depois"); }
        }
    }

    @Service
    public static class OrderService {
        @Timed public String place(String id)  { return "placed " + id + " / " + notify(id); }  // self-invocation!
        @Timed public String notify(String id) { return "notified " + id; }
        @Timed public String viaProxy(String id) {
            return ((OrderService) AopContext.currentProxy()).notify(id);
        }
        @Timed public final String finalMethod() { return "final"; }
    }

    @Configuration @EnableAspectJAutoProxy(exposeProxy = true)
    static class Cfg {}

    public static List<String> run() {
        out.clear();
        try (var ctx = new AnnotationConfigApplicationContext(Cfg.class, TimingAspect.class, OrderService.class)) {
            var svc = ctx.getBean(OrderService.class);
            out.add("é proxy CGLIB: aop=" + AopUtils.isAopProxy(svc) + " cglib=" + AopUtils.isCglibProxy(svc));
            out.add("-- place (chama notify internamente)");
            out.add(svc.place("o-1"));
            out.add("-- viaProxy (AopContext)");
            out.add(svc.viaProxy("o-2"));
            out.add("-- final");
            out.add(svc.finalMethod());
        }
        return new ArrayList<>(out);
    }

    public static void main(String[] args) { run().forEach(System.out::println); }
}
