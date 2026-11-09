package dev.estudos.springcore.ciclovida;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import java.util.ArrayList;
import java.util.List;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.stereotype.Component;

/**
 * SC04-09 · Bean com recurso (conexão): {@code @PostConstruct} abre quando as dependências já foram
 * injetadas; {@code @PreDestroy} fecha no {@code close()} do contexto.
 * Documento: 02-spring-core/04-ciclo-de-vida-e-escopos.md §5.1.
 *
 * <p>Saída real:
 * <pre>
 * broker: open
 * conectado após refresh -&gt; true
 * broker: close
 * conexão fechada após close() -&gt; true
 * </pre>
 */
public class RecursoComCicloDeVida {
    static class Connection {
        boolean open = true;
        void close() { open = false; }
    }

    @Component
    static class BrokerClient {
        Connection open() { LOG.add("broker: open"); return new Connection(); }
    }

    static final List<String> LOG = new ArrayList<>();

    @Component
    static class MessageConsumer {
        private final BrokerClient client;
        Connection connection;
        MessageConsumer(BrokerClient client) { this.client = client; }

        @PostConstruct void connect() { this.connection = client.open(); }   // dependências já injetadas
        @PreDestroy void disconnect() { LOG.add("broker: close"); connection.close(); }
    }

    public static List<String> run() {
        LOG.clear();
        List<String> out = new ArrayList<>();
        Connection c;
        try (var ctx = new AnnotationConfigApplicationContext(BrokerClient.class, MessageConsumer.class)) {
            c = ctx.getBean(MessageConsumer.class).connection;
            out.addAll(LOG);
            out.add("conectado após refresh -> " + c.open);
        }
        out.add("broker: close".equals(LOG.getLast()) ? "broker: close" : "(close não rodou)");
        out.add("conexão fechada após close() -> " + !c.open);
        return out;
    }

    public static void main(String[] args) { run().forEach(System.out::println); }
}
