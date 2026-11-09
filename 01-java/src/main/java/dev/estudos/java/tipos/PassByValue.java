package dev.estudos.java.tipos;

import java.util.List;

/**
 * J03-03 · Passagem de parâmetros: sempre por valor.
 * Documento: java-docs/01-java/03-tipos-e-memoria.md §2.3.
 *
 * <p>Saída real (JDK 25):
 * <pre>
 * a! 1 s
 * </pre>
 *
 * <p>Variações: faça o método devolver o novo {@code StringBuilder} e atribua no chamador.
 */
public class PassByValue {
    static void change(StringBuilder sb, int x, String s) {
        sb.append("!");                  // altera o objeto apontado: visível ao chamador
        x = 99;                          // altera a cópia local do int: invisível
        s = s + "!";                     // cria NOVA String e muda só a cópia local da referência
        sb = new StringBuilder("novo");  // reaponta a cópia local: invisível
    }

    public static List<String> run() {
        StringBuilder sb = new StringBuilder("a");
        int x = 1;
        String s = "s";
        change(sb, x, s);
        return List.of(sb + " " + x + " " + s);
    }

    public static void main(String[] args) {
        run().forEach(System.out::println);
    }
}
