package dev.estudos.cloud.orders;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/** CL05-08: orders chama catalog por um HTTP Service Client balanceado. */
@SpringBootApplication
public class OrdersApp {
    public static void main(String[] args) { SpringApplication.run(OrdersApp.class, args); }
}
