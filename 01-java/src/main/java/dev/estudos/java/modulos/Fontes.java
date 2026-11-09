package dev.estudos.java.modulos;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/** Utilitário dos exemplos de módulos: grava fontes em diretório temporário. Não é exemplo. */
final class Fontes {
    private Fontes() {}

    static void grava(Path raiz, String caminho, String conteudo) {
        try {
            Path p = raiz.resolve(caminho);
            Files.createDirectories(p.getParent());
            Files.writeString(p, conteudo);
        } catch (IOException e) { throw new IllegalStateException(e); }
    }

    static String ultimaLinha(String s) {
        String[] ls = s.strip().split("\n");
        return ls.length == 0 ? "" : ls[ls.length - 1].strip();
    }

    static void apaga(Path dir) {
        try (var w = Files.walk(dir)) { w.sorted(java.util.Comparator.reverseOrder()).forEach(p -> p.toFile().delete()); }
        catch (IOException e) { throw new IllegalStateException(e); }
    }
}
