package dev.estudos.boot.reativo;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webtestclient.autoconfigure.AutoConfigureWebTestClient;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
class ReativoTest {
    @Autowired WebTestClient web;
    @org.springframework.boot.test.web.server.LocalServerPort int port;

    @Test void sb0510_mono_e_sse() {
        web.get().uri("/orders/7").exchange().expectStatus().isOk().expectBody().jsonPath("$.id").isEqualTo(7);
        List<Order> sse = web.get().uri("/orders").accept(MediaType.TEXT_EVENT_STREAM).exchange()
            .returnResult(Order.class).getResponseBody().collectList().block(Duration.ofSeconds(5));
        assertThat(sse).extracting(Order::id).containsExactly(0L, 1L, 2L);
        // sem streaming: um array JSON
        web.get().uri("/orders").accept(MediaType.APPLICATION_JSON).exchange()
            .expectBody().jsonPath("$.length()").isEqualTo(3);
    }

    @Test void sb0511_operadores() {
        // flatMap NÃO preserva a ordem (cada elemento tem o próprio atraso); sob carga os timers disparam fora de ordem
        StepVerifier.create(Flux.range(1, 5).filter(i -> i % 2 == 1).map(i -> i * 10)
                .flatMap(i -> Mono.just(i).delayElement(Duration.ofMillis(10))).collectList())
            .assertNext(l -> assertThat(l).containsExactlyInAnyOrder(10, 30, 50)).verifyComplete();
        StepVerifier.create(Flux.just(1, 2, 0).map(i -> 10 / i).onErrorReturn(-1))
            .expectNext(10, 5, -1).verifyComplete();
        // backpressure + tempo virtual
        StepVerifier.withVirtualTime(() -> Flux.interval(Duration.ofHours(1)).take(2))
            .thenAwait(Duration.ofHours(2)).expectNext(0L, 1L).verifyComplete();
        // assembly vs execução: nada roda sem assinar; cada assinatura reexecuta
        int[] c = {0};
        Mono<Integer> m = Mono.fromSupplier(() -> ++c[0]);
        assertThat(c[0]).isZero();
        m.block(); m.block();
        assertThat(c[0]).isEqualTo(2);
    }

    @Test void sb0512_webclient() {
        WebClient client = WebClient.create("http://localhost:" + port);
        Order o = client.get().uri("/orders/{id}", 9).retrieve().bodyToMono(Order.class).block();
        assertThat(o).isEqualTo(new Order(9, BigDecimal.TEN));
        var par = Mono.zip(client.get().uri("/orders/1").retrieve().bodyToMono(Order.class),
                           client.get().uri("/orders/2").retrieve().bodyToMono(Order.class)).block();
        assertThat(par.getT2().id()).isEqualTo(2);
        // 4xx vira WebClientResponseException
        StepVerifier.create(client.get().uri("/fn/orders/500").retrieve().bodyToMono(Order.class))
            .expectError(WebClientResponseException.NotFound.class).verify();
    }

    @Test void sb0513_estilo_funcional() {
        web.get().uri("/fn/orders/5").exchange().expectStatus().isOk().expectBody().jsonPath("$.id").isEqualTo(5);
        web.get().uri("/fn/orders/500").exchange().expectStatus().isNotFound();
    }

    @Test void sb0514_isola_bloqueio() {
        String thread = Bloqueante.executar(Bloqueante::legado).block(Duration.ofSeconds(5));
        assertThat(thread).startsWith("boundedElastic");
    }
}
