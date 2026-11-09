package dev.estudos.java.concorrencia;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

/**
 * J12-03 · CompletableFuture: thenCombine, thenCompose, exceptionally/handle, allOf e orTimeout.
 * Documento: java-docs/01-java/12-concorrencia.md §2.3.
 *
 * <p>Saída real (JDK 25):
 * <pre>
 * thenCombine -&gt; 42
 * thenApply (map) -&gt; 40
 * thenCompose (flatMap) -&gt; 21
 * exceptionally -&gt; -1
 * handle -&gt; CompletionException/IllegalStateException
 * join -&gt; CompletionException(IllegalStateException)
 * get -&gt; ExecutionException(IllegalStateException)
 * allOf concluído -&gt; true
 * thread da etapa com executor próprio -&gt; io-pool
 * sem executor usa o commonPool -&gt; true
 * orTimeout -&gt; TimeoutException
 * completeOnTimeout -&gt; padrão
 * </pre>
 */
public class CompletableFutures {
    public static List<String> run() throws Exception {
        List<String> out = new ArrayList<>();
        CompletableFuture<Integer> a = CompletableFuture.supplyAsync(() -> 20);
        CompletableFuture<Integer> b = CompletableFuture.supplyAsync(() -> 22);
        out.add("thenCombine -> " + a.thenCombine(b, Integer::sum).exceptionally(t -> -1).get());
        out.add("thenApply (map) -> " + a.thenApply(x -> x * 2).get());
        out.add("thenCompose (flatMap) -> " + a.thenCompose(x -> CompletableFuture.supplyAsync(() -> x + 1)).get());

        CompletableFuture<Integer> falha = CompletableFuture.supplyAsync(() -> { throw new IllegalStateException("x"); });
        out.add("exceptionally -> " + falha.exceptionally(t -> -1).get());
        out.add("handle -> " + falha.handle((v, t) -> t == null ? "ok" : t.getClass().getSimpleName() + "/" + t.getCause().getClass().getSimpleName()).get());
        try { falha.join(); } catch (CompletionException e) { out.add("join -> CompletionException(" + e.getCause().getClass().getSimpleName() + ")"); }
        try { falha.get(); } catch (ExecutionException e) { out.add("get -> ExecutionException(" + e.getCause().getClass().getSimpleName() + ")"); }

        var todas = CompletableFuture.allOf(a, b);
        todas.join();
        out.add("allOf concluído -> " + todas.isDone());

        // Executor próprio: a etapa roda numa thread nossa (não no commonPool)
        try (ExecutorService io = Executors.newFixedThreadPool(1, r -> Thread.ofPlatform().name("io-pool").unstarted(r))) {
            String thread = CompletableFuture.supplyAsync(() -> Thread.currentThread().getName(), io).get();
            out.add("thread da etapa com executor próprio -> " + thread);
        }
        String comum = CompletableFuture.supplyAsync(() -> Thread.currentThread().getName()).get();
        out.add("sem executor usa o commonPool -> " + comum.startsWith("ForkJoinPool.commonPool"));

        CompletableFuture<String> lento = new CompletableFuture<>();   // nunca completa
        try { lento.orTimeout(50, TimeUnit.MILLISECONDS).join(); }
        catch (CompletionException e) { out.add("orTimeout -> " + e.getCause().getClass().getSimpleName()); }
        out.add("completeOnTimeout -> " + new CompletableFuture<String>().completeOnTimeout("padrão", 50, TimeUnit.MILLISECONDS).get());
        return out;
    }

    public static void main(String[] args) throws Exception { run().forEach(System.out::println); }
}
