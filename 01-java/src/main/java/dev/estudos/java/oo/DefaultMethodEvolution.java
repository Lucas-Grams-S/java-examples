package dev.estudos.java.oo;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * J04-14 · Interface {@code default} evoluindo um contrato sem quebrar implementações.
 * Documento: java-docs/01-java/04-orientacao-a-objetos.md §5.3.
 *
 * <p>{@code LegacyGateway} representa uma classe antiga, escrita antes de {@code refund} existir.
 *
 * <p>Saída real (JDK 25):
 * <pre>
 * legado charge -&gt; cobrado 10
 * legado refund -&gt; UnsupportedOperationException: refund não suportado
 * moderno refund -&gt; estornado 10
 * </pre>
 *
 * <p>Variações: troque o {@code default} por método abstrato e veja {@code LegacyGateway} deixar de compilar.
 */
public class DefaultMethodEvolution {
    public interface PaymentGateway {
        String charge(BigDecimal amount);
        default String refund(BigDecimal amount) { throw new UnsupportedOperationException("refund não suportado"); }
    }

    static class LegacyGateway implements PaymentGateway {
        @Override public String charge(BigDecimal amount) { return "cobrado " + amount; }
    }

    static class ModernGateway implements PaymentGateway {
        @Override public String charge(BigDecimal amount) { return "cobrado " + amount; }
        @Override public String refund(BigDecimal amount) { return "estornado " + amount; }
    }

    public static List<String> run() {
        List<String> out = new ArrayList<>();
        var ten = BigDecimal.TEN;
        out.add("legado charge -> " + new LegacyGateway().charge(ten));
        try {
            new LegacyGateway().refund(ten);
        } catch (UnsupportedOperationException e) {
            out.add("legado refund -> UnsupportedOperationException: " + e.getMessage());
        }
        out.add("moderno refund -> " + new ModernGateway().refund(ten));
        return out;
    }

    public static void main(String[] args) { run().forEach(System.out::println); }
}
