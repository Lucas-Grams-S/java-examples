package dev.estudos.outros.graphql;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/** OP05-02: Spring for GraphQL sobre MVC. Esquema em {@code resources/graphql/schema.graphqls}. */
@SpringBootApplication
public class GraphqlApplication {
    public static void main(String[] args) {
        SpringApplication.run(GraphqlApplication.class, args);
    }
}
