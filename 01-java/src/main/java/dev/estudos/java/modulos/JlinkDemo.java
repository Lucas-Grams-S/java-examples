package dev.estudos.java.modulos;

import dev.estudos.java.Procs;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * J14-08 · jlink: runtime sob medida com só os módulos necessários (jdeps descobre quais).
 * Documento: java-docs/01-java/14-modulos-e-build.md §3.3.
 *
 * <p>Tamanhos variam por JDK/SO; o teste afirma só as relações (runtime mínimo bem menor que o JDK).
 *
 * <p>Saída real (JDK 25):
 * <pre>
 * jdeps --print-module-deps -&gt; java.base
 * jlink -&gt; exit 0
 * módulos na imagem -&gt; [java.base]
 * roda a aplicação com o runtime mínimo -&gt; olá do runtime mínimo
 * runtime mínimo é menos da metade do JDK -&gt; true
 * </pre>
 */
public class JlinkDemo {
    static long tamanho(Path dir) throws java.io.IOException {
        try (var w = Files.walk(dir)) { return w.filter(Files::isRegularFile).mapToLong(f -> f.toFile().length()).sum(); }
    }

    public static List<String> run() throws Exception {
        List<String> out = new ArrayList<>();
        Path work = Files.createTempDirectory("jlink");
        Path runtime = work.resolve("runtime");
        try {
            Fontes.grava(work, "src/App.java", """
                public class App { public static void main(String[] a) { System.out.println("olá do runtime mínimo"); } }
                """);
            Path classes = work.resolve("classes");
            Procs.run(List.of(Procs.tool("javac"), "-d", classes.toString(), work.resolve("src/App.java").toString()), null);
            String deps = Procs.run(List.of(Procs.tool("jdeps"), "--print-module-deps", "--ignore-missing-deps", classes.toString()), null).strip();
            out.add("jdeps --print-module-deps -> " + deps);

            var jl = Procs.exec(List.of(Procs.tool("jlink"), "--add-modules", deps, "--output", runtime.toString(),
                "--strip-debug", "--compress", "zip-6", "--no-header-files", "--no-man-pages"), null);
            out.add("jlink -> exit " + jl.exitCode());
            String mods = Procs.run(List.of(runtime.resolve("bin/java").toString(), "--list-modules"), null).strip();
            out.add("módulos na imagem -> " + mods.lines().map(l -> l.replaceAll("@.*", "")).toList());
            out.add("roda a aplicação com o runtime mínimo -> " + Procs.run(List.of(runtime.resolve("bin/java").toString(), "-cp",
                classes.toString(), "App"), null).strip());
            long jdk = tamanho(Path.of(System.getProperty("java.home")));
            long min = tamanho(runtime);
            out.add("runtime mínimo é menos da metade do JDK -> " + (min * 2 < jdk));
        } finally { Fontes.apaga(work); }
        return out;
    }

    public static void main(String[] args) throws Exception { run().forEach(System.out::println); }
}
