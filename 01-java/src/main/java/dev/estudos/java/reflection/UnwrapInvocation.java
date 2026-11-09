package dev.estudos.java.reflection;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/**
 * J10-14 · Desembrulhando InvocationTargetException para expor a causa real.
 * Documento: java-docs/01-java/10-anotacoes-e-reflection.md §5.5.
 *
 * <p>Saída real (JDK 25):
 * <pre>
 * sem desembrulhar -&gt; InvocationTargetException, causa java.lang.IllegalStateException: falha real
 * desembrulhada -&gt; IllegalStateException: falha real
 * caminho feliz -&gt; ok x
 * argumento errado -&gt; IllegalArgumentException (sem wrapper)
 * </pre>
 *
 * <p>Variações: chame com argumento errado e veja IllegalArgumentException (sem wrapper).
 */
public class UnwrapInvocation {
    public static class Target {
        public static String boom() { throw new IllegalStateException("falha real"); }
        public static String ok(String s) { return "ok " + s; }
    }

    static Object invoke(Method m, Object target, Object... args) throws IllegalAccessException {
        try {
            return m.invoke(target, args);
        } catch (InvocationTargetException e) {
            throw (e.getCause() instanceof RuntimeException re) ? re : new RuntimeException(e.getCause());
        }
    }

    public static List<String> run() throws Exception {
        List<String> out = new ArrayList<>();
        Method boom = Target.class.getMethod("boom");
        try { boom.invoke(null); }
        catch (InvocationTargetException e) {
            out.add("sem desembrulhar -> " + e.getClass().getSimpleName() + ", causa " + e.getCause());
        }
        try { invoke(boom, null); }
        catch (IllegalStateException e) { out.add("desembrulhada -> " + e.getClass().getSimpleName() + ": " + e.getMessage()); }
        out.add("caminho feliz -> " + invoke(Target.class.getMethod("ok", String.class), null, "x"));
        try { Target.class.getMethod("ok", String.class).invoke(null, 1); }
        catch (IllegalArgumentException e) { out.add("argumento errado -> IllegalArgumentException (sem wrapper)"); }
        return out;
    }

    public static void main(String[] args) throws Exception { run().forEach(System.out::println); }
}
