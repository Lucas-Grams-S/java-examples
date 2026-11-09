package dev.estudos.java.io;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

/**
 * J13-11 · Realista: importar CSV grande linha a linha com Files.lines (sem carregar tudo na memória).
 * Documento: java-docs/01-java/13-io-nio-e-http.md §5.4.
 *
 * <p>Gera 200 mil linhas; o consumo de memória fica constante porque o stream é preguiçoso.
 *
 * <p>Saída real (JDK 25):
 * <pre>
 * tamanho do arquivo &gt; 1 MB -&gt; true
 * linhas importadas (total &gt; 0) -&gt; 180000
 * soma dos totais -&gt; 900000
 * </pre>
 */
public class CsvGrande {
    record OrderLine(String id, int total) {}

    public static List<String> run() throws IOException {
        List<String> out = new ArrayList<>();
        Path csv = Files.createTempFile("orders", ".csv");
        try {
            int linhas = 200_000;
            try (BufferedWriter w = Files.newBufferedWriter(csv)) {
                w.write("id;total\n");
                for (int i = 1; i <= linhas; i++) { w.write("o" + i + ";" + (i % 10) + "\n"); }   // total 0 em 10% das linhas
            }
            out.add("tamanho do arquivo > 1 MB -> " + (Files.size(csv) > 1_000_000));
            long[] importadas = {0}, soma = {0};
            try (Stream<String> lines = Files.lines(csv)) {
                lines.skip(1)
                     .map(l -> l.split(";"))
                     .map(p -> new OrderLine(p[0], Integer.parseInt(p[1])))
                     .filter(o -> o.total() > 0)
                     .forEach(o -> { importadas[0]++; soma[0] += o.total(); });
            }
            out.add("linhas importadas (total > 0) -> " + importadas[0]);
            out.add("soma dos totais -> " + soma[0]);
        } finally { Files.deleteIfExists(csv); }
        return out;
    }

    public static void main(String[] args) throws IOException { run().forEach(System.out::println); }
}
