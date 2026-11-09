package dev.estudos.padroes.arquiteturais;

import dev.estudos.padroes.arquiteturais.EventosDoPedido.*;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * DP06-15 · Event Sourcing: o estado é a dobra (fold) dos eventos do log.
 * Documento: java-docs/06-padroes-de-projeto/06-padroes-arquiteturais.md §5.4.
 *
 * <p>Saída real (JDK 25):
 * <pre>
 * eventos no log = 3
 * replay(1) -&gt; OrderState[id=1, total=50, paid=true]
 * replay(2) -&gt; OrderState[id=2, total=9, paid=false]
 * assinante viu -&gt; [Placed, Placed, Paid]
 * </pre>
 */
public class EventSourcingDobra {
    record OrderState(long id, BigDecimal total, boolean paid) {
        static OrderState empty() { return new OrderState(0, BigDecimal.ZERO, false); }
        OrderState apply(EventosDoPedido e) {                       // evolve
            return switch (e) {
                case Placed p -> new OrderState(p.id(), p.total(), false);
                case Paid p   -> new OrderState(id, total, true);
            };
        }
    }

    static class EventStore {
        private final List<EventosDoPedido> log = new ArrayList<>();      // append-only
        private final List<Consumer<EventosDoPedido>> subs = new ArrayList<>();
        void append(EventosDoPedido e) { log.add(e); subs.forEach(s -> s.accept(e)); }
        void subscribe(Consumer<EventosDoPedido> s) { subs.add(s); }
        int size() { return log.size(); }
        OrderState replay(long id) {
            return log.stream()
                .filter(e -> switch (e) { case Placed p -> p.id() == id; case Paid p -> p.id() == id; })
                .reduce(OrderState.empty(), OrderState::apply, (a, b) -> b);
        }
    }

    public static List<String> run() {
        List<String> out = new ArrayList<>();
        var store = new EventStore();
        List<String> assinante = new ArrayList<>();
        store.subscribe(e -> assinante.add(e.getClass().getSimpleName()));
        store.append(new Placed(1, new BigDecimal("50")));
        store.append(new Placed(2, new BigDecimal("9")));
        store.append(new Paid(1));
        out.add("eventos no log = " + store.size());
        out.add("replay(1) -> " + store.replay(1));
        out.add("replay(2) -> " + store.replay(2));
        out.add("assinante viu -> " + assinante);
        return out;
    }

    public static void main(String[] args) { run().forEach(System.out::println); }
}
