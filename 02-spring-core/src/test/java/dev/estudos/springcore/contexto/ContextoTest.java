package dev.estudos.springcore.contexto;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

/** Testes dos exemplos SC03-01, 03, 04, 05, 07, 09, 10, 11. */
class ContextoTest {
    @Test void sc0301_beanfactory_vs_context() {
        assertThat(BeanFactoryVsContext.run()).containsExactly(
            "BeanFactory: singleton existe antes do getBean? -> false", "Product criado",
            "BeanFactory: singleton existe depois do getBean? -> true", "ApplicationContext: antes do refresh",
            "Product criado", "ApplicationContext: depois do refresh");
    }
    @Test void sc0303_tres_formas() {
        assertThat(TresFormas.run()).containsExactly("estereótipo -> orderService (classe OrderService)",
            "@Bean -> clock (factoryMethod=clock, classe SystemClock)", "programático -> relogio2 (classe SystemClock)",
            "@Service, @Repository, @Controller, @Configuration são @Component -> true");
    }
    @Test void sc0304_bean_metodo() {
        assertThat(BeanMetodo.run()).containsExactly("nome do bean pelo método -> [cardGateway]",
            "apelidos de 'pix' -> [pixGateway]", "card recebeu o Clock por parâmetro -> true", "pix: initMethod chamado -> true",
            "prototype: duas instâncias diferentes -> true", "após close(): pix.close() inferido chamado -> true");
    }
    @Test void sc0305_full_vs_lite() {
        var out = FullVsLite.run();
        assertThat(out.get(0)).startsWith("full: holder.dep == dep: true").contains("FullVsLite$Full$$SpringCGLIB$$0");
        assertThat(out.get(1)).isEqualTo("lite: holder.dep == dep: false | holder2 (parâmetro): true | classe em runtime: Lite");
    }
    @Test void sc0307_beandefinition() {
        var out = AnatomiaBeanDefinition.run();
        assertThat(out.get(0)).contains("scope=singleton lazy=false primary=false dependsOn=[orderRepo]");
        assertThat(out.get(1)).contains("lazy=true");
        assertThat(out.get(2)).isEqualTo("relatorio: factoryMethod=relatorio scope=prototype primary=true initMethod=abrir");
        assertThat(out.get(3)).endsWith("-> true");
    }
    @Test void sc0309_primeiro_contexto() {
        assertThat(PrimeiroContexto.run()).allMatch(l -> l.endsWith("-> true"));
    }
    @Test void sc0310_registro_programatico() {
        assertThat(RegistroProgramatico.run()).containsExactly("antes do refresh, 0 singletons de aplicação criados",
            "OrderService (lazy) criado após refresh? -> false", "Clock (supplier, não lazy) criado após refresh? -> true",
            "após getBean, OrderService criado? -> true");
    }
    @Test void sc0311_lite_parametros() {
        assertThat(ConfigLiteParametros.run()).allMatch(l -> l.endsWith("-> true")).hasSize(3);
    }
}
