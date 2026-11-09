package dev.estudos.springcore.intro.pedidos;

public interface PaymentGateway {
    String charge(Order order);
}
