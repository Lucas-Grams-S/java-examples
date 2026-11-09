package dev.estudos.java.jvm;

import java.util.ArrayList;
import java.util.List;

/**
 * J02-10 · Descobrir quem carregou uma classe.
 * Documento: java-docs/01-java/02-jvm-por-dentro.md §5.1.
 *
 * <p>Classes do JDK em {@code java.base} aparecem com loader {@code null} (bootstrap); {@code java.sql} é carregada
 * pelo loader de plataforma; as do aplicativo, pelo loader de aplicação, cujo pai é o de plataforma.
 * Os ids ({@code @1b2c3d}) variam a cada execução.
 *
 * <p>Saída real (JDK 25; os hashes variam):
 * <pre>
 * WhoLoadedClass -&gt; jdk.internal.loader.ClassLoaders$AppClassLoader@341d43cd
 * String         -&gt; null
 * Connection     -&gt; jdk.internal.loader.ClassLoaders$PlatformClassLoader@58644d46
 * pai do App     -&gt; jdk.internal.loader.ClassLoaders$PlatformClassLoader@58644d46
 * </pre>
 */
public class WhoLoadedClass {
    public static List<String> run() {
        List<String> out = new ArrayList<>();
        out.add("WhoLoadedClass -> " + WhoLoadedClass.class.getClassLoader());
        out.add("String         -> " + String.class.getClassLoader());
        out.add("Connection     -> " + java.sql.Connection.class.getClassLoader());
        out.add("pai do App     -> " + ClassLoader.getSystemClassLoader().getParent());
        return out;
    }

    public static void main(String[] args) {
        run().forEach(System.out::println);
    }
}
