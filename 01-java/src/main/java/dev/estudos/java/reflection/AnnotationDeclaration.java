package dev.estudos.java.reflection;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.ArrayList;
import java.util.List;

/**
 * J10-01 · Declarando anotações: Retention, Target e elementos com default.
 * Documento: java-docs/01-java/10-anotacoes-e-reflection.md §2.1.
 *
 * <p>Saída real (JDK 25):
 * <pre>
 * RUNTIME visível -&gt; true
 * CLASS não visível por reflection -&gt; false
 * greet -&gt; value=greet level=2
 * plain (defaults) -&gt; value='' level=1
 * default de message -&gt; must not be blank
 * o objeto da anotação é um proxy -&gt; true
 * </pre>
 *
 * <p>Variações: troque RUNTIME por CLASS em RuntimeMark e veja isAnnotationPresent virar false.
 */
public class AnnotationDeclaration {
    @Retention(RetentionPolicy.RUNTIME) @Target(ElementType.FIELD)
    public @interface NotBlank { String message() default "must not be blank"; }

    @Retention(RetentionPolicy.RUNTIME) @Target(ElementType.METHOD)
    public @interface Audited { String value() default ""; int level() default 1; }

    @Retention(RetentionPolicy.CLASS) @Target(ElementType.TYPE)
    @interface ClassOnly {}

    @Retention(RetentionPolicy.RUNTIME) @Target(ElementType.TYPE)
    @interface RuntimeMark {}

    @ClassOnly @RuntimeMark
    static class Marked {
        @NotBlank String name;
        @Audited(value = "greet", level = 2) public void greet() {}
        @Audited public void plain() {}
    }

    public static List<String> run() throws Exception {
        List<String> out = new ArrayList<>();
        out.add("RUNTIME visível -> " + Marked.class.isAnnotationPresent(RuntimeMark.class));
        out.add("CLASS não visível por reflection -> " + Marked.class.isAnnotationPresent(ClassOnly.class));
        Audited a = Marked.class.getMethod("greet").getAnnotation(Audited.class);
        out.add("greet -> value=" + a.value() + " level=" + a.level());
        Audited d = Marked.class.getMethod("plain").getAnnotation(Audited.class);
        out.add("plain (defaults) -> value='" + d.value() + "' level=" + d.level());
        out.add("default de message -> " + Marked.class.getDeclaredField("name").getAnnotation(NotBlank.class).message());
        out.add("o objeto da anotação é um proxy -> " + java.lang.reflect.Proxy.isProxyClass(a.getClass()));
        return out;
    }

    public static void main(String[] args) throws Exception { run().forEach(System.out::println); }
}
