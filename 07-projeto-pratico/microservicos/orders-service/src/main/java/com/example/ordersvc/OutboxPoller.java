package com.example.ordersvc;

import java.util.List;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/** PP12: lê a outbox, publica no Kafka e SÓ ENTÃO marca como enviada (at-least-once). */
@Component
@ConditionalOnProperty(name = "outbox.enabled", havingValue = "true", matchIfMissing = true)
class OutboxPoller {
    private record Linha(long id, String topic, String payload) {}

    private final JdbcClient jdbc;
    private final KafkaTemplate<String, String> kafka;

    OutboxPoller(JdbcClient jdbc, KafkaTemplate<String, String> kafka) { this.jdbc = jdbc; this.kafka = kafka; }

    @Scheduled(fixedDelay = 500)
    void publicar() {
        List<Linha> pendentes = jdbc.sql("SELECT id, topic, payload FROM outbox WHERE NOT sent ORDER BY id").query(Linha.class).list();
        for (Linha l : pendentes) {
            try {
                kafka.send(l.topic(), String.valueOf(l.id()), l.payload()).get(3, java.util.concurrent.TimeUnit.SECONDS);
            } catch (Exception e) { return; }          // broker fora do ar: a linha continua pendente, tenta no próximo ciclo
            jdbc.sql("UPDATE outbox SET sent = TRUE WHERE id = ?").param(l.id()).update();
        }
    }
}
