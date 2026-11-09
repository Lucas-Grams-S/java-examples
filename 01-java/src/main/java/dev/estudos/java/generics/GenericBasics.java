package dev.estudos.java.generics;

import java.util.ArrayList;
import java.util.List;

/**
 * J06-01 · Classes, interfaces e métodos genéricos.
 * Documento: java-docs/01-java/06-generics.md §2.1.
 *
 * <p>Saída real (JDK 25):
 * <pre>
 * Box&lt;Customer&gt;.get() -&gt; Customer[name=Ana]
 * firstOrNull -&gt; Customer[name=Ana] / null
 * antes do Java 5 -&gt; ClassCastException só em execução
 * </pre>
 *
 * <p>Variações: tente {@code String s = firstOrNull(List.of(1));} (erro de compilação: o tipo é inferido como Integer).
 */
public class GenericBasics {
    public static class Box<T> {
        private final T value;
        public Box(T value) { this.value = value; }
        public T get() { return value; }
    }

    record Customer(String name) {}

    public static <T> T firstOrNull(List<T> list) { return list.isEmpty() ? null : list.get(0); }

    @SuppressWarnings({"rawtypes", "unchecked"})
    static String beforeGenerics() {
        List orders = new ArrayList();
        orders.add("não sou um pedido");
        try {
            Customer c = (Customer) orders.get(0);
            return "sem erro " + c;
        } catch (ClassCastException e) {
            return "ClassCastException só em execução";
        }
    }

    public static List<String> run() {
        List<String> out = new ArrayList<>();
        var b = new Box<>(new Customer("Ana"));
        Customer c = b.get();   // sem cast
        out.add("Box<Customer>.get() -> " + c);
        out.add("firstOrNull -> " + firstOrNull(List.of(new Customer("Ana"))) + " / " + firstOrNull(List.<String>of()));
        out.add("antes do Java 5 -> " + beforeGenerics());
        return out;
    }

    public static void main(String[] args) { run().forEach(System.out::println); }
}
