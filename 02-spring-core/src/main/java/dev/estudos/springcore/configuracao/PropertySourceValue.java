package dev.estudos.springcore.configuracao;

import java.util.ArrayList;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

/**
 * SC05-06 · {@code @PropertySource} e {@code @Value}: placeholder obrigatório, com padrão ({@code :10}),
 * propriedade ausente com padrão, conversão para {@code int} e SpEL combinado com placeholders. <b>Atenção:</b> a ausência de um
 * placeholder só falha na subida se houver um {@code PropertySourcesPlaceholderConfigurer} (o Boot registra um);
 * no contexto puro o valor fica literal (ver DIVERGENCIAS.md).
 * Documento: 02-spring-core/05-configuracao.md §2.6.
 *
 * <p>Saída real:
 * <pre>
 * banner -&gt; BRL/5/padrao/50
 * sem PropertySourcesPlaceholderConfigurer, $&#123;nao.existe&#125; fica literal -&gt; $&#123;nao.existe&#125;
 * com PropertySourcesPlaceholderConfigurer, placeholder ausente -&gt; PlaceholderResolutionException (Could not resolve placeholder 'nao.existe' ...)
 * </pre>
 */
public class PropertySourceValue {
    @Configuration
    @PropertySource("classpath:app.properties")          // orders.currency=BRL, orders.max-items=5
    static class Props {
        @Bean String banner(
            @Value("${orders.currency}") String cur,                                   // obrigatória
            @Value("${orders.max-items:10}") int max,                                  // com padrão; converte para int
            @Value("${orders.missing:padrao}") String miss,                            // ausente → "padrao"
            @Value("#{ T(java.lang.Math).max(2, ${orders.max-items}) * 10 }") int spel) {
            return cur + "/" + max + "/" + miss + "/" + spel;
        }
    }

    /** Sem PropertySourcesPlaceholderConfigurer: o resolvedor embutido do contexto é <b>leniente</b>. */
    @Configuration static class Leniente {
        @Bean String x(@Value("${nao.existe}") String v) { return v; }
    }

    /** Com o configurer (static, é um BFPP): placeholder ausente passa a falhar na subida. */
    @Configuration static class Estrita {
        @Bean static org.springframework.context.support.PropertySourcesPlaceholderConfigurer psc() {
            return new org.springframework.context.support.PropertySourcesPlaceholderConfigurer();
        }
        @Bean String x(@Value("${nao.existe}") String v) { return v; }
    }

    public static List<String> run() {
        List<String> out = new ArrayList<>();
        try (var ctx = new AnnotationConfigApplicationContext(Props.class)) {
            out.add("banner -> " + ctx.getBean("banner"));
        }
        try (var ctx = new AnnotationConfigApplicationContext(Leniente.class)) {
            out.add("sem PropertySourcesPlaceholderConfigurer, ${nao.existe} fica literal -> " + ctx.getBean("x"));
        }
        try (var ctx = new AnnotationConfigApplicationContext(Estrita.class)) {
            out.add("com PropertySourcesPlaceholderConfigurer, placeholder ausente -> subiu?!");
        } catch (Exception e) {
            Throwable r = e; while (r.getCause() != null) r = r.getCause();
            String m = r.getMessage();
            out.add("com PropertySourcesPlaceholderConfigurer, placeholder ausente -> " + r.getClass().getSimpleName()
                + " (" + m.substring(0, m.indexOf("'nao.existe'") + 12) + " ...)");
        }
        return out;
    }

    public static void main(String[] args) { run().forEach(System.out::println); }
}
