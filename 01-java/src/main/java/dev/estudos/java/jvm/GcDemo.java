package dev.estudos.java.jvm;

import java.util.ArrayList;
import java.util.List;

/**
 * Programa auxiliar dos exemplos de GC: aloca arrays de 1 MB e mantém 1 a cada 10 (o doc chama de {@code Gc}).
 * Com {@code -Xmx64m} os arrays de 1 MB são <em>humongous</em> no G1.
 */
public class GcDemo {
    public static void main(String[] args) throws Exception {
        List<byte[]> keep = new ArrayList<>();
        for (int i = 0; i < 300; i++) {
            byte[] b = new byte[1_000_000];
            if (i % 10 == 0) keep.add(b);
        }
        System.out.println("mantidos=" + keep.size());
        if (args.length > 0 && args[0].equals("sleep")) Thread.sleep(30_000);
    }
}
