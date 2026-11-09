package dev.estudos.java.jvm;

import dev.estudos.java.Procs;
import java.util.List;

/**
 * J02-09 · Falhas de memória e como lê-las.
 * Documento: java-docs/01-java/02-jvm-por-dentro.md §3.5.
 *
 * <p>Executa o {@link OomProgram} num subprocesso com {@code -Xmx32m} (para não esgotar a memória da JVM do
 * próprio exemplo/teste) e mostra o {@code StackOverflowError} e o {@code OutOfMemoryError: Java heap space}.
 *
 * <p>Saída real (JDK 25):
 * <pre>
 * profundidade &gt; 1000: true
 * Java heap space
 * </pre>
 *
 * <p>Variações: troque {@code -Xmx32m} por {@code -Xss256k} e compare a profundidade da recursão
 * (acrescente um {@code println(depth)}).
 */
public class MemoryFailures {
    public static List<String> run() {
        return Procs.java("-Xmx32m", "-cp", Procs.classpath(), "dev.estudos.java.jvm.OomProgram").lines().toList();
    }

    public static void main(String[] args) {
        run().forEach(System.out::println);
    }
}
