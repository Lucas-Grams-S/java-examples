package dev.estudos.java.concorrencia;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * J12-06 · JMM: volatile garante visibilidade (não atomicidade); join é aresta happens-before; holder idiom.
 * Documento: java-docs/01-java/12-concorrencia.md §3.1.
 *
 * <p>O caso "sem volatile o laço nunca termina" depende de JIT/CPU e pode não reproduzir; por isso é mostrado
 * com tempo-limite e o teste afirma só o invariante (com volatile sempre termina).
 *
 * <p>Saída real (JDK 25):
 * <pre>
 * volatile: laço terminou -&gt; true
 * sem volatile: terminou no prazo? (varia conforme o JIT) -&gt; false
 * volatile int com ++ perdeu atualizações? -&gt; true (valor 204435)
 * AtomicInteger -&gt; 400000
 * visível após join -&gt; 42
 * holder idiom devolve a mesma instância -&gt; true
 * </pre>
 */
public class JavaMemoryModel {
    static class Flag {
        private volatile boolean stop;
        long run() { long i = 0; while (!stop) i++; return i; }
        void halt() { stop = true; }
    }

    static class NaoVolatil {
        boolean stop;   // sem volatile: sem garantia de visibilidade
        void run() { while (!stop) { } }
    }

    static class Lazy {
        private Lazy() { }
        private static class Holder { static final Lazy INSTANCE = new Lazy(); }   // holder idiom: inicialização preguiçosa e segura
        static Lazy get() { return Holder.INSTANCE; }
    }

    static volatile int contadorVolatil;
    static int comum;

    public static List<String> run() throws Exception {
        List<String> out = new ArrayList<>();

        Flag f = new Flag();
        Thread t = Thread.ofPlatform().start(f::run);
        Thread.sleep(50);
        f.halt();
        t.join(2000);
        out.add("volatile: laço terminou -> " + !t.isAlive());

        NaoVolatil nv = new NaoVolatil();
        Thread d = Thread.ofPlatform().daemon().start(nv::run);
        Thread.sleep(100);
        nv.stop = true;
        d.join(500);
        out.add("sem volatile: terminou no prazo? (varia conforme o JIT) -> " + !d.isAlive());

        // volatile NÃO torna count++ atômico
        Thread[] ts = new Thread[4];
        for (int i = 0; i < 4; i++) ts[i] = Thread.ofPlatform().start(() -> { for (int k = 0; k < 100_000; k++) contadorVolatil++; });
        for (Thread x : ts) x.join();
        out.add("volatile int com ++ perdeu atualizações? -> " + (contadorVolatil < 400_000) + " (valor " + contadorVolatil + ")");
        AtomicInteger at = new AtomicInteger();
        for (int i = 0; i < 4; i++) ts[i] = Thread.ofPlatform().start(() -> { for (int k = 0; k < 100_000; k++) at.incrementAndGet(); });
        for (Thread x : ts) x.join();
        out.add("AtomicInteger -> " + at.get());

        // join() é happens-before: o que a thread escreveu é visível depois do join, mesmo sem volatile
        Thread w = Thread.ofPlatform().start(() -> comum = 42);
        w.join();
        out.add("visível após join -> " + comum);

        out.add("holder idiom devolve a mesma instância -> " + (Lazy.get() == Lazy.get()));
        return out;
    }

    public static void main(String[] args) throws Exception { run().forEach(System.out::println); }
}
