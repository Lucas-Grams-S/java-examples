package dev.estudos.padroes.criacionais;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.List;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * DP02-06 · Singleton: enum, holder, double-checked locking, reflection e o {@code singleton} do Spring.
 * Documento: java-docs/06-padroes-de-projeto/02-criacionais.md §5.1.
 *
 * <p>Saída real (JDK 25):
 * <pre>
 * enum: mesma instância -&gt; true
 * holder: antes de getInstance(), criado? -&gt; false
 * holder: após a 1ª chamada, log -&gt; [OrderIdGenerator criado]
 * holder: após a 2ª chamada, criações -&gt; 1; mesma instância -&gt; true
 * DCL: mesma instância -&gt; true
 * reflection quebra o clássico (2ª instância) -&gt; true
 * reflection no enum -&gt; IllegalArgumentException: Cannot reflectively create enum objects
 * Spring, mesmo contexto -&gt; true
 * Spring, contextos diferentes -&gt; false
 * </pre>
 *
 * <p>Variações: remova o {@code volatile} de {@code ClassicSingleton.instance} e leia sobre publicação insegura (JMM).
 */
public class SingletonIdiomas {

    static final List<String> LOG = new ArrayList<>();

    enum EnumSingleton { INSTANCE; long now() { return System.nanoTime(); } }

    static final class OrderIdGenerator {
        private OrderIdGenerator() { LOG.add("OrderIdGenerator criado"); }
        private static class Holder { static final OrderIdGenerator INSTANCE = new OrderIdGenerator(); }
        static OrderIdGenerator getInstance() { return Holder.INSTANCE; }
        private long next = 0;
        synchronized long nextId() { return ++next; }
    }

    static final class ClassicSingleton {
        private static volatile ClassicSingleton instance;
        private ClassicSingleton() {}
        static ClassicSingleton getInstance() {
            ClassicSingleton local = instance;
            if (local == null) {
                synchronized (ClassicSingleton.class) {
                    local = instance;
                    if (local == null) instance = local = new ClassicSingleton();
                }
            }
            return local;
        }
    }

    static class PriceTable {}
    @Configuration static class PricingConfig { @Bean PriceTable priceTable() { return new PriceTable(); } }

    public static List<String> run() throws Exception {
        LOG.clear();
        List<String> out = new ArrayList<>();
        out.add("enum: mesma instância -> " + (EnumSingleton.INSTANCE == EnumSingleton.valueOf("INSTANCE")));

        out.add("holder: antes de getInstance(), criado? -> " + LOG.contains("OrderIdGenerator criado"));
        var g = OrderIdGenerator.getInstance();
        out.add("holder: após a 1ª chamada, log -> " + LOG);
        OrderIdGenerator.getInstance();
        out.add("holder: após a 2ª chamada, criações -> " + LOG.size() + "; mesma instância -> " + (g == OrderIdGenerator.getInstance()));

        out.add("DCL: mesma instância -> " + (ClassicSingleton.getInstance() == ClassicSingleton.getInstance()));

        Constructor<ClassicSingleton> c = ClassicSingleton.class.getDeclaredConstructor();
        c.setAccessible(true);
        out.add("reflection quebra o clássico (2ª instância) -> " + (c.newInstance() != ClassicSingleton.getInstance()));
        try {
            var ec = EnumSingleton.class.getDeclaredConstructors()[0];
            ec.setAccessible(true);
            ec.newInstance("X", 1);
            out.add("reflection no enum -> criou?!");
        } catch (IllegalArgumentException e) {
            out.add("reflection no enum -> IllegalArgumentException: " + e.getMessage());
        }

        try (var ctx1 = new AnnotationConfigApplicationContext(PricingConfig.class);
             var ctx2 = new AnnotationConfigApplicationContext(PricingConfig.class)) {
            out.add("Spring, mesmo contexto -> " + (ctx1.getBean(PriceTable.class) == ctx1.getBean(PriceTable.class)));
            out.add("Spring, contextos diferentes -> " + (ctx1.getBean(PriceTable.class) == ctx2.getBean(PriceTable.class)));
        }
        return out;
    }

    public static void main(String[] args) throws Exception { run().forEach(System.out::println); }
}
