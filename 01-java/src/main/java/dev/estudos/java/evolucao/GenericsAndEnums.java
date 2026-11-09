package dev.estudos.java.evolucao;

import java.util.ArrayList;
import java.util.List;

/**
 * J00-01 · Java 5: generics, enums e annotations.
 * Documento: java-docs/01-java/00-evolucao-do-java.md §4.1.
 *
 * <p>Mostra a falha em <em>runtime</em> de uma lista sem generics, a versão verificada pelo compilador,
 * um enum como tipo seguro e uma annotation lida por reflection.
 *
 * <p>Saída real (JDK 25):
 * <pre>
 * sem generics -&gt; ClassCastException só em runtime
 * com generics -&gt; PEDIDO-1
 * enum seguro -&gt; PAID de [NEW, PAID, SHIPPED]
 * @Deprecated visível por reflection em runtime -&gt; true
 * </pre>
 *
 * <p>Variações: troque {@code List<String>} por {@code List<Integer>} e veja o erro de compilação;
 * adicione {@code SHIPPED} ao enum e use-o num {@code switch} sem {@code default}.
 */
public class GenericsAndEnums {
    enum OrderStatus { NEW, PAID, SHIPPED }

    @Deprecated
    static void legacy() {}

    @SuppressWarnings({"rawtypes", "unchecked"})
    public static List<String> run() throws Exception {
        List<String> out = new ArrayList<>();
        List items = new ArrayList();           // sem generics: aceita qualquer Object
        items.add("pedido-1");
        try {
            Integer errado = (Integer) items.get(0);
            out.add("não deveria chegar aqui: " + errado);
        } catch (ClassCastException e) {
            out.add("sem generics -> ClassCastException só em runtime");
        }
        List<String> ids = new ArrayList<>();
        ids.add("pedido-1");                    // ids.add(1) nem compila
        out.add("com generics -> " + ids.get(0).toUpperCase());
        out.add("enum seguro -> " + OrderStatus.valueOf("PAID") + " de " + List.of(OrderStatus.values()));
        boolean runtime = GenericsAndEnums.class.getDeclaredMethod("legacy").isAnnotationPresent(Deprecated.class);
        out.add("@Deprecated visível por reflection em runtime -> " + runtime);
        return out;
    }

    public static void main(String[] args) throws Exception {
        run().forEach(System.out::println);
    }
}
