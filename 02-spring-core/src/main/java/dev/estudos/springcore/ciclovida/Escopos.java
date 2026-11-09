package dev.estudos.springcore.ciclovida;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import org.springframework.context.support.SimpleThreadScope;
import jakarta.annotation.PreDestroy;

/**
 * SC04-04 · Escopos: {@code singleton} (um por <em>contexto</em>, não por JVM), {@code prototype} (novo a cada
 * pedido; o contêiner <b>não</b> destrói) e um escopo customizado ({@code SimpleThreadScope}, uma instância
 * por thread). Escopos web ({@code request}/{@code session}) precisam de contexto web e ficam fora.
 * Documento: 02-spring-core/04-ciclo-de-vida-e-escopos.md §2.4 (e §3.1: destruição só para singleton).
 *
 * <p>Saída real:
 * <pre>
 * singleton: getBean == getBean -&gt; true
 * prototype: getBean == getBean -&gt; false
 * dois contextos = dois "singletons" -&gt; true
 * escopo 'thread': mesma thread -&gt; true, outra thread -&gt; false
 * após close(): &#64;PreDestroy do singleton rodou -&gt; true, do prototype -&gt; false
 * </pre>
 */
public class Escopos {
    static final List<String> DESTROYED = new ArrayList<>();
    static class Counter {
        @PreDestroy void bye() { DESTROYED.add(getClass().getSimpleName() + "@" + System.identityHashCode(this)); }
    }
    static class SingleCounter extends Counter {}
    static class ProtoCounter extends Counter {}
    static class ThreadCounter {}

    @Configuration(proxyBeanMethods = false)
    static class Cfg {
        @Bean SingleCounter single() { return new SingleCounter(); }                       // singleton (padrão)
        @Bean @Scope("prototype") ProtoCounter proto() { return new ProtoCounter(); }
        @Bean @Scope("thread") ThreadCounter perThread() { return new ThreadCounter(); }
    }

    public static List<String> run() throws Exception {
        DESTROYED.clear();
        List<String> out = new ArrayList<>();
        SingleCounter single; 
        try (var ctx = new AnnotationConfigApplicationContext()) {
            ctx.getBeanFactory().registerScope("thread", new SimpleThreadScope());
            ctx.register(Cfg.class);
            ctx.refresh();
            single = ctx.getBean("single", SingleCounter.class);
            out.add("singleton: getBean == getBean -> " + (ctx.getBean("single") == ctx.getBean("single")));
            ctx.getBean("proto"); ctx.getBean("proto");
            out.add("prototype: getBean == getBean -> " + (ctx.getBean("proto") == ctx.getBean("proto")));
            try (var outro = new AnnotationConfigApplicationContext(SimpleCfg.class)) {
                out.add("dois contextos = dois \"singletons\" -> " + (outro.getBean(SingleCounter.class) != single));
            }
            Object here = ctx.getBean("perThread");
            AtomicReference<Object> other = new AtomicReference<>();
            Thread t = new Thread(() -> other.set(ctx.getBean("perThread")));
            t.start(); t.join();
            out.add("escopo 'thread': mesma thread -> " + (here == ctx.getBean("perThread")) + ", outra thread -> " + (here == other.get()));
        }
        long singleDestroyed = DESTROYED.stream().filter(s -> s.startsWith("SingleCounter@" + System.identityHashCode(single))).count();
        long protoDestroyed = DESTROYED.stream().filter(s -> s.startsWith("ProtoCounter")).count();
        out.add("após close(): @PreDestroy do singleton rodou -> " + (singleDestroyed == 1) + ", do prototype -> " + (protoDestroyed > 0));
        return out;
    }

    @Configuration(proxyBeanMethods = false)
    static class SimpleCfg { @Bean SingleCounter single() { return new SingleCounter(); } }

    public static void main(String[] args) throws Exception { run().forEach(System.out::println); }
}
