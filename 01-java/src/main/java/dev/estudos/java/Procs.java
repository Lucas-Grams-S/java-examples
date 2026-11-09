package dev.estudos.java;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/** Utilitário dos exemplos: executa ferramentas do mesmo JDK que está rodando o exemplo e devolve a saída. */
public final class Procs {
    private Procs() {}

    /** Caminho de uma ferramenta do JDK atual (java, javac, javap, jcmd...). */
    public static String tool(String name) {
        return Path.of(System.getProperty("java.home"), "bin", name).toString();
    }

    /** Resultado de um subprocesso: código de saída e saída combinada (stdout + stderr). */
    public record Result(int exitCode, String output) {}

    /** Executa o comando (stderr junto de stdout), espera até 60 s e devolve a saída. */
    public static String run(List<String> command, String stdin) {
        return exec(command, stdin).output();
    }

    /** Como {@link #run}, mas devolve também o código de saída. */
    public static Result exec(List<String> command, String stdin) {
        try {
            Process p = new ProcessBuilder(command).redirectErrorStream(true).start();
            if (stdin != null) {
                p.getOutputStream().write(stdin.getBytes());
            }
            p.getOutputStream().close();
            String out = new String(p.getInputStream().readAllBytes());
            if (!p.waitFor(60, TimeUnit.SECONDS)) {
                p.destroyForcibly();
                throw new IllegalStateException("tempo esgotado: " + command);
            }
            return new Result(p.exitValue(), out);
        } catch (IOException | InterruptedException e) {
            throw new IllegalStateException(e);
        }
    }

    /** Atalho: {@code tool(args[0]) args[1..]}. */
    public static String tool(String name, String... args) {
        List<String> cmd = new ArrayList<>();
        cmd.add(tool(name));
        cmd.addAll(List.of(args));
        return run(cmd, null);
    }

    /** Roda {@code java <args>} e devolve também o código de saída. */
    public static Result javaExec(String... args) {
        List<String> cmd = new ArrayList<>();
        cmd.add(tool("java"));
        cmd.addAll(List.of(args));
        return exec(cmd, null);
    }

    /** Classpath do processo atual, para subprocessos que rodam classes deste módulo. */
    public static String classpath() {
        return System.getProperty("java.class.path");
    }

    /** Roda {@code java <args>} com o JDK atual, usando o classpath do processo atual quando preciso. */
    public static String java(String... args) {
        return tool("java", args);
    }
}
