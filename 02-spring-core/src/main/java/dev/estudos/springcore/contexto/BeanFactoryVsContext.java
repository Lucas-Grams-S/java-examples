package dev.estudos.springcore.contexto;

import java.util.ArrayList;
import java.util.List;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.context.support.GenericApplicationContext;

/**
 * SC03-01 · {@code BeanFactory} vs {@code ApplicationContext}: a fábrica pura cria singletons sob demanda
 * (só existe a receita até o primeiro {@code getBean}); o contexto pré-instancia tudo no {@code refresh()}.
 * Documento: 02-spring-core/03-application-context-e-beans.md §2.1.
 *
 * <p>Saída real:
 * <pre>
 * BeanFactory: singleton existe antes do getBean? -&gt; false
 * Product criado
 * BeanFactory: singleton existe depois do getBean? -&gt; true
 * ApplicationContext: antes do refresh
 * Product criado
 * ApplicationContext: depois do refresh
 * </pre>
 */
public class BeanFactoryVsContext {
    public static class Product {
        public static List<String> log;
        public Product() { log.add("Product criado"); }
    }

    public static List<String> run() {
        List<String> out = new ArrayList<>();
        Product.log = out;
        var bf = new DefaultListableBeanFactory();
        bf.registerBeanDefinition("product", new RootBeanDefinition(Product.class));
        out.add("BeanFactory: singleton existe antes do getBean? -> " + bf.containsSingleton("product"));
        bf.getBean("product");
        out.add("BeanFactory: singleton existe depois do getBean? -> " + bf.containsSingleton("product"));

        try (var ctx = new GenericApplicationContext()) {
            ctx.registerBeanDefinition("product", new RootBeanDefinition(Product.class));
            out.add("ApplicationContext: antes do refresh");   // nada criado ainda
            ctx.refresh();                                      // "Product criado" aparece aqui
            out.add("ApplicationContext: depois do refresh");
        }
        return out;
    }

    public static void main(String[] args) { run().forEach(System.out::println); }
}
