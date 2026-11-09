package dev.estudos.springcore.ciclovida;

import java.util.ArrayList;
import java.util.List;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * SC04-03 · {@code BeanFactoryPostProcessor} (altera <em>receitas</em>, roda uma vez antes de instanciar) vs
 * {@code BeanPostProcessor} (opera sobre <em>instâncias</em>, roda para cada bean e pode substituir o objeto).
 * Os dois são métodos {@code @Bean static}. O BFPP aqui muda o {@code scope} de {@code stock} para
 * {@code prototype}; o BPP troca a instância por outra.
 * Documento: 02-spring-core/04-ciclo-de-vida-e-escopos.md §2.3.
 *
 * <p>Saída real:
 * <pre>
 * BFPP rodou antes de qualquer instância -&gt; true
 * BFPP mudou a receita: scope de 'stock' -&gt; prototype
 * getBean('stock') duas vezes -&gt; instâncias diferentes: true
 * BPP substituiu o objeto: etiqueta -&gt; trocado-pelo-BPP
 * </pre>
 */
public class BfppVsBpp {
    static final List<String> LOG = new ArrayList<>();
    static class Stock { String etiqueta = "original"; Stock() { LOG.add("Stock criado"); } }

    static class Bfpp implements BeanFactoryPostProcessor {
        @Override public void postProcessBeanFactory(ConfigurableListableBeanFactory bf) {
            LOG.add("BFPP sem instâncias=" + !bf.containsSingleton("stock"));
            bf.getBeanDefinition("stock").setScope("prototype");          // muda a receita
        }
    }

    static class Tracer implements BeanPostProcessor {
        @Override public Object postProcessAfterInitialization(Object bean, String name) {
            if (bean instanceof Stock s) { var novo = new Stock(); novo.etiqueta = "trocado-pelo-BPP"; return novo; }   // substitui o objeto
            return bean;
        }
    }

    @Configuration(proxyBeanMethods = false)
    static class Cfg {
        @Bean static Bfpp bfpp() { return new Bfpp(); }          // STATIC
        @Bean static Tracer tracer() { return new Tracer(); }    // STATIC
        @Bean Stock stock() { return new Stock(); }
    }

    public static List<String> run() {
        LOG.clear();
        List<String> out = new ArrayList<>();
        try (var ctx = new AnnotationConfigApplicationContext(Cfg.class)) {
            out.add("BFPP rodou antes de qualquer instância -> " + LOG.getFirst().endsWith("=true"));
            out.add("BFPP mudou a receita: scope de 'stock' -> " + ctx.getBeanDefinition("stock").getScope());
            out.add("getBean('stock') duas vezes -> instâncias diferentes: " + (ctx.getBean("stock") != ctx.getBean("stock")));
            out.add("BPP substituiu o objeto: etiqueta -> " + ctx.getBean(Stock.class).etiqueta);
        }
        return out;
    }

    public static void main(String[] args) { run().forEach(System.out::println); }
}
