package dev.estudos.springcore.configuracao;

import java.time.Clock;
import java.util.ArrayList;
import java.util.List;
import org.springframework.beans.factory.BeanRegistrar;
import org.springframework.beans.factory.BeanRegistry;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.ImportResource;
import org.springframework.context.annotation.ImportSelector;
import org.springframework.core.env.Environment;
import org.springframework.core.type.AnnotationMetadata;

/**
 * SC05-03 · {@code @Import} e composição: classes {@code @Configuration}, {@code ImportSelector} (decide
 * classes por código; base das auto-configurações do Boot), {@code @ImportResource} (XML) e o
 * {@code BeanRegistrar} do Spring 7.
 * Documento: 02-spring-core/05-configuracao.md §2.3.
 *
 * <p>Saída real:
 * <pre>
 * &#64;Import de &#64;Configuration -&gt; beans [infra, security]
 * ImportSelector escolheu -&gt; selecionado
 * &#64;ImportResource (beans.xml) -&gt; clock do XML presente: true
 * BeanRegistrar (7.0) registrou -&gt; registrado (via registrar)
 * </pre>
 */
public class ImportDemo {
    @Configuration(proxyBeanMethods = false) static class SecurityConfig { @Bean String security() { return "security"; } }
    @Configuration(proxyBeanMethods = false) static class InfraConfig { @Bean String infra() { return "infra"; } }

    @Configuration(proxyBeanMethods = false)
    @Import({SecurityConfig.class, InfraConfig.class})
    static class AppConfig {}

    @Configuration(proxyBeanMethods = false) static class Escolhida { @Bean String selecionado() { return "selecionado"; } }
    static class MeuSelector implements ImportSelector {
        @Override public String[] selectImports(AnnotationMetadata metadata) { return new String[] { Escolhida.class.getName() }; }
    }
    @Configuration(proxyBeanMethods = false) @Import(MeuSelector.class) static class ComSelector {}

    @Configuration(proxyBeanMethods = false) @ImportResource("classpath:beans.xml") static class ComXml {}

    static class MeuRegistrar implements BeanRegistrar {
        @Override public void register(BeanRegistry registry, Environment env) {
            registry.registerBean("registrado", String.class, spec -> spec.supplier(ctx -> "registrado (via registrar)"));
        }
    }
    @Configuration(proxyBeanMethods = false) @Import(MeuRegistrar.class) static class ComRegistrar {}

    public static List<String> run() {
        List<String> out = new ArrayList<>();
        try (var ctx = new AnnotationConfigApplicationContext(AppConfig.class)) {
            out.add("@Import de @Configuration -> beans " + ctx.getBeansOfType(String.class).keySet().stream().sorted().toList());
        }
        try (var ctx = new AnnotationConfigApplicationContext(ComSelector.class)) {
            out.add("ImportSelector escolheu -> " + ctx.getBean(String.class));
        }
        try (var ctx = new AnnotationConfigApplicationContext(ComXml.class)) {
            out.add("@ImportResource (beans.xml) -> clock do XML presente: " + (ctx.getBean("clock") instanceof Clock));
        }
        try (var ctx = new AnnotationConfigApplicationContext(ComRegistrar.class)) {
            out.add("BeanRegistrar (7.0) registrou -> " + ctx.getBean("registrado"));
        }
        return out;
    }

    public static void main(String[] args) { run().forEach(System.out::println); }
}
