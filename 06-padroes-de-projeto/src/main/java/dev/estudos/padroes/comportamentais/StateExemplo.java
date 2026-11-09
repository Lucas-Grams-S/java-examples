package dev.estudos.padroes.comportamentais;

import java.util.ArrayList;
import java.util.List;

/**
 * DP04-12 · State: {@code sealed} + {@code switch} com guardas e o {@code enum} com corpo por constante.
 * Documento: java-docs/06-padroes-de-projeto/04-comportamentais.md §5.7.
 *
 * <p>Saída real (JDK 25):
 * <pre>
 * New -&gt; pay -&gt; ship = Shipped[]
 * cancel em Shipped -&gt; Shipped não aceita cancel
 * enum: RED.next().next() = YELLOW
 * </pre>
 *
 * <p>Variações: acrescente o evento {@code deliver} (de {@code Shipped} para um novo estado) e veja o que o compilador exige.
 */
public class StateExemplo {

    sealed interface OrderState permits New, Paid, Shipped, Cancelled {}
    record New() implements OrderState {}
    record Paid() implements OrderState {}
    record Shipped() implements OrderState {}
    record Cancelled() implements OrderState {}

    static OrderState next(OrderState s, String ev) {
        return switch (s) {
            case New n when ev.equals("pay") -> new Paid();
            case New n when ev.equals("cancel") -> new Cancelled();
            case Paid p when ev.equals("ship") -> new Shipped();
            case Paid p when ev.equals("cancel") -> new Cancelled();
            default -> throw new IllegalStateException(s.getClass().getSimpleName() + " não aceita " + ev);
        };
    }

    enum Light {
        RED { Light next() { return GREEN; } },
        GREEN { Light next() { return YELLOW; } },
        YELLOW { Light next() { return RED; } };
        abstract Light next();
    }

    public static List<String> run() {
        List<String> out = new ArrayList<>();
        OrderState s = new New();
        s = next(next(s, "pay"), "ship");
        out.add("New -> pay -> ship = " + s);
        try {
            next(s, "cancel");
        } catch (IllegalStateException e) {
            out.add("cancel em Shipped -> " + e.getMessage());
        }
        out.add("enum: RED.next().next() = " + Light.RED.next().next());
        return out;
    }

    public static void main(String[] args) { run().forEach(System.out::println); }
}
