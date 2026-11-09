package dev.estudos.java.excecoes;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * J08-12 · Validar argumentos na entrada: requireNonNull e IllegalArgumentException.
 * Documento: java-docs/01-java/08-excecoes.md §5.2.
 *
 * <p>Saída real (JDK 25):
 * <pre>
 * Customer[name=Ana, email=ana&#64;x.com]
 * NullPointerException: name must not be null
 * IllegalArgumentException: invalid email: sem-arroba
 * IllegalArgumentException: invalid email: null
 * UnsupportedOperationException (lista imutável)
 * </pre>
 *
 * <p>Variações: passe name=null e compare a mensagem com a do email inválido.
 */
public class ArgumentValidation {
    record Customer(String name, String email) {
        Customer {
            Objects.requireNonNull(name, "name must not be null");
            if (email == null || !email.contains("@"))
                throw new IllegalArgumentException("invalid email: " + email);
        }
    }

    static String attempt(String name, String email) {
        try { return new Customer(name, email).toString(); }
        catch (NullPointerException | IllegalArgumentException e) {
            return e.getClass().getSimpleName() + ": " + e.getMessage();
        }
    }

    public static List<String> run() {
        List<String> out = new ArrayList<>();
        out.add(attempt("Ana", "ana@x.com"));
        out.add(attempt(null, "ana@x.com"));
        out.add(attempt("Ana", "sem-arroba"));
        out.add(attempt("Ana", null));
        try { List.of(1).add(2); } catch (UnsupportedOperationException e) { out.add("UnsupportedOperationException (lista imutável)"); }
        return out;
    }

    public static void main(String[] args) { run().forEach(System.out::println); }
}
