package dev.estudos.cloud.resiliencia;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.concurrent.Executors;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class ResilienciaTest {
    @Autowired Catalogo cat;
    @Autowired CatalogGateway gw;
    @Autowired PriceService price;

    private String nome(int i) { return (String) gw.product(i).get("name"); }

    /** CL06-06 + CL06-10: fecha -> abre -> half-open -> fecha, e timeout vira TimeoutException. */
    @Test
    void circuitBreakerCicloCompleto() throws Exception {
        cat.falhando.set(true);
        for (int i = 1; i <= 4; i++) assertThat(nome(i)).isEqualTo("FALLBACK(RuntimeException)");
        long antes = cat.chamadas.get();
        for (int i = 5; i <= 7; i++) assertThat(nome(i)).isEqualTo("FALLBACK(CallNotPermittedException)");
        assertThat(cat.chamadas.get()).as("circuito aberto não toca na rede").isEqualTo(antes);

        Thread.sleep(2200);
        cat.falhando.set(false);
        for (int i = 8; i <= 11; i++) assertThat(nome(i)).isEqualTo("produto");

        cat.atrasoMs.set(1000);
        long t = System.nanoTime();
        assertThat(nome(12)).isEqualTo("FALLBACK(TimeoutException)");
        assertThat((System.nanoTime() - t) / 1_000_000).isBetween(250L, 800L);
        cat.atrasoMs.set(0);
    }

    @Test
    void retryNativoTerceiraTentativa() {
        assertThat(price.fetchPrice()).isEqualTo("ok na tentativa 3");
        assertThat(price.tentativas()).isEqualTo(3);
    }

    /** O doc mediu 2 simultâneos e ~620 ms com 6 virtual threads de 200 ms. */
    @Test
    void concurrencyLimitMaximoDois() throws Exception {
        long t = System.nanoTime();
        try (var ex = Executors.newVirtualThreadPerTaskExecutor()) {
            var fs = new ArrayList<java.util.concurrent.Future<?>>();
            for (int i = 0; i < 6; i++) fs.add(ex.submit(() -> { price.generateReport(); return null; }));
            for (var f : fs) f.get();
        }
        assertThat(price.maximoSimultaneo()).isEqualTo(2);
        assertThat((System.nanoTime() - t) / 1_000_000).isGreaterThanOrEqualTo(550L);
    }
}
