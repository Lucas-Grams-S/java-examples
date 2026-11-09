package dev.estudos.springcore.configuracao;

import java.util.ArrayList;
import java.util.List;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MutablePropertySources;

/**
 * SC05-05 · {@code Environment} e {@code PropertySource}: o {@code Environment} consulta uma lista
 * <b>ordenada</b> de fontes e a primeira que tiver a chave vence. Aqui: propriedades do sistema, variáveis de
 * ambiente e o {@code app.properties} do {@code @PropertySource}. {@code getProperty(chave, Integer.class)}
 * converte o tipo.
 * Documento: 02-spring-core/05-configuracao.md §2.5.
 *
 * <p>Saída real:
 * <pre>
 * fontes (em ordem) -&gt; [systemProperties, systemEnvironment, class path resource [app.properties]]
 * orders.max-items como Integer -&gt; 5
 * orders.currency -&gt; BRL
 * propriedade do sistema user.language presente -&gt; true
 * -Dorders.currency=USD vence o arquivo -&gt; USD
 * </pre>
 */
public class EnvironmentPropertySources {
    @Configuration @PropertySource("classpath:app.properties") static class Cfg {}

    public static List<String> run() {
        List<String> out = new ArrayList<>();
        try (var ctx = new AnnotationConfigApplicationContext(Cfg.class)) {
            ConfigurableEnvironment env = ctx.getEnvironment();
            MutablePropertySources fontes = env.getPropertySources();
            out.add("fontes (em ordem) -> " + fontes.stream().map(org.springframework.core.env.PropertySource::getName).toList());
            out.add("orders.max-items como Integer -> " + env.getProperty("orders.max-items", Integer.class));
            out.add("orders.currency -> " + env.getProperty("orders.currency"));
            out.add("propriedade do sistema user.language presente -> " + (env.getProperty("user.language") != null));
        }
        String antes = System.getProperty("orders.currency");
        System.setProperty("orders.currency", "USD");       // equivale a java -Dorders.currency=USD
        try (var ctx = new AnnotationConfigApplicationContext(Cfg.class)) {
            out.add("-Dorders.currency=USD vence o arquivo -> " + ctx.getEnvironment().getProperty("orders.currency"));
        } finally {
            if (antes == null) System.clearProperty("orders.currency"); else System.setProperty("orders.currency", antes);
        }
        return out;
    }

    public static void main(String[] args) { run().forEach(System.out::println); }
}
