package dev.estudos.java.modernos;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * J05-01 · {@code var}: inferência de tipo local.
 * Documento: java-docs/01-java/05-recursos-modernos-da-linguagem.md §2.1.
 *
 * <p>Saída real (JDK 25):
 * <pre>
 * var orders = new ArrayList&lt;String&gt;() -&gt; ArrayList
 * for (var entry) -&gt; ana=[o1, o2]
 * var + diamante infere ArrayList&lt;Object&gt; -&gt; [1, dois]
 * var n = 1 -&gt; Integer; var d = 1.0f -&gt; Float
 * var var = 1 -&gt; 1
 * tipo estático continua fixo (Map&lt;String,Integer&gt; explícito) -&gt; true
 * </pre>
 *
 * <p>Variações: tente {@code var x = null;}, {@code var f = () -> 1;} e {@code var} num campo (nenhum compila).
 */
public class VarLocal {
    public static List<String> run() {
        List<String> out = new ArrayList<>();
        var orders = new ArrayList<String>();
        out.add("var orders = new ArrayList<String>() -> " + orders.getClass().getSimpleName());
        var byCustomer = new HashMap<String, List<String>>();
        byCustomer.put("ana", List.of("o1", "o2"));
        for (var entry : byCustomer.entrySet()) out.add("for (var entry) -> " + entry.getKey() + "=" + entry.getValue());
        var diamond = new ArrayList<>();
        diamond.add(1);
        diamond.add("dois");
        out.add("var + diamante infere ArrayList<Object> -> " + diamond);
        var n = 1;
        var d = 1.0f;
        out.add("var n = 1 -> " + ((Object) n).getClass().getSimpleName() + "; var d = 1.0f -> " + ((Object) d).getClass().getSimpleName());
        var var = 1;   // 'var' é nome de tipo reservado, não palavra reservada
        out.add("var var = 1 -> " + var);
        Map<String, Integer> explicit = Map.of();
        out.add("tipo estático continua fixo (Map<String,Integer> explícito) -> " + explicit.isEmpty());
        return out;
    }

    public static void main(String[] args) { run().forEach(System.out::println); }
}
