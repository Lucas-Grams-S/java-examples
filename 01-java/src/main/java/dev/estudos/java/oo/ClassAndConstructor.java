package dev.estudos.java.oo;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * J04-01 · Classe, objeto e construtor.
 * Documento: java-docs/01-java/04-orientacao-a-objetos.md §2.1.
 *
 * <p>Mostra a ordem de inicialização (campo/bloco de instância → corpo do construtor) e a delegação com {@code this(...)}.
 *
 * <p>Saída real (JDK 25):
 * <pre>
 * Product -&gt; X1 / Caneta
 * this(...) -&gt; sem nome
 * sku nulo -&gt; NullPointerException
 * ordem de inicialização -&gt; campo a &gt; bloco de instância &gt; campo b &gt; corpo do construtor
 * </pre>
 *
 * <p>Variações: troque a ordem do bloco de instância e do campo; remova o construtor e veja o padrão deixar de existir.
 */
public class ClassAndConstructor {
    public static class Product {
        private final String sku;
        private final String name;

        public Product(String sku, String name) {
            this.sku = Objects.requireNonNull(sku);
            this.name = Objects.requireNonNull(name);
        }

        public Product(String sku) { this(sku, "sem nome"); }   // delega a outro construtor

        public String sku() { return sku; }
        public String name() { return name; }
    }

    static class Trace {
        static final List<String> LOG = new ArrayList<>();
        String a = log("campo a");
        { log("bloco de instância"); }
        String b = log("campo b");
        Trace() { log("corpo do construtor"); }
        static String log(String s) { LOG.add(s); return s; }
    }

    public static List<String> run() {
        List<String> out = new ArrayList<>();
        var p = new Product("X1", "Caneta");
        out.add("Product -> " + p.sku() + " / " + p.name());
        out.add("this(...) -> " + new Product("X2").name());
        try {
            new Product(null, "x");
        } catch (NullPointerException e) {
            out.add("sku nulo -> NullPointerException");
        }
        Trace.LOG.clear();
        new Trace();
        out.add("ordem de inicialização -> " + String.join(" > ", Trace.LOG));
        return out;
    }

    public static void main(String[] args) { run().forEach(System.out::println); }
}
