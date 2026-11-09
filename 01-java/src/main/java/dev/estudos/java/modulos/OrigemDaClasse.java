package dev.estudos.java.modulos;

import dev.estudos.java.Procs;

import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * J14-10 · Descobrir de onde vem uma classe: getProtectionDomain().getCodeSource().getLocation(),
 * Class.getModule() e o problema do "primeiro JAR vence" (classpath hell).
 * Documento: java-docs/01-java/14-modulos-e-build.md §5.2.
 *
 * <p>Saída real (JDK 25):
 * <pre>
 * esta classe vem de um diretório de classes -&gt; true
 * módulo desta classe -&gt; unnamed module &#64;1dbd16a6
 * String (JDK): CodeSource -&gt; null ; módulo -&gt; java.base
 * ClassLoader de String -&gt; null (bootstrap)
 * classpath [a-1.0, b-2.0] -&gt; 1.0 (de a-1.0.jar)
 * classpath [b-2.0, a-1.0] -&gt; 2.0 (de b-2.0.jar)
 * </pre>
 */
public class OrigemDaClasse {
    /** Cria um JAR com a classe lib.Versao devolvendo a versão informada. */
    static Path criaJar(Path dir, String nome, String versao) {
        Path src = dir.resolve("src-" + nome);
        Path cls = dir.resolve("cls-" + nome);
        Fontes.grava(src, "lib/Versao.java", "package lib; public class Versao { public static String v() { return \"" + versao + "\"; } }\n");
        Procs.run(List.of(Procs.tool("javac"), "-d", cls.toString(), src.resolve("lib/Versao.java").toString()), null);
        Path jar = dir.resolve(nome + ".jar");
        Procs.run(List.of(Procs.tool("jar"), "--create", "--file", jar.toString(), "-C", cls.toString(), "."), null);
        return jar;
    }

    static String quemGanha(Path... jars) throws Exception {
        URL[] urls = new URL[jars.length];
        for (int i = 0; i < jars.length; i++) urls[i] = jars[i].toUri().toURL();
        try (URLClassLoader cl = new URLClassLoader(urls, null)) {
            Class<?> c = cl.loadClass("lib.Versao");
            String v = (String) c.getMethod("v").invoke(null);
            String onde = Path.of(c.getProtectionDomain().getCodeSource().getLocation().toURI()).getFileName().toString();
            return v + " (de " + onde + ")";
        }
    }

    public static List<String> run() throws Exception {
        List<String> out = new ArrayList<>();
        URL aqui = OrigemDaClasse.class.getProtectionDomain().getCodeSource().getLocation();
        out.add("esta classe vem de um diretório de classes -> " + aqui.getPath().endsWith("/classes/"));
        out.add("módulo desta classe -> " + OrigemDaClasse.class.getModule());
        out.add("String (JDK): CodeSource -> " + String.class.getProtectionDomain().getCodeSource()
            + " ; módulo -> " + String.class.getModule().getName());
        out.add("ClassLoader de String -> " + String.class.getClassLoader() + " (bootstrap)");

        Path dir = Files.createTempDirectory("origem");
        try {
            Path a = criaJar(dir, "a-1.0", "1.0");
            Path b = criaJar(dir, "b-2.0", "2.0");
            out.add("classpath [a-1.0, b-2.0] -> " + quemGanha(a, b));
            out.add("classpath [b-2.0, a-1.0] -> " + quemGanha(b, a));
        } finally { Fontes.apaga(dir); }
        return out;
    }

    public static void main(String[] args) throws Exception { run().forEach(System.out::println); }
}
