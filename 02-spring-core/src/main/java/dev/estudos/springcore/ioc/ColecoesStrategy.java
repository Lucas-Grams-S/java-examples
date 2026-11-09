package dev.estudos.springcore.ioc;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

/**
 * SC02-04 · Injetar coleções: o Strategy "do jeito Spring". {@code Map<String,T>} (chave = nome do bean) e
 * {@code List<T>} (ordem por {@code @Order}). Adicionar uma implementação não exige editar o serviço.
 * Documento: 02-spring-core/02-ioc-e-injecao-de-dependencia.md §2.4.
 *
 * <p>Saída real:
 * <pre>
 * pay("pix") -&gt; pix
 * pay("boleto") -&gt; boleto
 * chaves do Map -&gt; [boleto, card, pix]
 * List ordenada por @Order -&gt; [pix, card, boleto]
 * </pre>
 */
public class ColecoesStrategy {
    interface PaymentGateway { String name(); }
    @Component("pix") @Order(1) static class Pix implements PaymentGateway { public String name() { return "pix"; } }
    @Component("card") @Order(2) static class Card implements PaymentGateway { public String name() { return "card"; } }
    @Component("boleto") @Order(3) static class Boleto implements PaymentGateway { public String name() { return "boleto"; } }

    @Service
    static class CheckoutService {
        final Map<String, PaymentGateway> gateways;
        final List<PaymentGateway> ordenados;
        CheckoutService(Map<String, PaymentGateway> gateways, List<PaymentGateway> ordenados) {
            this.gateways = gateways; this.ordenados = ordenados;
        }
        String pay(String method) { return gateways.get(method).name(); }
    }

    public static List<String> run() {
        List<String> out = new ArrayList<>();
        try (var ctx = new AnnotationConfigApplicationContext(Pix.class, Card.class, Boleto.class, CheckoutService.class)) {
            var s = ctx.getBean(CheckoutService.class);
            out.add("pay(\"pix\") -> " + s.pay("pix"));
            out.add("pay(\"boleto\") -> " + s.pay("boleto"));
            out.add("chaves do Map -> " + new TreeMap<>(s.gateways).keySet());
            out.add("List ordenada por @Order -> " + s.ordenados.stream().map(PaymentGateway::name).toList());
        }
        return out;
    }

    public static void main(String[] args) { run().forEach(System.out::println); }
}
