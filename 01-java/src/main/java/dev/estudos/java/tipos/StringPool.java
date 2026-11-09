package dev.estudos.java.tipos;

import java.util.ArrayList;
import java.util.List;

/**
 * J03-05 · {@code String}: imutável, com pool.
 * Documento: java-docs/01-java/03-tipos-e-memoria.md §2.5.
 *
 * <p>A medição de tempo da concatenação em laço é uma demonstração (não um benchmark): o tempo varia.
 *
 * <p>Saída real (JDK 25; os tempos variam):
 * <pre>
 * s1 == s2 (pool) -&gt; true
 * s1 == s3 (new) -&gt; false
 * s1.equals(s3) -&gt; true
 * s1 == s3.intern() -&gt; true
 * toUpperCase não altera o original -&gt; JAVA / java
 * mesmo resultado -&gt; true
 * += em laço: 56 ms · StringBuilder: 1 ms (varia)
 * emoji: length()=2 codePoints=1
 * </pre>
 */
public class StringPool {
    @SuppressWarnings({"StringEquality", "UnnecessaryStringBuilder"})
    public static List<String> run() {
        List<String> out = new ArrayList<>();
        String s1 = "java", s2 = "java", s3 = new String("java");
        out.add("s1 == s2 (pool) -> " + (s1 == s2));
        out.add("s1 == s3 (new) -> " + (s1 == s3));
        out.add("s1.equals(s3) -> " + s1.equals(s3));
        out.add("s1 == s3.intern() -> " + (s1 == s3.intern()));
        out.add("toUpperCase não altera o original -> " + s1.toUpperCase() + " / " + s1);

        long t0 = System.nanoTime();
        String r = "";
        for (int k = 0; k < 20_000; k++) r += "x";
        long concatMs = (System.nanoTime() - t0) / 1_000_000;
        t0 = System.nanoTime();
        var sb = new StringBuilder();
        for (int k = 0; k < 20_000; k++) sb.append('x');
        long sbMs = (System.nanoTime() - t0) / 1_000_000;
        out.add("mesmo resultado -> " + r.equals(sb.toString()));
        out.add("+= em laço: " + concatMs + " ms · StringBuilder: " + sbMs + " ms (varia)");
        String emoji = "😀";
        out.add("emoji: length()=" + emoji.length() + " codePoints=" + emoji.codePoints().count());
        return out;
    }

    public static void main(String[] args) {
        run().forEach(System.out::println);
    }
}
