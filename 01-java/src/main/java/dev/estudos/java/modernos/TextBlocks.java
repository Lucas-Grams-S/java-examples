package dev.estudos.java.modernos;

import java.util.ArrayList;
import java.util.List;

/**
 * J05-02 · Text blocks.
 * Documento: java-docs/01-java/05-recursos-modernos-da-linguagem.md §2.2.
 *
 * <p>Saída real (JDK 25):
 * <pre>
 * &#123;
 *   "id": 1,
 *   "name": "Café grande"
 * &#125;
 * termina com quebra -&gt; true
 * '\' no fim da linha junta -&gt; true
 * \s preserva espaço final -&gt; [a ]
 * formatted -&gt; Olá, mundo!
 * aspas "simples" sem escape
 * </pre>
 *
 * <p>Variações: mova o {@code """} de fechamento para a esquerda/direita e veja a indentação mudar.
 */
public class TextBlocks {
    static final String JSON = """
            {
              "id": 1,
              "name": "Café \
            grande"
            }
            """;

    public static List<String> run() {
        List<String> out = new ArrayList<>();
        out.addAll(JSON.lines().toList());
        out.add("termina com quebra -> " + JSON.endsWith("\n"));
        out.add("'\\' no fim da linha junta -> " + JSON.contains("Café grande"));
        String sp = """
                a\s
                b""";
        out.add("\\s preserva espaço final -> [" + sp.lines().findFirst().orElse("") + "]");
        String t = """
                Olá, %s!""".formatted("mundo");
        out.add("formatted -> " + t);
        String q = """
                aspas "simples" sem escape""";
        out.add(q);
        return out;
    }

    public static void main(String[] args) { run().forEach(System.out::println); }
}
