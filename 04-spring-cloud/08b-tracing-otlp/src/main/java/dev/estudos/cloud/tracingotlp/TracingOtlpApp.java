package dev.estudos.cloud.tracingotlp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * CL08-10: o mesmo par orders/catalog do CL08-08, mas com {@code spring-boot-starter-opentelemetry}
 * (ponte OTel + exportação OTLP/HTTP) em vez de Brave + Zipkin. Perfis {@code orders} e {@code catalog}.
 */
@SpringBootApplication
public class TracingOtlpApp {
    public static void main(String[] args) { SpringApplication.run(TracingOtlpApp.class, args); }
}
