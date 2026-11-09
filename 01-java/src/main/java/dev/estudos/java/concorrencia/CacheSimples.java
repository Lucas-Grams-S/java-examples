package dev.estudos.java.concorrencia;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.LongAdder;

/**
 * J12-15 · Cache/contadores seguros com ConcurrentHashMap: computeIfAbsent e merge atômicos
 * (vs o if-containsKey-put, que é corrida); chaves/valores null proibidos.
 * Documento: java-docs/01-java/12-concorrencia.md §5.4 (e §2.5).
 *
 * <p>Saída real (JDK 25):
 * <pre>
 * hits de /orders -&gt; 80000
 * cálculos executados para a mesma chave -&gt; 1
 * merge (contagem) -&gt; &#123;a=3, b=2, c=1&#125;
 * valor null -&gt; NullPointerException
 * chave null -&gt; NullPointerException
 * </pre>
 */
public class CacheSimples {
    public static List<String> run() throws Exception {
        List<String> out = new ArrayList<>();

        ConcurrentHashMap<String, LongAdder> hits = new ConcurrentHashMap<>();
        Thread[] ts = new Thread[8];
        for (int i = 0; i < ts.length; i++) {
            ts[i] = Thread.ofPlatform().start(() -> {
                for (int k = 0; k < 10_000; k++) hits.computeIfAbsent("/orders", key -> new LongAdder()).increment();
            });
        }
        for (Thread t : ts) t.join();
        out.add("hits de /orders -> " + hits.get("/orders").sum());

        // computeIfAbsent: a função cara roda uma única vez por chave, mesmo com várias threads
        ConcurrentHashMap<String, String> cache = new ConcurrentHashMap<>();
        AtomicInteger calculos = new AtomicInteger();
        for (int i = 0; i < ts.length; i++) {
            ts[i] = Thread.ofPlatform().start(() -> cache.computeIfAbsent("k", key -> { calculos.incrementAndGet(); return "valor"; }));
        }
        for (Thread t : ts) t.join();
        out.add("cálculos executados para a mesma chave -> " + calculos.get());

        ConcurrentHashMap<String, Integer> palavras = new ConcurrentHashMap<>();
        for (String p : "a b a c a b".split(" ")) palavras.merge(p, 1, Integer::sum);
        out.add("merge (contagem) -> " + new java.util.TreeMap<>(palavras));

        try { cache.put("x", null); } catch (NullPointerException e) { out.add("valor null -> NullPointerException"); }
        try { cache.put(null, "x"); } catch (NullPointerException e) { out.add("chave null -> NullPointerException"); }
        return out;
    }

    public static void main(String[] args) throws Exception { run().forEach(System.out::println); }
}
