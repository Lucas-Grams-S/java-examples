package dev.estudos.springcore.minidi.app;

import dev.estudos.springcore.minidi.Component;

@Component
public class CardGateway implements PaymentGateway {
    public String charge(long cents) { return "cobrado " + cents; }
}
