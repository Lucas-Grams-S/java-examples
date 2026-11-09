package dev.estudos.boot.tarefas;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

/** Capítulo 11: cache, @Async, @Scheduled e @Retryable. Classe principal sem {@code @Enable*} (ver {@link InfraConfig}). */
@SpringBootApplication
public class TarefasApplication {
    public static void main(String[] args) { SpringApplication.run(TarefasApplication.class, args); }

    @Bean CommandLineRunner demo(PriceService ps, Notifier n) {
        return args -> {
            ps.price("A"); ps.price("A"); ps.viaThis("A");
            System.out.println("chamadas reais ao price(): " + ps.calls());
            System.out.println("async em: " + n.where().join() + " | via this em: " + n.viaThis());
        };
    }
}
