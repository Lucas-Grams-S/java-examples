package dev.estudos.cloud.tracing;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * CL08-08/09: o MESMO código sobe como dois serviços, escolhidos por perfil:
 * {@code orders} (porta 8081, chama catalog) e {@code catalog} (porta 8082).
 * Precisa de um Zipkin em localhost:9411 (os testes sobem um contêiner).
 * Ex.: {@code scripts/run-example.sh CL08-08 --spring.profiles.active=catalog}
 */
@SpringBootApplication
public class TracingApp {
    public static void main(String[] args) { SpringApplication.run(TracingApp.class, args); }
}
