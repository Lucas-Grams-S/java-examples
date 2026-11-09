package dev.estudos.springcore.aop;

import java.util.ArrayList;
import java.util.List;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.context.annotation.Lazy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

/**
 * SC07-11 · Corrigir self-invocation. Compara: (0) chamada interna sem proxy, (1) mover para outro bean
 * (preferível) e (2) auto-injeção {@code @Lazy}. {@code AopContext.currentProxy()} (3) já está em SC07-09;
 * AspectJ com weaving (4) é conceitual.
 * Documento: 02-spring-core/07-aop-e-proxies.md §5.3.
 *
 * <p>Saída real:
 * <pre>
 * 0 interna:     [intercepta place]
 * 1 outro bean:  [intercepta place, intercepta notifyCustomer]
 * 2 auto-@Lazy:  [intercepta a, intercepta b]
 * </pre>
 */
public class CorrigirSelfInvocation {
    static final List<String> calls = new ArrayList<>();

    @Aspect @Component
    static class Spy {
        @Around("execution(* dev.estudos.springcore.aop.CorrigirSelfInvocation.*Service*.*(..))")
        Object around(ProceedingJoinPoint pjp) throws Throwable {
            calls.add("intercepta " + pjp.getSignature().getName());
            return pjp.proceed();
        }
    }

    // 0) problema
    // 1) outro bean
    @Service static class NotificationService { public void notifyCustomer() {} }
    @Service static class SeparadoService {
        private final NotificationService notifications;
        SeparadoService(NotificationService n) { this.notifications = n; }
        public void place() { notifications.notifyCustomer(); }
    }
    // 2) auto-injeção
    @Service static class SelfService {
        @Lazy @Autowired private SelfService self;
        public void a() { self.b(); }
        public void b() {}
    }

    @Configuration @EnableAspectJAutoProxy static class Cfg {}

    public static List<String> run() {
        List<String> out = new ArrayList<>();
        // o pointcut casa com classes cujo nome contém "Service"
        try (var ctx = new AnnotationConfigApplicationContext(Cfg.class, Spy.class, InternoService.class,
                NotificationService.class, SeparadoService.class, SelfService.class)) {
            calls.clear(); ctx.getBean(InternoService.class).place();
            out.add("0 interna:     " + calls);
            calls.clear(); ctx.getBean(SeparadoService.class).place();
            out.add("1 outro bean:  " + calls);
            calls.clear(); ctx.getBean(SelfService.class).a();
            out.add("2 auto-@Lazy:  " + calls);
        }
        return out;
    }

    @Service static class InternoService {
        public void place() { notifyCustomer(); }
        public void notifyCustomer() {}
    }

    public static void main(String[] args) { run().forEach(System.out::println); }
}
