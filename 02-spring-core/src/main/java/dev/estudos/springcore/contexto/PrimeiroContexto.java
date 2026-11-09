package dev.estudos.springcore.contexto;

import dev.estudos.springcore.contexto.app.OrderRepo;
import dev.estudos.springcore.contexto.app.OrderSvc;
import java.util.ArrayList;
import java.util.List;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

/**
 * SC03-09 (e SC03-12) · Meu primeiro contexto: {@code @ComponentScan} no pacote {@code contexto.app} e
 * {@code getBean} em {@code main} (uso legítimo; dentro de beans de negócio prefira injeção).
 * Documento: 02-spring-core/03-application-context-e-beans.md §5.1 e §5.4.
 *
 * <p>Saída real:
 * <pre>
 * OrderSvc obtido -&gt; true
 * repo injetado é o mesmo singleton do contêiner -&gt; true
 * definições (2 estereótipos + infraestrutura + AppConfig) &gt; 3 -&gt; true
 * </pre>
 */
public class PrimeiroContexto {
    @Configuration
    @ComponentScan(basePackages = "dev.estudos.springcore.contexto.app")
    static class AppConfig {}

    public static List<String> run() {
        List<String> out = new ArrayList<>();
        try (var ctx = new AnnotationConfigApplicationContext(AppConfig.class)) {
            OrderSvc svc = ctx.getBean(OrderSvc.class);
            out.add("OrderSvc obtido -> " + (svc != null));
            out.add("repo injetado é o mesmo singleton do contêiner -> " + (svc.repo() == ctx.getBean(OrderRepo.class)));
            out.add("definições (2 estereótipos + infraestrutura + AppConfig) > 3 -> " + (ctx.getBeanDefinitionCount() > 3));
        }
        return out;
    }

    public static void main(String[] args) { run().forEach(System.out::println); }
}
