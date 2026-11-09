package dev.estudos.outros.integration;

import static org.assertj.core.api.Assertions.assertThat;

import java.lang.reflect.Proxy;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.integration.core.MessagingTemplate;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.support.MessageBuilder;

/** OP02-02. */
@SpringBootTest
class IntegrationTest {
    @Autowired Pricing pricing;
    @Autowired MessageChannel in;

    @Test void op0202_gateway_filtraETransforma() {
        assertThat(pricing.price("abc")).isEqualTo("ABC-OK");
    }

    @Test void op0202_gateway_eProxyJdk() {
        assertThat(Proxy.isProxyClass(pricing.getClass())).isTrue();
    }

    @Test void op0202_mensagemPeloCanal_mesmoResultado() {
        var t = new MessagingTemplate();
        var reply = t.sendAndReceive(in, MessageBuilder.withPayload("xyz").build());
        assertThat(reply.getPayload()).isEqualTo("XYZ-OK");
    }

    /** Mensagem filtrada some sem resposta: sem timeout o gateway bloquearia para sempre (doc §8, replyTimeout). */
    @Test void op0202_filtroDescartaVazio_semRespostaAteOTimeout() {
        var t = new MessagingTemplate();
        t.setReceiveTimeout(500);
        assertThat(t.sendAndReceive(in, MessageBuilder.withPayload("  ").build())).isNull();
    }
}
