package dev.estudos.cloud.mensageria;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.function.Function;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

/**
 * CL07-09/10/11: orders -> invoice (Function) -> invoices -> audit (Consumer), com Kafka como binder.
 * Precisa de um broker em {@code spring.cloud.stream.kafka.binder.brokers} (padrão localhost:9092);
 * os testes sobem um Kafka em contêiner (Testcontainers). {@code StreamBridge} publica de forma imperativa.
 */
@SpringBootApplication
@org.springframework.scheduling.annotation.EnableScheduling
public class MensageriaApp {

    public record OrderPlaced(long orderId, String customer, long cents) {}
    public record Invoice(long orderId, String text) {}

    /** Auditoria em memória (para os testes observarem o que o consumidor recebeu). */
    public static class Auditoria {
        public final List<Invoice> recebidas = new CopyOnWriteArrayList<>();
        public final AtomicInteger tentativasFalhas = new AtomicInteger();
    }

    @Bean Auditoria auditoria() { return new Auditoria(); }

    /** Lê "orders", escreve em "invoices". O cliente "falha" provoca exceção (para retry/DLQ, CL07-11). */
    @Bean Function<OrderPlaced, Invoice> invoice(Auditoria a) {
        return o -> {
            if ("falha".equals(o.customer())) {
                a.tentativasFalhas.incrementAndGet();
                throw new IllegalStateException("não consegui faturar o pedido " + o.orderId());
            }
            return new Invoice(o.orderId(), "NF para " + o.customer() + " R$" + o.cents() / 100.0);
        };
    }

    /** Só consome "invoices". */
    @Bean Consumer<Invoice> audit(Auditoria a) {
        return i -> {
            System.out.println("[audit] " + i);
            a.recebidas.add(i);
        };
    }

    public static void main(String[] args) { SpringApplication.run(MensageriaApp.class, args); }
}
