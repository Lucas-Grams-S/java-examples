package dev.estudos.java.modulos;

import dev.estudos.java.Procs;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * J14-03 · Validação rápida: javac --module-source-path, java -p -m, jar -d e jdeps --print-module-deps.
 * Documento: java-docs/01-java/14-modulos-e-build.md §2.3. Também mostra a API {@link Module} em runtime.
 *
 * <p>Saída real (JDK 25):
 * <pre>
 * javac -&gt; exit 0
 * java -p out -m ... -&gt; ok com.example.orders
 * jar -d mostra exports -&gt; true
 * jar -d lista internal como não exportado -&gt; true
 * jdeps --print-module-deps -&gt; java.base
 * String está no módulo -&gt; java.base (nomeado=true)
 * exporta java.lang -&gt; true
 * exporta jdk.internal.misc para o unnamed? -&gt; false
 * este código está em módulo nomeado? (classpath =&gt; unnamed) -&gt; false
 * </pre>
 */
public class ValidacaoRapida {
    public static List<String> run() throws Exception {
        List<String> out = new ArrayList<>();
        Path src = Files.createTempDirectory("val-src");
        Path classes = Files.createTempDirectory("val-out");
        try {
            Fontes.grava(src, "com.example.orders/module-info.java", """
                module com.example.orders { exports com.example.orders.api; }
                """);
            Fontes.grava(src, "com.example.orders/com/example/orders/api/Main.java", """
                package com.example.orders.api;
                import com.example.orders.internal.Secret;
                public class Main {
                    public static void main(String[] a) {
                        System.out.println(Secret.v() + " " + Main.class.getModule().getName());
                    }
                }
                """);
            Fontes.grava(src, "com.example.orders/com/example/orders/internal/Secret.java", """
                package com.example.orders.internal;
                public class Secret { public static String v() { return "ok"; } }
                """);
            var javac = Procs.exec(List.of(Procs.tool("javac"), "-d", classes.toString(), "--module-source-path", src.toString(),
                "--module", "com.example.orders"), null);
            out.add("javac -> exit " + javac.exitCode());
            out.add("java -p out -m ... -> " + Procs.run(List.of(Procs.tool("java"), "-p", classes.toString(), "-m",
                "com.example.orders/com.example.orders.api.Main"), null).strip());

            Path jar = classes.resolveSibling(classes.getFileName() + ".jar");
            Procs.run(List.of(Procs.tool("jar"), "--create", "--file", jar.toString(), "-C",
                classes.resolve("com.example.orders").toString(), "."), null);
            String desc = Procs.run(List.of(Procs.tool("jar"), "-d", "--file", jar.toString()), null);
            out.add("jar -d mostra exports -> " + desc.contains("exports com.example.orders.api"));
            out.add("jar -d lista internal como não exportado -> " + (desc.contains("contains com.example.orders.internal")));
            String deps = Procs.run(List.of(Procs.tool("jdeps"), "--module-path", classes.toString(), "--print-module-deps",
                "-m", "com.example.orders"), null).strip();
            out.add("jdeps --print-module-deps -> " + deps);
            Files.deleteIfExists(jar);
        } finally { Fontes.apaga(src); Fontes.apaga(classes); }

        // API de módulos em runtime
        Module base = String.class.getModule();
        out.add("String está no módulo -> " + base.getName() + " (nomeado=" + base.isNamed() + ")");
        out.add("exporta java.lang -> " + base.isExported("java.lang"));
        out.add("exporta jdk.internal.misc para o unnamed? -> " + base.isExported("jdk.internal.misc", ValidacaoRapida.class.getModule()));
        out.add("este código está em módulo nomeado? (classpath => unnamed) -> " + ValidacaoRapida.class.getModule().isNamed());
        return out;
    }

    public static void main(String[] args) throws Exception { run().forEach(System.out::println); }
}
