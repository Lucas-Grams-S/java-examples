package dev.estudos.cloud.resiliencia;

import java.util.Map;
import org.springframework.cloud.client.circuitbreaker.CircuitBreaker;
import org.springframework.cloud.client.circuitbreaker.CircuitBreakerFactory;

/** CL06-10: cliente de catalog protegido por circuit breaker (abstração do Spring Cloud). */
public class CatalogGateway {
    private final Catalogo client;
    private final CircuitBreaker breaker;

    public CatalogGateway(Catalogo client, CircuitBreakerFactory<?, ?> factory) {
        this.client = client;
        this.breaker = factory.create("catalog");
    }

    /** Devolve o produto, ou "FALLBACK(tipo da exceção)" no nome. */
    public Map<String, Object> product(long id) {
        return breaker.run(() -> client.product(id),
            ex -> Map.of("id", id, "name", "FALLBACK(" + ex.getClass().getSimpleName() + ")"));
    }
}
