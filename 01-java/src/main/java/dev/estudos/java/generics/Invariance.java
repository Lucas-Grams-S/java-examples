package dev.estudos.java.generics;

import java.util.ArrayList;
import java.util.List;

/**
 * J06-03 · Invariância: {@code List<Integer>} não é {@code List<Number>}.
 * Documento: java-docs/01-java/06-generics.md §2.3.
 *
 * <p>Saída real (JDK 25):
 * <pre>
 * array covariante -&gt; ArrayStoreException: java.lang.Double
 * List&lt;? extends Number&gt; aceita List&lt;Integer&gt; -&gt; [1, 2]
 * Integer[] é Number[] -&gt; true
 * ArrayList&lt;Integer&gt; e ArrayList&lt;Number&gt; têm a mesma classe -&gt; true
 * </pre>
 *
 * <p>Variações: descomente {@code List<Number> nums = new ArrayList<Integer>();} e leia o erro do compilador.
 */
public class Invariance {
    public static List<String> run() {
        List<String> out = new ArrayList<>();
        Number[] arr = new Integer[1];   // arrays são covariantes
        try {
            arr[0] = 1.5;                // compila
        } catch (ArrayStoreException e) {
            out.add("array covariante -> ArrayStoreException: " + e.getMessage());
        }
        // List<Number> nums = new ArrayList<Integer>();   // NÃO compila: incompatible types
        List<? extends Number> readOnly = new ArrayList<Integer>(List.of(1, 2));
        out.add("List<? extends Number> aceita List<Integer> -> " + readOnly);
        out.add("Integer[] é Number[] -> " + (Number[].class.isAssignableFrom(Integer[].class)));
        out.add("ArrayList<Integer> e ArrayList<Number> têm a mesma classe -> "
                + (new ArrayList<Integer>().getClass() == new ArrayList<Number>().getClass()));
        return out;
    }

    public static void main(String[] args) { run().forEach(System.out::println); }
}
