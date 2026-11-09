package dev.estudos.outros.integration;

import org.springframework.integration.annotation.MessagingGateway;

/** Gateway: o proxy transforma a chamada em {@code Message} para o canal {@code in}. */
@MessagingGateway(defaultRequestChannel = "in")
public interface Pricing {
    String price(String order);
}
