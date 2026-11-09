package dev.estudos.java.modernos;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * J05-06 · Sealed classes e interfaces.
 * Documento: java-docs/01-java/05-recursos-modernos-da-linguagem.md §2.6.
 *
 * <p>Saída real (JDK 25):
 * <pre>
 * isSealed -&gt; true
 * permitidos -&gt; [Pix, Card, Boleto]
 * Vehicle permite -&gt; [Car, Truck, Custom]
 * Custom (non-sealed) isSealed -&gt; false; MyCustom estende Custom -&gt; true
 * </pre>
 *
 * <p>Variações: crie {@code record Cripto implements Payment} e veja o erro (não está em {@code permits}).
 */
public class SealedTypes {
    public sealed interface Payment permits Pix, Card, Boleto {}
    public record Pix(String key) implements Payment {}
    public record Card(String last4, int installments) implements Payment {}
    public record Boleto(String barcode) implements Payment {}

    sealed static abstract class Vehicle permits Car, Truck, Custom {}
    static final class Car extends Vehicle {}
    static sealed class Truck extends Vehicle permits BigTruck {}
    static final class BigTruck extends Truck {}
    static non-sealed class Custom extends Vehicle {}
    static class MyCustom extends Custom {}   // permitido: Custom reabre a hierarquia

    public static List<String> run() {
        List<String> out = new ArrayList<>();
        out.add("isSealed -> " + Payment.class.isSealed());
        out.add("permitidos -> " + Arrays.stream(Payment.class.getPermittedSubclasses()).map(Class::getSimpleName).toList());
        out.add("Vehicle permite -> " + Arrays.stream(Vehicle.class.getPermittedSubclasses()).map(Class::getSimpleName).toList());
        out.add("Custom (non-sealed) isSealed -> " + Custom.class.isSealed() + "; MyCustom estende Custom -> "
                + (MyCustom.class.getSuperclass() == Custom.class));
        return out;
    }

    public static void main(String[] args) { run().forEach(System.out::println); }
}
