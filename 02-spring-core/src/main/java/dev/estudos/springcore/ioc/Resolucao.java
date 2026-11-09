package dev.estudos.springcore.ioc;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

/**
 * SC02-09 · Resolução com {@code @Primary}, {@code @Qualifier}, {@code List} e {@code Map}, com as classes
 * <b>aninhadas</b> como no documento: o nome padrão do bean sai como {@code resolucao.CardGateway}
 * (nome simples composto com a classe externa), por isso dê nomes explícitos quando injetar {@code Map}.
 * Documento: 02-spring-core/02-ioc-e-injecao-de-dependencia.md §5.1.
 *
 * <p>Saída real:
 * <pre>
 * card boleto
 * [boleto, card, pix]
 * [boleto, resolucao.CardGateway, resolucao.PixGateway]
 * </pre>
 */
public class Resolucao {
    interface PaymentGateway { String name(); }
    @Component static class PixGateway implements PaymentGateway { public String name() { return "pix"; } }
    @Component @Primary static class CardGateway implements PaymentGateway { public String name() { return "card"; } }
    @Component("boleto") static class BoletoGateway implements PaymentGateway { public String name() { return "boleto"; } }

    @Service
    static class OrderService {
        final PaymentGateway primary, chosen;
        final List<PaymentGateway> all;
        final Map<String, PaymentGateway> byName;
        OrderService(PaymentGateway primary, @Qualifier("boleto") PaymentGateway chosen,
                     List<PaymentGateway> all, Map<String, PaymentGateway> byName) {
            this.primary = primary; this.chosen = chosen; this.all = all; this.byName = byName;
        }
    }

    public static List<String> run() {
        List<String> out = new ArrayList<>();
        try (var ctx = new AnnotationConfigApplicationContext(PixGateway.class, CardGateway.class, BoletoGateway.class, OrderService.class)) {
            var s = ctx.getBean(OrderService.class);
            out.add(s.primary.name() + " " + s.chosen.name());
            out.add(s.all.stream().map(PaymentGateway::name).sorted().toList().toString());
            out.add(new TreeMap<>(s.byName).keySet().toString());
        }
        return out;
    }

    public static void main(String[] args) { run().forEach(System.out::println); }
}
