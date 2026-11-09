package dev.estudos.boot.config;

import dev.estudos.boot.printconfig.PrintConfig;
import java.util.ArrayList;
import java.util.List;

/**
 * SB03-04 · Perfis: o documento YAML com {@code on-profile: prod} só vale com o perfil ativo.
 * Documento: 03-configuracao-externalizada.md §2.4.
 *
 * <p>Saída real:
 * <pre>
 * sem perfil : paymentTimeout=PT30S maxItems=50
 * perfil prod: paymentTimeout=PT45S maxItems=10
 * </pre>
 */
public class Perfis {
    public static List<String> run() {
        List<String> out = new ArrayList<>();
        out.add("sem perfil : " + resume(PrintConfig.run(List.of()).get(0)));
        out.add("perfil prod: " + resume(PrintConfig.run(List.of(), "--spring.profiles.active=prod").get(0)));
        return out;
    }

    private static String resume(String props) {
        var t = props.replaceAll(".*paymentTimeout=(PT\\w+).*maxItems=(\\d+).*", "paymentTimeout=$1 maxItems=$2");
        return t;
    }

    public static void main(String[] args) { run().forEach(System.out::println); }
}
