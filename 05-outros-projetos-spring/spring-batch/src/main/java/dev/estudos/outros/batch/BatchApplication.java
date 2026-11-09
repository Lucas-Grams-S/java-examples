package dev.estudos.outros.batch;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/** OP01-02: Spring Batch 6 com H2. Roda o job {@code billing} na inicialização (padrão do Boot). */
@SpringBootApplication
public class BatchApplication {
    public static void main(String[] args) {
        SpringApplication.run(BatchApplication.class, args);
    }
}
