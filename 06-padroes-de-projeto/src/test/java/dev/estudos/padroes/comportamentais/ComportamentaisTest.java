package dev.estudos.padroes.comportamentais;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

/** Testes dos exemplos DP04-06 a DP04-15. */
class ComportamentaisTest {
    @Test void dp0406_chain() {
        assertThat(ChainOfResponsibilityExemplo.run()).contains(
            "total 50, 2 itens -> aprovado", "total 5000 -> revisão manual", "total 0, 0 itens -> rejeitado: vazio",
            "pipeline com next -> [log core(ABC) log]");
    }

    @Test void dp0407_command() throws Exception {
        assertThat(CommandExemplo.run()).contains("após 3 execuções -> [a, b, c]", "após 2 undo -> [a]",
            "Callable entregue ao executor roda em outra thread -> true");
    }

    @Test void dp0408_iterator() {
        assertThat(IteratorExemplo.run()).contains("soma de Range(1,5) com for-each -> 10",
            "remover no for-each -> ConcurrentModificationException", "remover pelo iterador -> [1, 3]");
    }

    @Test void dp0409_mediator() {
        assertThat(MediatorExemplo.run()).containsExactly("ana envia \"oi\" -> {ana=[], bob=[ana: oi], cy=[ana: oi]}");
    }

    @Test void dp0410_memento() {
        assertThat(MementoExemplo.run()).contains("antes do restore -> [x, y]", "após restore -> [x]",
            "snapshot sem cópia 'restaura' o estado alterado -> [x, y]");
    }

    @Test void dp0411_observer() {
        assertThat(ObserverExemplo.run()).contains("A recebeu [pedido-1], B recebeu [pedido-1, pedido-2]",
            "PropertyChangeSupport -> status NEW->PAID", "Spring: mesma thread do publicador -> true");
    }

    @Test void dp0412_state() {
        assertThat(StateExemplo.run()).contains("New -> pay -> ship = Shipped[]",
            "cancel em Shipped -> Shipped não aceita cancel", "enum: RED.next().next() = YELLOW");
    }

    @Test void dp0413_strategy() {
        assertThat(StrategyExemplo.run()).contains("vip -> 90.00, bf -> 70.00, none -> 100.00",
            "Comparator como estratégia -> [a, cc, bbb]", "beans Notifier -> [email, sms]", "notify(\"sms\", \"oi\") -> sms:oi");
    }

    @Test void dp0414_template_method() {
        assertThat(TemplateMethodExemplo.run()).contains("process lança, log do esqueleto -> [open, process, close]",
            "callback com sucesso -> ok [begin, commit]", "callback que falha -> [begin, rollback]");
    }

    @Test void dp0415_visitor() {
        assertThat(VisitorExemplo.run()).contains("show -> (1 + 2) * 4", "eval -> 12");
    }
}
