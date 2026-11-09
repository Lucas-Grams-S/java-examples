package dev.estudos.springcore.transacoes;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

/** Testes de SC09-04 e SC09-11 a 14. */
class TransacoesTest {
    @Test void sc0911_matriz() {
        assertThat(MatrizTransacional.run()).containsExactly(
            "okCommit        -> nenhuma | linhas=1 | auditoria=0",
            "runtimeFail     -> IllegalStateException | linhas=0 | auditoria=0",
            "checkedFail     -> IOException | linhas=1 | auditoria=0",
            "checkedRollback -> IOException | linhas=0 | auditoria=0",
            "withAudit       -> IllegalStateException | linhas=0 | auditoria=1",
            "swallow         -> UnexpectedRollbackException | linhas=0 | auditoria=0",
            "setRollbackOnly -> nenhuma | linhas=0 | auditoria=0",
            "inner(self)     -> IllegalStateException | linhas=1 | auditoria=0",
            "noTx(MANDATORY) -> IllegalTransactionStateException | linhas=0 | auditoria=0");
    }
    @Test void sc0904_regras_de_rollback() {
        assertThat(RegrasRollback.run()).containsExactly("RuntimeException   -> IllegalStateException | linhas=0 | auditoria=0",
            "checked (padrão)   -> IOException | linhas=1 | auditoria=0",
            "checked + rollbackFor -> IOException | linhas=0 | auditoria=0");
    }
    @Test void sc0912_transaction_template() {
        assertThat(TransactionTemplateDemo.run()).containsExactly("setRollbackOnly no callback -> linhas=0",
            "callback normal -> linhas=1", "RuntimeException no callback -> linhas=0 (IllegalStateException)");
    }
    @Test void sc0913_fronteira() {
        assertThat(FronteiraDoCasoDeUso.run()).containsExactly(
            "caso de uso falha no 2º insert -> linhas=0 (tudo ou nada), e-mails enviados=[]",
            "sem fronteira (cada insert comita) -> linhas=1 (parcial!)",
            "caso de uso ok -> linhas=2, e-mails enviados=[email p-1]");
    }
    @Test void sc0914_somente_leitura() {
        assertThat(SomenteLeitura.run()).containsExactly("método de leitura: transação ativa=true, readOnly=true",
            "método de escrita: transação ativa=true, readOnly=false", "sem @Transactional: transação ativa=false");
    }
}
