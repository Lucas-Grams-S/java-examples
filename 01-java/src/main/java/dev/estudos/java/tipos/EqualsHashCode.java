package dev.estudos.java.tipos;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * J03-06 · Identidade, igualdade e o contrato {@code equals}/{@code hashCode}.
 * Documento: java-docs/01-java/03-tipos-e-memoria.md §2.6.
 *
 * <p>Saída real (JDK 25):
 * <pre>
 * Bad: contains(new Bad(1)) -&gt; false
 * Good: contains(new Good(1)) -&gt; true
 * Good depois de mutar a chave: contains(g) -&gt; false
 * record Product: iguais por valor -&gt; true
 * Object.equals é ==: new Object().equals(new Object()) -&gt; false
 * x.equals(null) -&gt; false
 * </pre>
 *
 * <p>Variações: mute {@code id} de um {@code Good} depois de inseri-lo no {@code HashSet} e veja o objeto "sumir".
 */
public class EqualsHashCode {
    static class Bad {
        int id;
        Bad(int id) { this.id = id; }
        @Override public boolean equals(Object o) { return o instanceof Bad b && b.id == id; }
        // esqueceu o hashCode!
    }

    static class Good {
        int id;
        Good(int id) { this.id = id; }
        @Override public boolean equals(Object o) { return o instanceof Good g && g.id == id; }
        @Override public int hashCode() { return Objects.hash(id); }
    }

    record Product(String sku, String name) {}            // equals/hashCode automáticos

    public static List<String> run() {
        List<String> out = new ArrayList<>();
        Set<Bad> bad = new HashSet<>();
        bad.add(new Bad(1));
        out.add("Bad: contains(new Bad(1)) -> " + bad.contains(new Bad(1)));
        Set<Good> good = new HashSet<>();
        Good g = new Good(1);
        good.add(g);
        out.add("Good: contains(new Good(1)) -> " + good.contains(new Good(1)));
        g.id = 2;                                           // muta o campo usado no hashCode
        out.add("Good depois de mutar a chave: contains(g) -> " + good.contains(g));
        out.add("record Product: iguais por valor -> " + new Product("A", "x").equals(new Product("A", "x")));
        out.add("Object.equals é ==: new Object().equals(new Object()) -> " + new Object().equals(new Object()));
        out.add("x.equals(null) -> " + new Product("A", "x").equals(null));
        return out;
    }

    public static void main(String[] args) {
        run().forEach(System.out::println);
    }
}
