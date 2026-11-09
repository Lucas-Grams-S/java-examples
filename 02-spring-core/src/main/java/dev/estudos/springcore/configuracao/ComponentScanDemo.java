package dev.estudos.springcore.configuracao;

import dev.estudos.springcore.configuracao.scan.app.OrderService;
import dev.estudos.springcore.configuracao.scan.fora.ForaDoPacote;
import java.util.ArrayList;
import java.util.List;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.FilterType;
import org.springframework.stereotype.Repository;

/**
 * SC05-02 · {@code @ComponentScan}: o que varre, {@code excludeFilters} (regex), {@code useDefaultFilters=false}
 * com {@code includeFilters} e a causa nº 1 de {@code NoSuchBeanDefinitionException}: classe fora do pacote.
 * Documento: 02-spring-core/05-configuracao.md §2.2.
 *
 * <p>Saída real:
 * <pre>
 * scan simples (3 estereótipos em scan.app) -&gt; [legacyService, orderRepository, orderService]
 * com excludeFilters REGEX .*Legacy.* -&gt; [orderRepository, orderService]
 * com useDefaultFilters=false + include &#64;Repository -&gt; [orderRepository]
 * classe em pacote irmão não é vista -&gt; NoSuchBeanDefinitionException
 * scan do pacote pai enxerga os dois -&gt; true
 * </pre>
 */
public class ComponentScanDemo {
    private static final String PKG = "dev.estudos.springcore.configuracao.scan.app";

    @Configuration @ComponentScan(basePackages = PKG) static class Simples {}

    @Configuration
    @ComponentScan(basePackages = PKG,
        excludeFilters = @ComponentScan.Filter(type = FilterType.REGEX, pattern = ".*Legacy.*"))
    static class ComExclusao {}

    @Configuration
    @ComponentScan(basePackages = PKG, useDefaultFilters = false,
        includeFilters = @ComponentScan.Filter(type = FilterType.ANNOTATION, classes = Repository.class))
    static class SoRepository {}

    @Configuration @ComponentScan(basePackages = "dev.estudos.springcore.configuracao.scan") static class Pai {}

    private static List<String> nomes(Class<?> cfg) {
        try (var ctx = new AnnotationConfigApplicationContext(cfg)) {
            return java.util.Arrays.stream(ctx.getBeanDefinitionNames())
                .filter(n -> !n.contains(".") && !n.startsWith("org.") && ctx.getBeanDefinition(n).getBeanClassName() != null
                    && ctx.getBeanDefinition(n).getBeanClassName().contains(".scan.")).sorted().toList();
        }
    }

    public static List<String> run() {
        List<String> out = new ArrayList<>();
        out.add("scan simples (3 estereótipos em scan.app) -> " + nomes(Simples.class));
        out.add("com excludeFilters REGEX .*Legacy.* -> " + nomes(ComExclusao.class));
        out.add("com useDefaultFilters=false + include @Repository -> " + nomes(SoRepository.class));
        try (var ctx = new AnnotationConfigApplicationContext(Simples.class)) {
            ctx.getBean(ForaDoPacote.class);
            out.add("classe em pacote irmão não é vista -> (achou?!)");
        } catch (NoSuchBeanDefinitionException e) {
            out.add("classe em pacote irmão não é vista -> NoSuchBeanDefinitionException");
        }
        try (var ctx = new AnnotationConfigApplicationContext(Pai.class)) {
            out.add("scan do pacote pai enxerga os dois -> " + (ctx.getBean(OrderService.class) != null && ctx.getBean(ForaDoPacote.class) != null));
        }
        return out;
    }

    public static void main(String[] args) { run().forEach(System.out::println); }
}
