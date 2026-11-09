package dev.estudos.springcore.configuracao;

import java.time.Clock;
import java.util.ArrayList;
import java.util.List;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.stereotype.Component;

/**
 * SC05-01 · Três gerações de configuração (XML, Java Config, anotações + scanning) geram o mesmo resultado:
 * {@code BeanDefinition}s. O XML vem de {@code src/main/resources/beans.xml}.
 * Documento: 02-spring-core/05-configuracao.md §2.1.
 *
 * <p>Saída real:
 * <pre>
 * XML -&gt; SystemClock
 * Java Config (&#64;Bean) -&gt; SystemClock
 * Anotação (&#64;Component) -&gt; Relogio
 * as três geraram BeanDefinition -&gt; true
 * </pre>
 */
public class TresGeracoes {
    @Component("relogio") static class Relogio {}

    @Configuration(proxyBeanMethods = false)
    static class JavaConfig { @Bean Clock clock() { return Clock.systemUTC(); } }

    public static List<String> run() {
        List<String> out = new ArrayList<>();
        boolean todas = true;
        try (var c = new ClassPathXmlApplicationContext("beans.xml")) {
            out.add("XML -> " + c.getBean("clock").getClass().getSimpleName());
            todas &= c.containsBeanDefinition("clock");
        }
        try (var c = new AnnotationConfigApplicationContext(JavaConfig.class)) {
            out.add("Java Config (@Bean) -> " + c.getBean("clock").getClass().getSimpleName());
            todas &= c.containsBeanDefinition("clock");
        }
        try (var c = new AnnotationConfigApplicationContext(Relogio.class)) {
            out.add("Anotação (@Component) -> " + c.getBean("relogio").getClass().getSimpleName());
            todas &= c.containsBeanDefinition("relogio");
        }
        out.add("as três geraram BeanDefinition -> " + todas);
        return out;
    }

    public static void main(String[] args) { run().forEach(System.out::println); }
}
