package dev.estudos.java.reflection;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.ArrayList;
import java.util.List;

/**
 * J10-02 · Lendo anotações: método, @Inherited em classes e meta-anotação que o JDK não enxerga.
 * Documento: java-docs/01-java/10-anotacoes-e-reflection.md §2.2.
 *
 * <p>Saída real (JDK 25):
 * <pre>
 * Customer tem &#64;Component -&gt; true
 * método greet level -&gt; 2
 * &#64;Inherited: subclasse herda -&gt; true
 * meta-anotação: getAnnotation não procura -&gt; false
 * meta-anotação: busca manual acha -&gt; true
 * anotação de interface não chega à implementação -&gt; true
 * </pre>
 *
 * <p>Variações: remova @Inherited e veja a subclasse perder a anotação.
 */
public class ReadingAnnotations {
    @Retention(RetentionPolicy.RUNTIME) @Target(ElementType.METHOD)
    @interface Audited { String value() default ""; int level() default 1; }

    @Retention(RetentionPolicy.RUNTIME) @Target(ElementType.TYPE) @Inherited
    @interface Component {}

    @Retention(RetentionPolicy.RUNTIME) @Target(ElementType.TYPE) @Component
    @interface Service {}                      // meta-anotada com @Component

    @Component static class Customer {
        @Audited(value = "greet", level = 2) public String greet(String who) { return "hi " + who; }
    }
    static class VipCustomer extends Customer {}
    @Service static class OrderService {}

    interface Greeter { @Audited("iface") String hello(); }
    static class GreeterImpl implements Greeter { @Override public String hello() { return "hello"; } }

    public static List<String> run() throws Exception {
        List<String> out = new ArrayList<>();
        out.add("Customer tem @Component -> " + Customer.class.isAnnotationPresent(Component.class));
        out.add("método greet level -> " + Customer.class.getMethod("greet", String.class).getAnnotation(Audited.class).level());
        out.add("@Inherited: subclasse herda -> " + VipCustomer.class.isAnnotationPresent(Component.class));
        out.add("meta-anotação: getAnnotation não procura -> " + OrderService.class.isAnnotationPresent(Component.class));
        boolean viaMeta = java.util.Arrays.stream(OrderService.class.getAnnotations())
            .anyMatch(a -> a.annotationType().isAnnotationPresent(Component.class));
        out.add("meta-anotação: busca manual acha -> " + viaMeta);
        out.add("anotação de interface não chega à implementação -> "
                + (GreeterImpl.class.getMethod("hello").getAnnotation(Audited.class) == null));
        return out;
    }

    public static void main(String[] args) throws Exception { run().forEach(System.out::println); }
}
