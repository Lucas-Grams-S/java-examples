package dev.estudos.java.funcional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import dev.estudos.java.funcional.Domain.*;

/**
 * J09-14 · Estratégia como lambda: DiscountPolicy escolhida em um Map de políticas.
 * Documento: java-docs/01-java/09-programacao-funcional.md §5.4.
 *
 * <p>Saída real (JDK 25):
 * <pre>
 * none -&gt; pedido1=120 pedido4=80
 * tenOff -&gt; pedido1=108 pedido4=72
 * bigOrders -&gt; pedido1=108 pedido4=80
 * </pre>
 *
 * <p>Variações: adicione uma política 'frete grátis' como nova lambda.
 */
public class StrategyLambda {
    @FunctionalInterface
    interface DiscountPolicy { BigDecimal apply(Order order); }

    public static List<String> run() {
        DiscountPolicy none = Order::total;
        DiscountPolicy tenOff = o -> o.total().multiply(new BigDecimal("0.90"));
        DiscountPolicy bigOrders = o -> o.total().compareTo(new BigDecimal("100")) > 0 ? tenOff.apply(o) : o.total();
        Map<String, DiscountPolicy> policies = new LinkedHashMap<>();
        policies.put("none", none); policies.put("tenOff", tenOff); policies.put("bigOrders", bigOrders);
        List<String> out = new ArrayList<>();
        Order o1 = Domain.orders().get(0), o4 = Domain.orders().get(3);
        policies.forEach((name, p) -> out.add(name + " -> pedido1=" + p.apply(o1).stripTrailingZeros().toPlainString()
                + " pedido4=" + p.apply(o4).stripTrailingZeros().toPlainString()));
        return out;
    }

    public static void main(String[] args) { run().forEach(System.out::println); }
}
