package dev.estudos.springcore.transacoes;

import dev.estudos.springcore.transacoes.TxFixture.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import org.springframework.stereotype.Component;

/**
 * SC09-13 · Onde colocar {@code @Transactional}: na fronteira do caso de uso (serviço), não no repositório.
 * Um caso de uso = uma transação curta; efeitos externos (e-mail) só depois do commit
 * ({@code AFTER_COMMIT}). Compara com repositório "transacional por chamada" (cada insert comita sozinho).
 * Documento: 02-spring-core/09-transacoes.md §5.3.
 *
 * <p>Saída real:
 * <pre>
 * caso de uso falha no 2º insert -&gt; linhas=0 (tudo ou nada), e-mails enviados=[]
 * sem fronteira (cada insert comita) -&gt; linhas=1 (parcial!)
 * caso de uso ok -&gt; linhas=2, e-mails enviados=[email p-1]
 * </pre>
 */
public class FronteiraDoCasoDeUso {
    public record PedidoFeito(String id) {}

    @Component static class Emails {
        final List<String> enviados = Collections.synchronizedList(new ArrayList<>());
        @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
        void on(PedidoFeito e) { enviados.add("email " + e.id()); }
    }

    @Service static class PlaceOrderUseCase {
        private final Orders orders; private final ApplicationEventPublisher pub;
        PlaceOrderUseCase(Orders o, ApplicationEventPublisher p) { orders = o; pub = p; }

        /** 1 caso de uso = 1 transação curta. */
        @Transactional public void execute(String id, boolean falhaNoSegundo) {
            orders.insert(id);
            if (falhaNoSegundo) throw new IllegalStateException("falha no 2º passo");
            orders.insert(id + "-itens");
            pub.publishEvent(new PedidoFeito(id));
        }

        /** Sem fronteira: cada insert comita sozinho (auto-commit). */
        public void semFronteira(String id) {
            orders.insert(id);
            throw new IllegalStateException("falha no 2º passo");
        }
    }

    public static List<String> run() {
        List<String> out = new ArrayList<>();
        try (var ctx = new AnnotationConfigApplicationContext(TxFixture.Cfg.class, Orders.class, Emails.class, PlaceOrderUseCase.class)) {
            var uc = ctx.getBean(PlaceOrderUseCase.class);
            var orders = ctx.getBean(Orders.class);
            var emails = ctx.getBean(Emails.class);

            orders.clear();
            try { uc.execute("p-0", true); } catch (IllegalStateException ignored) { }
            out.add("caso de uso falha no 2º insert -> linhas=" + orders.count() + " (tudo ou nada), e-mails enviados=" + emails.enviados);

            orders.clear();
            try { uc.semFronteira("p-0"); } catch (IllegalStateException ignored) { }
            out.add("sem fronteira (cada insert comita) -> linhas=" + orders.count() + " (parcial!)");

            orders.clear();
            uc.execute("p-1", false);
            out.add("caso de uso ok -> linhas=" + orders.count() + ", e-mails enviados=" + emails.enviados);
        }
        return out;
    }

    public static void main(String[] args) { run().forEach(System.out::println); }
}
