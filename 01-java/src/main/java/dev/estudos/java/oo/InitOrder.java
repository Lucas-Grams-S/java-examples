package dev.estudos.java.oo;

import java.util.ArrayList;
import java.util.List;

/**
 * J04-10 · Ordem de inicialização e o perigo de chamar método sobrescrevível no construtor.
 * Documento: java-docs/01-java/04-orientacao-a-objetos.md §3.2.
 *
 * <p>Saída real (JDK 25):
 * <pre>
 * init key=null
 * depois do construtor key=k
 * </pre>
 *
 * <p>Variações: faça {@code key} {@code final} com valor constante ({@code "k"}): vira constante de compilação e
 * {@code init()} passa a ver {@code "k"}.
 */
public class InitOrder {
    static final List<String> LOG = new ArrayList<>();

    static abstract class Payment {
        final String id;
        Payment(String id) { this.id = id; init(); }
        void init() {}
        abstract String pay();
    }

    static class Pix extends Payment {
        String key = "k";
        Pix(String id) { super(id); }
        @Override void init() { LOG.add("init key=" + key); }
        @Override String pay() { return "pix"; }
    }

    public static List<String> run() {
        LOG.clear();
        var p = new Pix("1");
        LOG.add("depois do construtor key=" + p.key);
        return new ArrayList<>(LOG);
    }

    public static void main(String[] args) { run().forEach(System.out::println); }
}
