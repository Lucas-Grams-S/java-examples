package dev.estudos.outros.modulith;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/** OP03-02: cada subpacote direto (orders, inventory) é um módulo da aplicação. */
@SpringBootApplication
public class ModulithApplication {
    public static void main(String[] args) {
        SpringApplication.run(ModulithApplication.class, args);
    }
}
