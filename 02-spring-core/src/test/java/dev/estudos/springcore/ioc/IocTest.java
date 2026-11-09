package dev.estudos.springcore.ioc;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

/** Testes dos exemplos SC02-01, 03, 04, 05, 07, 08, 09, 10, 11 e 12. */
class IocTest {
    @Test void sc0201_tres_estilos() {
        assertThat(TresEstilos.run()).containsExactly("construtor -> repo injetado=true (campo final)",
            "setter opcional (nenhum Notifier no contexto) -> null", "campo -> audit injetado=true",
            "setter opcional (com Notifier no contexto) -> email");
    }
    @Test void sc0203_qualifier_primary() {
        assertThat(QualifierPrimary.run()).containsExactly("@Primary -> card | @Qualifier(\"boleto\") -> boleto",
            "nome padrão do bean escaneado -> cardGateway", "sem desempate -> NoUniqueBeanDefinitionException",
            "nome do parâmetro 'pix' desempata -> pix");
    }
    @Test void sc0204_colecoes() {
        assertThat(ColecoesStrategy.run()).containsExactly("pay(\"pix\") -> pix", "pay(\"boleto\") -> boleto",
            "chaves do Map -> [boleto, card, pix]", "List ordenada por @Order -> [pix, card, boleto]");
    }
    @Test void sc0205_e_0210_opcionais() {
        assertThat(Opcionais.run()).containsExactly("sem bean: Optional.isPresent -> false",
            "sem bean: provider.getIfAvailable -> null", "sem bean: @Nullable -> null", "sem bean: provider.stream().count -> 0",
            "com bean: Optional.isPresent -> true", "com bean: provider.getIfAvailable -> auditado", "com bean: @Nullable -> auditado");
    }
    @Test void sc0207_genericos() {
        assertThat(GenericosQualificador.run()).containsExactly("Repository<Order,Long> injetado -> OrderRepo",
            "Repository<Customer,Long> injetado -> CustomerRepo");
    }
    @Test void sc0208_0211_circular() {
        assertThat(Circular.run()).containsExactly("construtor A(B)/B(A) -> BeanCurrentlyInCreationException",
            "campo X/Y (padrão) -> funciona, y.x == x: true",
            "campo X/Y com setAllowCircularReferences(false) -> BeanCurrentlyInCreationException");
    }
    @Test void sc0209_resolucao() {
        assertThat(Resolucao.run()).containsExactly("card boleto", "[boleto, card, pix]",
            "[boleto, resolucao.CardGateway, resolucao.PixGateway]");
    }
    @Test void sc0212_testar_sem_spring() throws Exception {
        assertThat(TestarSemSpring.run()).containsExactly("construtor: repo.findAll().size() -> 1",
            "campo sem contêiner -> NullPointerException", "campo com reflection (setField manual) -> 1");
    }
}
