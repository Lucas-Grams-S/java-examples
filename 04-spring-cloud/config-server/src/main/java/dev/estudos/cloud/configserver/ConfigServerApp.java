package dev.estudos.cloud.configserver;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.config.server.EnableConfigServer;

/** CL02-08: o Config Server, servindo um repositorio Git local. */
@SpringBootApplication
@EnableConfigServer
public class ConfigServerApp {
    public static void main(String[] args) { SpringApplication.run(ConfigServerApp.class, args); }
}
