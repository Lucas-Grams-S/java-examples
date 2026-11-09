package dev.estudos.java.concorrencia;

import java.util.ArrayList;
import java.util.List;

/**
 * J12-10 · Scoped Values (finais no Java 25): valor imutável visível na cadeia de chamadas dentro do escopo.
 * Documento: java-docs/01-java/12-concorrencia.md §3.5.
 *
 * <p>Saída real (JDK 25):
 * <pre>
 * antes do escopo isBound -&gt; false
 * dentro do escopo -&gt; ana
 * servico vê ana
 * escopo aninhado -&gt; bia
 * de volta ao externo -&gt; ana
 * depois do escopo isBound -&gt; false
 * call devolve valor -&gt; CAIO
 * get fora do escopo -&gt; NoSuchElementException
 * orElse fora do escopo -&gt; anônimo
 * </pre>
 */
public class ScopedValues {
    static final ScopedValue<String> USER = ScopedValue.newInstance();

    static String servico() { return "servico vê " + USER.get(); }

    public static List<String> run() throws Exception {
        List<String> out = new ArrayList<>();
        out.add("antes do escopo isBound -> " + USER.isBound());
        ScopedValue.where(USER, "ana").run(() -> {
            out.add("dentro do escopo -> " + USER.get());
            out.add(servico());   // visível em toda a cadeia de chamadas, sem passar por parâmetro
            // religação aninhada: vale só no escopo interno
            ScopedValue.where(USER, "bia").run(() -> out.add("escopo aninhado -> " + USER.get()));
            out.add("de volta ao externo -> " + USER.get());
        });
        out.add("depois do escopo isBound -> " + USER.isBound());
        String r = ScopedValue.where(USER, "caio").call(() -> USER.get().toUpperCase());
        out.add("call devolve valor -> " + r);
        try { USER.get(); } catch (java.util.NoSuchElementException e) { out.add("get fora do escopo -> NoSuchElementException"); }
        out.add("orElse fora do escopo -> " + USER.orElse("anônimo"));
        return out;
    }

    public static void main(String[] args) throws Exception { run().forEach(System.out::println); }
}
