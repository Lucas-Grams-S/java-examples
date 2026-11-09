package dev.estudos.cloud.gatewaymvc;

import static org.springframework.cloud.gateway.server.mvc.filter.AfterFilterFunctions.addResponseHeader;
import static org.springframework.cloud.gateway.server.mvc.filter.BeforeFilterFunctions.stripPrefix;
import static org.springframework.cloud.gateway.server.mvc.filter.LoadBalancerFilterFunctions.lb;
import static org.springframework.cloud.gateway.server.mvc.handler.GatewayRouterFunctions.route;
import static org.springframework.cloud.gateway.server.mvc.handler.HandlerFunctions.http;
import static org.springframework.web.servlet.function.RequestPredicates.path;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.servlet.function.RouterFunction;
import org.springframework.web.servlet.function.ServerResponse;

/** CL04-08: gateway no sabor Web MVC: rota em Java DSL (catalog) + rota em YAML (orders). */
@SpringBootApplication
public class GatewayMvcApp {
    public static void main(String[] args) { SpringApplication.run(GatewayMvcApp.class, args); }

    @Bean
    RouterFunction<ServerResponse> catalogRoute() {
        return route("catalog")
            .route(path("/api/catalog/**"), http())
            .before(stripPrefix(2))
            .filter(lb("catalog"))
            .after(addResponseHeader("X-Gateway", "mvc"))
            .build();
    }
}
