package dev.estudos.java.modernos;

import java.util.ArrayList;
import java.util.List;

/**
 * J05-03 · Switch expressions.
 * Documento: java-docs/01-java/05-recursos-modernos-da-linguagem.md §2.3.
 *
 * <p>Saída real (JDK 25):
 * <pre>
 * CREATED -&gt; 1
 * PAID -&gt; 4
 * SHIPPED -&gt; 0
 * SAT -&gt; fim de semana; MON -&gt; dia útil
 * switch antigo, n=1 (fall-through) -&gt; um dois fim
 * </pre>
 *
 * <p>Variações: adicione uma constante ao {@code enum} e veja {@code days} deixar de compilar.
 */
public class SwitchExpressions {
    enum OrderStatus { CREATED, PAID, SHIPPED }

    static int days(OrderStatus status) {
        return switch (status) {
            case CREATED -> 1;
            case PAID -> { int t = 2; yield t * 2; }
            case SHIPPED -> 0;
        };
    }

    static String kind(String day) {
        return switch (day) {
            case "SAT", "SUN" -> "fim de semana";
            default -> "dia útil";
        };
    }

    @SuppressWarnings("fallthrough")
    static String oldStyle(int n) {
        String r = "";
        switch (n) {
            case 1: r += "um ";
            case 2: r += "dois ";   // fall-through acidental
            default: r += "fim";
        }
        return r;
    }

    public static List<String> run() {
        List<String> out = new ArrayList<>();
        for (var s : OrderStatus.values()) out.add(s + " -> " + days(s));
        out.add("SAT -> " + kind("SAT") + "; MON -> " + kind("MON"));
        out.add("switch antigo, n=1 (fall-through) -> " + oldStyle(1));
        return out;
    }

    public static void main(String[] args) { run().forEach(System.out::println); }
}
