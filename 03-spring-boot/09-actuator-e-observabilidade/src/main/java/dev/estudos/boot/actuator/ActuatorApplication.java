package dev.estudos.boot.actuator;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.scheduling.annotation.EnableScheduling;

/** Aplicação do capítulo 09 (SB09-02/03/10..14): Actuator com health próprio, métrica de negócio e inspeção. */
@SpringBootApplication
@EnableCaching
@EnableScheduling
public class ActuatorApplication {
    public static void main(String[] args) { SpringApplication.run(ActuatorApplication.class, args); }
}
