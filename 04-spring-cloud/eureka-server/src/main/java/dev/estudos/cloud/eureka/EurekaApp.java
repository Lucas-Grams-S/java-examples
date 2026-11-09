package dev.estudos.cloud.eureka;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.server.EnableEurekaServer;

/** CL03-09: o registro de servicos. */
@SpringBootApplication
@EnableEurekaServer
public class EurekaApp {
    public static void main(String[] args) { SpringApplication.run(EurekaApp.class, args); }
}
