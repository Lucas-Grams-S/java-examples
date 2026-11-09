package dev.estudos.java.modulos;

import dev.estudos.java.Procs;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

/**
 * J14-05 · Gradle de verdade: o {@code build.gradle.kts} do documento (plugin do Spring Boot + dependency-management),
 * {@code build}, {@code bootJar} e as configurações {@code implementation}/{@code runtimeOnly}.
 * Documento: java-docs/01-java/14-modulos-e-build.md §2.5.
 *
 * <p>Gradle não vem com o projeto: procura {@code $GRADLE_HOME/bin/gradle}, {@code ~/tools/gradle-*}/bin/gradle ou
 * {@code gradle} no PATH. Precisa de rede (plugins e dependências vêm do Maven Central / Plugin Portal).
 * Sem Gradle, {@link #run()} devolve uma lista vazia.
 *
 * <p>Saída real (Gradle 9.8.2, JDK 25):
 * <pre>
 * build -&gt; exit 0
 * jar executável (bootJar) -&gt; true
 * jar simples (plain) -&gt; true
 * implementation sem versão (vem do plugin) -&gt; org.springframework.boot:spring-boot-starter-web -&gt; 4.1.2
 * runtimeOnly fora do classpath de compilação -&gt; true
 * runtimeOnly no classpath de execução -&gt; true
 * </pre>
 */
public class GradleDemo {
    static final String BUILD = """
        plugins {
            java
            id("org.springframework.boot") version "4.1.2"
            id("io.spring.dependency-management") version "1.1.7"
        }
        java { toolchain { languageVersion = JavaLanguageVersion.of(25) } }
        repositories { mavenCentral() }
        dependencies {
            implementation("org.springframework.boot:spring-boot-starter-web")
            runtimeOnly("org.postgresql:postgresql")
            testImplementation("org.springframework.boot:spring-boot-starter-test")
        }
        tasks.test { useJUnitPlatform() }
        """;

    static Path gradle() {
        String home = System.getenv("GRADLE_HOME");
        if (home != null && Files.isExecutable(Path.of(home, "bin", "gradle"))) return Path.of(home, "bin", "gradle");
        Path tools = Path.of(System.getProperty("user.home"), "tools");
        if (Files.isDirectory(tools)) {
            try (Stream<Path> s = Files.list(tools)) {
                var achado = s.filter(d -> d.getFileName().toString().startsWith("gradle-"))
                    .map(d -> d.resolve("bin/gradle")).filter(Files::isExecutable).findFirst();
                if (achado.isPresent()) return achado.get();
            } catch (IOException ignored) { }
        }
        for (String d : System.getenv().getOrDefault("PATH", "").split(":"))
            if (Files.isExecutable(Path.of(d, "gradle"))) return Path.of(d, "gradle");
        return null;
    }

    static String gradle(Path g, Path projeto, String... args) {
        List<String> cmd = new ArrayList<>(List.of("env", "JAVA_HOME=" + System.getProperty("java.home"),
            g.toString(), "--no-daemon", "-q", "-p", projeto.toString()));
        cmd.addAll(List.of(args));
        return Procs.run(cmd, null);
    }

    public static List<String> run() throws Exception {
        List<String> out = new ArrayList<>();
        Path g = gradle();
        if (g == null) return out;
        Path p = Files.createTempDirectory("gradle-demo");
        try {
            Files.writeString(p.resolve("build.gradle.kts"), BUILD);
            Files.writeString(p.resolve("settings.gradle.kts"), "rootProject.name = \"orders\"\n");
            Fontes.grava(p, "src/main/java/demo/App.java", """
                package demo;
                import org.springframework.boot.autoconfigure.SpringBootApplication;
                @SpringBootApplication public class App { public static void main(String[] a) { } }
                """);
            gradle(g, p, "build");
            out.add("build -> exit 0");   // Procs.run lança exceção se o exit code não for 0
            out.add("jar executável (bootJar) -> " + Files.exists(p.resolve("build/libs/orders.jar")));
            out.add("jar simples (plain) -> " + Files.exists(p.resolve("build/libs/orders-plain.jar")));
            String compile = gradle(g, p, "dependencies", "--configuration", "compileClasspath");
            compile.lines().filter(l -> l.contains("spring-boot-starter-web ->")).findFirst()
                .ifPresent(l -> out.add("implementation sem versão (vem do plugin) -> " + l.substring(l.indexOf("org."))));
            out.add("runtimeOnly fora do classpath de compilação -> " + !compile.contains("org.postgresql:postgresql"));
            String runtime = gradle(g, p, "dependencies", "--configuration", "runtimeClasspath");
            out.add("runtimeOnly no classpath de execução -> " + runtime.contains("org.postgresql:postgresql"));
        } finally { Fontes.apaga(p); }
        return out;
    }

    public static void main(String[] args) throws Exception { run().forEach(System.out::println); }
}
