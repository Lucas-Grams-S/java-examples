package dev.estudos.boot.config;

import dev.estudos.boot.printconfig.PrintConfig;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

/**
 * SB03-14 · Executando com configuração externa e precedência (SB03-01 na prática): roda {@link PrintConfig}
 * em subprocesso com ambiente, {@code SPRING_APPLICATION_JSON}, argumentos e {@code --spring.config.additional-location}.
 * Documento: 03-configuracao-externalizada.md §2.1 e §5.5.
 *
 * <p>Saída real:
 * <pre>
 * só o YAML                         -> BRL
 * ORDERS_CURRENCY=EUR               -> EUR
 * env EUR + SPRING_APPLICATION_JSON -> USD
 * env EUR + --orders.currency=JPY   -> JPY
 * additional-location (arquivo)     -> CHF
 * perfil prod + --orders.limits.max-items=7 -> maxItems=7
 * </pre>
 */
public class Precedencia {
    static String exec(Map<String, String> env, String... args) throws IOException, InterruptedException {
        List<String> cmd = new ArrayList<>(List.of(Path.of(System.getProperty("java.home"), "bin", "java").toString(),
            "-cp", classpath(), PrintConfig.class.getName()));
        cmd.addAll(List.of(args));
        ProcessBuilder pb = new ProcessBuilder(cmd).redirectErrorStream(true);
        pb.environment().putAll(env);
        Process p = pb.start();
        String out = new String(p.getInputStream().readAllBytes());
        p.waitFor();
        return out.lines().filter(l -> l.startsWith("OrdersProperties[")).findFirst().orElse(out);
    }

    /** Classpath do processo atual; sob {@code exec:java} as classes vêm de um URLClassLoader, não de java.class.path. */
    static String classpath() {
        if (Precedencia.class.getClassLoader() instanceof java.net.URLClassLoader u) {
            return java.util.Arrays.stream(u.getURLs()).map(x -> Path.of(java.net.URI.create(x.toString())).toString())
                .collect(java.util.stream.Collectors.joining(java.io.File.pathSeparator));
        }
        return System.getProperty("java.class.path");
    }

    static String currency(String props) { return props.replaceAll("OrdersProperties\\[currency=([^,]*),.*", "$1"); }

    public static List<String> run() throws IOException, InterruptedException {
        List<String> out = new ArrayList<>();
        out.add("só o YAML                         -> " + currency(exec(Map.of())));
        out.add("ORDERS_CURRENCY=EUR               -> " + currency(exec(Map.of("ORDERS_CURRENCY", "EUR"))));
        out.add("env EUR + SPRING_APPLICATION_JSON -> " + currency(exec(
            Map.of("ORDERS_CURRENCY", "EUR", "SPRING_APPLICATION_JSON", "{\"orders\":{\"currency\":\"USD\"}}"))));
        out.add("env EUR + --orders.currency=JPY   -> " + currency(exec(Map.of("ORDERS_CURRENCY", "EUR"), "--orders.currency=JPY")));
        Path dir = Files.createTempDirectory("etc-orders");
        try {
            Files.writeString(dir.resolve("application.properties"), "orders.currency=CHF\n");
            out.add("additional-location (arquivo)     -> " + currency(exec(Map.of(), "--spring.config.additional-location=file:" + dir + "/")));
        } finally {
            try (Stream<Path> s = Files.walk(dir)) { s.sorted(Comparator.reverseOrder()).forEach(p -> p.toFile().delete()); }
        }
        String prod = exec(Map.of(), "--spring.profiles.active=prod", "--orders.limits.max-items=7");
        out.add("perfil prod + --orders.limits.max-items=7 -> maxItems=" + prod.replaceAll(".*maxItems=(\\d+).*", "$1"));
        return out;
    }

    public static void main(String[] args) throws Exception { run().forEach(System.out::println); }
}
