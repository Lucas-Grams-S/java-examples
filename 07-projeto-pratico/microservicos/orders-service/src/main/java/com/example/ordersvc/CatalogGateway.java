package com.example.ordersvc;

import java.util.concurrent.atomic.AtomicInteger;
import org.springframework.cloud.client.circuitbreaker.CircuitBreaker;
import org.springframework.cloud.client.circuitbreaker.CircuitBreakerFactory;
import org.springframework.resilience.annotation.Retryable;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;

/**
 * PP12: a chamada orders → catalog. Camadas, de fora para dentro: {@code @Retryable} (falhas transitórias),
 * circuit breaker (para de chamar quando o catalog está mal) e o timeout do cliente HTTP.
 */
@Component
class CatalogGateway {
    private final CatalogClient client;
    private final CircuitBreaker breaker;
    private final AtomicInteger chamadasAoCatalog = new AtomicInteger();

    /** Por método: o bean é proxiado (CGLIB) por causa do @Retryable; campo lido no proxy seria nulo. */
    int chamadas() { return chamadasAoCatalog.get(); }

    CatalogGateway(CatalogClient client, CircuitBreakerFactory<?, ?> factory) {
        this.client = client; this.breaker = factory.create("catalog");
    }

    /** Só repete erro 5xx e de I/O/timeout; 404 (produto inexistente) NÃO é transitório. */
    @Retryable(includes = {HttpServerErrorException.class, ResourceAccessException.class}, maxRetries = 2, delay = 50)
    CatalogClient.Product find(long id) {
        return breaker.run(() -> { chamadasAoCatalog.incrementAndGet(); return client.product(id); });
    }
}
