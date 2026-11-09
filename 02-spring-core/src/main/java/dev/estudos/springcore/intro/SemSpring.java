package dev.estudos.springcore.intro;

import dev.estudos.springcore.intro.pedidos.*;
import java.util.ArrayList;
import java.util.List;

/**
 * SC01-04 · Sem Spring: quem monta o grafo de objetos é você ({@code new} em cadeia).
 * Documento: 02-spring-core/01-o-que-e-spring.md §5.1.
 *
 * <p>Saída real:
 * <pre>
 * cartão cobrou 1500 centavos de p-1
 * pedidos salvos -&gt; 1
 * </pre>
 */
public class SemSpring {
    public static List<String> run() {
        List<String> out = new ArrayList<>();
        var repo = new InMemoryOrderRepository();
        var gateway = new CardPaymentGateway();
        var service = new OrderService(repo, gateway);   // quem monta é você
        out.add(service.place(new Order("p-1", 1500)));
        out.add("pedidos salvos -> " + service.count());
        return out;
    }

    public static void main(String[] args) { run().forEach(System.out::println); }
}
