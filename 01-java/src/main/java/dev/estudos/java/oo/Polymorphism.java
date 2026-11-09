package dev.estudos.java.oo;

import java.util.ArrayList;
import java.util.List;

/**
 * J04-04 · Polimorfismo e despacho dinâmico.
 * Documento: java-docs/01-java/04-orientacao-a-objetos.md §2.4.
 *
 * <p>Método de instância: despacho pelo tipo real. Campo e método estático: tipo declarado (ocultação).
 * Sobrecarga: escolhida em compilação pelo tipo declarado do argumento.
 *
 * <p>Saída real (JDK 25):
 * <pre>
 * dog animal Animal
 * sobrecarga com tipo declarado Object -&gt; over(Object)
 * sobrecarga com tipo declarado String -&gt; over(String)
 * </pre>
 *
 * <p>Variações: adicione {@code @Override} em {@code Dog.s()} (não compila: método estático não sobrescreve).
 */
public class Polymorphism {
    static class Animal {
        static String s() { return "Animal"; }
        String f = "animal";
        String who() { return "animal"; }
    }

    static class Dog extends Animal {
        static String s() { return "Dog"; }
        String f = "dog";
        @Override String who() { return "dog"; }
    }

    static String over(Object o) { return "over(Object)"; }
    static String over(String s) { return "over(String)"; }

    @SuppressWarnings("static-access")
    public static List<String> run() {
        List<String> out = new ArrayList<>();
        Animal an = new Dog();
        out.add(an.who() + " " + an.f + " " + an.s());
        Object o = "texto";
        out.add("sobrecarga com tipo declarado Object -> " + over(o));
        out.add("sobrecarga com tipo declarado String -> " + over("texto"));
        return out;
    }

    public static void main(String[] args) { run().forEach(System.out::println); }
}
