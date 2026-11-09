package dev.estudos.padroes.antipadroes;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * DP08-14 · Bean singleton com estado mutável: um campo compartilhado perde atualizações; o resultado varia.
 * Documento: java-docs/06-padroes-de-projeto/08-anti-padroes.md §5.4.
 *
 * <p>Saída real (JDK 25), um exemplo (o valor muda a cada execução):
 * <pre>
 * contador compartilhado: 584694 (esperado 800000)
 * com AtomicInteger: 800000
 * </pre>
 */
public class BeanSingletonComEstado {
    static class Counter { int n; }

    /** @return {valor sem sincronização, valor com AtomicInteger} */
    static int[] contar(int threads, int voltas) throws Exception {
        var shared = new Counter();                       // equivale a um bean singleton com um campo mutável
        var atomico = new AtomicInteger();
        var pool = Executors.newFixedThreadPool(threads);
        for (int i = 0; i < threads; i++) pool.submit(() -> {
            for (int k = 0; k < voltas; k++) { shared.n++; atomico.incrementAndGet(); }
        });
        pool.shutdown();
        pool.awaitTermination(30, TimeUnit.SECONDS);
        return new int[] { shared.n, atomico.get() };
    }

    public static List<String> run() throws Exception {
        List<String> out = new ArrayList<>();
        int[] r = contar(8, 100_000);
        out.add("contador compartilhado: " + r[0] + " (esperado 800000)");
        out.add("com AtomicInteger: " + r[1]);
        return out;
    }

    public static void main(String[] args) throws Exception { run().forEach(System.out::println); }
}
