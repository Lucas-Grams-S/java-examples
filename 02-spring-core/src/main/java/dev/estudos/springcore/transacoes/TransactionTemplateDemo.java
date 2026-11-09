package dev.estudos.springcore.transacoes;

import dev.estudos.springcore.transacoes.TxFixture.*;
import java.util.ArrayList;
import java.util.List;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

/**
 * SC09-12 · Transação programática com {@code TransactionTemplate}: rollback explícito, commit, e
 * {@code RuntimeException} no callback também desfaz. Documento: 02-spring-core/09-transacoes.md §5.2.
 *
 * <p>Saída real:
 * <pre>
 * setRollbackOnly no callback -&gt; linhas=0
 * callback normal -&gt; linhas=1
 * RuntimeException no callback -&gt; linhas=0 (IllegalStateException)
 * </pre>
 */
public class TransactionTemplateDemo {
    public static List<String> run() {
        List<String> out = new ArrayList<>();
        try (var ctx = TxFixture.novoContexto()) {
            var orders = ctx.getBean(Orders.class);
            var tt = new TransactionTemplate(ctx.getBean(PlatformTransactionManager.class));

            orders.clear();
            tt.executeWithoutResult(status -> { orders.insert("t"); status.setRollbackOnly(); });
            out.add("setRollbackOnly no callback -> linhas=" + orders.count());

            orders.clear();
            tt.executeWithoutResult(status -> orders.insert("t"));
            out.add("callback normal -> linhas=" + orders.count());

            orders.clear();
            String ex = "nenhuma";
            try {
                tt.executeWithoutResult(status -> { orders.insert("t"); throw new IllegalStateException("x"); });
            } catch (IllegalStateException e) { ex = e.getClass().getSimpleName(); }
            out.add("RuntimeException no callback -> linhas=" + orders.count() + " (" + ex + ")");
        }
        return out;
    }

    public static void main(String[] args) { run().forEach(System.out::println); }
}
