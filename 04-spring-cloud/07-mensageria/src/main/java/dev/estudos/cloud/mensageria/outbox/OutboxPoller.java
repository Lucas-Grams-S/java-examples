package dev.estudos.cloud.mensageria.outbox;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * CL07-13: processo separado que lê a {@code outbox}, publica no broker e só então marca {@code sent}.
 * Se publicar e cair antes do UPDATE, a mensagem sai de novo: entrega <b>at-least-once</b> (consumidor idempotente).
 */
@Component
public class OutboxPoller {
    private record Linha(long id, String topic, String payload) {}

    private final JdbcClient jdbc;
    private final StreamBridge bridge;
    /** Simula o broker fora do ar (para o teste provar que a linha continua pendente). */
    public final AtomicBoolean brokerForaDoAr = new AtomicBoolean();

    OutboxPoller(JdbcClient jdbc, StreamBridge bridge) { this.jdbc = jdbc; this.bridge = bridge; }

    @Scheduled(fixedDelay = 300)
    public void publicarPendentes() {
        List<Linha> pendentes = jdbc.sql("SELECT id, topic, payload FROM outbox WHERE NOT sent ORDER BY id")
            .query(Linha.class).list();
        for (Linha l : pendentes) {
            if (brokerForaDoAr.get() || !bridge.send(l.topic(), l.payload().getBytes(), org.springframework.util.MimeTypeUtils.APPLICATION_JSON)) return;
            jdbc.sql("UPDATE outbox SET sent = TRUE WHERE id = ?").param(l.id()).update();
        }
    }
}
