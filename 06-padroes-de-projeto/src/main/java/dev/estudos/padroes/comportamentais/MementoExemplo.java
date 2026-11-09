package dev.estudos.padroes.comportamentais;

import java.util.ArrayList;
import java.util.List;

/**
 * DP04-10 · Memento: snapshot imutável com {@code record} e a armadilha de não copiar a lista.
 * Documento: java-docs/06-padroes-de-projeto/04-comportamentais.md §5.5.
 *
 * <p>Saída real (JDK 25):
 * <pre>
 * antes do restore -&gt; [x, y]
 * após restore -&gt; [x]
 * snapshot sem cópia 'restaura' o estado alterado -&gt; [x, y]
 * </pre>
 *
 * <p>Variações: troque {@code List.copyOf(items)} por {@code items} em {@code saveRaso} e restaure.
 */
public class MementoExemplo {

    record Snapshot(List<String> items) {}

    static class Editor {
        List<String> items = new ArrayList<>();
        Snapshot save() { return new Snapshot(List.copyOf(items)); }
        Snapshot saveRaso() { return new Snapshot(items); }          // a armadilha: compartilha a lista mutável
        void restore(Snapshot s) { items = new ArrayList<>(s.items()); }
    }

    public static List<String> run() {
        List<String> out = new ArrayList<>();
        var e = new Editor();
        e.items.add("x");
        var s = e.save();
        e.items.add("y");
        out.add("antes do restore -> " + e.items);
        e.restore(s);
        out.add("após restore -> " + e.items);

        var e2 = new Editor();
        e2.items.add("x");
        var raso = e2.saveRaso();
        e2.items.add("y");
        e2.restore(raso);
        out.add("snapshot sem cópia 'restaura' o estado alterado -> " + e2.items);
        return out;
    }

    public static void main(String[] args) { run().forEach(System.out::println); }
}
