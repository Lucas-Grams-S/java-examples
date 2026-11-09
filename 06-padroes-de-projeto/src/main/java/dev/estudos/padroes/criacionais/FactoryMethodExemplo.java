package dev.estudos.padroes.criacionais;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * DP02-07 · Factory Method: herança, static factory, cache do JDK e {@code FactoryBean}.
 * Documento: java-docs/06-padroes-de-projeto/02-criacionais.md §5.2.
 *
 * <p>Saída real (JDK 25):
 * <pre>
 * EMAIL para ana: Pedido 7 confirmado
 * SMS para ana: Pedido 7 confirmado
 * static factory -&gt; Money[amount=9.90, currency=BRL] | Money[amount=0, currency=BRL]
 * Integer.valueOf(127) == Integer.valueOf(127) -&gt; true
 * List.of(1,2) é List12, List.of(1,2,3) é ListN
 * getBean("notifier") -&gt; EmailNotifier
 * getBean("&amp;notifier") -&gt; NotifierFactoryBean
 * produto em cache (singleton) -&gt; true
 * </pre>
 *
 * <p>Variações: adicione um {@code PushOrderNotification} sem alterar {@code OrderNotification}.
 */
public class FactoryMethodExemplo {

    interface Notifier { String send(String to, String msg); }
    record EmailNotifier() implements Notifier { public String send(String to, String m) { return "EMAIL para " + to + ": " + m; } }
    record SmsNotifier() implements Notifier { public String send(String to, String m) { return "SMS para " + to + ": " + m; } }

    static abstract class OrderNotification {
        protected abstract Notifier createNotifier();                       // Factory Method
        String notifyCustomer(String customer, long orderId) {
            return createNotifier().send(customer, "Pedido " + orderId + " confirmado");
        }
    }
    static class EmailOrderNotification extends OrderNotification { protected Notifier createNotifier() { return new EmailNotifier(); } }
    static class SmsOrderNotification extends OrderNotification { protected Notifier createNotifier() { return new SmsNotifier(); } }

    record Money(BigDecimal amount, String currency) {
        static Money of(String amount, String currency) { return new Money(new BigDecimal(amount), currency); }
        static Money zero(String currency) { return new Money(BigDecimal.ZERO, currency); }
    }

    static class NotifierFactoryBean implements FactoryBean<Notifier> {
        public Notifier getObject() { return new EmailNotifier(); }          // o PRODUTO
        public Class<?> getObjectType() { return Notifier.class; }
    }
    @Configuration static class NotifierConfig { @Bean NotifierFactoryBean notifier() { return new NotifierFactoryBean(); } }

    public static List<String> run() {
        List<String> out = new ArrayList<>();
        out.add(new EmailOrderNotification().notifyCustomer("ana", 7));
        out.add(new SmsOrderNotification().notifyCustomer("ana", 7));
        out.add("static factory -> " + Money.of("9.90", "BRL") + " | " + Money.zero("BRL"));
        out.add("Integer.valueOf(127) == Integer.valueOf(127) -> " + (Integer.valueOf(127) == Integer.valueOf(127)));
        out.add("List.of(1,2) é " + List.of(1, 2).getClass().getSimpleName() + ", List.of(1,2,3) é " + List.of(1, 2, 3).getClass().getSimpleName());
        try (var ctx = new AnnotationConfigApplicationContext(NotifierConfig.class)) {
            out.add("getBean(\"notifier\") -> " + ctx.getBean("notifier").getClass().getSimpleName());
            out.add("getBean(\"&notifier\") -> " + ctx.getBean("&notifier").getClass().getSimpleName());
            out.add("produto em cache (singleton) -> " + (ctx.getBean("notifier") == ctx.getBean("notifier")));
        }
        return out;
    }

    public static void main(String[] args) { run().forEach(System.out::println); }
}
