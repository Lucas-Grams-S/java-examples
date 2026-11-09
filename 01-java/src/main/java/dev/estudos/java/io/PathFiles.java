package dev.estudos.java.io;

import java.io.IOException;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

/**
 * J13-02 · Path e Files (NIO.2): resolve, readString/writeString, Files.lines (fechar!), copy, walk e path traversal.
 * Documento: java-docs/01-java/13-io-nio-e-http.md §2.2 (e §8, path traversal).
 *
 * <p>Saída real (JDK 25):
 * <pre>
 * readString -&gt; o1;10\no2;20\n
 * soma via Files.lines -&gt; 30
 * primeira linha -&gt; o1;10
 * walk (dir + 2 arquivos) -&gt; 3
 * list (um nível) -&gt; [copy.txt, orders.txt]
 * size -&gt; 12
 * isDirectory(dir) -&gt; true
 * copy sem REPLACE_EXISTING -&gt; FileAlreadyExistsException
 * APPEND -&gt; [o1;10, o2;20, o3;30]
 * resolve não usa '/' manual -&gt; true
 * arquivo ausente -&gt; NoSuchFileException
 * resolveSeguro('a.txt') -&gt; a.txt
 * path traversal -&gt; SecurityException
 * </pre>
 */
public class PathFiles {
    static Path resolveSeguro(Path base, String entrada) {
        Path p = base.resolve(entrada).normalize();
        if (!p.startsWith(base)) throw new SecurityException("fora do diretório base: " + entrada);
        return p;
    }

    public static List<String> run() throws IOException {
        List<String> out = new ArrayList<>();
        Path dir = Files.createTempDirectory("orders");
        try {
            Path f = dir.resolve("orders.txt");
            Files.writeString(f, "o1;10\no2;20\n");
            out.add("readString -> " + Files.readString(f).replace("\n", "\\n"));
            try (Stream<String> lines = Files.lines(f)) {
                out.add("soma via Files.lines -> " + lines.map(l -> l.split(";")[1]).mapToInt(Integer::parseInt).sum());
            }
            try (var r = Files.newBufferedReader(f)) { out.add("primeira linha -> " + r.readLine()); }
            Files.copy(f, dir.resolve("copy.txt"));
            try (Stream<Path> walk = Files.walk(dir)) { out.add("walk (dir + 2 arquivos) -> " + walk.count()); }
            try (Stream<Path> list = Files.list(dir)) { out.add("list (um nível) -> " + list.map(p -> p.getFileName().toString()).sorted().toList()); }
            out.add("size -> " + Files.size(f));
            out.add("isDirectory(dir) -> " + Files.isDirectory(dir));
            try { Files.copy(f, dir.resolve("copy.txt")); }
            catch (FileAlreadyExistsException e) { out.add("copy sem REPLACE_EXISTING -> FileAlreadyExistsException"); }
            Files.copy(f, dir.resolve("copy.txt"), StandardCopyOption.REPLACE_EXISTING);
            Files.writeString(f, "o3;30\n", StandardOpenOption.APPEND);
            out.add("APPEND -> " + Files.readAllLines(f));
            out.add("resolve não usa '/' manual -> " + dir.resolve("a").resolve("b").endsWith(Path.of("a", "b")));
            try { Files.readString(dir.resolve("nao-existe.txt")); }
            catch (NoSuchFileException e) { out.add("arquivo ausente -> NoSuchFileException"); }

            out.add("resolveSeguro('a.txt') -> " + dir.relativize(resolveSeguro(dir, "a.txt")));
            try { resolveSeguro(dir, "../../etc/passwd"); }
            catch (SecurityException e) { out.add("path traversal -> SecurityException"); }
        } finally {
            try (Stream<Path> walk = Files.walk(dir)) { walk.sorted(Comparator.reverseOrder()).forEach(p -> p.toFile().delete()); }
        }
        return out;
    }

    public static void main(String[] args) throws IOException { run().forEach(System.out::println); }
}
