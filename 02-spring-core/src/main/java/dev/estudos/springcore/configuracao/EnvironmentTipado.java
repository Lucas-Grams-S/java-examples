package dev.estudos.springcore.configuracao;

import dev.estudos.springcore.configuracao.camadas.OrdersApplicationConfig;
import dev.estudos.springcore.configuracao.camadas.PricingService;
import java.util.ArrayList;
import java.util.List;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

/**
 * SC05-14 · Ler propriedades de forma tipada via {@code Environment}: {@code getRequiredProperty(chave, tipo)}
 * converte o tipo e, se a chave faltar, lança {@code IllegalStateException} com mensagem clara.
 * Documento: 02-spring-core/05-configuracao.md §5.3.
 *
 * <p>Saída real:
 * <pre>
 * getRequiredProperty('orders.max-items', Integer) -&gt; 5
 * chave ausente -&gt; IllegalStateException: Required key 'orders.max-items' not found
 * </pre>
 */
public class EnvironmentTipado {
    /** Só escaneia o {@code PricingService}, sem o {@code @PropertySource}: a chave fica ausente. */
    @Configuration
    @ComponentScan(basePackageClasses = PricingService.class,
        useDefaultFilters = false,
        includeFilters = @ComponentScan.Filter(type = org.springframework.context.annotation.FilterType.ASSIGNABLE_TYPE, classes = PricingService.class))
    static class SemPropriedades {}

    public static List<String> run() {
        List<String> out = new ArrayList<>();
        try (var ctx = new AnnotationConfigApplicationContext(OrdersApplicationConfig.class)) {
            out.add("getRequiredProperty('orders.max-items', Integer) -> " + ctx.getBean(PricingService.class).maxItems());
        }
        try (var ctx = new AnnotationConfigApplicationContext(SemPropriedades.class)) {
            out.add("chave ausente -> subiu?!");
        } catch (Exception e) {
            Throwable r = e; while (r.getCause() != null) r = r.getCause();
            out.add("chave ausente -> " + r.getClass().getSimpleName() + ": " + r.getMessage());
        }
        return out;
    }

    public static void main(String[] args) { run().forEach(System.out::println); }
}
