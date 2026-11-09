package dev.estudos.java.modernos;

import dev.estudos.java.modernos.SealedTypes.Boleto;
import dev.estudos.java.modernos.SealedTypes.Card;
import dev.estudos.java.modernos.SealedTypes.Payment;
import dev.estudos.java.modernos.SealedTypes.Pix;
import java.util.ArrayList;
import java.util.List;

/**
 * J05-07 · Pattern matching em {@code switch} e record patterns.
 * Documento: java-docs/01-java/05-recursos-modernos-da-linguagem.md §2.7.
 *
 * <p>Saída real (JDK 25):
 * <pre>
 * fee Pix -&gt; 0
 * fee Card 3x -&gt; parcelado 3
 * fee Card 1x -&gt; à vista 1234
 * fee Boleto -&gt; 3.50
 * desc -&gt; nulo  grande 42  int 5  str 3  linha 1 Point[x=3, y=4]  outro
 * switch sem case null -&gt; NullPointerException
 * </pre>
 *
 * <p>Variações: inverta a ordem dos dois {@code case Card} (erro: rótulo dominado); remova {@code case null}.
 */
public class SwitchPatterns {
    record Point(int x, int y) {}
    record Line(Point a, Point b) {}

    static String fee(Payment p) {
        return switch (p) {
            case Pix _ -> "0";
            case Card(var last4, var n) when n > 1 -> "parcelado " + n;
            case Card c -> "à vista " + c.last4();
            case Boleto _ -> "3.50";
        };
    }

    static String desc(Object o) {
        return switch (o) {
            case null -> "nulo";
            case Integer i when i > 10 -> "grande " + i;
            case Integer i -> "int " + i;
            case String s -> "str " + s.length();
            case Line(Point(var x1, _), Point p2) -> "linha " + x1 + " " + p2;
            default -> "outro";
        };
    }

    public static List<String> run() {
        List<String> out = new ArrayList<>();
        out.add("fee Pix -> " + fee(new Pix("k")));
        out.add("fee Card 3x -> " + fee(new Card("1234", 3)));
        out.add("fee Card 1x -> " + fee(new Card("1234", 1)));
        out.add("fee Boleto -> " + fee(new Boleto("000")));
        out.add("desc -> " + String.join("  ", desc(null), desc(42), desc(5), desc("abc"),
                desc(new Line(new Point(1, 2), new Point(3, 4))), desc(3.5)));
        try {
            String s = null;
            switch (s) { case "a" -> {} default -> {} }
        } catch (NullPointerException e) {
            out.add("switch sem case null -> NullPointerException");
        }
        return out;
    }

    public static void main(String[] args) { run().forEach(System.out::println); }
}
