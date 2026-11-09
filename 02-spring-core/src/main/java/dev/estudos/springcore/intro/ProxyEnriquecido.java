package dev.estudos.springcore.intro;

import dev.estudos.springcore.intro.pedidos.*;
import java.util.ArrayList;
import java.util.List;
import org.aopalliance.intercept.MethodInterceptor;
import org.springframework.aop.framework.ProxyFactory;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

/**
 * SC01-06 · O serviço enriquecido por proxy: o que {@code @Transactional} faz, em miniatura. Um
 * {@link BeanPostProcessor} devolve um proxy CGLIB no lugar do {@code OrderService}, que "abre e fecha
 * transação" ao redor de {@code place}. O código da classe de negócio não muda.
 * Documento: 02-spring-core/01-o-que-e-spring.md §5.3.
 *
 * <p>Saída real:
 * <pre>
 * eventos -&gt; [begin, commit]
 * é proxy do Spring AOP -&gt; true
 * classe real -&gt; OrderService
 * classe em runtime contém $$SpringCGLIB$$ -&gt; true
 * </pre>
 */
public class ProxyEnriquecido {
    static final List<String> EVENTS = new ArrayList<>();

    /** Equivalente didático do auto-proxy creator. */
    static class TxBpp implements BeanPostProcessor {
        @Override public Object postProcessAfterInitialization(Object bean, String name) {
            if (!(bean instanceof OrderService)) return bean;
            var pf = new ProxyFactory(bean);
            pf.setProxyTargetClass(true);
            pf.addAdvice((MethodInterceptor) inv -> {
                if (!inv.getMethod().getName().equals("place")) return inv.proceed();
                EVENTS.add("begin");
                try { Object r = inv.proceed(); EVENTS.add("commit"); return r; }
                catch (Throwable t) { EVENTS.add("rollback"); throw t; }
            });
            return pf.getProxy();
        }
    }

    public static List<String> run() {
        EVENTS.clear();
        List<String> out = new ArrayList<>();
        try (var ctx = new AnnotationConfigApplicationContext()) {
            ctx.register(AppConfig.class);
            ctx.registerBean("txBpp", TxBpp.class);
            ctx.refresh();
            var service = ctx.getBean(OrderService.class);
            service.place(new Order("p-1", 1500));
            out.add("eventos -> " + EVENTS);
            out.add("é proxy do Spring AOP -> " + AopUtils.isAopProxy(service));
            out.add("classe real -> " + AopUtils.getTargetClass(service).getSimpleName());
            out.add("classe em runtime contém $$SpringCGLIB$$ -> " + service.getClass().getName().contains("$$SpringCGLIB$$"));
        }
        return out;
    }

    public static void main(String[] args) { run().forEach(System.out::println); }
}
