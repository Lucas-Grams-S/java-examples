package com.example.orders;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/** PP01: esqueleto Boot. Os {@code @Enable...} ficam em {@link InfraConfig}, para não quebrar os testes de fatia. */
@SpringBootApplication
public class OrdersApplication {
    public static void main(String[] args) { SpringApplication.run(OrdersApplication.class, args); }
}
