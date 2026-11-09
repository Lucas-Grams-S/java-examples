package dev.estudos.java.evolucao;

import java.math.BigDecimal;
import java.util.List;

/**
 * J00-07 · Java 14 a 21: o "Java moderno".
 * Documento: java-docs/01-java/00-evolucao-do-java.md §4.7.
 *
 * <p>Switch expression, {@code instanceof} com padrão, text block, record, sealed e pattern matching em switch.
 *
 * <p>Saída real (JDK 25):
 * <pre>
 * switch expression -&gt; Pago / Outro
 * instanceof com padrão -&gt; dinheiro positivo 9.90
 * record -&gt; Money[amount=1, currency=BRL]
 * sealed + record pattern -&gt; Cartão 3456; Pix a@b.c
 * text block -&gt; 3 linhas, termina com \n: true
 * </pre>
 *
 * <p>Variações: remova o {@code case Pix} e veja o erro de exaustividade; adicione um terceiro tipo ao {@code permits}.
 */
public class ModernJava {
    enum Status { NEW, PAID, SHIPPED }
    record Money(BigDecimal amount, String currency) {}
    sealed interface PaymentMethod permits Card, Pix {}
    record Card(String number) implements PaymentMethod {}
    record Pix(String key) implements PaymentMethod {}

    static String label(Status s) {
        return switch (s) {
            case NEW -> "Novo";
            case PAID -> "Pago";
            default -> "Outro";
        };
    }

    static String describe(PaymentMethod p) {
        return switch (p) {                       // exaustivo: sem default
            case Card(String n) -> "Cartão " + n.substring(n.length() - 4);
            case Pix(String k) -> "Pix " + k;
        };
    }

    static String positive(Object obj) {
        if (obj instanceof Money m && m.amount().signum() > 0) return "dinheiro positivo " + m.amount();
        return "outro";
    }

    public static List<String> run() {
        String sql = """
                SELECT *
                FROM orders
                WHERE id = ?
                """;
        return List.of("switch expression -> " + label(Status.PAID) + " / " + label(Status.SHIPPED),
                "instanceof com padrão -> " + positive(new Money(new BigDecimal("9.90"), "BRL")),
                "record -> " + new Money(new BigDecimal("1"), "BRL"),
                "sealed + record pattern -> " + describe(new Card("1234567890123456")) + "; " + describe(new Pix("a@b.c")),
                "text block -> " + sql.lines().count() + " linhas, termina com \\n: " + sql.endsWith("\n"));
    }

    public static void main(String[] args) {
        run().forEach(System.out::println);
    }
}
