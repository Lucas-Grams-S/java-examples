package dev.estudos.java.oo;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

/** Testes dos exemplos J04-01 a J04-14. */
class OoTest {
    @Test void j0401_classe_e_construtor() {
        assertThat(ClassAndConstructor.run()).contains("this(...) -> sem nome", "sku nulo -> NullPointerException",
                "ordem de inicialização -> campo a > bloco de instância > campo b > corpo do construtor");
    }

    @Test void j0403_heranca() {
        assertThat(Inheritance.run()).contains("describe -> Payment 42 via Pix", "superclasse de Payment -> java.lang.Object");
    }

    @Test void j0404_polimorfismo() {
        assertThat(Polymorphism.run()).contains("dog animal Animal", "sobrecarga com tipo declarado Object -> over(Object)",
                "sobrecarga com tipo declarado String -> over(String)");
    }

    @Test void j0405_abstracao() {
        assertThat(AbstractAndInterfaces.run()).contains("diamante resolvido com A.super/B.super -> AB",
                "interface mais específica vence -> Specific", "default usa método private -> 3.33");
    }

    @Test void j0406_classes_internas() {
        assertThat(NestedClasses.run()).contains("Inner.y (usa x do Outer) -> 2", "campos de Inner -> [this$0 (sintético), y]",
                "anônima vê local=5 e x=1", "lambda é classe anônima? -> false");
    }

    @Test void j0407_enums_e_records() {
        assertThat(EnumsRecordsTypes.run()).contains("record toString -> Point[x=1, y=2]", "record equals -> true",
                "sealed + switch exaustivo -> círculo r=2.0");
    }

    @Test void j0408_composicao_vs_heranca() {
        assertThat(CompositionVsInheritance.run()).contains("herança HashSet: addAll de 3 -> adds=6 (esperado 3)",
                "composição: addAll de 3 -> adds=3, size=3");
    }

    @Test void j0410_ordem_de_inicializacao() {
        assertThat(InitOrder.run()).containsExactly("init key=null", "depois do construtor key=k");
    }

    @Test void j0412_dominio_de_pedidos() {
        assertThat(DiscountDomain.run()).contains("10% -> 108.00", "pagar duas vezes -> só pedidos CREATED podem ser pagos");
    }

    @Test void j0413_template_method() {
        assertThat(TemplateMethod.run()).contains("== Relatório ==", "vendas: 3", "== fim ==", "-- estoque fim --");
    }

    @Test void j0414_default_evolui_contrato() {
        assertThat(DefaultMethodEvolution.run()).contains("legado charge -> cobrado 10",
                "legado refund -> UnsupportedOperationException: refund não suportado", "moderno refund -> estornado 10");
    }
}
