package dev.estudos.java.io;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

/**
 * J13-08 · Ler um arquivo de texto: java.io clássico (Java 6) vs NIO.2 moderno; bytes vs caracteres e charset explícito.
 * Documento: java-docs/01-java/13-io-nio-e-http.md §5.1 (e §2.1).
 *
 * <p>Saída real (JDK 25):
 * <pre>
 * antigo -&gt; [pedido-1;açaí, pedido-2;pão]
 * readAllLines -&gt; [pedido-1;açaí, pedido-2;pão]
 * lines -&gt; [pedido-1;açaí, pedido-2;pão]
 * bytes de 'ç' -&gt; UTF-8=2, ISO-8859-1=1
 * UTF-8 lido como ISO-8859-1 -&gt; Ã§
 * charset padrão (Java 18+) -&gt; UTF-8
 * decorators -&gt; pedido-1;açaí
 * InputStream.read() devolve byte -&gt; 97
 * </pre>
 */
public class AntigoVsModerno {
    static List<String> antigo(Path p) throws IOException {
        List<String> r = new ArrayList<>();
        BufferedReader br = null;
        try {
            br = new BufferedReader(new FileReader(p.toFile(), StandardCharsets.UTF_8));
            String line; while ((line = br.readLine()) != null) r.add(line);
        } finally { if (br != null) br.close(); }
        return r;
    }

    public static List<String> run() throws IOException {
        List<String> out = new ArrayList<>();
        Path p = Files.createTempFile("orders", ".txt");
        try {
            Files.writeString(p, "pedido-1;açaí\npedido-2;pão\n");
            out.add("antigo -> " + antigo(p));
            out.add("readAllLines -> " + Files.readAllLines(p));
            try (Stream<String> lines = Files.lines(p)) { out.add("lines -> " + lines.toList()); }

            // bytes vs caracteres: 'ç' ocupa 2 bytes em UTF-8, 1 em ISO-8859-1
            byte[] utf8 = "ç".getBytes(StandardCharsets.UTF_8);
            byte[] latin1 = "ç".getBytes(StandardCharsets.ISO_8859_1);
            out.add("bytes de 'ç' -> UTF-8=" + utf8.length + ", ISO-8859-1=" + latin1.length);
            String lidoErrado = new String(utf8, StandardCharsets.ISO_8859_1);
            out.add("UTF-8 lido como ISO-8859-1 -> " + lidoErrado);
            out.add("charset padrão (Java 18+) -> " + java.nio.charset.Charset.defaultCharset());

            // decorators: BufferedReader(InputStreamReader(FileInputStream))
            try (var br = new BufferedReader(new InputStreamReader(new FileInputStream(p.toFile()), StandardCharsets.UTF_8))) {
                out.add("decorators -> " + br.readLine());
            }
            try (var in = new ByteArrayInputStream("abc".getBytes())) { out.add("InputStream.read() devolve byte -> " + in.read()); }
        } finally { Files.deleteIfExists(p); }
        return out;
    }

    public static void main(String[] args) throws IOException { run().forEach(System.out::println); }
}
