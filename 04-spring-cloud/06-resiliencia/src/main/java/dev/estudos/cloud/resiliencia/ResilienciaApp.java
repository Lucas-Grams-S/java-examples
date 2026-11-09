package dev.estudos.cloud.resiliencia;

import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.timelimiter.TimeLimiterConfig;
import java.time.Duration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.circuitbreaker.resilience4j.Resilience4JCircuitBreakerFactory;
import org.springframework.cloud.circuitbreaker.resilience4j.Resilience4JConfigBuilder;
import org.springframework.cloud.client.circuitbreaker.CircuitBreakerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.cloud.client.circuitbreaker.Customizer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.resilience.annotation.EnableResilientMethods;

/**
 * CL06-06 / CL06-10: circuit breaker (janela de 4, 50%, 2 s aberto, timeout 300 ms) e
 * CL06-12: {@code @Retryable}/{@code @ConcurrencyLimit} nativos do Framework 7.
 * O {@code main} roda a simulação do doc §5.2 e imprime a linha de cada chamada.
 */
@SpringBootApplication
@EnableResilientMethods
public class ResilienciaApp {

    @Bean Customizer<Resilience4JCircuitBreakerFactory> defaults() {
        return f -> f.configureDefault(id -> new Resilience4JConfigBuilder(id)
            .circuitBreakerConfig(CircuitBreakerConfig.custom()
                .slidingWindowSize(4).minimumNumberOfCalls(4).failureRateThreshold(50)
                .waitDurationInOpenState(Duration.ofSeconds(2))
                .permittedNumberOfCallsInHalfOpenState(2).build())
            .timeLimiterConfig(TimeLimiterConfig.custom().timeoutDuration(Duration.ofMillis(300)).build())
            .build());
    }

    @Bean Catalogo catalogo() { return new Catalogo(); }

    @Bean CatalogGateway catalogGateway(Catalogo c, CircuitBreakerFactory<?, ?> f) { return new CatalogGateway(c, f); }

    @Bean PriceService priceService() { return new PriceService(); }

    public static void main(String[] args) throws Exception {
        try (ConfigurableApplicationContext ctx = SpringApplication.run(ResilienciaApp.class, args)) {
            var cat = ctx.getBean(Catalogo.class);
            var gw = ctx.getBean(CatalogGateway.class);
            cat.falhando.set(true);
            for (int i = 1; i <= 7; i++) linha(i, gw);
            System.out.println("-- espera 2,2s (open -> half-open), catalog volta");
            Thread.sleep(2200);
            cat.falhando.set(false);
            for (int i = 8; i <= 11; i++) linha(i, gw);
            System.out.println("-- catalog lento (1s) com timeout 300ms");
            cat.atrasoMs.set(1000);
            for (int i = 12; i <= 13; i++) linha(i, gw);
            var p = ctx.getBean(PriceService.class);
            System.out.println("retry: " + p.fetchPrice() + " (tentativas=" + p.tentativas() + ")");
        }
    }

    private static void linha(int i, CatalogGateway gw) {
        long t = System.nanoTime();
        var r = gw.product(i);
        System.out.println("chamada " + i + " -> " + r.get("name") + " (" + (System.nanoTime() - t) / 1_000_000 + " ms)");
    }
}
