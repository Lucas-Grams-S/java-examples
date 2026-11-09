package dev.estudos.java.evolucao;

import dev.estudos.java.Procs;
import java.util.List;

/**
 * J00-04 · Java 9: módulos e JShell.
 * Documento: java-docs/01-java/00-evolucao-do-java.md §4.4.
 *
 * <p>O {@code module-info.java} do documento não entra neste módulo Maven (que roda no classpath, como a maioria
 * dos projetos Spring). Aqui inspecionamos o sistema de módulos em runtime e executamos o JShell de verdade.
 * Um {@code module-info.java} compilável fica em {@code J14} (documento 14-modulos-e-build).
 *
 * <p>Saída real (JDK 25):
 * <pre>
 * String está no módulo -&gt; java.base (nomeado=true)
 * esta classe está no módulo -&gt; unnamed (classpath)
 * java.base exporta java.util -&gt; true
 * java.base exporta jdk.internal.misc -&gt; false
 * jshell -&gt; $1 ==&gt; 6
 * </pre>
 */
public class ModulesAndJShell {
    public static List<String> run() {
        Module base = String.class.getModule();
        Module app = ModulesAndJShell.class.getModule();
        String jshell = Procs.run(List.of(Procs.tool("jshell")), "List.of(1, 2, 3).stream().mapToInt(i -> i).sum()\n/exit\n");
        return List.of("String está no módulo -> " + base.getName() + " (nomeado=" + base.isNamed() + ")",
                "esta classe está no módulo -> " + (app.isNamed() ? app.getName() : "unnamed (classpath)"),
                "java.base exporta java.util -> " + base.isExported("java.util"),
                "java.base exporta jdk.internal.misc -> " + base.isExported("jdk.internal.misc"),
                "jshell -> " + jshell.lines().filter(l -> l.contains("==>")).map(l -> l.replace("jshell> ", "").strip()).findFirst().orElse("?"));
    }

    public static void main(String[] args) {
        run().forEach(System.out::println);
    }
}
