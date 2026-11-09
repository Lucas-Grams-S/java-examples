package dev.estudos.java.generics;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;
import java.util.function.IntFunction;

/**
 * J06-08 · Reificação e arrays genéricos.
 * Documento: java-docs/01-java/06-generics.md §3.3 (e a tabela de restrições de §3.2).
 *
 * <p>Saída real (JDK 25):
 * <pre>
 * new String[1] é reificado -&gt; String[]
 * List&lt;String&gt; e List&lt;Integer&gt; -&gt; mesma classe: true
 * newArray(String.class, 2) -&gt; String[] length=2
 * IntFunction&lt;T[]&gt; (String[]::new) -&gt; String[]
 * stream.toArray(String[]::new) -&gt; String[]
 * instanceof List&lt;?&gt; (único permitido) -&gt; true
 * array de List (sem argumento) -&gt; List[]
 * </pre>
 *
 * <p>Variações: tente {@code new List<String>[10]} (erro: generic array creation) e {@code obj instanceof List<String>}.
 */
public class ReificationArrays {
    @SuppressWarnings("unchecked")   // Array.newInstance devolve o tipo certo em execução
    static <T> T[] newArray(Class<T> type, int n) { return (T[]) Array.newInstance(type, n); }

    static <T> T[] fromFactory(IntFunction<T[]> factory, int n) { return factory.apply(n); }

    public static List<String> run() {
        List<String> out = new ArrayList<>();
        out.add("new String[1] é reificado -> " + new String[1].getClass().getSimpleName());
        out.add("List<String> e List<Integer> -> mesma classe: "
                + (new ArrayList<String>().getClass() == new ArrayList<Integer>().getClass()));
        String[] a = newArray(String.class, 2);
        out.add("newArray(String.class, 2) -> " + a.getClass().getSimpleName() + " length=" + a.length);
        out.add("IntFunction<T[]> (String[]::new) -> " + fromFactory(String[]::new, 3).getClass().getSimpleName());
        out.add("stream.toArray(String[]::new) -> " + List.of("x", "y").stream().toArray(String[]::new).getClass().getSimpleName());
        Object obj = new ArrayList<String>();
        out.add("instanceof List<?> (único permitido) -> " + (obj instanceof List<?>));
        @SuppressWarnings("unchecked")
        Object[] poluido = (Object[]) new List[1];   // array de tipo apagado
        out.add("array de List (sem argumento) -> " + poluido.getClass().getSimpleName());
        return out;
    }

    public static void main(String[] args) { run().forEach(System.out::println); }
}
