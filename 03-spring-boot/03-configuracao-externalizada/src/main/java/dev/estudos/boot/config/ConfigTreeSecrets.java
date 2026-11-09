package dev.estudos.boot.config;

import dev.estudos.boot.printconfig.PrintConfig;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

/**
 * SB03-13 · Segredos em contêiner: {@code configtree:} mapeia nome do arquivo → chave e conteúdo → valor.
 * Aqui o diretório de segredos é temporário (no Docker/K8s seria {@code /run/secrets/}).
 * Documento: 03-configuracao-externalizada.md §5.4.
 *
 * <p>Saída real:
 * <pre>
 * orders.db.password = s3cr3t
 * orders.api-key = abc-123
 * </pre>
 */
public class ConfigTreeSecrets {
    public static List<String> run() throws IOException {
        Path dir = Files.createTempDirectory("run-secrets");
        try {
            Files.writeString(dir.resolve("orders.db.password"), "s3cr3t");
            Files.writeString(dir.resolve("orders.api-key"), "abc-123");
            var r = PrintConfig.run(List.of("orders.db.password", "orders.api-key"),
                "--spring.config.import=optional:configtree:" + dir + "/");
            return r.subList(1, 3);
        } finally {
            try (Stream<Path> s = Files.walk(dir)) { s.sorted(Comparator.reverseOrder()).forEach(p -> p.toFile().delete()); }
        }
    }

    public static void main(String[] args) throws IOException { run().forEach(System.out::println); }
}
