package dev.estudos.java.colecoes;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * J07-14 · Cache LRU com {@code LinkedHashMap}.
 * Documento: java-docs/01-java/07-colecoes.md §5.4.
 *
 * <p>Não é seguro entre threads; para produção, prefira Caffeine.
 *
 * <p>Saída real (JDK 25):
 * <pre>
 * chaves após get(a) e put(c) -&gt; [a, c]
 * b foi despejada -&gt; true
 * </pre>
 *
 * <p>Variações: passe {@code false} como terceiro argumento do {@code super} (ordem de inserção: vira FIFO).
 */
public class LruCache<K, V> extends LinkedHashMap<K, V> {
    private final int max;

    public LruCache(int max) {
        super(16, 0.75f, true);   // true = ordem de acesso
        this.max = max;
    }

    @Override protected boolean removeEldestEntry(Map.Entry<K, V> eldest) { return size() > max; }

    public static List<String> run() {
        List<String> out = new ArrayList<>();
        var cache = new LruCache<String, Integer>(2);
        cache.put("a", 1);
        cache.put("b", 2);
        cache.get("a");   // 'a' passa a ser o mais recente
        cache.put("c", 3);   // estoura o limite: sai 'b' (o menos usado)
        out.add("chaves após get(a) e put(c) -> " + cache.keySet());
        out.add("b foi despejada -> " + !cache.containsKey("b"));
        return out;
    }

    public static void main(String[] args) { run().forEach(System.out::println); }
}
