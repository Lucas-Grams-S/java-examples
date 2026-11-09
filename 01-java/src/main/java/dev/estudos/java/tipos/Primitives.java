package dev.estudos.java.tipos;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * J03-01 · Os oito primitivos.
 * Documento: java-docs/01-java/03-tipos-e-memoria.md §2.1.
 *
 * <p>Armadilhas numéricas (todas verificadas no Java 25), valores padrão de campos e aritmética exata.
 *
 * <p>Saída real (JDK 25):
 * <pre>
 * 0.1 + 0.2 = 0.30000000000000004
 * Integer.MAX_VALUE + 1 = -2147483648
 * 5 / 2 = 2
 * -5 % 3 = -2
 * Math.floorMod(-5, 3) = 1
 * (byte) 200 = -56
 * NaN == NaN = false
 * BigDecimal 0.1 + 0.2 = 0.3
 * Math.addExact(MAX_VALUE, 1) -&gt; ArithmeticException: integer overflow
 * padrões de campo: byte=0 short=0 int=0 long=0L float=0.0 double=0.0 char=0 boolean=false
 * tamanhos (bits): byte=8 short=16 int=32 long=64 float=32 double=64 char=16
 * </pre>
 *
 * <p>Variações: troque {@code Math.addExact} por {@code +} e veja o overflow silencioso; use {@code new BigDecimal(0.1)}
 * (a partir de {@code double}) e veja os dígitos extras.
 */
public class Primitives {
    static byte b; static short sh; static int i; static long l; static float f; static double d; static char c; static boolean z;

    @SuppressWarnings("divzero")
    public static List<String> run() {
        List<String> out = new ArrayList<>();
        out.add("0.1 + 0.2 = " + (0.1 + 0.2));
        out.add("Integer.MAX_VALUE + 1 = " + (Integer.MAX_VALUE + 1));
        out.add("5 / 2 = " + (5 / 2));
        out.add("-5 % 3 = " + (-5 % 3));
        out.add("Math.floorMod(-5, 3) = " + Math.floorMod(-5, 3));
        out.add("(byte) 200 = " + (byte) 200);
        out.add("NaN == NaN = " + (Double.NaN == Double.NaN));
        out.add("BigDecimal 0.1 + 0.2 = " + new BigDecimal("0.1").add(new BigDecimal("0.2")));
        try {
            Math.addExact(Integer.MAX_VALUE, 1);
        } catch (ArithmeticException e) {
            out.add("Math.addExact(MAX_VALUE, 1) -> ArithmeticException: " + e.getMessage());
        }
        out.add("padrões de campo: byte=" + b + " short=" + sh + " int=" + i + " long=" + l + "L float=" + f + " double=" + d
                + " char=" + (int) c + " boolean=" + z);
        out.add("tamanhos (bits): byte=" + Byte.SIZE + " short=" + Short.SIZE + " int=" + Integer.SIZE + " long=" + Long.SIZE
                + " float=" + Float.SIZE + " double=" + Double.SIZE + " char=" + Character.SIZE);
        return out;
    }

    public static void main(String[] args) {
        run().forEach(System.out::println);
    }
}
