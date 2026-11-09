package dev.estudos.padroes.enterprise;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

/** Testes dos exemplos DP05-10 a DP05-15. */
class EnterpriseTest {
    @Test void dp0510_entidade() {
        assertThat(EntidadeComRegras.run()).contains("total = 20.00", "pay vazio -> pedido vazio",
            "status após pay = PAID", "findById(99) vazio = true");
    }

    @Test void dp0511_dto() {
        assertThat(DtoEMapper.run()).contains("request tem id/status? false");
        assertThat(DtoEMapper.run().getFirst()).contains("total=25.50", "customer=Ana");
    }

    @Test void dp0512_service() {
        assertThat(ServiceLayerFina.run()).contains("pago: PAID", "erro: cliente 9", "erro: pedido vazio");
    }

    @Test void dp0513_specification() {
        assertThat(SpecificationExemplo.run()).contains("(pago e >20) ou (vip e não pago) -> [1, 3]",
            "Predicate 'pago' -> [1, 2]");
    }

    @Test void dp0514_unitOfWork() {
        assertThat(UnitOfWorkManual.run()).contains("commit: inserts=[10] updates=[11]",
            "no repositório: [10, 11]", "commit: inserts=[] updates=[]");
    }

    @Test void dp0515_springDataReal() {
        assertThat(ComSpringRepositorySpecUow.run()).containsExactly("repo é proxy? true",
            "identity map: mesma instância = true", "antes do flush: updates=0", "depois do flush: updates=1",
            "pago e >60: [Cid]", "pago ou >60: 2");
    }
}
