package dev.estudos.java.generics;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * J06-10 · O que sobrevive ao apagamento: metadados ({@code Signature}) e super type token.
 * Documento: java-docs/01-java/06-generics.md §3.5.
 *
 * <p>Saída real (JDK 25):
 * <pre>
 * Field.getType() -&gt; java.util.List
 * Field.getGenericType() -&gt; java.util.List&lt;java.lang.String&gt;
 * Map aninhado -&gt; java.util.Map&lt;java.lang.String, java.util.List&lt;java.lang.Integer&gt;&gt;
 * super type token -&gt; java.util.Map&lt;java.lang.String, java.lang.Integer&gt;
 * objeto em execução não sabe: java.util.AbstractList&lt;E&gt;
 * </pre>
 *
 * <p>Variações: crie {@code new TypeRef<List<String>>()} sem as chaves {@code {}} e veja o erro de cast (não é subclasse anônima).
 */
public class SignatureMetadata {
    static class Holder {
        List<String> names;
        Map<String, List<Integer>> index;
    }

    public abstract static class TypeRef<T> {
        final Type type = ((ParameterizedType) getClass().getGenericSuperclass()).getActualTypeArguments()[0];
    }

    public static List<String> run() throws NoSuchFieldException {
        List<String> out = new ArrayList<>();
        out.add("Field.getType() -> " + Holder.class.getDeclaredField("names").getType().getName());
        out.add("Field.getGenericType() -> " + Holder.class.getDeclaredField("names").getGenericType());
        out.add("Map aninhado -> " + Holder.class.getDeclaredField("index").getGenericType());
        var ref = new TypeRef<Map<String, Integer>>() {};
        out.add("super type token -> " + ref.type);
        out.add("objeto em execução não sabe: " + new ArrayList<String>().getClass().getGenericSuperclass());
        return out;
    }

    public static void main(String[] args) throws Exception { run().forEach(System.out::println); }
}
