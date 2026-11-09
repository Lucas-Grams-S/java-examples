package dev.estudos.java.modernos;

import java.util.ArrayList;
import java.util.List;

/**
 * J05-05 · Pattern matching para {@code instanceof}.
 * Documento: java-docs/01-java/05-recursos-modernos-da-linguagem.md §2.5.
 *
 * <p>Saída real (JDK 25):
 * <pre>
 * antes -&gt; longa java25
 * depois -&gt; longa java25 / outro / outro
 * n continua no escopo -&gt; 42
 * não é Integer
 * </pre>
 *
 * <p>Variações: troque {@code &&} por {@code ||} em {@code longString} e leia o erro de escopo da variável de padrão.
 */
public class InstanceofPattern {
    static String before(Object obj) {
        if (obj instanceof String) {
            String s = (String) obj;
            if (s.length() > 3) return "longa " + s;
        }
        return "outro";
    }

    static String longString(Object obj) {
        if (obj instanceof String s && s.length() > 3) return "longa " + s;
        return "outro";
    }

    static String early(Object obj) {
        if (!(obj instanceof Integer n)) return "não é Integer";
        return "n continua no escopo -> " + (n + 1);   // flow scoping
    }

    public static List<String> run() {
        List<String> out = new ArrayList<>();
        out.add("antes -> " + before("java25"));
        out.add("depois -> " + longString("java25") + " / " + longString("ab") + " / " + longString(7));
        out.add(early(41));
        out.add(early("x"));
        return out;
    }

    public static void main(String[] args) { run().forEach(System.out::println); }
}
