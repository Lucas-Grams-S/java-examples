package dev.estudos.outros.restdocs;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/** OP09-02: a API é documentada pelos próprios testes (snippets em {@code target/snippets}). */
@SpringBootApplication
public class RestdocsApplication {
    public static void main(String[] args) {
        SpringApplication.run(RestdocsApplication.class, args);
    }
}
