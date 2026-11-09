package dev.estudos.boot.jpa;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

/** Aplicação do capítulo 06: sobe, popula e imprime o resumo do N+1 (SB06-13). */
@SpringBootApplication
public class JpaApplication {
    public static void main(String[] args) { SpringApplication.run(JpaApplication.class, args); }

    @Bean CommandLineRunner demo(OrderService s) {
        return args -> {
            s.seed(5);
            System.out.println(s.nPlusOne());
            System.out.println(s.joinFetch());
            System.out.println(s.entityGraph());
        };
    }
}
