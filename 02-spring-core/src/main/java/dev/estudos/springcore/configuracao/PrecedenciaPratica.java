package dev.estudos.springcore.configuracao;

import java.util.ArrayList;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.MapPropertySource;
import java.util.Map;

/**
 * SC05-16 · Ordem de precedência prática: {@code -Dorders.currency=USD} (propriedade do sistema) vence o
 * arquivo; uma fonte adicionada com {@code addFirst} vence todas; {@code addLast} perde para as demais.
 * Documento: 02-spring-core/05-configuracao.md §5.5.
 *
 * <p>Saída real:
 * <pre>
 * só o arquivo -&gt; BRL
 * com -Dorders.currency=USD -&gt; USD
 * fonte addFirst (mapa) vence o -D -&gt; EUR
 * fonte addLast perde para o -D -&gt; USD
 * </pre>
 */
public class PrecedenciaPratica {
    @Configuration @PropertySource("classpath:app.properties")
    static class Cfg { @Bean String moeda(@Value("${orders.currency}") String c) { return c; } }

    private static String moeda(String sobrescritaSistema, String addFirst, String addLast) {
        String antes = System.getProperty("orders.currency");
        if (sobrescritaSistema != null) System.setProperty("orders.currency", sobrescritaSistema);
        try (var ctx = new AnnotationConfigApplicationContext()) {
            if (addFirst != null) ctx.getEnvironment().getPropertySources().addFirst(new MapPropertySource("primeira", Map.of("orders.currency", addFirst)));
            if (addLast != null) ctx.getEnvironment().getPropertySources().addLast(new MapPropertySource("ultima", Map.of("orders.currency", addLast)));
            ctx.register(Cfg.class);
            ctx.refresh();
            return ctx.getBean("moeda", String.class);
        } finally {
            if (antes == null) System.clearProperty("orders.currency"); else System.setProperty("orders.currency", antes);
        }
    }

    public static List<String> run() {
        List<String> out = new ArrayList<>();
        out.add("só o arquivo -> " + moeda(null, null, null));
        out.add("com -Dorders.currency=USD -> " + moeda("USD", null, null));
        out.add("fonte addFirst (mapa) vence o -D -> " + moeda("USD", "EUR", null));
        out.add("fonte addLast perde para o -D -> " + moeda("USD", null, "JPY"));
        return out;
    }

    public static void main(String[] args) { run().forEach(System.out::println); }
}
