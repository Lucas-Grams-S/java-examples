package dev.estudos.cloud.r4jprops;

import io.github.resilience4j.bulkhead.annotation.Bulkhead;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import io.github.resilience4j.retry.annotation.Retry;
import java.util.concurrent.atomic.AtomicInteger;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

/**
 * CL06-14: Resilience4j por anotação ({@code @CircuitBreaker}, {@code @Retry}, {@code @RateLimiter}, {@code @Bulkhead})
 * configurado por propriedades {@code resilience4j.*.instances.<nome>.*} (artefato {@code resilience4j-spring-boot4}).
 * Módulo próprio: o {@code resilience4j-spring-boot4} só existe na 2.4.0 (o BOM do Spring Cloud traz a 2.3.0) e a 2.4.0 muda
 * a exceção do timeout no CL06-10 (ver DIVERGENCIAS.md).
 */
@SpringBootApplication
public class AnotacoesApp {

    public static class Servico {
        private final AtomicInteger chamadas = new AtomicInteger();
        private final AtomicInteger tentativas = new AtomicInteger();
        public int chamadas() { return chamadas.get(); }
        private volatile boolean falhando;

        /** Estado por método: o bean é proxiado por CGLIB, um campo público seria lido/escrito no proxy, não no alvo. */
        public void setFalhando(boolean f) { this.falhando = f; }

        @CircuitBreaker(name = "backend", fallbackMethod = "alternativa")
        public String comBreaker() {
            chamadas.incrementAndGet();
            if (falhando) throw new IllegalStateException("backend fora do ar");
            return "ok";
        }

        String alternativa(Throwable t) { return "FALLBACK(" + t.getClass().getSimpleName() + ")"; }

        @Retry(name = "flaky")
        public String comRetry() {
            int n = tentativas.incrementAndGet();
            if (n < 3) throw new IllegalStateException("transitória");
            return "ok na tentativa " + n;
        }

        @RateLimiter(name = "limitado")
        public String comLimite() { return "ok"; }

        @Bulkhead(name = "paredao", type = Bulkhead.Type.SEMAPHORE)
        public String comBulkhead() throws InterruptedException { Thread.sleep(300); return "ok"; }
    }

    @Bean Servico servico() { return new Servico(); }
}
