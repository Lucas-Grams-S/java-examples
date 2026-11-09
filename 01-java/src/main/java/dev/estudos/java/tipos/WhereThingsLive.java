package dev.estudos.java.tipos;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

/**
 * J03-08 · Onde vive cada coisa.
 * Documento: java-docs/01-java/03-tipos-e-memoria.md §3.1.
 *
 * <p>Demonstra com {@link WeakReference} que um campo {@code static} é raiz do GC (retém o objeto) e que um objeto
 * só referenciado por variável local fica elegível à coleta. {@code System.gc()} é só uma sugestão; o exemplo tenta
 * algumas vezes. Também compara o custo de {@code int[]} e {@code Integer[]} pela contagem de objetos distintos.
 *
 * <p>Saída real (JDK 25):
 * <pre>
 * objeto só em variável local, após o método terminar -&gt; coletado: true
 * objeto em campo static -&gt; coletado: false
 * depois de zerar o campo static -&gt; coletado: true
 * int[1000]: 1 objeto, valores contíguos · Integer[1000]: 1 array + 1000 objetos Integer (apenas ponteiros no array)
 * prim.length=1000
 * </pre>
 */
public class WhereThingsLive {
    static Object staticField;                            // campo estático: raiz do GC enquanto a classe viver

    static boolean collected(WeakReference<?> ref) throws InterruptedException {
        for (int i = 0; i < 10 && ref.get() != null; i++) { System.gc(); Thread.sleep(20); }
        return ref.get() == null;
    }

    static WeakReference<Object> localOnly() {
        Object tmp = new Object();                        // só no frame desta chamada
        return new WeakReference<>(tmp);
    }

    public static List<String> run() throws InterruptedException {
        List<String> out = new ArrayList<>();
        out.add("objeto só em variável local, após o método terminar -> coletado: " + collected(localOnly()));
        staticField = new Object();
        var ref = new WeakReference<>(staticField);
        out.add("objeto em campo static -> coletado: " + collected(ref));
        staticField = null;
        out.add("depois de zerar o campo static -> coletado: " + collected(ref));
        int[] prim = new int[1000];
        Integer[] boxed = new Integer[1000];
        for (int i = 0; i < 1000; i++) boxed[i] = i + 1000;   // fora do cache -128..127: 1000 objetos distintos
        out.add("int[1000]: 1 objeto, valores contíguos · Integer[1000]: 1 array + "
                + java.util.Arrays.stream(boxed).distinct().count() + " objetos Integer (apenas ponteiros no array)");
        out.add("prim.length=" + prim.length);
        return out;
    }

    public static void main(String[] args) throws InterruptedException {
        run().forEach(System.out::println);
    }
}
