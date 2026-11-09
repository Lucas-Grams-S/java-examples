package dev.estudos.java.concorrencia;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * J12-02 · Executores: submit/Future, ExecutionException, close() que espera, fila limitada com rejeição explícita.
 * Documento: java-docs/01-java/12-concorrencia.md §2.2.
 *
 * <p>Saída real (JDK 25):
 * <pre>
 * get -&gt; 42
 * falha embrulhada -&gt; ExecutionException(boom)
 * tarefas concluídas após close -&gt; 6
 * terceira tarefa -&gt; RejectedExecutionException
 * threads nomeadas -&gt; api-1 criada: true
 * </pre>
 */
public class Executores {
    public static List<String> run() throws Exception {
        List<String> out = new ArrayList<>();

        try (ExecutorService pool = Executors.newFixedThreadPool(4)) {
            Future<Integer> f = pool.submit(() -> 21 * 2);
            out.add("get -> " + f.get());
            Future<Integer> falha = pool.submit(() -> { throw new IllegalStateException("boom"); });
            try { falha.get(); }
            catch (ExecutionException e) { out.add("falha embrulhada -> ExecutionException(" + e.getCause().getMessage() + ")"); }
        }

        // close() espera as tarefas já submetidas terminarem
        AtomicInteger feitas = new AtomicInteger();
        try (ExecutorService pool = Executors.newFixedThreadPool(2)) {
            for (int i = 0; i < 6; i++) pool.submit(() -> { sleep(20); feitas.incrementAndGet(); });
        }
        out.add("tarefas concluídas após close -> " + feitas.get());

        // Produção: fila LIMITADA + política de rejeição explícita + threads com nome
        AtomicInteger seq = new AtomicInteger();
        ThreadFactory tf = r -> Thread.ofPlatform().name("api-" + seq.incrementAndGet()).unstarted(r);
        CountDownLatch segura = new CountDownLatch(1);
        ThreadPoolExecutor tpe = new ThreadPoolExecutor(1, 1, 0, TimeUnit.SECONDS,
            new ArrayBlockingQueue<>(1), tf, new ThreadPoolExecutor.AbortPolicy());
        try {
            Runnable bloqueada = () -> { try { segura.await(); } catch (InterruptedException e) { Thread.currentThread().interrupt(); } };
            tpe.execute(bloqueada);   // ocupa a única thread
            tpe.execute(bloqueada);   // ocupa a única vaga da fila
            try { tpe.execute(bloqueada); }
            catch (RejectedExecutionException e) { out.add("terceira tarefa -> RejectedExecutionException"); }
        } finally {
            segura.countDown();
            tpe.shutdown();
            tpe.awaitTermination(5, TimeUnit.SECONDS);
        }
        out.add("threads nomeadas -> api-1 criada: " + (seq.get() >= 1));
        return out;
    }

    static void sleep(long ms) { try { Thread.sleep(ms); } catch (InterruptedException e) { Thread.currentThread().interrupt(); } }

    public static void main(String[] args) throws Exception { run().forEach(System.out::println); }
}
