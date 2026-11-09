package dev.estudos.springcore.configuracao;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

/** Testes dos exemplos SC05-01 a 07 e 12 a 16. */
class ConfiguracaoTest {
    @Test void sc0501_tres_geracoes() {
        assertThat(TresGeracoes.run()).containsExactly("XML -> SystemClock", "Java Config (@Bean) -> SystemClock",
            "Anotação (@Component) -> Relogio", "as três geraram BeanDefinition -> true");
    }
    @Test void sc0502_component_scan() {
        assertThat(ComponentScanDemo.run()).containsExactly(
            "scan simples (3 estereótipos em scan.app) -> [legacyService, orderRepository, orderService]",
            "com excludeFilters REGEX .*Legacy.* -> [orderRepository, orderService]",
            "com useDefaultFilters=false + include @Repository -> [orderRepository]",
            "classe em pacote irmão não é vista -> NoSuchBeanDefinitionException",
            "scan do pacote pai enxerga os dois -> true");
    }
    @Test void sc0503_import() {
        assertThat(ImportDemo.run()).containsExactly("@Import de @Configuration -> beans [infra, security]",
            "ImportSelector escolheu -> selecionado", "@ImportResource (beans.xml) -> clock do XML presente: true",
            "BeanRegistrar (7.0) registrou -> registrado (via registrar)");
    }
    @Test void sc0504_profiles() {
        assertThat(ProfilesDemo.run()).containsExactly("perfil dev -> console", "perfil prod -> mail",
            "nenhum perfil (default) -> mail", "perfil prod, 'MailNotifier' registrado? true | 'ConsoleNotifier' registrado? false",
            "@Profile(\"prod & !eu\") com prod -> ativo; com prod+eu -> inativo");
    }
    @Test void sc0505_environment() {
        var out = EnvironmentPropertySources.run();
        assertThat(out.get(0)).isEqualTo("fontes (em ordem) -> [systemProperties, systemEnvironment, class path resource [app.properties]]");
        assertThat(out).contains("orders.max-items como Integer -> 5", "orders.currency -> BRL",
            "-Dorders.currency=USD vence o arquivo -> USD");
    }
    @Test void sc0506_value() {
        assertThat(PropertySourceValue.run()).containsExactly("banner -> BRL/5/padrao/50",
            "sem PropertySourcesPlaceholderConfigurer, ${nao.existe} fica literal -> ${nao.existe}",
            "com PropertySourcesPlaceholderConfigurer, placeholder ausente -> PlaceholderResolutionException (Could not resolve placeholder 'nao.existe' ...)");
    }
    @Test void sc0507_spel() {
        assertThat(SpelDemo.run()).containsExactly("'pedido-' + 42 -> pedido-42", "T(java.lang.Math).max(2, 5) * 10 -> 50",
            "3 > 2 ? 'a' : 'b' -> a", "nome ?: 'x' (nome nulo) -> x", "obj?.prop (obj nulo) -> null",
            "itens.?[preco > 10] -> [caro1, caro2]", "itens.![nome] -> [barato, caro1, caro2]",
            "@Value(\"#{config.moeda}\") -> BRL", "SimpleEvaluationContext bloqueia T() -> SpelEvaluationException");
    }
    @Test void sc0512_camadas() {
        assertThat(ConfigPorCamadas.run()).containsExactly("gateway.url -> http://pagamentos.local/api",
            "gateway.clock é o Clock da InfraConfig -> true", "PricingService (escaneado) -> maxItems=5 currency=BRL");
    }
    @Test void sc0513_profile() {
        assertThat(TrocarPorProfile.run()).containsExactly("prod -> HttpPaymentGateway(http://pagamentos.local/api)",
            "dev -> InMemoryPaymentGateway", "sem perfil -> InMemoryPaymentGateway");
    }
    @Test void sc0514_environment_tipado() {
        assertThat(EnvironmentTipado.run()).containsExactly("getRequiredProperty('orders.max-items', Integer) -> 5",
            "chave ausente -> IllegalStateException: Required key 'orders.max-items' not found");
    }
    @Test void sc0515_valor_padrao_spel() {
        assertThat(ValorPadraoSpel.run()).containsExactly("currency (sem a propriedade, padrão) -> BRL",
            "tags (SpEL split do padrão) -> [a, b]", "com -Dorders.tags=x,y,z -> [x, y, z]");
    }
    @Test void sc0516_precedencia() {
        assertThat(PrecedenciaPratica.run()).containsExactly("só o arquivo -> BRL", "com -Dorders.currency=USD -> USD",
            "fonte addFirst (mapa) vence o -D -> EUR", "fonte addLast perde para o -D -> USD");
    }
}
