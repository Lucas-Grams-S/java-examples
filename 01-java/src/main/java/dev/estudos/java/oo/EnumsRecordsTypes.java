package dev.estudos.java.oo;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * J04-07 · Enums, records e tipos especiais.
 * Documento: java-docs/01-java/04-orientacao-a-objetos.md §2.7.
 *
 * <p>Saída real (JDK 25):
 * <pre>
 * valores -&gt; [CREATED, PAID, SHIPPED, CANCELLED]
 * canCancel -&gt; [CREATED, PAID]
 * constante é singleton -&gt; true
 * ordinal de SHIPPED -&gt; 2
 * record toString -&gt; Point[x=1, y=2]
 * record equals -&gt; true
 * record hashCode igual -&gt; true
 * sealed + switch exaustivo -&gt; círculo r=2.0
 * toString do Object -&gt; true
 * </pre>
 *
 * <p>Variações: adicione um estado ao enum e acompanhe o {@code switch} em {@code label}.
 */
public class EnumsRecordsTypes {
    public enum OrderStatus {
        CREATED, PAID, SHIPPED, CANCELLED;
        public boolean canCancel() { return this == CREATED || this == PAID; }
    }

    record Point(int x, int y) {}

    sealed interface Shape permits Circle, Square {}
    record Circle(double r) implements Shape {}
    record Square(double side) implements Shape {}

    public static List<String> run() {
        List<String> out = new ArrayList<>();
        out.add("valores -> " + Arrays.toString(OrderStatus.values()));
        out.add("canCancel -> " + Arrays.stream(OrderStatus.values()).filter(OrderStatus::canCancel).toList());
        out.add("constante é singleton -> " + (OrderStatus.valueOf("PAID") == OrderStatus.PAID));
        out.add("ordinal de SHIPPED -> " + OrderStatus.SHIPPED.ordinal());
        var p = new Point(1, 2);
        out.add("record toString -> " + p);
        out.add("record equals -> " + p.equals(new Point(1, 2)));
        out.add("record hashCode igual -> " + (p.hashCode() == new Point(1, 2).hashCode()));
        Shape s = new Circle(2);
        String d = switch (s) {
            case Circle c -> "círculo r=" + c.r();
            case Square q -> "quadrado " + q.side();
        };
        out.add("sealed + switch exaustivo -> " + d);
        out.add("toString do Object -> " + new Object().toString().startsWith("java.lang.Object@"));
        return out;
    }

    public static void main(String[] args) { run().forEach(System.out::println); }
}
