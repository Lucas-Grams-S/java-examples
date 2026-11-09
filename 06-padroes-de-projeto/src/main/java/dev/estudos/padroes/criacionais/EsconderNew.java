package dev.estudos.padroes.criacionais;

import java.util.ArrayList;
import java.util.List;

/**
 * DP02-01 · Por que esconder o {@code new}: serviço acoplado à classe concreta vs. dependência recebida.
 * Documento: java-docs/06-padroes-de-projeto/02-criacionais.md §2.1.
 *
 * <p>Saída real (JDK 25):
 * <pre>
 * acoplado (sempre SMTP real) -&gt; SMTP smtp.corp:587 -&gt; ana: ...
 * injetado, em teste -&gt; FAKE -&gt; ana
 * injetado, em produção -&gt; SMTP smtp.corp:25 -&gt; ana: ...
 * </pre>
 *
 * <p>Variações: troque o {@code FakeSender} por outro canal sem editar {@code NotificationService}.
 */
public class EsconderNew {

    interface Sender { String send(String to, String msg); }
    record SmtpEmailSender(String host, int port, boolean tls) implements Sender {
        public String send(String to, String msg) { return "SMTP " + host + ":" + port + " -> " + to + ": " + msg; }
    }

    /** Acoplado: conhece a classe concreta e o jeito de criá-la. */
    static class AcopladoService {
        String notify(String to) { return new SmtpEmailSender("smtp.corp", 587, true).send(to, "..."); }
    }

    /** Desacoplado: a decisão de criação mora em outro lugar. */
    static class NotificationService {
        private final Sender sender;
        NotificationService(Sender sender) { this.sender = sender; }
        String notify(String to) { return sender.send(to, "..."); }
    }

    public static List<String> run() {
        List<String> out = new ArrayList<>();
        out.add("acoplado (sempre SMTP real) -> " + new AcopladoService().notify("ana"));
        Sender fake = (to, msg) -> "FAKE -> " + to;          // teste sem servidor SMTP
        out.add("injetado, em teste -> " + new NotificationService(fake).notify("ana"));
        out.add("injetado, em produção -> " + new NotificationService(new SmtpEmailSender("smtp.corp", 25, false)).notify("ana"));
        return out;
    }

    public static void main(String[] args) { run().forEach(System.out::println); }
}
