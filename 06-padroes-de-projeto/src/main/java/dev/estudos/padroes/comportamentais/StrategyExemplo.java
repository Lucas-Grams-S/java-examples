package dev.estudos.padroes.comportamentais;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.stereotype.Component;

/**
 * DP04-13 · Strategy: mapa de lambdas, {@code Comparator} e {@code Map<String, Interface>} injetado pelo Spring.
 * Documento: java-docs/06-padroes-de-projeto/04-comportamentais.md §5.8.
 *
 * <p>Saída real (JDK 25):
 * <pre>
 * vip -&gt; 90.00, bf -&gt; 70.00, none -&gt; 100.00
 * Comparator como estratégia -&gt; [a, cc, bbb]
 * beans Notifier -&gt; [email, sms]
 * notify("sms", "oi") -&gt; sms:oi
 * </pre>
 *
 * <p>Variações: crie {@code @Component("push")} e chame {@code notify("push", ...)} sem editar {@code Svc}.
 */
public class StrategyExemplo {

    interface Discount { BigDecimal apply(BigDecimal v); }

    interface Notifier { String send(String m); }
    @Component("email") static class EmailN implements Notifier { public String send(String m) { return "email:" + m; } }
    @Component("sms") static class SmsN implements Notifier { public String send(String m) { return "sms:" + m; } }
    @Component static class Svc {
        final Map<String, Notifier> byChannel;
        Svc(Map<String, Notifier> m) { byChannel = m; }
        String notify(String ch, String m) { return byChannel.get(ch).send(m); }
    }

    public static List<String> run() {
        List<String> out = new ArrayList<>();
        Map<String, Discount> strategies = Map.of(
            "none", v -> v,
            "vip", v -> v.multiply(new BigDecimal("0.90")),
            "bf", v -> v.multiply(new BigDecimal("0.70")));
        var cem = new BigDecimal("100");
        out.add("vip -> " + strategies.get("vip").apply(cem).setScale(2)
                + ", bf -> " + strategies.get("bf").apply(cem).setScale(2)
                + ", none -> " + strategies.get("none").apply(cem).setScale(2));

        var words = new ArrayList<>(List.of("bbb", "a", "cc"));
        words.sort(Comparator.comparingInt(String::length));
        out.add("Comparator como estratégia -> " + words);

        try (var ctx = new AnnotationConfigApplicationContext(EmailN.class, SmsN.class, Svc.class)) {
            out.add("beans Notifier -> " + ctx.getBeansOfType(Notifier.class).keySet().stream().sorted().toList());
            out.add("notify(\"sms\", \"oi\") -> " + ctx.getBean(Svc.class).notify("sms", "oi"));
        }
        return out;
    }

    public static void main(String[] args) { run().forEach(System.out::println); }
}
