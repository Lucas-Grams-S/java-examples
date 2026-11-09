package dev.estudos.padroes.estruturais;

import java.util.ArrayList;
import java.util.List;

/**
 * DP03-07 · Bridge: duas hierarquias (mensagem × canal) ligadas por composição.
 * Documento: java-docs/06-padroes-de-projeto/03-estruturais.md §5.2.
 *
 * <p>Saída real (JDK 25):
 * <pre>
 * email -&gt; ana&#64;x.com: Pedido 1 confirmado. Obrigado pela compra!
 * sms   -&gt; +5511999: Pedido 1 confirmado.
 * sms   -&gt; +5511999: Pedido 1 saiu para e
 * email -&gt; ana&#64;x.com: Pedido 1 saiu para entrega.
 * 2 mensagens + 2 canais = 4 classes de implementação (não 4 combinações); com 10 + 10, 20 em vez de 100
 * </pre>
 *
 * <p>Variações: crie {@code PushChannel}; todas as mensagens passam a poder usá-lo sem nova subclasse.
 */
public class BridgeExemplo {

    interface MessageChannel { void deliver(String to, String text); }

    static class EmailChannel implements MessageChannel {
        private final List<String> log;
        EmailChannel(List<String> log) { this.log = log; }
        public void deliver(String to, String t) { log.add("email -> " + to + ": " + t); }
    }
    static class SmsChannel implements MessageChannel {
        private final List<String> log;
        SmsChannel(List<String> log) { this.log = log; }
        public void deliver(String to, String t) { log.add("sms   -> " + to + ": " + t.substring(0, Math.min(20, t.length()))); }
    }

    static abstract class OrderMessage {
        protected final MessageChannel channel;
        protected OrderMessage(MessageChannel channel) { this.channel = channel; }
        abstract void send(String to, long orderId);
    }
    static class OrderConfirmed extends OrderMessage {
        OrderConfirmed(MessageChannel c) { super(c); }
        void send(String to, long id) { channel.deliver(to, "Pedido " + id + " confirmado. Obrigado pela compra!"); }
    }
    static class OrderShipped extends OrderMessage {
        OrderShipped(MessageChannel c) { super(c); }
        void send(String to, long id) { channel.deliver(to, "Pedido " + id + " saiu para entrega."); }
    }

    public static List<String> run() {
        List<String> out = new ArrayList<>();
        var email = new EmailChannel(out);
        var sms = new SmsChannel(out);
        new OrderConfirmed(email).send("ana@x.com", 1);
        new OrderConfirmed(sms).send("+5511999", 1);
        new OrderShipped(sms).send("+5511999", 1);
        new OrderShipped(email).send("ana@x.com", 1);
        out.add("2 mensagens + 2 canais = 4 classes de implementação (não 4 combinações); com 10 + 10, 20 em vez de 100");
        return out;
    }

    public static void main(String[] args) { run().forEach(System.out::println); }
}
