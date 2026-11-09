package dev.estudos.java.concorrencia;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.LongAdder;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * J12-04 · Exclusão mútua: synchronized (reentrante), ReentrantLock com tryLock, AtomicInteger e LongAdder.
 * Documento: java-docs/01-java/12-concorrencia.md §2.4.
 *
 * <p>Saída real (JDK 25):
 * <pre>
 * synchronized -&gt; 400000
 * reentrante (incDuas) -&gt; 2
 * AtomicInteger -&gt; 400000
 * LongAdder -&gt; 400000
 * ReentrantLock -&gt; 400000
 * tryLock(50ms) com lock ocupado -&gt; false
 * tryLock com lock livre -&gt; true
 * compareAndSet(5-&gt;9) -&gt; true, compareAndSet(5-&gt;1) -&gt; false, valor 9
 * </pre>
 */
public class Exclusao {
    static class Counter {
        private int n;
        synchronized void inc() { n++; }
        synchronized int get() { return n; }
        synchronized int incDuas() { inc(); inc(); return n; }   // reentrante: mesmo monitor adquirido de novo
    }

    static void paralelo(int threads, int porThread, Runnable r) throws InterruptedException {
        Thread[] ts = new Thread[threads];
        for (int i = 0; i < threads; i++) {
            ts[i] = Thread.ofPlatform().start(() -> { for (int k = 0; k < porThread; k++) r.run(); });
        }
        for (Thread t : ts) t.join();
    }

    public static List<String> run() throws Exception {
        List<String> out = new ArrayList<>();
        Counter c = new Counter();
        paralelo(4, 100_000, c::inc);
        out.add("synchronized -> " + c.get());
        out.add("reentrante (incDuas) -> " + new Counter().incDuas());

        AtomicInteger at = new AtomicInteger();
        paralelo(4, 100_000, at::incrementAndGet);
        out.add("AtomicInteger -> " + at.get());

        LongAdder adder = new LongAdder();
        paralelo(4, 100_000, adder::increment);
        out.add("LongAdder -> " + adder.sum());

        Lock lock = new ReentrantLock();
        int[] n = {0};
        paralelo(4, 100_000, () -> { lock.lock(); try { n[0]++; } finally { lock.unlock(); } });
        out.add("ReentrantLock -> " + n[0]);

        // tryLock com timeout: outra thread segura o lock
        Lock ocupado = new ReentrantLock();
        Thread dono = Thread.ofPlatform().start(() -> { ocupado.lock(); try { Thread.sleep(300); } catch (InterruptedException e) { Thread.currentThread().interrupt(); } finally { ocupado.unlock(); } });
        while (!((ReentrantLock) ocupado).isLocked()) Thread.onSpinWait();
        out.add("tryLock(50ms) com lock ocupado -> " + ocupado.tryLock(50, TimeUnit.MILLISECONDS));
        dono.join();
        out.add("tryLock com lock livre -> " + ocupado.tryLock());
        ocupado.unlock();

        // CAS manual
        AtomicInteger cas = new AtomicInteger(5);
        out.add("compareAndSet(5->9) -> " + cas.compareAndSet(5, 9) + ", compareAndSet(5->1) -> " + cas.compareAndSet(5, 1) + ", valor " + cas.get());
        return out;
    }

    public static void main(String[] args) throws Exception { run().forEach(System.out::println); }
}
