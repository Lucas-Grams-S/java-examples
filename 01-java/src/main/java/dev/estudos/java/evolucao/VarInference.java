package dev.estudos.java.evolucao;

import java.util.ArrayList;
import java.util.List;

/**
 * J00-05 · Java 10: {@code var}.
 * Documento: java-docs/01-java/00-evolucao-do-java.md §4.5.
 *
 * <p>Saída real (JDK 25):
 * <pre>
 * elemento -&gt; pedido-1 (String)
 * var n = 10 -&gt; tipo estático int; n + 1 = 11
 * var orders -&gt; ArrayList
 * </pre>
 *
 * <p>Variações: tente {@code var x;} ou {@code var n = null;} (não compilam: sem inicializador não há o que inferir).
 */
public class VarInference {
    public static List<String> run() {
        var orders = new ArrayList<String>();      // tipo inferido: ArrayList<String>
        orders.add("pedido-1");
        var out = new ArrayList<String>();
        for (var o : orders) out.add("elemento -> " + o + " (" + o.getClass().getSimpleName() + ")");
        var n = 10;                                  // int, estático: nada de tipagem dinâmica
        out.add("var n = 10 -> tipo estático int; n + 1 = " + (n + 1));
        out.add("var orders -> " + orders.getClass().getSimpleName());
        return out;
    }

    public static void main(String[] args) {
        run().forEach(System.out::println);
    }
}
