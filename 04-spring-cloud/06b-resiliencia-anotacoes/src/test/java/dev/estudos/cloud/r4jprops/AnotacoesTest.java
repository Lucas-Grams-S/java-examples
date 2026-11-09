package dev.estudos.cloud.r4jprops;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.github.resilience4j.bulkhead.BulkheadFullException;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.ratelimiter.RequestNotPermitted;
import java.util.concurrent.CompletableFuture;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

/** CL06-14: anotações do Resilience4j + propriedades (resilience4j-spring-boot4 2.4.0). */
@SpringBootTest(classes = AnotacoesApp.class, properties = "spring.config.import=classpath:r4jprops.yaml")
class AnotacoesTest {
    @Autowired AnotacoesApp.Servico svc;
    @Autowired CircuitBreakerRegistry breakers;

    @Test void cl0614_circuitBreakerPorAnotacaoAbreEUsaFallback() {
        svc.setFalhando(true);
        for (int i = 0; i < 4; i++) assertThat(svc.comBreaker()).isEqualTo("FALLBACK(IllegalStateException)");
        assertThat(breakers.circuitBreaker("backend").getState()).isEqualTo(CircuitBreaker.State.OPEN);
        int antes = svc.chamadas();
        assertThat(svc.comBreaker()).isEqualTo("FALLBACK(CallNotPermittedException)");   // aberto: nem chama
        assertThat(svc.chamadas()).isEqualTo(antes);
    }

    @Test void cl0614_retryPorPropriedades() {
        assertThat(svc.comRetry()).isEqualTo("ok na tentativa 3");
    }

    @Test void cl0614_rateLimiterEBulkhead() {
        assertThat(svc.comLimite()).isEqualTo("ok");
        assertThat(svc.comLimite()).isEqualTo("ok");
        assertThatThrownBy(svc::comLimite).isInstanceOf(RequestNotPermitted.class);

        var lenta = CompletableFuture.supplyAsync(() -> { try { return svc.comBulkhead(); } catch (Exception e) { throw new RuntimeException(e); } });
        try { Thread.sleep(100); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
        assertThatThrownBy(svc::comBulkhead).isInstanceOf(BulkheadFullException.class);
        assertThat(lenta.join()).isEqualTo("ok");
    }
}
