package dev.estudos.boot.config;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

/** Aplicação web do capítulo 03: {@code @ConfigurationPropertiesScan} encontra {@link OrdersProperties}. */
@SpringBootApplication
@ConfigurationPropertiesScan
public class ConfigApplication {
    public static void main(String[] args) { SpringApplication.run(ConfigApplication.class, args); }
}
