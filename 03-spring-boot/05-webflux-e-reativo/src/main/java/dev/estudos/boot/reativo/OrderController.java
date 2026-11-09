package dev.estudos.boot.reativo;

import java.math.BigDecimal;
import java.time.Duration;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/** SB05-10: Mono, Flux e Server-Sent Events. */
@RestController
@RequestMapping("/orders")
public class OrderController {

    @GetMapping("/{id}")
    Mono<Order> byId(@PathVariable long id) {
        return Mono.just(new Order(id, BigDecimal.TEN)).delayElement(Duration.ofMillis(50));
    }

    @GetMapping(produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    Flux<Order> stream() {
        return Flux.interval(Duration.ofMillis(20)).take(3).map(i -> new Order(i, BigDecimal.valueOf(i)));
    }

    /** Sem produces de streaming: o Flux é coletado e vira array JSON. */
    @GetMapping
    Flux<Order> list() {
        return Flux.range(1, 3).map(i -> new Order(i, BigDecimal.valueOf(i)));
    }
}
