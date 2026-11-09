package dev.estudos.cloud.mensageria.outbox;

import dev.estudos.cloud.mensageria.MensageriaApp.OrderPlaced;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.json.JsonMapper;

/**
 * CL07-13: Transactional Outbox. O pedido e a linha da {@code outbox} entram na MESMA transação do banco;
 * nada é publicado aqui. Quem publica é o {@link OutboxPoller}.
 */
@Service
public class OrderService {
    private final JdbcClient jdbc;
    private final JsonMapper json = JsonMapper.builder().build();

    OrderService(JdbcClient jdbc) { this.jdbc = jdbc; }

    @Transactional
    public void place(OrderPlaced o, boolean quebraDepoisDoOutbox) {
        jdbc.sql("INSERT INTO orders(id, customer, cents) VALUES (?,?,?)").params(o.orderId(), o.customer(), o.cents()).update();
        jdbc.sql("INSERT INTO outbox(topic, payload) VALUES (?,?)").params("orders-out", json.writeValueAsString(o)).update();
        if (quebraDepoisDoOutbox) throw new IllegalStateException("falha antes do commit");   // rollback dos DOIS inserts
    }

    public int pedidos() { return jdbc.sql("SELECT count(*) FROM orders").query(Integer.class).single(); }
    public int pendentes() { return jdbc.sql("SELECT count(*) FROM outbox WHERE NOT sent").query(Integer.class).single(); }
}
