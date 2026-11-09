package dev.estudos.java.io;

import java.io.IOException;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * J13-12 · WatchService: observar criação de arquivo num diretório.
 * Documento: java-docs/01-java/13-io-nio-e-http.md §5.5.
 *
 * <p>No Linux usa inotify; em outros SOs pode ser por polling (latência maior). O exemplo usa poll com prazo.
 *
 * <p>Saída real (JDK 25):
 * <pre>
 * ENTRY_CREATE novo.txt
 * reset (continua válida) -&gt; true
 * </pre>
 */
public class WatchServiceDemo {
    public static List<String> run() throws Exception {
        List<String> out = new ArrayList<>();
        Path dir = Files.createTempDirectory("watch");
        try (WatchService ws = FileSystems.getDefault().newWatchService()) {
            dir.register(ws, StandardWatchEventKinds.ENTRY_CREATE);
            Files.writeString(dir.resolve("novo.txt"), "x");
            WatchKey key = ws.poll(10, TimeUnit.SECONDS);
            if (key == null) { out.add("nenhum evento no prazo"); return out; }
            key.pollEvents().forEach(e -> out.add(e.kind() + " " + e.context()));
            out.add("reset (continua válida) -> " + key.reset());
        } finally {
            Files.deleteIfExists(dir.resolve("novo.txt"));
            Files.deleteIfExists(dir);
        }
        return out;
    }

    public static void main(String[] args) throws Exception { run().forEach(System.out::println); }
}
