package dev.estudos.cloud.gateway;

import java.util.UUID;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/** CL04-07: filtro global proprio (correlacao). */
@Configuration
class CorrelationConfig {
    @Bean
    GlobalFilter correlationId() { return new CorrelationFilter(); }
}

class CorrelationFilter implements GlobalFilter, Ordered {
    @Override
    public Mono<Void> filter(ServerWebExchange ex, GatewayFilterChain chain) {
        String id = ex.getRequest().getHeaders().getFirst("X-Correlation-Id");
        String cid = id != null ? id : UUID.randomUUID().toString();
        var mutated = ex.mutate().request(r -> r.header("X-Correlation-Id", cid)).build();
        mutated.getResponse().getHeaders().set("X-Correlation-Id", cid);
        return chain.filter(mutated);
    }
    @Override public int getOrder() { return Ordered.HIGHEST_PRECEDENCE; }
}
