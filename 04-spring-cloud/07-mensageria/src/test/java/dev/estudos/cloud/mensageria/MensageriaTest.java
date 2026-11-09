package dev.estudos.cloud.mensageria;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

import dev.estudos.cloud.mensageria.MensageriaApp.Auditoria;
import dev.estudos.cloud.mensageria.MensageriaApp.Invoice;
import dev.estudos.cloud.mensageria.MensageriaApp.OrderPlaced;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.kafka.KafkaContainer;

/** Requer Docker e a imagem {@code apache/kafka}. */
@SpringBootTest
@Testcontainers
class MensageriaTest {
    @Container static final KafkaContainer KAFKA = new KafkaContainer("apache/kafka:latest");

    @DynamicPropertySource
    static void props(DynamicPropertyRegistry r) {
        r.add("spring.cloud.stream.kafka.binder.brokers", KAFKA::getBootstrapServers);
    }

    @Autowired StreamBridge bridge;
    @Autowired Auditoria auditoria;
    @Autowired dev.estudos.cloud.mensageria.outbox.OrderService pedidos;
    @Autowired dev.estudos.cloud.mensageria.outbox.OutboxPoller poller;

    /** CL07-10: StreamBridge -> orders -> invoice -> invoices -> audit. */
    @Test
    void processadorEConsumidor() {
        assertThat(bridge.send("orders-out", new OrderPlaced(1, "ana", 1000))).isTrue();
        await().atMost(Duration.ofSeconds(30)).untilAsserted(() ->
            assertThat(auditoria.recebidas).contains(new Invoice(1, "NF para ana R$10.0")));
    }

    /** CL07-11: 3 tentativas e a mensagem vai para a DLQ; o fluxo seguinte continua. */
    @Test
    void retryEDlq() {
        bridge.send("orders-out", new OrderPlaced(2, "falha", 500));
        try (var c = new KafkaConsumer<String, String>(Map.<String, Object>of(
                ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, KAFKA.getBootstrapServers(),
                ConsumerConfig.GROUP_ID_CONFIG, "teste-dlq",
                ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest",
                ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class,
                ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class))) {
            c.subscribe(List.of("orders-billing-dlq"));
            var valores = new java.util.ArrayList<String>();
            await().atMost(Duration.ofSeconds(30)).untilAsserted(() -> {
                c.poll(Duration.ofMillis(500)).forEach(r -> valores.add(r.value()));
                assertThat(valores).anyMatch(v -> v.contains("\"orderId\":2"));
            });
        }
        assertThat(auditoria.tentativasFalhas.get()).isEqualTo(3);
        bridge.send("orders-out", new OrderPlaced(3, "bia", 2500));
        await().atMost(Duration.ofSeconds(30)).untilAsserted(() ->
            assertThat(auditoria.recebidas).contains(new Invoice(3, "NF para bia R$25.0")));
    }

    /** CL07-13: rollback desfaz pedido E outbox; com broker fora do ar a linha fica pendente; ao voltar, o evento sai. */
    @Test
    void outboxTransacional() {
        int antes = pedidos.pedidos();
        org.assertj.core.api.Assertions.assertThatThrownBy(() -> pedidos.place(new OrderPlaced(10, "x", 100), true))
            .isInstanceOf(IllegalStateException.class);
        assertThat(pedidos.pedidos()).isEqualTo(antes);        // nada gravado
        assertThat(pedidos.pendentes()).isZero();              // e nenhum evento falso

        poller.brokerForaDoAr.set(true);
        try {
            pedidos.place(new OrderPlaced(11, "carla", 4200), false);
            assertThat(pedidos.pedidos()).isEqualTo(antes + 1);
            await().during(Duration.ofSeconds(1)).atMost(Duration.ofSeconds(3)).untilAsserted(() ->
                assertThat(pedidos.pendentes()).isEqualTo(1)); // commit ocorreu, publicação ainda não: o evento não se perde
            assertThat(auditoria.recebidas).noneMatch(i -> i.orderId() == 11);
        } finally { poller.brokerForaDoAr.set(false); }
        await().atMost(Duration.ofSeconds(30)).untilAsserted(() ->
            assertThat(auditoria.recebidas).contains(new Invoice(11, "NF para carla R$42.0")));
        assertThat(pedidos.pendentes()).isZero();
    }
}
