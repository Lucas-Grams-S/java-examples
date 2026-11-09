package dev.estudos.java.funcional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

/**
 * J09-08 · Streams: preguiça, pipelining elemento a elemento e curto-circuito.
 * Documento: java-docs/01-java/09-programacao-funcional.md §3.2.
 *
 * <p>Saída real (JDK 25):
 * <pre>
 * findFirst -&gt; [peek 1, peek 2]
 * um a um (sem coleção intermediária) -&gt; [filter a, map a, filter b, map b]
 * sorted é stateful (barreira) -&gt; [antes 3, antes 1, antes 2, depois 1, depois 2, depois 3]
 * limit em fonte infinita -&gt; [gera 1, gera 2, gera 3]
 * </pre>
 *
 * <p>Variações: troque findFirst por toList e veja todos os elementos passarem.
 */
public class StreamLaziness {
    public static List<String> run() {
        List<String> trace = new ArrayList<>();
        Stream.of(1, 2, 3)
            .peek(i -> trace.add("peek " + i))
            .filter(i -> i > 1)
            .findFirst();
        List<String> out = new ArrayList<>();
        out.add("findFirst -> " + trace);

        List<String> ordem = new ArrayList<>();
        Stream.of("a", "b").filter(s -> { ordem.add("filter " + s); return true; })
            .map(s -> { ordem.add("map " + s); return s; }).toList();
        out.add("um a um (sem coleção intermediária) -> " + ordem);

        List<String> sorted = new ArrayList<>();
        Stream.of(3, 1, 2).peek(i -> sorted.add("antes " + i)).sorted().peek(i -> sorted.add("depois " + i)).toList();
        out.add("sorted é stateful (barreira) -> " + sorted);

        List<String> limite = new ArrayList<>();
        Stream.iterate(1, i -> i + 1).peek(i -> limite.add("gera " + i)).limit(3).toList();
        out.add("limit em fonte infinita -> " + limite);
        return out;
    }

    public static void main(String[] args) { run().forEach(System.out::println); }
}
