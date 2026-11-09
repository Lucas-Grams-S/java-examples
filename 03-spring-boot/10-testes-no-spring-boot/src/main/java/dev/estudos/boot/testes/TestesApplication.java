package dev.estudos.boot.testes;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/** Classe principal SEM {@code @Enable*} (SB10-08): as anotações de infraestrutura ficam em {@link InfraConfig}. */
@SpringBootApplication
public class TestesApplication {
    public static void main(String[] args) { SpringApplication.run(TestesApplication.class, args); }
}
