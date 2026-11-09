package dev.estudos.java.reflection;

import java.lang.reflect.RecordComponent;
import java.util.ArrayList;
import java.util.List;

/**
 * J10-13 · Inspecionando um record via getRecordComponents (como o Jackson faz).
 * Documento: java-docs/01-java/10-anotacoes-e-reflection.md §5.4.
 *
 * <p>Saída real (JDK 25):
 * <pre>
 * Point -&gt; x=3 y=4
 * Person -&gt; name=Ana tags=[a, b]
 * é record -&gt; true
 * tipo genérico preservado -&gt; java.util.List&lt;java.lang.String&gt;
 * </pre>
 *
 * <p>Variações: adicione uma anotação RUNTIME num componente e leia-a em rc.getAnnotation.
 */
public class RecordInspection {
    record Point(int x, int y) {}
    record Person(String name, java.util.List<String> tags) {}

    static String dump(Object rec) throws Exception {
        StringBuilder sb = new StringBuilder();
        for (RecordComponent rc : rec.getClass().getRecordComponents())
            sb.append(rc.getName()).append('=').append(rc.getAccessor().invoke(rec)).append(' ');
        return sb.toString().trim();
    }

    public static List<String> run() throws Exception {
        List<String> out = new ArrayList<>();
        out.add("Point -> " + dump(new Point(3, 4)));
        out.add("Person -> " + dump(new Person("Ana", List.of("a", "b"))));
        out.add("é record -> " + Point.class.isRecord());
        RecordComponent rc = Person.class.getRecordComponents()[1];
        out.add("tipo genérico preservado -> " + rc.getGenericType().getTypeName());
        return out;
    }

    public static void main(String[] args) throws Exception { run().forEach(System.out::println); }
}
