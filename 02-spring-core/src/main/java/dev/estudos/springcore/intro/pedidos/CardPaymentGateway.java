package dev.estudos.springcore.intro.pedidos;

import org.springframework.stereotype.Component;

@Component
public class CardPaymentGateway implements PaymentGateway {
    public String charge(Order order) { return "cartão cobrou " + order.cents() + " centavos de " + order.id(); }
}
