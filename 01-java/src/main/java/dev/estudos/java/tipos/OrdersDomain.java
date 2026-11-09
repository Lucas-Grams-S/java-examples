package dev.estudos.java.tipos;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * J03-11 · Domínio de pedidos com identidade e valor.
 * Documento: java-docs/01-java/03-tipos-e-memoria.md §5.1.
 *
 * <p>{@link Money} é objeto de valor (record: igualdade por todos os componentes); {@link Customer} é entidade
 * (igualdade pela identidade de negócio, o id).
 *
 * <p>Saída real (JDK 25):
 * <pre>
 * 10 + 5,50 -&gt; Money[amount=15.50, currency=BRL]
 * somar moedas diferentes -&gt; moedas diferentes
 * mesmo id, nomes diferentes → equals -&gt; true
 * depois de renomear, hashCode igual -&gt; true
 * </pre>
 */
public class OrdersDomain {
    // Valor: igualdade por conteúdo → record
    public record Money(BigDecimal amount, String currency) {
        public Money {
            Objects.requireNonNull(amount);
            Objects.requireNonNull(currency);
        }
        public Money plus(Money other) {
            if (!currency.equals(other.currency)) throw new IllegalArgumentException("moedas diferentes");
            return new Money(amount.add(other.amount), currency);
        }
    }

    // Entidade: igualdade pela identidade de negócio (id), não por todos os campos
    public static final class Customer {
        private final UUID id;
        private String name;                       // mutável, mas fora de equals/hashCode

        public Customer(UUID id, String name) { this.id = id; this.name = name; }
        public void rename(String name) { this.name = name; }
        public String name() { return name; }

        @Override public boolean equals(Object o) { return o instanceof Customer c && id.equals(c.id); }
        @Override public int hashCode() { return id.hashCode(); }
    }

    public static List<String> run() {
        List<String> out = new ArrayList<>();
        var brl = (java.util.function.Function<String, Money>) v -> new Money(new BigDecimal(v), "BRL");
        out.add("10 + 5,50 -> " + brl.apply("10").plus(brl.apply("5.50")));
        try {
            brl.apply("1").plus(new Money(BigDecimal.ONE, "USD"));
        } catch (IllegalArgumentException e) {
            out.add("somar moedas diferentes -> " + e.getMessage());
        }
        UUID id = UUID.fromString("00000000-0000-0000-0000-000000000001");
        Customer a = new Customer(id, "Ana"), b = new Customer(id, "Ana Souza");
        out.add("mesmo id, nomes diferentes → equals -> " + a.equals(b));
        b.rename("Outro nome");
        out.add("depois de renomear, hashCode igual -> " + (a.hashCode() == b.hashCode()));
        return out;
    }

    public static void main(String[] args) {
        run().forEach(System.out::println);
    }
}
