package dev.estudos.java.concorrencia;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

/** Testes dos exemplos J12-01 a J12-15 (só invariantes: tempos e corridas variam). */
class ConcorrenciaTest {
    @Test void j1201_threads() throws Exception {
        assertThat(Threads.run()).contains("estado antes de start -> NEW", "estado depois de join -> TERMINATED",
            "virtual? -> true", "platform é virtual? -> false", "estado dormindo -> TIMED_WAITING", "flag restaurado no catch -> true");
    }
    @Test void j1202_executores() throws Exception {
        assertThat(Executores.run()).contains("get -> 42", "falha embrulhada -> ExecutionException(boom)",
            "tarefas concluídas após close -> 6", "terceira tarefa -> RejectedExecutionException");
    }
    @Test void j1203_completable_future() throws Exception {
        assertThat(CompletableFutures.run()).contains("thenCombine -> 42", "thenApply (map) -> 40", "thenCompose (flatMap) -> 21",
            "exceptionally -> -1", "handle -> CompletionException/IllegalStateException",
            "join -> CompletionException(IllegalStateException)", "get -> ExecutionException(IllegalStateException)",
            "allOf concluído -> true", "thread da etapa com executor próprio -> io-pool",
            "sem executor usa o commonPool -> true", "orTimeout -> TimeoutException", "completeOnTimeout -> padrão");
    }
    @Test void j1204_exclusao() throws Exception {
        assertThat(Exclusao.run()).contains("synchronized -> 400000", "reentrante (incDuas) -> 2", "AtomicInteger -> 400000",
            "LongAdder -> 400000", "ReentrantLock -> 400000", "tryLock(50ms) com lock ocupado -> false",
            "tryLock com lock livre -> true", "compareAndSet(5->9) -> true, compareAndSet(5->1) -> false, valor 9");
    }
    @Test void j1206_jmm() throws Exception {
        assertThat(JavaMemoryModel.run()).contains("volatile: laço terminou -> true", "AtomicInteger -> 400000",
            "visível após join -> 42", "holder idiom devolve a mesma instância -> true");
    }
    @Test void j1209_virtual_threads() throws Exception {
        assertThat(VirtualThreads.run()).contains("soma de 1000 tarefas bloqueantes -> 499500",
            "máximo simultâneo com Semaphore(5) -> true", "tarefa roda em thread -> virtual", "é daemon por definição -> true");
    }
    @Test void j1210_scoped_values() throws Exception {
        assertThat(ScopedValues.run()).containsExactly("antes do escopo isBound -> false", "dentro do escopo -> ana",
            "servico vê ana", "escopo aninhado -> bia", "de volta ao externo -> ana", "depois do escopo isBound -> false",
            "call devolve valor -> CAIO", "get fora do escopo -> NoSuchElementException", "orElse fora do escopo -> anônimo");
    }
    @Test void j1211_structured_concurrency() throws Exception {
        assertThat(StructuredConcurrency.run()).contains("sem --enable-preview -> recusado (API em preview)",
            "resultado -> ana7", "falha de uma subtarefa cancela as outras -> falhou");
    }
    @Test void j1212_contador() throws Exception {
        var r = ContadorSemSync.run();
        assertThat(r).contains("AtomicInteger -> 400000", "synchronized -> 400000");
        assertThat(r.get(0)).startsWith("sem sincronização");
    }
    @Test void j1213_produtor_consumidor() throws Exception {
        assertThat(ProdutorConsumidor.run()).containsExactly("processando order-0", "processando order-1", "processando order-2",
            "processando order-3", "processando order-4", "fila vazia no final -> true");
    }
    @Test void j1214_chamadas_em_paralelo() throws Exception {
        assertThat(ChamadasEmParalelo.run()).contains("resultado -> OrderView[order=Order[id=1], customer=Customer[id=9, nome=ana]]",
            "em paralelo: ~200ms e não ~400ms -> true", "estouro de prazo -> CompletionException(TimeoutException)",
            "com virtual threads -> OrderView[order=Order[id=1], customer=Customer[id=9, nome=ana]]");
    }
    @Test void j1215_cache() throws Exception {
        assertThat(CacheSimples.run()).contains("hits de /orders -> 80000", "cálculos executados para a mesma chave -> 1",
            "merge (contagem) -> {a=3, b=2, c=1}", "valor null -> NullPointerException", "chave null -> NullPointerException");
    }
}
