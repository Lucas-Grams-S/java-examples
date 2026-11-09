package dev.estudos.java.modernos;

import dev.estudos.java.modernos.SealedTypes.Boleto;
import dev.estudos.java.modernos.SealedTypes.Card;
import dev.estudos.java.modernos.SealedTypes.Payment;
import dev.estudos.java.modernos.SealedTypes.Pix;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * J05-11 · Antes e depois: modelagem de pagamento.
 * Documento: java-docs/01-java/05-recursos-modernos-da-linguagem.md §5.1.
 *
 * <p>Saída real (JDK 25):
 * <pre>
 * Pix[key=k] -&gt; antes=0.0 depois=0
 * Card[last4=1234, installments=3] -&gt; antes=2.5 depois=2.5
 * Card[last4=1234, installments=1] -&gt; antes=1.0 depois=1.0
 * Boleto[barcode=000] -&gt; antes=3.5 depois=3.5
 * antes: tipo fora da lista -&gt; IllegalArgumentException em execução
 * </pre>
 *
 * <p>Variações: adicione um subtipo a {@code Payment}: a versão "antes" continua compilando e quebra em execução; a "depois" não compila.
 */
public class PaymentBeforeAfter {
    /** ANTES (estilo Java 8): cadeia de instanceof + cast; esquecer um caso só falha em execução. */
    static double feeBefore(Object p) {
        if (p instanceof Pix) return 0;
        else if (p instanceof Card) {
            Card c = (Card) p;
            return c.installments() > 1 ? 2.5 : 1.0;
        } else if (p instanceof Boleto) return 3.5;
        throw new IllegalArgumentException("tipo desconhecido: " + p);
    }

    /** DEPOIS (Java 21+): sealed + records + switch exaustivo. */
    static BigDecimal fee(Payment p) {
        return switch (p) {
            case Pix _ -> BigDecimal.ZERO;
            case Card(_, var n) when n > 1 -> new BigDecimal("2.5");
            case Card _ -> new BigDecimal("1.0");
            case Boleto _ -> new BigDecimal("3.5");
        };
    }

    public static List<String> run() {
        List<String> out = new ArrayList<>();
        List<Payment> all = List.of(new Pix("k"), new Card("1234", 3), new Card("1234", 1), new Boleto("000"));
        for (Payment p : all) out.add(p + " -> antes=" + feeBefore(p) + " depois=" + fee(p));
        try {
            feeBefore("não é pagamento");
        } catch (IllegalArgumentException e) {
            out.add("antes: tipo fora da lista -> IllegalArgumentException em execução");
        }
        return out;
    }

    public static void main(String[] args) { run().forEach(System.out::println); }
}
