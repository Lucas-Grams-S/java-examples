package dev.estudos.padroes.criacionais;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

/** Testes dos exemplos DP02-01 e DP02-06 a DP02-10. */
class CriacionaisTest {
    @Test void dp0201_esconder_new() {
        assertThat(EsconderNew.run()).contains("injetado, em teste -> FAKE -> ana")
            .anyMatch(l -> l.startsWith("acoplado") && l.contains("smtp.corp:587"));
    }

    @Test void dp0206_singleton() throws Exception {
        assertThat(SingletonIdiomas.run()).contains(
            "holder: antes de getInstance(), criado? -> false",
            "holder: após a 1ª chamada, log -> [OrderIdGenerator criado]",
            "holder: após a 2ª chamada, criações -> 1; mesma instância -> true",
            "reflection quebra o clássico (2ª instância) -> true",
            "reflection no enum -> IllegalArgumentException: Cannot reflectively create enum objects",
            "Spring, mesmo contexto -> true", "Spring, contextos diferentes -> false");
    }

    @Test void dp0207_factory_method() {
        assertThat(FactoryMethodExemplo.run()).contains(
            "EMAIL para ana: Pedido 7 confirmado", "SMS para ana: Pedido 7 confirmado",
            "Integer.valueOf(127) == Integer.valueOf(127) -> true",
            "getBean(\"notifier\") -> EmailNotifier", "getBean(\"&notifier\") -> NotifierFactoryBean",
            "produto em cache (singleton) -> true");
    }

    @Test void dp0208_abstract_factory() throws Exception {
        assertThat(AbstractFactoryExemplo.run()).contains(
            "fábrica PDF -> [PDF] Nota fiscal 1 | [PDF] Recibo 1",
            "fábrica HTML -> <h1>Nota fiscal 1</h1> | <p>Recibo 1</p>",
            "Spring, perfil pdf -> [PDF] Nota fiscal 1 | [PDF] Recibo 1",
            "Spring, perfil web -> <h1>Nota fiscal 1</h1> | <p>Recibo 1</p>");
    }

    @Test void dp0209_builder() {
        assertThat(BuilderExemplo.run()).contains(
            "Order[c-1, 2 itens, total=25.50, coupon=PROMO10, note=null]",
            "sem itens -> IllegalStateException: order needs at least one item",
            "JDK HttpRequest.newBuilder -> GET http://localhost/orders",
            "Spring UriComponentsBuilder -> http://localhost:8080/orders/42?expand=items");
    }

    @Test void dp0210_prototype() {
        assertThat(PrototypeExemplo.run()).contains(
            "clone raso: d1.skus -> [A, B] (alterado pelo clone), mesma lista -> true",
            "construtor de cópia: o1 -> ana[A, X], o2 -> ana[A, X, Y]",
            "escopo prototype: instâncias distintas -> true");
    }
}
