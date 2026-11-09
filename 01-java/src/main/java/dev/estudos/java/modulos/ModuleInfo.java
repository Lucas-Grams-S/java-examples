package dev.estudos.java.modulos;

import dev.estudos.java.Procs;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * J14-02 · module-info.java de verdade: exports, exports ... to, opens, uses/provides, e o que acontece quando
 * outro módulo tenta usar um pacote não exportado ou fazer reflection em pacote não aberto.
 * Documento: java-docs/01-java/14-modulos-e-build.md §2.2.
 *
 * <p>Os módulos são gravados num diretório temporário e compilados com {@code javac --module-source-path}
 * (o módulo Maven deste projeto roda no classpath e não tem module-info).
 *
 * <p>Saída real (JDK 25):
 * <pre>
 * compila (sem opens) -&gt; exit 0
 * [sem opens] ServiceLoader -&gt; cartão 500
 * [sem opens] método público -&gt; o-1
 * [sem opens] reflection no campo private -&gt; InaccessibleObjectException
 * compila (opens ... to client) -&gt; exit 0
 * [com opens] reflection no campo private -&gt; o-1
 * usar pacote não exportado -&gt; exit 1
 * erro do javac -&gt; package com.example.orders.internal is not visible
 * </pre>
 */
public class ModuleInfo {
    static final String ORDERS_API = """
        package com.example.orders.api;
        public interface PaymentProvider { String pay(int cents); }
        """;
    static final String ORDERS_MODEL = """
        package com.example.orders.model;
        public class Order { private String id = "o-1"; public String id() { return id; } }
        """;
    static final String ORDERS_INTERNAL = """
        package com.example.orders.internal;
        public class Secret { public static String v() { return "ok"; } }
        """;
    static final String ORDERS_PAY = """
        package com.example.orders.pay;
        public class CardProvider implements com.example.orders.api.PaymentProvider {
            public String pay(int cents) { return "cartão " + cents; }
        }
        """;

    static String orders(String opens) {
        return """
            module com.example.orders {
                exports com.example.orders.api;
                exports com.example.orders.model;
                %s
                provides com.example.orders.api.PaymentProvider with com.example.orders.pay.CardProvider;
            }
            """.formatted(opens);
    }

    static final String CLIENT_MODULE = """
        module com.example.client {
            requires com.example.orders;
            uses com.example.orders.api.PaymentProvider;
        }
        """;

    static String clientMain(String importExtra, String corpoExtra) {
        return """
            package com.example.client;
            import java.util.ServiceLoader;
            import com.example.orders.api.PaymentProvider;
            import com.example.orders.model.Order;
            %s
            public class Main {
                public static void main(String[] a) throws Exception {
                    for (PaymentProvider p : ServiceLoader.load(PaymentProvider.class)) System.out.println("ServiceLoader -> " + p.pay(500));
                    Order o = new Order();
                    System.out.println("método público -> " + o.id());
                    %s
                }
            }
            """.formatted(importExtra, corpoExtra);
    }

    static final String REFLECTION = """
        try {
            var f = Order.class.getDeclaredField("id");
            f.setAccessible(true);
            System.out.println("reflection no campo private -> " + f.get(o));
        } catch (java.lang.reflect.InaccessibleObjectException e) {
            System.out.println("reflection no campo private -> InaccessibleObjectException");
        }
        """;

    static class Resultado {
        final List<String> out = new ArrayList<>();
    }

    static Path monta(String opens, String importExtra, String corpoExtra) {
        try {
            Path src = Files.createTempDirectory("jpms");
            Fontes.grava(src, "com.example.orders/module-info.java", orders(opens));
            Fontes.grava(src, "com.example.orders/com/example/orders/api/PaymentProvider.java", ORDERS_API);
            Fontes.grava(src, "com.example.orders/com/example/orders/model/Order.java", ORDERS_MODEL);
            Fontes.grava(src, "com.example.orders/com/example/orders/internal/Secret.java", ORDERS_INTERNAL);
            Fontes.grava(src, "com.example.orders/com/example/orders/pay/CardProvider.java", ORDERS_PAY);
            Fontes.grava(src, "com.example.client/module-info.java", CLIENT_MODULE);
            Fontes.grava(src, "com.example.client/com/example/client/Main.java", clientMain(importExtra, corpoExtra));
            return src;
        } catch (java.io.IOException e) { throw new IllegalStateException(e); }
    }

    static Procs.Result compila(Path src, Path out) {
        return Procs.exec(List.of(Procs.tool("javac"), "-d", out.toString(), "--module-source-path", src.toString(),
            "--module", "com.example.client,com.example.orders"), null);
    }

    static String executa(Path out) {
        return Procs.run(List.of(Procs.tool("java"), "-p", out.toString(), "-m", "com.example.client/com.example.client.Main"), null).strip();
    }

    public static List<String> run() throws Exception {
        List<String> out = new ArrayList<>();

        // 1) Sem opens: exports permite usar o tipo público, mas NÃO reflection em membro privado
        Path src1 = monta("", "", REFLECTION);
        Path out1 = Files.createTempDirectory("jpms-out");
        out.add("compila (sem opens) -> exit " + compila(src1, out1).exitCode());
        for (String l : executa(out1).split("\n")) out.add("[sem opens] " + l);

        // 2) Com opens ... to client: reflection profunda liberada só para esse módulo
        Path src2 = monta("opens com.example.orders.model to com.example.client;", "", REFLECTION);
        Path out2 = Files.createTempDirectory("jpms-out");
        out.add("compila (opens ... to client) -> exit " + compila(src2, out2).exitCode());
        for (String l : executa(out2).split("\n")) if (l.startsWith("reflection")) out.add("[com opens] " + l);

        // 3) Pacote não exportado: erro de compilação
        Path src3 = monta("", "import com.example.orders.internal.Secret;", "System.out.println(Secret.v());");
        Procs.Result r3 = compila(src3, Files.createTempDirectory("jpms-out"));
        out.add("usar pacote não exportado -> exit " + r3.exitCode());
        r3.output().lines().filter(l -> l.contains("error:")).findFirst()
            .ifPresent(l -> out.add("erro do javac -> " + l.substring(l.indexOf("error:") + 7)));

        for (Path p : List.of(src1, out1, src2, out2, src3)) Fontes.apaga(p);
        return out;
    }

    public static void main(String[] args) throws Exception { run().forEach(System.out::println); }
}
