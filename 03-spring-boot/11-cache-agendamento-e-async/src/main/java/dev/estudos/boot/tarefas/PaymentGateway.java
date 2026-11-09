package dev.estudos.boot.tarefas;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;
import org.springframework.resilience.annotation.ConcurrencyLimit;
import org.springframework.resilience.annotation.Retryable;
import org.springframework.stereotype.Service;

/** SB11-04/15: {@code @Retryable} e {@code @ConcurrencyLimit} (Spring Framework 7). */
@Service
public class PaymentGateway {
    final AtomicInteger attempts = new AtomicInteger();
    final AtomicInteger running = new AtomicInteger();
    final AtomicInteger maxRunning = new AtomicInteger();
    volatile int failUntil = 2;

    @Retryable(maxRetries = 3, delay = 10, multiplier = 2, includes = IOException.class)
    public String charge() throws IOException {
        if (attempts.incrementAndGet() <= failUntil) throw new IOException("falha " + attempts.get());
        return "ok na tentativa " + attempts.get();
    }

    @ConcurrencyLimit(2)
    public void heavy() throws InterruptedException {
        maxRunning.accumulateAndGet(running.incrementAndGet(), Math::max);
        Thread.sleep(100);
        running.decrementAndGet();
    }

    /** Estado por MÉTODO: o bean é proxy CGLIB e campos do proxy ficam nulos (SB11-05c). */
    public int attempts() { return attempts.get(); }
    public int maxRunning() { return maxRunning.get(); }

    public void reset(int failUntil) { attempts.set(0); this.failUntil = failUntil; }
}
