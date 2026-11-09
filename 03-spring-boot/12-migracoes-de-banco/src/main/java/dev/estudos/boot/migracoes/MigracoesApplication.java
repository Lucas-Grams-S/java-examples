package dev.estudos.boot.migracoes;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.jdbc.core.simple.JdbcClient;

/** Capítulo 12: sobe, o Flyway migra antes de tudo, e imprime o histórico (SB12-08/10/11). */
@SpringBootApplication
public class MigracoesApplication {
    public static void main(String[] args) { SpringApplication.run(MigracoesApplication.class, args); }

    @Bean CommandLineRunner historico(JdbcClient jdbc) {
        return args -> jdbc.sql("select \"installed_rank\", \"version\", \"description\", \"type\" from \"flyway_schema_history\" order by \"installed_rank\"")
            .query((rs, i) -> rs.getInt(1) + " | " + rs.getString(2) + " | " + rs.getString(3) + " | " + rs.getString(4))
            .list().forEach(System.out::println);
    }
}
