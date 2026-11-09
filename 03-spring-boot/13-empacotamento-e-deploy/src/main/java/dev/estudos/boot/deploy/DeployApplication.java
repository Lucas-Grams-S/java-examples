package dev.estudos.boot.deploy;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/** Capítulo 13: aplicação mínima para empacotar (fat jar, camadas, AOT cache, Docker). */
@SpringBootApplication
public class DeployApplication {
    public static void main(String[] args) { SpringApplication.run(DeployApplication.class, args); }
}
