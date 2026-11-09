package dev.estudos.springcore.ioc;

import java.util.ArrayList;
import java.util.List;
import org.springframework.beans.factory.BeanCurrentlyInCreationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.stereotype.Component;

/**
 * SC02-08, SC02-11 e SC04-08 · Dependência circular. Por construtor: sempre falha
 * ({@code BeanCurrentlyInCreationException}). Por campo: o Spring "resolve" com referência antecipada do
 * singleton incompleto (cache de 3 níveis), a menos que {@code setAllowCircularReferences(false)}.
 * Documento: 02-spring-core/02-ioc-e-injecao-de-dependencia.md §3.3 e §5.3; 04-ciclo-de-vida-e-escopos.md §3.3.
 *
 * <p>Saída real:
 * <pre>
 * construtor A(B)/B(A) -&gt; BeanCurrentlyInCreationException
 * campo X/Y (padrão) -&gt; funciona, y.x == x: true
 * campo X/Y com setAllowCircularReferences(false) -&gt; BeanCurrentlyInCreationException
 * </pre>
 */
public class Circular {
    @Component static class A { A(B b) {} }
    @Component static class B { B(A a) {} }

    @Component static class X { @Autowired Y y; }
    @Component static class Y { @Autowired X x; }

    private static String causa(Throwable e) {
        for (Throwable t = e; t != null; t = t.getCause()) if (t instanceof BeanCurrentlyInCreationException) return "BeanCurrentlyInCreationException";
        return e.toString();
    }

    public static List<String> run() {
        List<String> out = new ArrayList<>();
        try (var ctx = new AnnotationConfigApplicationContext(A.class, B.class)) {
            out.add("construtor A(B)/B(A) -> subiu?!");
        } catch (Exception e) { out.add("construtor A(B)/B(A) -> " + causa(e)); }

        try (var ctx = new AnnotationConfigApplicationContext(X.class, Y.class)) {
            out.add("campo X/Y (padrão) -> funciona, y.x == x: " + (ctx.getBean(X.class).y.x == ctx.getBean(X.class)));
        }

        try (var ctx = new AnnotationConfigApplicationContext()) {
            ctx.setAllowCircularReferences(false);
            ctx.register(X.class, Y.class);
            ctx.refresh();
            out.add("campo X/Y com setAllowCircularReferences(false) -> subiu?!");
        } catch (Exception e) { out.add("campo X/Y com setAllowCircularReferences(false) -> " + causa(e)); }
        return out;
    }

    public static void main(String[] args) { run().forEach(System.out::println); }
}
