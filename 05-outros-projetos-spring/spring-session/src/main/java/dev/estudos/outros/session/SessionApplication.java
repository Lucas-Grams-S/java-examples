package dev.estudos.outros.session;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/** OP07-02: sessão HTTP guardada em tabela (H2) em vez de na memória do Tomcat. */
@SpringBootApplication
public class SessionApplication {
    public static void main(String[] args) {
        SpringApplication.run(SessionApplication.class, args);
    }
}
