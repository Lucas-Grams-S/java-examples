package dev.estudos.java.funcional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

/**
 * J09-09 · Streams paralelos: forEach em lista não thread-safe vs toList; resultado e threads.
 * Documento: java-docs/01-java/09-programacao-funcional.md §3.3.
 *
 * <p>Saída real (JDK 25):
 * <pre>
 * toList paralelo -&gt; tamanho 1000, ordem preservada true
 * soma paralela (associativa) -&gt; 500500
 * forEach em ArrayList (indefinido; varia a cada execução) -&gt; perdeu elementos
 * pool comum -&gt; paralelismo 11 (cpus 12 - 1)
 * </pre>
 *
 * <p>Não determinístico: a linha do forEach e o paralelismo do pool (CPUs - 1) variam por máquina/execução.
 *
 * <p>Variações: aumente o intervalo e execute várias vezes: o tamanho da lista insegura varia.
 */
public class ParallelStreams {
    public static List<String> run() {
        List<String> out = new ArrayList<>();
        List<Integer> ok = IntStream.range(0, 1000).parallel().boxed().toList();
        out.add("toList paralelo -> tamanho " + ok.size() + ", ordem preservada " + ok.equals(IntStream.range(0, 1000).boxed().toList()));
        out.add("soma paralela (associativa) -> " + IntStream.rangeClosed(1, 1000).parallel().sum());
        // forEach com coleção não thread-safe: resultado indefinido (perde elementos ou lança exceção)
        List<Integer> unsafe = new ArrayList<>();
        String resultado;
        try {
            IntStream.range(0, 100_000).parallel().forEach(unsafe::add);
            resultado = unsafe.size() == 100_000 ? "completo (sorte)" : "perdeu elementos";
        } catch (RuntimeException e) {
            resultado = "lançou " + e.getClass().getSimpleName();
        }
        out.add("forEach em ArrayList (indefinido; varia a cada execução) -> " + resultado);
        out.add("pool comum -> paralelismo " + java.util.concurrent.ForkJoinPool.commonPool().getParallelism()
                + " (cpus " + Runtime.getRuntime().availableProcessors() + " - 1)");
        return out;
    }

    public static void main(String[] args) { run().forEach(System.out::println); }
}
