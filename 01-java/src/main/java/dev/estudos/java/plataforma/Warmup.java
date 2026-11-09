package dev.estudos.java.plataforma;

import java.util.ArrayList;
import java.util.List;

/**
 * J01-13 · Ver o efeito do aquecimento.
 * Documento: java-docs/01-java/01-plataforma-java.md §5.3.
 *
 * <p><b>Demonstração, não benchmark:</b> as primeiras rodadas tendem a ser mais lentas até o laço ser compilado
 * pelo C2; os tempos variam por máquina e por execução. Para medir de verdade, use JMH.
 *
 * <p>Saída real (JDK 25; os tempos variam):
 * <pre>
 * rodada 1: 46 ms (r=149999997)
 * rodada 2: 40 ms (r=149999997)
 * rodada 3: 35 ms (r=149999997)
 * rodada 4: 35 ms (r=149999997)
 * rodada 5: 35 ms (r=149999997)
 * </pre>
 *
 * <p>Variações: rode com {@code -Xint} (sem JIT) e compare com o padrão.
 */
public class Warmup {
    static long work(int n) {
        long acc = 0;
        for (int i = 0; i < n; i++) acc += i % 7;
        return acc;
    }

    public static List<String> run(int rounds, int n) {
        List<String> out = new ArrayList<>();
        for (int round = 1; round <= rounds; round++) {
            long t0 = System.nanoTime();
            long r = work(n);
            long ms = (System.nanoTime() - t0) / 1_000_000;
            out.add("rodada " + round + ": " + ms + " ms (r=" + r + ")");
        }
        return out;
    }

    public static void main(String[] args) {
        run(5, 50_000_000).forEach(System.out::println);
    }
}
