package dev.estudos.java.evolucao;

import dev.estudos.java.Procs;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import module java.base;                           // JEP 511: importa todos os pacotes exportados de java.base

/**
 * J00-09 · Java 25 (LTS de referência).
 * Documento: java-docs/01-java/00-evolucao-do-java.md §4.9.
 *
 * <p>Module import (JEP 511), flexible constructor bodies (JEP 513), Scoped Values (JEP 506) e arquivo-fonte compacto
 * com {@code main} de instância e {@code IO.println} (JEP 512), executado em subprocesso com {@code java Hello.java}.
 *
 * <p>Saída real (JDK 25):
 * <pre>
 * flexible constructor -&gt; deve ser positivo
 * PositiveAmount(5) -&gt; 5
 * ScopedValue dentro do escopo -&gt; lucas
 * ScopedValue fora do escopo -&gt; false
 * java Hello.java (compacto) -&gt; Olá, pedidos
 * </pre>
 *
 * <p>Variações: passe 0 para {@code new PositiveAmount(0)} e veja a validação antes do {@code super()}.
 */
public class Java25Features {
    static class Amount { final long value; Amount(long v) { value = v; } }

    static class PositiveAmount extends Amount {
        PositiveAmount(long v) {
            if (v <= 0) throw new IllegalArgumentException("deve ser positivo");   // antes do super()
            super(v);
        }
    }

    static final ScopedValue<String> USER = ScopedValue.newInstance();

    public static List<String> run() throws Exception {
        List<String> out = new ArrayList<>();               // sem import java.util.ArrayList: vem do import module
        try {
            new PositiveAmount(0);
        } catch (IllegalArgumentException e) {
            out.add("flexible constructor -> " + e.getMessage());
        }
        out.add("PositiveAmount(5) -> " + new PositiveAmount(5).value);
        ScopedValue.where(USER, "lucas").run(() -> out.add("ScopedValue dentro do escopo -> " + USER.get()));
        out.add("ScopedValue fora do escopo -> " + USER.isBound());

        Path dir = Files.createTempDirectory("hello");
        Path hello = dir.resolve("Hello.java");
        Files.writeString(hello, """
                void main() {
                    IO.println("Olá, pedidos");
                }
                """);
        out.add("java Hello.java (compacto) -> " + Procs.java(hello.toString()).strip());
        Files.delete(hello);
        Files.delete(dir);
        return out;
    }

    public static void main(String[] args) throws Exception {
        run().forEach(System.out::println);
    }
}
