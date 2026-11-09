package dev.estudos.java.modernos;

import java.util.ArrayList;
import java.util.List;

/**
 * J05-14 · Desestruturação aninhada.
 * Documento: java-docs/01-java/05-recursos-modernos-da-linguagem.md §5.4.
 *
 * <p>Saída real (JDK 25):
 * <pre>
 * x + y -&gt; 15
 * Box&lt;String&gt; casa com Box(Point)? -&gt; false
 * Box(String) -&gt; texto
 * </pre>
 *
 * <p>Variações: troque {@code Box<?>} por {@code Box<Point>} e acrescente um nível ({@code Box<Box<Point>>}).
 */
public class NestedDeconstruction {
    record Point(int x, int y) {}
    record Box<T>(T value) {}

    public static List<String> run() {
        List<String> out = new ArrayList<>();
        Object bx = new Box<>(new Point(7, 8));
        if (bx instanceof Box<?>(Point(var x, var y))) out.add("x + y -> " + (x + y));
        Object other = new Box<>("texto");
        out.add("Box<String> casa com Box(Point)? -> " + (other instanceof Box<?>(Point(var a, var b))));
        if (other instanceof Box<?>(String s)) out.add("Box(String) -> " + s);
        return out;
    }

    public static void main(String[] args) { run().forEach(System.out::println); }
}
