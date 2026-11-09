package dev.estudos.boot.config;

import dev.estudos.boot.printconfig.PrintConfig;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

/**
 * SB03-03 · {@code spring.config.import}: importa um arquivo externo (sobrescreve quem importa) e tolera
 * um local ausente com {@code optional:}. Sem {@code optional:}, o local ausente derruba o startup.
 * Documento: 03-configuracao-externalizada.md §2.3.
 *
 * <p>Saída real:
 * <pre>
 * orders.currency com import = EUR
 * app.extra = vindo do arquivo importado
 * sem 'optional:' e arquivo ausente -> ConfigDataResourceNotFoundException
 * </pre>
 */
public class ConfigImport {
    public static List<String> run() throws IOException {
        Path dir = Files.createTempDirectory("config-import");
        try {
            Files.writeString(dir.resolve("dev.properties"), "orders.currency=EUR\napp.extra=vindo do arquivo importado\n");
            String imports = "optional:file:" + dir.resolve("dev.properties") + ",optional:file:" + dir.resolve("ausente.properties");
            List<String> out = new ArrayList<>();
            var r = PrintConfig.run(List.of("orders.currency", "app.extra"), "--spring.config.import=" + imports);
            out.add("orders.currency com import = " + r.get(1).substring("orders.currency = ".length()));
            out.add("app.extra = " + r.get(2).substring("app.extra = ".length()));
            try {
                PrintConfig.run(List.of(), "--spring.config.import=file:" + dir.resolve("ausente.properties"));
                out.add("sem 'optional:' e arquivo ausente -> subiu (inesperado)");
            } catch (Exception e) {
                out.add("sem 'optional:' e arquivo ausente -> " + e.getClass().getSimpleName());
            }
            return out;
        } finally {
            try (Stream<Path> s = Files.walk(dir)) { s.sorted(Comparator.reverseOrder()).forEach(p -> p.toFile().delete()); }
        }
    }

    public static void main(String[] args) throws IOException { run().forEach(System.out::println); }
}
