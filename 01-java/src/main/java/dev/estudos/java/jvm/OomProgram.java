package dev.estudos.java.jvm;

import java.util.ArrayList;

/** Programa do documento ({@code Oom}): provoca StackOverflowError e OutOfMemoryError. Rode com {@code -Xmx32m}. */
public class OomProgram {
    static int depth = 0;
    static void recurse() { depth++; recurse(); }

    public static void main(String[] args) {
        try { recurse(); }
        catch (StackOverflowError e) { System.out.println("profundidade > 1000: " + (depth > 1000)); }
        try {
            var list = new ArrayList<long[]>();
            while (true) list.add(new long[1_000_000]);
        } catch (OutOfMemoryError e) { System.out.println(e.getMessage()); }
    }
}
