package dev.estudos.java.concorrencia;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * J12-12 · O bug clássico: n++ não é atômico. Contador sem sincronização perde atualizações;
 * AtomicInteger e synchronized chegam a 400000.
 * Documento: java-docs/01-java/12-concorrencia.md §5.1.
 *
 * <p>O valor do contador "racy" varia a cada execução (quase sempre &lt; 400000); o teste afirma só o invariante.
 *
 * <p>Saída real (JDK 25):
 * <pre>
 * sem sincronização (varia) -&gt; 105127 ; perdeu atualizações: true
 * AtomicInteger -&gt; 400000
 * synchronized -&gt; 400000
 * </pre>
 */
public class ContadorSemSync {
    static class Racy { static int n; }
    static final Object LOCK = new Object();
    static int sync;

    static void roda(Runnable corpo) throws InterruptedException {
        Thread[] ts = new Thread[4];
        for (int i = 0; i < 4; i++) ts[i] = Thread.ofPlatform().start(() -> { for (int k = 0; k < 100_000; k++) corpo.run(); });
        for (Thread t : ts) t.join();
    }

    public static List<String> run() throws Exception {
        List<String> out = new ArrayList<>();
        Racy.n = 0;
        roda(() -> Racy.n++);
        out.add("sem sincronização (varia) -> " + Racy.n + " ; perdeu atualizações: " + (Racy.n < 400_000));
        AtomicInteger at = new AtomicInteger();
        roda(at::incrementAndGet);
        out.add("AtomicInteger -> " + at.get());
        sync = 0;
        roda(() -> { synchronized (LOCK) { sync++; } });
        out.add("synchronized -> " + sync);
        return out;
    }

    public static void main(String[] args) throws Exception { run().forEach(System.out::println); }
}
