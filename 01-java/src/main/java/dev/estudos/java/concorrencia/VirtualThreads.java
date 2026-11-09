package dev.estudos.java.concorrencia;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

/**
 * J12-09 · Virtual threads: 1000 tarefas bloqueantes, carrier, Semaphore para limitar recursos, sem pool.
 * Documento: java-docs/01-java/12-concorrencia.md §3.4.
 *
 * <p>Tempos variam por máquina; o teste afirma a soma (499500), não a duração.
 *
 * <p>Saída real (JDK 25):
 * <pre>
 * soma de 1000 tarefas bloqueantes -&gt; 499500
 * terminou bem antes de 1000*10ms=10s (muito menos) -&gt; true
 * é daemon por definição -&gt; true
 * máximo simultâneo com Semaphore(5) -&gt; true
 * tarefa roda em thread -&gt; virtual
 * </pre>
 */
public class VirtualThreads {
    public static List<String> run() throws Exception {
        List<String> out = new ArrayList<>();
        long ini = System.nanoTime();
        long sum = 0;
        try (var ex = Executors.newVirtualThreadPerTaskExecutor()) {
            List<Future<Integer>> fs = new ArrayList<>();
            for (int i = 0; i < 1000; i++) {
                int n = i;
                fs.add(ex.submit(() -> { Thread.sleep(10); return n; }));
            }
            for (var f : fs) sum += f.get();
        }
        long ms = (System.nanoTime() - ini) / 1_000_000;
        out.add("soma de 1000 tarefas bloqueantes -> " + sum);
        out.add("terminou bem antes de 1000*10ms=10s (muito menos) -> " + (ms < 5_000));

        Thread v = Thread.ofVirtual().name("v-1").start(() -> { });
        v.join();
        out.add("é daemon por definição -> " + v.isDaemon());

        // Limite de recurso (ex.: conexões de banco) com Semaphore, não com tamanho de pool
        Semaphore conexoes = new Semaphore(5);
        int[] atual = {0}, maximo = {0};
        Object trava = new Object();
        try (var ex = Executors.newVirtualThreadPerTaskExecutor()) {
            for (int i = 0; i < 100; i++) {
                ex.submit(() -> {
                    conexoes.acquire();
                    try {
                        synchronized (trava) { atual[0]++; maximo[0] = Math.max(maximo[0], atual[0]); }
                        Thread.sleep(5);
                        synchronized (trava) { atual[0]--; }
                    } finally { conexoes.release(); }
                    return null;
                });
            }
        }
        out.add("máximo simultâneo com Semaphore(5) -> " + (maximo[0] <= 5));

        String nome = Executors.newVirtualThreadPerTaskExecutor().submit(() -> Thread.currentThread().isVirtual()).get() ? "virtual" : "platform";
        out.add("tarefa roda em thread -> " + nome);
        return out;
    }

    public static void main(String[] args) throws Exception { run().forEach(System.out::println); }
}
