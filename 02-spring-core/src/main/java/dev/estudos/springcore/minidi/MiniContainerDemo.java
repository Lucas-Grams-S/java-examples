package dev.estudos.springcore.minidi;

import dev.estudos.springcore.minidi.app.*;
import java.util.ArrayList;
import java.util.List;

/**
 * SC06-11 · Mini-container de DI em Java puro (~100 linhas): scanning, definições, injeção por construtor,
 * singleton, callback de init e proxy JDK. Documento: 02-spring-core/06-como-java-e-spring-conversam.md §5.1.
 *
 * <p>Saída real:
 * <pre>
 * [init] OrderRepository pronto
 * salvo o-1 / cobrado 1500
 * [proxy] CardGateway.charge chamado
 * gateway é proxy JDK -&gt; true
 * mesma instância (singleton) -&gt; true
 * </pre>
 */
public class MiniContainerDemo {
    public static List<String> run() throws Exception {
        MiniContext.log.clear();
        var ctx = new MiniContext("dev.estudos.springcore.minidi.app");
        List<String> out = new ArrayList<>(MiniContext.log);   // [init] aconteceu na subida
        int antes = out.size();
        out.add(ctx.getBean(OrderService.class).place("o-1"));
        out.addAll(MiniContext.log.subList(antes, MiniContext.log.size()));
        out.add("gateway é proxy JDK -> " + ctx.getBean(PaymentGateway.class).getClass().getSimpleName().startsWith("$Proxy"));
        out.add("mesma instância (singleton) -> " + (ctx.getBean("orderService") == ctx.getBean(OrderService.class)));
        return out;
    }

    /** Dois beans que dependem um do outro: a subida falha com "Dependência circular". */
    public static String ciclo() throws Exception {
        try {
            new MiniContext("dev.estudos.springcore.minidi.ciclo");
            return "sem erro";
        } catch (IllegalStateException e) {
            return e.getMessage();
        }
    }

    public static void main(String[] args) throws Exception { run().forEach(System.out::println); System.out.println(ciclo()); }
}
