package dev.estudos.boot.reativo;

import java.math.BigDecimal;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.*;
import reactor.core.publisher.Mono;

/** SB05-13/SB05-04: estilo funcional (RouterFunction + HandlerFunction). */
@Component
public class OrderHandler {
    Mono<ServerResponse> byId(ServerRequest req) {
        long id = Long.parseLong(req.pathVariable("id"));
        Mono<Order> found = id < 100 ? Mono.just(new Order(id, BigDecimal.ONE)) : Mono.empty();
        return found.flatMap(o -> ServerResponse.ok().bodyValue(o))
                    .switchIfEmpty(ServerResponse.notFound().build());
    }

    @Configuration
    static class Routes {
        @Bean RouterFunction<ServerResponse> fn(OrderHandler h) {
            return RouterFunctions.route().GET("/fn/orders/{id}", h::byId).build();
        }
    }
}
