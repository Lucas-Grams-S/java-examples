package dev.estudos.springcore.intro;

import dev.estudos.springcore.intro.pedidos.*;
import java.util.ArrayList;
import java.util.List;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

/**
 * SC01-05 · Com Spring: o contêiner descobre os estereótipos, cria e injeta.
 * Documento: 02-spring-core/01-o-que-e-spring.md §5.2.
 *
 * <p>Saída real:
 * <pre>
 * cartão cobrou 1500 centavos de p-1
 * pedidos salvos -&gt; 1
 * repo injetado é InMemoryOrderRepository -&gt; true
 * </pre>
 */
public class ComSpring {
    public static List<String> run() {
        List<String> out = new ArrayList<>();
        try (var ctx = new AnnotationConfigApplicationContext(AppConfig.class)) {
            var service = ctx.getBean(OrderService.class);
            out.add(service.place(new Order("p-1", 1500)));
            out.add("pedidos salvos -> " + service.count());
            out.add("repo injetado é InMemoryOrderRepository -> "
                + (ctx.getBean(OrderRepository.class) instanceof InMemoryOrderRepository));
        }
        return out;
    }

    public static void main(String[] args) { run().forEach(System.out::println); }
}
