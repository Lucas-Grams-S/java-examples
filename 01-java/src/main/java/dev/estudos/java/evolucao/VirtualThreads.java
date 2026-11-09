package dev.estudos.java.evolucao;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * J00-08 · Java 21: virtual threads.
 * Documento: java-docs/01-java/00-evolucao-do-java.md §4.8.
 *
 * <p>10.000 tarefas que bloqueiam 100 ms cada, uma virtual thread por tarefa. Com um pool de 200 threads de plataforma
 * levaria ≈ 5 s; com virtual threads, bem menos (o tempo varia por máquina).
 *
 * <p>Saída real (JDK 25; o tempo varia):
 * <pre>
 * tarefas -&gt; 10000
 * executadas em virtual threads -&gt; 10000
 * tempo total &lt; 3 s (um pool de 200 levaria ~5 s) -&gt; true
 * </pre>
 *
 * <p>Variações: troque por {@code Executors.newFixedThreadPool(200)} e compare o tempo.
 */
public class VirtualThreads {
    public static List<String> run(int tasks) throws Exception {
        AtomicInteger virtual = new AtomicInteger();
        long t0 = System.nanoTime();
        try (var exec = Executors.newVirtualThreadPerTaskExecutor()) {
            List<Future<?>> futures = new ArrayList<>();
            for (int i = 0; i < tasks; i++) {
                futures.add(exec.submit(() -> {
                    if (Thread.currentThread().isVirtual()) virtual.incrementAndGet();
                    Thread.sleep(Duration.ofMillis(100));     // bloqueia: desmonta da carrier thread
                    return null;
                }));
            }
            for (var f : futures) f.get();
        }
        long ms = (System.nanoTime() - t0) / 1_000_000;
        return List.of("tarefas -> " + tasks, "executadas em virtual threads -> " + virtual.get(),
                "tempo total < 3 s (um pool de 200 levaria ~5 s) -> " + (ms < 3000));
    }

    public static void main(String[] args) throws Exception {
        run(10_000).forEach(System.out::println);
    }
}
