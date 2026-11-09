package dev.estudos.boot.config;

import dev.estudos.boot.printconfig.PrintConfig;
import java.util.ArrayList;
import java.util.List;

/**
 * SB03-05 · Placeholders ({@code ${app.name}}, {@code ${orders.currency:BRL}}) e valores aleatórios
 * ({@code ${random.uuid}}, {@code ${random.int[1024,65536]}}). Os valores aleatórios mudam a cada execução.
 * Documento: 03-configuracao-externalizada.md §2.5.
 *
 * <p>Saída real (uuid e porta variam):
 * <pre>
 * app.description = Orders roda na moeda BRL
 * app.description com --orders.currency=USD = Orders roda na moeda USD
 * secret é UUID = true
 * port em [1024,65536) = true
 * </pre>
 */
public class Placeholders {
    public static List<String> run() {
        List<String> out = new ArrayList<>();
        var base = PrintConfig.run(List.of("app.description", "app.secret", "app.port"));
        out.add(base.get(1));
        out.add("app.description com --orders.currency=USD = "
            + PrintConfig.run(List.of("app.description"), "--orders.currency=USD").get(1).substring("app.description = ".length()));
        out.add("secret é UUID = " + base.get(2).substring("app.secret = ".length())
            .matches("[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}"));
        int port = Integer.parseInt(base.get(3).substring("app.port = ".length()));
        out.add("port em [1024,65536) = " + (port >= 1024 && port < 65536));
        return out;
    }

    public static void main(String[] args) { run().forEach(System.out::println); }
}
