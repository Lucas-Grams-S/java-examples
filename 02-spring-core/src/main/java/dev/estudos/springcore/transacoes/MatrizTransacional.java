package dev.estudos.springcore.transacoes;

import static dev.estudos.springcore.transacoes.TxFixture.executar;

import dev.estudos.springcore.transacoes.TxFixture.*;
import java.util.ArrayList;
import java.util.List;

/**
 * SC09-11 · Matriz de comportamento de {@code @Transactional}, executada em H2: commit, rollback padrão,
 * checked exception (commit!), {@code rollbackFor}, {@code REQUIRES_NEW}, rollback-only,
 * self-invocation e {@code MANDATORY}. Documento: 02-spring-core/09-transacoes.md §5.1.
 *
 * <p>Saída real:
 * <pre>
 * okCommit        -&gt; nenhuma | linhas=1 | auditoria=0
 * runtimeFail     -&gt; IllegalStateException | linhas=0 | auditoria=0
 * checkedFail     -&gt; IOException | linhas=1 | auditoria=0
 * checkedRollback -&gt; IOException | linhas=0 | auditoria=0
 * withAudit       -&gt; IllegalStateException | linhas=0 | auditoria=1
 * swallow         -&gt; UnexpectedRollbackException | linhas=0 | auditoria=0
 * setRollbackOnly -&gt; nenhuma | linhas=0 | auditoria=0
 * inner(self)     -&gt; IllegalStateException | linhas=1 | auditoria=0
 * noTx(MANDATORY) -&gt; IllegalTransactionStateException | linhas=0 | auditoria=0
 * </pre>
 * (Na coluna "linhas" do documento, {@code withAudit} = 1 conta a linha de auditoria que sobreviveu.)
 */
public class MatrizTransacional {
    public static List<String> run() {
        List<String> out = new ArrayList<>();
        try (var ctx = TxFixture.novoContexto()) {
            var svc = ctx.getBean(OrderService.class);
            var orders = ctx.getBean(Orders.class);
            out.add("okCommit        -> " + executar(orders, () -> svc.okCommit("a")));
            out.add("runtimeFail     -> " + executar(orders, () -> svc.runtimeFail("a")));
            out.add("checkedFail     -> " + executar(orders, () -> svc.checkedFail("a")));
            out.add("checkedRollback -> " + executar(orders, () -> svc.checkedRollback("a")));
            out.add("withAudit       -> " + executar(orders, () -> svc.withAudit("a")));
            out.add("swallow         -> " + executar(orders, () -> svc.swallow("a")));
            out.add("setRollbackOnly -> " + executar(orders, () -> svc.setRollbackOnly("a")));
            out.add("inner(self)     -> " + executar(orders, () -> svc.inner("a")));
            out.add("noTx(MANDATORY) -> " + executar(orders, svc::noTx));
        }
        return out;
    }

    public static void main(String[] args) { run().forEach(System.out::println); }
}
