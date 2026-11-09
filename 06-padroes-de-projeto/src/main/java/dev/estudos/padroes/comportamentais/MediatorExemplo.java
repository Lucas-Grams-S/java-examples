package dev.estudos.padroes.comportamentais;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * DP04-09 · Mediator: um chat em que os colegas só falam com o mediador.
 * Documento: java-docs/06-padroes-de-projeto/04-comportamentais.md §5.4.
 *
 * <p>Saída real (JDK 25):
 * <pre>
 * ana envia "oi" -&gt; &#123;ana=[], bob=[ana: oi], cy=[ana: oi]&#125;
 * </pre>
 *
 * <p>Variações: faça o mediador ignorar usuários silenciados; a regra mora só nele.
 */
public class MediatorExemplo {

    interface Mediator { void notifyChange(String from, String msg); }

    static class Chat implements Mediator {
        final Map<String, List<String>> inbox = new LinkedHashMap<>();
        void join(String user) { inbox.put(user, new ArrayList<>()); }
        public void notifyChange(String from, String msg) {
            inbox.forEach((user, box) -> { if (!user.equals(from)) box.add(from + ": " + msg); });
        }
    }

    static class User {
        private final String name; private final Mediator mediator;
        User(String name, Mediator mediator) { this.name = name; this.mediator = mediator; }
        void send(String msg) { mediator.notifyChange(name, msg); }
    }

    public static List<String> run() {
        var chat = new Chat();
        for (var u : List.of("ana", "bob", "cy")) chat.join(u);
        new User("ana", chat).send("oi");
        return new ArrayList<>(List.of("ana envia \"oi\" -> " + chat.inbox));
    }

    public static void main(String[] args) { run().forEach(System.out::println); }
}
