package dev.estudos.java.generics;

import java.util.ArrayList;
import java.util.List;

/**
 * J06-02 · Limites ({@code bounds}).
 * Documento: java-docs/01-java/06-generics.md §2.2 (e a nota de {@code Comparable<? super T>} em §8).
 *
 * <p>Saída real (JDK 25):
 * <pre>
 * max de strings -&gt; uva
 * max de inteiros -&gt; 9
 * maxNumber (Number &amp; Comparable) -&gt; 2.5
 * maxSuper com Comparable&lt;? super T&gt; -&gt; Dog5
 * </pre>
 *
 * <p>Variações: chame {@code max(List.of(new Object()))} (não compila: {@code Object} não é {@code Comparable}).
 */
public class Bounds {
    public static <T extends Comparable<T>> T max(List<T> list) {
        T best = list.get(0);
        for (T t : list) if (t.compareTo(best) > 0) best = t;
        return best;
    }

    /** Limites múltiplos: classe primeiro, depois interfaces. */
    public static <T extends Number & Comparable<T>> T maxNumber(List<T> list) { return max(list); }

    static class Animal implements Comparable<Animal> {
        final int size;
        Animal(int size) { this.size = size; }
        public int compareTo(Animal o) { return Integer.compare(size, o.size); }
        public String toString() { return getClass().getSimpleName() + size; }
    }

    static class Dog extends Animal { Dog(int size) { super(size); } }

    /** Mais permissivo: aceita List<Dog> mesmo que o compareTo seja de Animal. */
    static <T extends Comparable<? super T>> T maxSuper(List<T> list) {
        T best = list.get(0);
        for (T t : list) if (t.compareTo(best) > 0) best = t;
        return best;
    }

    public static List<String> run() {
        List<String> out = new ArrayList<>();
        out.add("max de strings -> " + max(List.of("pera", "uva", "maçã")));
        out.add("max de inteiros -> " + max(List.of(3, 9, 4)));
        out.add("maxNumber (Number & Comparable) -> " + maxNumber(List.of(2.5, 1.5)));
        // max(List.of(new Dog(1), new Dog(2))) não compila: Dog é Comparable<Animal>, não Comparable<Dog>
        out.add("maxSuper com Comparable<? super T> -> " + maxSuper(List.of(new Dog(1), new Dog(5))));
        return out;
    }

    public static void main(String[] args) { run().forEach(System.out::println); }
}
