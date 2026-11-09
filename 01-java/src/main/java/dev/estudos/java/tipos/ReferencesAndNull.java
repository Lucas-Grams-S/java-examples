package dev.estudos.java.tipos;

import java.util.ArrayList;
import java.util.List;

/**
 * J03-02 · Tipos de referência e {@code null}.
 * Documento: java-docs/01-java/03-tipos-e-memoria.md §2.2.
 *
 * <p>{@code ==} compara identidade; duas variáveis podem apontar para o mesmo objeto; {@code null} gera NPE com
 * mensagem detalhada (helpful NPE messages, padrão desde o Java 15).
 *
 * <p>Saída real (JDK 25):
 * <pre>
 * a == b (mesmo objeto) -&gt; true
 * a.name vê a mudança feita por b -&gt; alterado via b
 * new Order(1) == new Order(1) -&gt; false
 * NPE -&gt; Cannot read field "name" because "c" is null
 * NPE -&gt; Cannot invoke "String.length()" because "a.name" is null
 * </pre>
 */
public class ReferencesAndNull {
    static class Order { final int id; String name; Order(int id) { this.id = id; } }

    public static List<String> run() {
        List<String> out = new ArrayList<>();
        Order a = new Order(1);
        Order b = a;
        Order c = null;
        b.name = "alterado via b";
        out.add("a == b (mesmo objeto) -> " + (a == b));
        out.add("a.name vê a mudança feita por b -> " + a.name);
        out.add("new Order(1) == new Order(1) -> " + (new Order(1) == new Order(1)));
        try {
            System.out.println(c.name.length());
        } catch (NullPointerException e) {
            out.add("NPE -> " + e.getMessage());
        }
        try {
            a.name = null;
            a.name.length();
        } catch (NullPointerException e) {
            out.add("NPE -> " + e.getMessage());
        }
        return out;
    }

    public static void main(String[] args) {
        run().forEach(System.out::println);
    }
}
