package dev.estudos.java.colecoes;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * J07-15 · Chave mutável quebra o {@code HashMap}.
 * Documento: java-docs/01-java/07-colecoes.md §5.5.
 *
 * <p>Saída real (JDK 25):
 * <pre>
 * antes de mutar: get -&gt; v
 * depois de key.add(2): get(key) -&gt; null
 * containsKey(List.of(1)) -&gt; false
 * a entrada continua lá: size -&gt; 1
 * </pre>
 *
 * <p>Variações: use {@code List.of(1)} (imutável) como chave e mude o resto do programa para não precisar mutá-la.
 */
public class MutableKey {
    public static List<String> run() {
        List<String> out = new ArrayList<>();
        var key = new ArrayList<>(List.of(1));
        var map = new HashMap<List<Integer>, String>();
        map.put(key, "v");
        out.add("antes de mutar: get -> " + map.get(key));
        key.add(2);   // muda o hashCode da chave
        out.add("depois de key.add(2): get(key) -> " + map.get(key));
        out.add("containsKey(List.of(1)) -> " + map.containsKey(List.of(1)));
        out.add("a entrada continua lá: size -> " + map.size());
        return out;
    }

    public static void main(String[] args) { run().forEach(System.out::println); }
}
