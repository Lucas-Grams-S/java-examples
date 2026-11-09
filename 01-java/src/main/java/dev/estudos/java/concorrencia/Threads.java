package dev.estudos.java.concorrencia;

import java.util.ArrayList;
import java.util.List;

/**
 * J12-01 · Threads: platform vs virtual, join, estados e interrupção cooperativa.
 * Documento: java-docs/01-java/12-concorrencia.md §2.1.
 *
 * <p>Variações: remova o {@code interrupt()} dentro do catch e veja a tarefa não saber que foi interrompida.
 *
 * <p>Saída real (JDK 25):
 * <pre>
 * estado antes de start -&gt; NEW
 * nome -&gt; worker-1
 * estado depois de join -&gt; TERMINATED
 * virtual? -&gt; true
 * platform é virtual? -&gt; false
 * estado dormindo -&gt; TIMED_WAITING
 * flag restaurado no catch -&gt; true
 * </pre>
 */
public class Threads {
    public static List<String> run() throws Exception {
        List<String> out = new ArrayList<>();
        Thread t = Thread.ofPlatform().name("worker-1").unstarted(() -> { });
        out.add("estado antes de start -> " + t.getState());
        t.start();
        t.join();
        out.add("nome -> " + t.getName());
        out.add("estado depois de join -> " + t.getState());

        Thread v = Thread.ofVirtual().start(() -> { });
        v.join();
        out.add("virtual? -> " + v.isVirtual());
        out.add("platform é virtual? -> " + t.isVirtual());

        // Interrupção: sleep lança InterruptedException e LIMPA o flag; a tarefa deve restaurá-lo.
        boolean[] restaurou = {false};
        Thread sleeper = Thread.ofPlatform().start(() -> {
            try { Thread.sleep(10_000); }
            catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                restaurou[0] = Thread.currentThread().isInterrupted();
            }
        });
        while (sleeper.getState() != Thread.State.TIMED_WAITING) Thread.onSpinWait();
        out.add("estado dormindo -> " + sleeper.getState());
        sleeper.interrupt();
        sleeper.join();
        out.add("flag restaurado no catch -> " + restaurou[0]);
        return out;
    }

    public static void main(String[] args) throws Exception { run().forEach(System.out::println); }
}
