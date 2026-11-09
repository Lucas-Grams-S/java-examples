package dev.estudos.java.tipos;

import java.util.List;
import java.util.Objects;

/**
 * J03-10 · {@code hashCode} na prática.
 * Documento: java-docs/01-java/03-tipos-e-memoria.md §3.3 (a seção só tinha texto; os valores citados foram verificados aqui).
 *
 * <p>Saída real (JDK 25; o {@code hashCode} de {@code Object} varia entre execuções, por isso só se mostra a consistência):
 * <pre>
 * "hello".hashCode() -&gt; 99162322
 * fórmula manual com 31 -&gt; 99162322
 * List.of(1, 2).hashCode() -&gt; 994
 * Objects.hash(1, 2) -&gt; 994
 * new Object().hashCode() varia por execução; só é garantido ser consistente na mesma execução -&gt; true
 * </pre>
 */
public class HashCodeInPractice {
    public static List<String> run() {
        String h = "hello";
        Object o = new Object();
        int manual = 0;
        for (char c : h.toCharArray()) manual = 31 * manual + c;   // s[0]*31^(n-1) + ... + s[n-1]
        return List.of("\"hello\".hashCode() -> " + h.hashCode(), "fórmula manual com 31 -> " + manual,
                "List.of(1, 2).hashCode() -> " + List.of(1, 2).hashCode(),
                "Objects.hash(1, 2) -> " + Objects.hash(1, 2),
                "new Object().hashCode() varia por execução; só é garantido ser consistente na mesma execução -> " + (o.hashCode() == o.hashCode()));
    }

    public static void main(String[] args) {
        run().forEach(System.out::println);
    }
}
