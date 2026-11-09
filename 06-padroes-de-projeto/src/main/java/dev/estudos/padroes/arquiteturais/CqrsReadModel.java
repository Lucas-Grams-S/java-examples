package dev.estudos.padroes.arquiteturais;

import dev.estudos.padroes.arquiteturais.EventosDoPedido.*;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * DP06-14 · CQRS: o lado de escrita publica eventos; o lado de leitura mantém uma projeção.
 * Documento: java-docs/06-padroes-de-projeto/06-padroes-arquiteturais.md §5.3.
 *
 * <p>Saída real (JDK 25):
 * <pre>
 * após Placed(1, 50) -&gt; &#123;1=OrderSummary[id=1, total=50, paid=false]&#125;
 * após Paid(1) e Placed(2, 9) -&gt; &#123;1=OrderSummary[id=1, total=50, paid=true], 2=OrderSummary[id=2, total=9, paid=false]&#125;
 * Paid de pedido desconhecido não cria nada -&gt; [1, 2]
 * </pre>
 */
public class CqrsReadModel {
    record OrderSummary(long id, BigDecimal total, boolean paid) {}

    static class OrderReadModel {                       // lado da consulta
        final Map<Long, OrderSummary> view = new TreeMap<>();
        void on(EventosDoPedido e) {
            switch (e) {                                // sem default: o compilador exige todos os casos
                case Placed p -> view.put(p.id(), new OrderSummary(p.id(), p.total(), false));
                case Paid p   -> view.computeIfPresent(p.id(), (k, v) -> new OrderSummary(k, v.total(), true));
            }
        }
    }

    public static List<String> run() {
        List<String> out = new ArrayList<>();
        var rm = new OrderReadModel();
        rm.on(new Placed(1, new BigDecimal("50")));
        out.add("após Placed(1, 50) -> " + rm.view);
        rm.on(new Paid(1));
        rm.on(new Placed(2, new BigDecimal("9")));
        out.add("após Paid(1) e Placed(2, 9) -> " + rm.view);
        rm.on(new Paid(99));
        out.add("Paid de pedido desconhecido não cria nada -> " + rm.view.keySet());
        return out;
    }

    public static void main(String[] args) { run().forEach(System.out::println); }
}
