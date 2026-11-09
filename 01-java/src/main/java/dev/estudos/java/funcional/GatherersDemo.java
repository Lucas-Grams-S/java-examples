package dev.estudos.java.funcional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Gatherers;
import java.util.stream.Stream;

/**
 * J09-10 · Gatherers (Java 24+): windowFixed, windowSliding, scan e fold.
 * Documento: java-docs/01-java/09-programacao-funcional.md §3.4.
 *
 * <p>Saída real (JDK 25):
 * <pre>
 * windowFixed(2) -&gt; [[1, 2], [3, 4], [5, 6]]
 * windowFixed(4) -&gt; [[1, 2, 3, 4], [5, 6]]
 * windowSliding(3) -&gt; [[1, 2, 3], [2, 3, 4], [3, 4, 5]]
 * scan (soma acumulada) -&gt; [1, 3, 6, 10]
 * fold -&gt; [10]
 * mapConcurrent -&gt; [10, 20, 30]
 * </pre>
 *
 * <p>Variações: troque windowFixed(2) por windowFixed(4) e veja a última janela parcial.
 */
public class GatherersDemo {
    public static List<String> run() {
        List<String> out = new ArrayList<>();
        out.add("windowFixed(2) -> " + Stream.of(1, 2, 3, 4, 5, 6).gather(Gatherers.windowFixed(2)).toList());
        out.add("windowFixed(4) -> " + Stream.of(1, 2, 3, 4, 5, 6).gather(Gatherers.windowFixed(4)).toList());
        out.add("windowSliding(3) -> " + Stream.of(1, 2, 3, 4, 5).gather(Gatherers.windowSliding(3)).toList());
        out.add("scan (soma acumulada) -> " + Stream.of(1, 2, 3, 4).gather(Gatherers.scan(() -> 0, Integer::sum)).toList());
        out.add("fold -> " + Stream.of(1, 2, 3, 4).gather(Gatherers.fold(() -> 0, Integer::sum)).toList());
        out.add("mapConcurrent -> " + Stream.of(1, 2, 3).gather(Gatherers.mapConcurrent(2, i -> i * 10)).toList());
        return out;
    }

    public static void main(String[] args) { run().forEach(System.out::println); }
}
