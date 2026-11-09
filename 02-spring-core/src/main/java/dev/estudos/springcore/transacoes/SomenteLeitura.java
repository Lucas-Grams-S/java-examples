package dev.estudos.springcore.transacoes;

import dev.estudos.springcore.transacoes.TxFixture.*;
import java.util.ArrayList;
import java.util.List;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronizationManager;

/**
 * SC09-14 · {@code @Transactional(readOnly = true)} em consultas. O flag chega ao {@code TransactionSynchronizationManager}
 * (o Hibernate/JPA o usa para evitar flush e o driver pode otimizar). JPA em si fica para o módulo Boot
 * (não há JPA neste módulo). Documento: 02-spring-core/09-transacoes.md §5.4.
 *
 * <p>Saída real:
 * <pre>
 * método de leitura: transação ativa=true, readOnly=true
 * método de escrita: transação ativa=true, readOnly=false
 * sem @Transactional: transação ativa=false
 * </pre>
 */
public class SomenteLeitura {
    @Service static class OrderQueries {
        @Transactional(readOnly = true)
        public String recent() { return estado(); }
        @Transactional public String write() { return estado(); }
        public String none() { return estado(); }
        private String estado() {
            boolean ativa = TransactionSynchronizationManager.isActualTransactionActive();
            return ativa ? "transação ativa=true, readOnly=" + TransactionSynchronizationManager.isCurrentTransactionReadOnly()
                         : "transação ativa=false";
        }
    }

    public static List<String> run() {
        List<String> out = new ArrayList<>();
        try (var ctx = new AnnotationConfigApplicationContext(TxFixture.Cfg.class, OrderQueries.class)) {
            var q = ctx.getBean(OrderQueries.class);
            out.add("método de leitura: " + q.recent());
            out.add("método de escrita: " + q.write());
            out.add("sem @Transactional: " + q.none());
        }
        return out;
    }

    public static void main(String[] args) { run().forEach(System.out::println); }
}
