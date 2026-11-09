package dev.estudos.java.modernos;

import dev.estudos.java.Procs;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * J05-15 · Padrões primitivos (PREVIEW no Java 25, JEP 507).
 * Documento: java-docs/01-java/05-recursos-modernos-da-linguagem.md §5.5.
 *
 * <p>Para não contaminar o módulo inteiro com {@code --enable-preview}, o programa de exemplo é gravado num diretório
 * temporário e rodado em subprocesso, primeiro sem e depois com a flag.
 *
 * <p>Saída real (JDK 25):
 * <pre>
 * sem --enable-preview -&gt; exit=1 · error: primitive patterns are a preview feature and are disabled by default.
 * com --enable-preview -&gt; exit=0 · padrão primitivo: 3
 * </pre>
 *
 * <p>Variações: rode você mesmo {@code java --enable-preview --source 25 Prim.java}.
 */
public class PrimitivePatterns {
    static final String SOURCE = """
            public class Prim {
                public static void main(String[] args) {
                    int i = 3;
                    if (i instanceof int j) { System.out.println("padrão primitivo: " + j); }
                }
            }
            """;

    public static List<String> run() throws IOException {
        List<String> out = new ArrayList<>();
        Path dir = Files.createTempDirectory("prim");
        try {
            Path src = dir.resolve("Prim.java");
            Files.writeString(src, SOURCE);
            var semFlag = Procs.exec(List.of(Procs.tool("javac"), "-d", dir.toString(), src.toString()), null);
            String erro = semFlag.output().lines().filter(l -> l.contains("error")).findFirst().orElse("(sem erro)");
            out.add("sem --enable-preview -> exit=" + semFlag.exitCode() + " · " + erro.substring(erro.indexOf("error:")));
            var comFlag = Procs.exec(List.of(Procs.tool("java"), "--enable-preview", "--source", "25", src.toString()), null);
            String linha = comFlag.output().lines().filter(l -> l.startsWith("padrão")).findFirst().orElse(comFlag.output());
            out.add("com --enable-preview -> exit=" + comFlag.exitCode() + " · " + linha);
        } finally {
            try (var files = Files.list(dir)) { files.forEach(f -> f.toFile().delete()); }
            Files.delete(dir);
        }
        return out;
    }

    public static void main(String[] args) throws IOException { run().forEach(System.out::println); }
}
