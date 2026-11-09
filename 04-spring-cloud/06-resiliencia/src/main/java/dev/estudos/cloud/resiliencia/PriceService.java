package dev.estudos.cloud.resiliencia;

import java.util.concurrent.atomic.AtomicInteger;
import org.springframework.resilience.annotation.ConcurrencyLimit;
import org.springframework.resilience.annotation.Retryable;

/** CL06-12: resiliência nativa do Framework 7. Estado exposto por MÉTODOS (o bean é proxiado por CGLIB). */
public class PriceService {
    private final AtomicInteger tentativas = new AtomicInteger();
    private final AtomicInteger atual = new AtomicInteger();
    private final AtomicInteger maximo = new AtomicInteger();

    /** Falha nas 2 primeiras tentativas e funciona na 3ª. */
    @Retryable(maxRetries = 3, delay = 50, multiplier = 2)
    public String fetchPrice() {
        int n = tentativas.incrementAndGet();
        if (n < 3) throw new IllegalStateException("falha transitória " + n);
        return "ok na tentativa " + n;
    }

    /** No máximo 2 execuções simultâneas; o excesso espera. */
    @ConcurrencyLimit(2)
    public void generateReport() throws InterruptedException {
        int n = atual.incrementAndGet();
        maximo.accumulateAndGet(n, Math::max);
        try { Thread.sleep(200); } finally { atual.decrementAndGet(); }
    }

    public int tentativas() { return tentativas.get(); }
    public int maximoSimultaneo() { return maximo.get(); }
}
