package dev.estudos.java.modernos;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

/**
 * J05-04 · Records.
 * Documento: java-docs/01-java/05-recursos-modernos-da-linguagem.md §2.4.
 *
 * <p>Saída real (JDK 25):
 * <pre>
 * toString -&gt; Customer[id=00000000-0000-0000-0000-000000000001, name=Ana, email=ana&#64;x.com]
 * accessor -&gt; Ana
 * equals por valor -&gt; true
 * Range.of(5).length() -&gt; 5
 * construtor compacto valida -&gt; lo&gt;hi
 * estende Record -&gt; true
 * componentes por reflection -&gt; [java.lang.String last4, int installments]
 * imutabilidade rasa: lista mutada por fora -&gt; [a, b]
 * </pre>
 *
 * <p>Variações: rode {@code javap -c -p -cp 01-java/target/classes 'dev.estudos.java.modernos.RecordsDemo$Range'} e veja o {@code invokedynamic}.
 */
public class RecordsDemo {
    record Customer(UUID id, String name, String email) {}

    record Range(int lo, int hi) {
        public Range {
            if (lo > hi) throw new IllegalArgumentException("lo>hi");
        }
        public static Range of(int hi) { return new Range(0, hi); }
        public int length() { return hi - lo; }
    }

    record Card(String last4, int installments) {}

    record Shallow(List<String> items) {}

    public static List<String> run() {
        List<String> out = new ArrayList<>();
        var c = new Customer(new UUID(0, 1), "Ana", "ana@x.com");
        out.add("toString -> " + c);
        out.add("accessor -> " + c.name());
        out.add("equals por valor -> " + c.equals(new Customer(new UUID(0, 1), "Ana", "ana@x.com")));
        out.add("Range.of(5).length() -> " + Range.of(5).length());
        try {
            new Range(3, 1);
        } catch (IllegalArgumentException e) {
            out.add("construtor compacto valida -> " + e.getMessage());
        }
        out.add("estende Record -> " + (Range.class.getSuperclass() == Record.class));
        out.add("componentes por reflection -> " + Arrays.toString(Card.class.getRecordComponents()));
        var list = new ArrayList<>(List.of("a"));
        var sh = new Shallow(list);
        list.add("b");
        out.add("imutabilidade rasa: lista mutada por fora -> " + sh.items());
        return out;
    }

    public static void main(String[] args) { run().forEach(System.out::println); }
}
