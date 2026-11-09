package dev.estudos.springcore.configuracao;

import java.util.ArrayList;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.stereotype.Component;

/**
 * SC05-15 · Valor com padrão e SpEL em campo: {@code ${orders.currency:BRL}} e a lista construída por SpEL
 * sobre o texto do placeholder ({@code '${orders.tags:a,b}'.split(',')}).
 * Documento: 02-spring-core/05-configuracao.md §5.4.
 *
 * <p>Saída real:
 * <pre>
 * currency (sem a propriedade, padrão) -&gt; BRL
 * tags (SpEL split do padrão) -&gt; [a, b]
 * com -Dorders.tags=x,y,z -&gt; [x, y, z]
 * </pre>
 */
public class ValorPadraoSpel {
    @Component
    static class Config {
        @Value("${orders.currency:BRL}") String currency;
        @Value("#{'${orders.tags:a,b}'.split(',')}") List<String> tags;
    }

    public static List<String> run() {
        List<String> out = new ArrayList<>();
        try (var ctx = new AnnotationConfigApplicationContext(Config.class)) {
            var c = ctx.getBean(Config.class);
            out.add("currency (sem a propriedade, padrão) -> " + c.currency);
            out.add("tags (SpEL split do padrão) -> " + c.tags);
        }
        System.setProperty("orders.tags", "x,y,z");
        try (var ctx = new AnnotationConfigApplicationContext(Config.class)) {
            out.add("com -Dorders.tags=x,y,z -> " + ctx.getBean(Config.class).tags);
        } finally { System.clearProperty("orders.tags"); }
        return out;
    }

    public static void main(String[] args) { run().forEach(System.out::println); }
}
