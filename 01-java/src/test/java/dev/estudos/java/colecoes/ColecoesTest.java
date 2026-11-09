package dev.estudos.java.colecoes;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

/** Testes dos exemplos J07-05 a J07-15. */
class ColecoesTest {
    @Test void j0705_sequenced_collections() {
        assertThat(SequencedCollections.run()).contains("getFirst/getLast -> 1/3", "reversed() é visão: após list.add(4) -> [4, 3, 2, 1]",
                "putFirst(z, 9) -> {z=9, x=1, y=2}", "List.of(...).addFirst -> UnsupportedOperationException");
    }

    @Test void j0708_fail_fast() {
        assertThat(FailFast.run()).contains("remover 2 de [1,2,3,4] no for-each -> ConcurrentModificationException",
                "remover o PENÚLTIMO (3) de [1,2,3,4] -> terminou sem erro -> [1, 2, 4]", "removeIf (correto) -> [1, 3]");
    }

    @Test void j0711_agrupando_pedidos() {
        assertThat(GroupingOrders.run()).contains("merge como contador -> {P1=3, P2=1}",
                "unmodifiableList é visão -> [a, b, c]; List.copyOf é cópia -> [a, b]", "List.of com null -> NullPointerException",
                "Arrays.asList escreve no array -> 9");
    }

    @Test void j0712_treemap() {
        assertThat(TreeMapNavigation.run()).contains("floorKey(6) -> 5", "ceilingKey(6) -> 9", "headMap(5) -> {1=a}",
                "tailMap(5, true) -> {5=e, 9=i}", "TreeSet com comparing(sku): 2 produtos de mesmo SKU -> tamanho 1");
    }

    @Test void j0713_fila_pilha_prioridade() {
        assertThat(QueuesAndStacks.run()).contains("pilha: pop -> 2 (LIFO)", "prioridade (reverseOrder): poll -> 9",
                "ArrayDeque não aceita null -> NullPointerException");
    }

    @Test void j0714_cache_lru() {
        assertThat(LruCache.run()).contains("chaves após get(a) e put(c) -> [a, c]", "b foi despejada -> true");
    }

    @Test void j0715_chave_mutavel() {
        assertThat(MutableKey.run()).contains("depois de key.add(2): get(key) -> null", "containsKey(List.of(1)) -> false",
                "a entrada continua lá: size -> 1");
    }
}
