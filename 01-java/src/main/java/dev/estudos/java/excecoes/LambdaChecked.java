package dev.estudos.java.excecoes;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * J08-13 · Lambdas e checked exceptions: envolver em UncheckedIOException e interface funcional que lança.
 * Documento: java-docs/01-java/08-excecoes.md §5.3.
 *
 * <p>Saída real (JDK 25):
 * <pre>
 * lido -&gt; [alfa, beta]
 * helper -&gt; [alfa, beta]
 * falha -&gt; UncheckedIOException, causa NoSuchFileException
 * </pre>
 *
 * <p>Variações: aponte um caminho inexistente e veja a causa original dentro da UncheckedIOException.
 */
public class LambdaChecked {
    @FunctionalInterface
    interface ThrowingFunction<T, R> { R apply(T t) throws Exception; }

    static <T, R> java.util.function.Function<T, R> unchecked(ThrowingFunction<T, R> f) {
        return t -> {
            try { return f.apply(t); }
            catch (RuntimeException e) { throw e; }
            catch (Exception e) { throw new RuntimeException(e); }
        };
    }

    public static List<String> run() throws IOException {
        List<String> out = new ArrayList<>();
        Path dir = Files.createTempDirectory("j0813");
        Path a = Files.writeString(dir.resolve("a.txt"), "alfa");
        Path b = Files.writeString(dir.resolve("b.txt"), "beta");
        try {
            List<String> lines = List.of(a, b).stream().map(p -> {
                try { return Files.readString(p); }
                catch (IOException e) { throw new UncheckedIOException(e); }
            }).toList();
            out.add("lido -> " + lines);
            out.add("helper -> " + List.of(a, b).stream().map(unchecked(Files::readString)).toList());
            try {
                List.of(dir.resolve("nao-existe.txt")).stream().map(p -> {
                    try { return Files.readString(p); }
                    catch (IOException e) { throw new UncheckedIOException(e); }
                }).toList();
            } catch (UncheckedIOException e) {
                out.add("falha -> UncheckedIOException, causa " + e.getCause().getClass().getSimpleName());
            }
        } finally {
            Files.deleteIfExists(a); Files.deleteIfExists(b); Files.deleteIfExists(dir);
        }
        return out;
    }

    public static void main(String[] args) throws IOException { run().forEach(System.out::println); }
}
