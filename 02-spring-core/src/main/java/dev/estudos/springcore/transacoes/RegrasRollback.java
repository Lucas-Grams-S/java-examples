package dev.estudos.springcore.transacoes;

import static dev.estudos.springcore.transacoes.TxFixture.executar;

import dev.estudos.springcore.transacoes.TxFixture.*;
import java.util.ArrayList;
import java.util.List;

/**
 * SC09-04 · Regras de rollback (o ponto mais mal compreendido): por padrão só {@code RuntimeException}/
 * {@code Error} desfazem; exceção checada faz COMMIT; {@code rollbackFor} corrige.
 * Documento: 02-spring-core/09-transacoes.md §2.4.
 *
 * <p>Saída real:
 * <pre>
 * RuntimeException   -&gt; IllegalStateException | linhas=0 | auditoria=0
 * checked (padrão)   -&gt; IOException | linhas=1 | auditoria=0
 * checked + rollbackFor -&gt; IOException | linhas=0 | auditoria=0
 * </pre>
 */
public class RegrasRollback {
    public static List<String> run() {
        List<String> out = new ArrayList<>();
        try (var ctx = TxFixture.novoContexto()) {
            var svc = ctx.getBean(OrderService.class);
            var orders = ctx.getBean(Orders.class);
            out.add("RuntimeException   -> " + executar(orders, () -> svc.runtimeFail("a")));
            out.add("checked (padrão)   -> " + executar(orders, () -> svc.checkedFail("a")));
            out.add("checked + rollbackFor -> " + executar(orders, () -> svc.checkedRollback("a")));
        }
        return out;
    }

    public static void main(String[] args) { run().forEach(System.out::println); }
}
