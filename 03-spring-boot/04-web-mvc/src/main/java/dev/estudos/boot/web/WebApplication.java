package dev.estudos.boot.web;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

/** Aplicação web do capítulo 04 (pedidos). */
@SpringBootApplication
@ConfigurationPropertiesScan
public class WebApplication {
    public static void main(String[] args) { SpringApplication.run(WebApplication.class, args); }
}
