package dev.estudos.java.oo;

import java.util.ArrayList;
import java.util.List;

/**
 * J04-03 · Herança.
 * Documento: java-docs/01-java/04-orientacao-a-objetos.md §2.3.
 *
 * <p>Saída real (JDK 25):
 * <pre>
 * describe -&gt; Payment 42 via Pix
 * p instanceof Payment -&gt; true
 * superclasse de PixPayment -&gt; Payment
 * superclasse de Payment -&gt; java.lang.Object
 * </pre>
 *
 * <p>Variações: marque {@code describe()} como {@code final} e veja o erro de compilação; tire o {@code @Override}.
 */
public class Inheritance {
    public static class Payment {
        protected final String id;
        public Payment(String id) { this.id = id; }
        public String describe() { return "Payment " + id; }
    }

    public static class PixPayment extends Payment {
        public PixPayment(String id) { super(id); }
        @Override public String describe() { return super.describe() + " via Pix"; }
    }

    public static List<String> run() {
        List<String> out = new ArrayList<>();
        Payment p = new PixPayment("42");
        out.add("describe -> " + p.describe());
        out.add("p instanceof Payment -> " + (p instanceof Payment));
        out.add("superclasse de PixPayment -> " + PixPayment.class.getSuperclass().getSimpleName());
        out.add("superclasse de Payment -> " + Payment.class.getSuperclass().getName());
        return out;
    }

    public static void main(String[] args) { run().forEach(System.out::println); }
}
