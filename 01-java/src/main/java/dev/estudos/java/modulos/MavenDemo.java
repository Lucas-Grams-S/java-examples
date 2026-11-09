package dev.estudos.java.modulos;

import dev.estudos.java.Procs;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * J14-04 · Maven de verdade: POM mínimo herdando spring-boot-starter-parent, dependência sem versão (vem do BOM),
 * ciclo de vida (fases executam as anteriores) e dependency:tree.
 * Documento: java-docs/01-java/14-modulos-e-build.md §2.4.
 *
 * <p>Usa o Maven Wrapper do repositório em modo offline ({@code -o}); precisa que os plugins já estejam em
 * {@code ~/.m2} (a build do projeto já os baixa).
 *
 * <p>Saída real (JDK 25):
 * <pre>
 * goals executados por 'package' -&gt; [resources:resources, compiler:compile]
 * BUILD SUCCESS -&gt; false
 * jar gerado -&gt; false
 * versão sem &lt;version&gt; no POM (vem do BOM) -&gt; org.junit.jupiter:junit-jupiter:jar:6.0.3:test
 * tree mostra transitivas (junit-jupiter-engine) -&gt; true
 * clean apaga target/ -&gt; true
 * </pre>
 */
public class MavenDemo {
    static final String POM = """
        <project xmlns="http://maven.apache.org/POM/4.0.0">
          <modelVersion>4.0.0</modelVersion>
          <parent>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-parent</artifactId>
            <version>4.1.2</version>
          </parent>
          <groupId>demo</groupId><artifactId>orders</artifactId><version>0.0.1-SNAPSHOT</version>
          <properties><java.version>25</java.version></properties>
          <dependencies>
            <dependency>
              <groupId>org.junit.jupiter</groupId><artifactId>junit-jupiter</artifactId>
              <scope>test</scope>
            </dependency>
          </dependencies>
        </project>
        """;

    /** Procura o mvnw subindo a partir do diretório atual. */
    static Path mvnw() {
        for (Path d = Path.of("").toAbsolutePath(); d != null; d = d.getParent()) {
            if (Files.isExecutable(d.resolve("mvnw"))) return d.resolve("mvnw");
        }
        throw new IllegalStateException("mvnw não encontrado");
    }

    static String mvn(Path projeto, String... args) {
        List<String> cmd = new ArrayList<>(List.of(mvnw().toString(), "-o", "-B", "-f", projeto.resolve("pom.xml").toString()));
        cmd.addAll(List.of(args));
        return Procs.run(cmd, null);
    }

    public static List<String> run() throws Exception {
        List<String> out = new ArrayList<>();
        Path p = Files.createTempDirectory("mvn-demo");
        try {
            Files.writeString(p.resolve("pom.xml"), POM);
            Fontes.grava(p, "src/main/java/demo/A.java", "package demo; public class A {}\n");

            String log = mvn(p, "package");
            List<String> plugins = log.lines().filter(l -> l.startsWith("[INFO] --- "))
                .map(l -> l.substring(11, l.indexOf(" (")).replaceAll(":[0-9.]+:", ":")).toList();
            out.add("goals executados por 'package' -> " + plugins);
            out.add("BUILD SUCCESS -> " + log.contains("BUILD SUCCESS"));
            out.add("jar gerado -> " + Files.exists(p.resolve("target/orders-0.0.1-SNAPSHOT.jar")));

            String tree = mvn(p, "dependency:tree");
            tree.lines().filter(l -> l.contains("org.junit.jupiter:junit-jupiter:jar"))
                .findFirst().ifPresent(l -> out.add("versão sem <version> no POM (vem do BOM) -> " + l.substring(l.indexOf("org."))));
            out.add("tree mostra transitivas (junit-jupiter-engine) -> " + tree.contains("junit-jupiter-engine"));

            mvn(p, "clean");
            out.add("clean apaga target/ -> " + !Files.exists(p.resolve("target")));
        } finally { Fontes.apaga(p); }
        return out;
    }

    public static void main(String[] args) throws Exception { run().forEach(System.out::println); }
}
