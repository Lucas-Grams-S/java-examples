package dev.estudos.java.plataforma;

import dev.estudos.java.Procs;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * J01-10 · Ferramentas do JDK.
 * Documento: java-docs/01-java/01-plataforma-java.md §3.6.
 *
 * <p>Exercita {@code javac}, {@code jar}, {@code jdeps} e {@code jlink} sobre o {@code Hello}. O {@code jlink}
 * depende de o JDK trazer os {@code jmods}; se não trouxer, o exemplo informa em vez de falhar.
 *
 * <p>Saída real (JDK 25):
 * <pre>
 * ferramentas conferidas: javac java javap jar jshell jlink jpackage jdeps jcmd jfr jstack jmap
 * jar tf -&gt; META-INF/ META-INF/MANIFEST.MF Hello.class
 * java -jar app.jar -&gt; 599994
 * jdeps --print-module-deps -&gt; java.base
 * jlink -&gt; runtime mínimo criado; módulos: java.base@25.0.4.1
 * java do runtime mínimo executa o jar -&gt; 599994
 * </pre>
 */
public class JdkTools {
    public static List<String> run() throws Exception {
        Path dir = HelloSource.write();
        try {
            List<String> out = new ArrayList<>();
            for (String t : List.of("javac", "java", "javap", "jar", "jshell", "jlink", "jpackage", "jdeps", "jcmd", "jfr", "jstack", "jmap"))
                if (!Files.isExecutable(Path.of(Procs.tool(t)))) out.add("FALTA no JDK: " + t);
            out.add("ferramentas conferidas: javac java javap jar jshell jlink jpackage jdeps jcmd jfr jstack jmap");
            Procs.tool("javac", "-d", dir.toString(), dir.resolve("Hello.java").toString());
            Path jar = dir.resolve("app.jar");
            Procs.tool("jar", "--create", "--file", jar.toString(), "--main-class", "Hello", "-C", dir.toString(), "Hello.class");
            out.add("jar tf -> " + Procs.tool("jar", "--list", "--file", jar.toString()).replace("\n", " ").strip());
            out.add("java -jar app.jar -> " + Procs.java("-jar", jar.toString()).strip());
            out.add("jdeps --print-module-deps -> " + Procs.tool("jdeps", "--print-module-deps", "--ignore-missing-deps", jar.toString()).strip());
            Path rt = dir.resolve("meu-runtime");
            String jl = Procs.tool("jlink", "--add-modules", "java.base", "--output", rt.toString(), "--strip-debug", "--no-header-files", "--no-man-pages");
            if (Files.isExecutable(rt.resolve("bin/java"))) {
                out.add("jlink -> runtime mínimo criado; módulos: " + Procs.run(List.of(rt.resolve("bin/java").toString(), "--list-modules"), null).strip());
                out.add("java do runtime mínimo executa o jar -> " + Procs.run(List.of(rt.resolve("bin/java").toString(), "-jar", jar.toString()), null).strip());
            } else {
                out.add("jlink indisponível neste JDK: " + jl.strip());
            }
            return out;
        } finally {
            HelloSource.deleteRecursively(dir);
        }
    }

    public static void main(String[] args) throws Exception {
        run().forEach(System.out::println);
    }
}
