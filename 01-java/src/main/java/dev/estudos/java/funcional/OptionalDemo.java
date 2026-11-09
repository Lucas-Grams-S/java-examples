package dev.estudos.java.funcional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import dev.estudos.java.funcional.Domain.*;

/**
 * J09-06 · Optional: map/filter/orElse*, orElse avaliado sempre, or, stream.
 * Documento: java-docs/01-java/09-programacao-funcional.md §2.6.
 *
 * <p>Saída real (JDK 25):
 * <pre>
 * Ana -&gt; Recife
 * Bia (cidade vazia) -&gt; desconhecida
 * Zed -&gt; desconhecida
 * presente: orElse avaliou 1x, orElseGet 0x
 * orElseThrow() -&gt; NoSuchElementException
 * or -&gt; Caio
 * stream -&gt; [Ana, Caio]
 * ifPresentOrElse -&gt; não achou
 * </pre>
 *
 * <p>Variações: troque orElse por orElseGet e veja o contador não incrementar.
 */
public class OptionalDemo {
    static int calls = 0;
    static String expensive() { calls++; return "calculado"; }

    static Optional<Customer> find(String name) {
        return Domain.orders().stream().map(Order::customer).filter(c -> c.name().equals(name)).findFirst();
    }

    public static List<String> run() {
        List<String> out = new ArrayList<>();
        out.add("Ana -> " + find("Ana").map(Customer::city).filter(c -> !c.isBlank()).orElse("desconhecida"));
        out.add("Bia (cidade vazia) -> " + find("Bia").map(Customer::city).filter(c -> !c.isBlank()).orElse("desconhecida"));
        out.add("Zed -> " + find("Zed").map(Customer::city).orElse("desconhecida"));
        calls = 0;
        find("Ana").orElse(new Customer(expensive(), ""));
        int comOrElse = calls;
        calls = 0;
        find("Ana").orElseGet(() -> new Customer(expensive(), ""));
        out.add("presente: orElse avaliou " + comOrElse + "x, orElseGet " + calls + "x");
        try { find("Zed").orElseThrow(); } catch (java.util.NoSuchElementException e) { out.add("orElseThrow() -> NoSuchElementException"); }
        out.add("or -> " + find("Zed").or(() -> find("Caio")).map(Customer::name).get());
        out.add("stream -> " + List.of("Ana", "Zed", "Caio").stream().flatMap(n -> find(n).stream()).map(Customer::name).toList());
        StringBuilder sb = new StringBuilder();
        find("Zed").ifPresentOrElse(c -> sb.append("achou"), () -> sb.append("não achou"));
        out.add("ifPresentOrElse -> " + sb);
        return out;
    }

    public static void main(String[] args) { run().forEach(System.out::println); }
}
