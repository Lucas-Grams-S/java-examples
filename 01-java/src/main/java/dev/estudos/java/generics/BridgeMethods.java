package dev.estudos.java.generics;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * J06-09 · Métodos ponte.
 * Documento: java-docs/01-java/06-generics.md §3.4.
 *
 * <p>Saída real (JDK 25):
 * <pre>
 * compareTo(Name) -&gt; bridge=false synthetic=false
 * compareTo(Object) -&gt; bridge=true synthetic=true
 * chamada pelo tipo raw -&gt; ClassCastException lançada pela ponte
 * </pre>
 *
 * <p>Variações: rode {@code javap -p -cp 01-java/target/classes 'dev.estudos.java.generics.BridgeMethods$Name'}.
 */
public class BridgeMethods {
    static class Name implements Comparable<Name> {
        final String value;
        Name(String value) { this.value = value; }
        @Override public int compareTo(Name o) { return value.compareTo(o.value); }
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    static String viaRaw() {
        Comparable raw = new Name("a");
        try {
            raw.compareTo("não sou Name");   // passa pela ponte compareTo(Object), que faz o cast
            return "sem erro";
        } catch (ClassCastException e) {
            return "ClassCastException lançada pela ponte";
        }
    }

    public static List<String> run() {
        List<String> out = new ArrayList<>();
        var methods = Arrays.stream(Name.class.getDeclaredMethods()).filter(m -> m.getName().equals("compareTo"))
                .sorted(java.util.Comparator.comparing((Method m) -> m.getParameterTypes()[0].getSimpleName())).toList();
        for (Method m : methods)
            out.add("compareTo(" + m.getParameterTypes()[0].getSimpleName() + ") -> bridge=" + m.isBridge() + " synthetic=" + m.isSynthetic());
        out.add("chamada pelo tipo raw -> " + viaRaw());
        return out;
    }

    public static void main(String[] args) { run().forEach(System.out::println); }
}
