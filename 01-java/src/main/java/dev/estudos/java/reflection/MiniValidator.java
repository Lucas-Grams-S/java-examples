package dev.estudos.java.reflection;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

/**
 * J10-10 · Um validador mínimo com @NotBlank (a ideia por trás do Bean Validation).
 * Documento: java-docs/01-java/10-anotacoes-e-reflection.md §5.1.
 *
 * <p>Saída real (JDK 25):
 * <pre>
 * válido -&gt; []
 * nome em branco -&gt; [name: must not be blank]
 * ambos inválidos -&gt; [name: must not be blank, email: é obrigatório]
 * </pre>
 *
 * <p>Variações: adicione outro campo @NotBlank e veja duas violações na lista.
 */
public class MiniValidator {
    @Retention(RetentionPolicy.RUNTIME) @Target(ElementType.FIELD)
    @interface NotBlank { String message() default "must not be blank"; }

    static class Customer {
        @NotBlank private String name;
        @NotBlank(message = "é obrigatório") private String email;
        private int age;
        Customer(String n, String e) { name = n; email = e; }
    }

    static List<String> validate(Object target) throws IllegalAccessException {
        var errors = new ArrayList<String>();
        for (Field f : target.getClass().getDeclaredFields()) {
            NotBlank nb = f.getAnnotation(NotBlank.class);
            if (nb == null) continue;
            f.setAccessible(true);
            Object v = f.get(target);
            if (v == null || v.toString().isBlank()) errors.add(f.getName() + ": " + nb.message());
        }
        return errors;
    }

    public static List<String> run() throws Exception {
        List<String> out = new ArrayList<>();
        out.add("válido -> " + validate(new Customer("Ana", "a@x.com")));
        out.add("nome em branco -> " + validate(new Customer(" ", "a@x.com")));
        out.add("ambos inválidos -> " + validate(new Customer("", null)));
        return out;
    }

    public static void main(String[] args) throws Exception { run().forEach(System.out::println); }
}
