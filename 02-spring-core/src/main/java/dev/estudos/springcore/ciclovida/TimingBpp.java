package dev.estudos.springcore.ciclovida;

import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.List;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.stereotype.Component;

/**
 * SC04-11 · Um BPP que mede tempo e devolve um <b>proxy de verdade</b> ({@code java.lang.reflect.Proxy}) no
 * lugar do {@code PaymentGateway}: versão minúscula do que o Spring AOP faz em
 * {@code postProcessAfterInitialization}. As durações variam; o exemplo registra apenas o nome do método.
 * Documento: 02-spring-core/04-ciclo-de-vida-e-escopos.md §5.3 (o bloco do documento está marcado
 * "não compilado"; esta versão, com {@code PaymentGateway} declarada, compila e roda).
 *
 * <p>Saída real:
 * <pre>
 * charge devolveu -&gt; cobrou 100 via card
 * medições registradas pelo proxy -&gt; [card.charge]
 * o bean injetado é um proxy JDK -&gt; true
 * não é um proxy do Spring AOP (é o java.lang.reflect.Proxy puro) -&gt; true
 * </pre>
 */
public class TimingBpp {
    interface PaymentGateway { String charge(int cents); }

    @Component("card")
    static class CardGateway implements PaymentGateway {
        public String charge(int cents) { return "cobrou " + cents + " via card"; }
    }

    static final List<String> MEDICOES = new ArrayList<>();

    @Component
    static class Timing implements BeanPostProcessor {
        @Override
        public Object postProcessAfterInitialization(Object bean, String name) {
            if (!(bean instanceof PaymentGateway g)) return bean;
            return Proxy.newProxyInstance(bean.getClass().getClassLoader(), new Class<?>[]{PaymentGateway.class},
                (proxy, method, args) -> {
                    long t0 = System.nanoTime();
                    try { return method.invoke(g, args); }
                    finally { long ns = System.nanoTime() - t0; MEDICOES.add(name + "." + method.getName()); assert ns >= 0; }
                });
        }
    }

    public static List<String> run() {
        MEDICOES.clear();
        List<String> out = new ArrayList<>();
        try (var ctx = new AnnotationConfigApplicationContext(CardGateway.class, Timing.class)) {
            PaymentGateway g = ctx.getBean(PaymentGateway.class);
            out.add("charge devolveu -> " + g.charge(100));
            out.add("medições registradas pelo proxy -> " + MEDICOES);
            out.add("o bean injetado é um proxy JDK -> " + Proxy.isProxyClass(g.getClass()));
            out.add("não é um proxy do Spring AOP (é o java.lang.reflect.Proxy puro) -> " + !AopUtils.isAopProxy(g));
        }
        return out;
    }

    public static void main(String[] args) { run().forEach(System.out::println); }
}
