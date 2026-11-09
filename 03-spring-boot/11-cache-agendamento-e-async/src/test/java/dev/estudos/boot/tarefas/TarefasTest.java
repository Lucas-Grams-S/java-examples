package dev.estudos.boot.tarefas;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.awaitility.Awaitility.await;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.CacheManager;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.scheduling.config.ScheduledTaskHolder;

@SpringBootTest(properties = {"spring.cache.type=caffeine", "spring.cache.cache-names=prices",
    "spring.cache.caffeine.spec=maximumSize=10,expireAfterWrite=5m"})
class TarefasTest {
    @Autowired PriceService prices;
    @Autowired Notifier notifier;
    @Autowired Jobs jobs;
    @Autowired PaymentGateway gateway;
    @Autowired CacheManager cacheManager;
    @Autowired ScheduledTaskHolder tasks;
    @Autowired AsyncErrors asyncErrors;

    @Test void sb1111_caffeine_e_sb1105_selfInvocation() {
        assertThat(cacheManager).isInstanceOf(CaffeineCacheManager.class);   // SB11-07 (spring.cache.type)
        prices.limpa();
        int antes = prices.calls();
        prices.price("A"); prices.price("A"); prices.viaThis("A");
        assertThat(prices.calls() - antes).isEqualTo(2);   // a viaThis() ignorou o cache
    }

    @Test void sb1105_async_e_selfInvocation() {
        String viaProxy = notifier.where().join();
        assertThat(viaProxy).startsWith("task-");                    // applicationTaskExecutor
        assertThat(notifier.viaThis()).isEqualTo(Thread.currentThread().getName());   // síncrono
    }

    @Test void sb1113_erros_em_async() {
        int antes = asyncErrors.count.get();
        notifier.falhaVoid();                                        // vai para o AsyncUncaughtExceptionHandler
        await().atMost(2, TimeUnit.SECONDS).until(() -> asyncErrors.count.get() > antes);
        assertThatThrownBy(() -> notifier.falhaFuture().join())      // future completa excepcionalmente
            .hasRootCauseInstanceOf(IllegalStateException.class);
    }

    @Test void sb1114_agendamento() {
        await().atMost(2, TimeUnit.SECONDS).until(() -> jobs.ticks.get() >= 3);
        assertThat(tasks.getScheduledTasks()).hasSizeGreaterThanOrEqualTo(3);
    }

    @Test void sb1104_1115_retryable() throws Exception {
        gateway.reset(2);
        assertThat(gateway.charge()).isEqualTo("ok na tentativa 3");   // 2 falhas + 1 sucesso (≤ 1+3 tentativas)
        gateway.reset(10);
        assertThatThrownBy(gateway::charge).isInstanceOf(Exception.class);
        assertThat(gateway.attempts()).isEqualTo(4);               // original + maxRetries(3)
    }

    @Test void sb1104_concurrencyLimit() throws Exception {
        ExecutorService ex = Executors.newFixedThreadPool(6);
        List<Future<?>> fs = new java.util.ArrayList<>();
        for (int i = 0; i < 6; i++) fs.add(ex.submit(() -> { gateway.heavy(); return null; }));
        for (Future<?> f : fs) f.get();
        ex.shutdown();
        assertThat(gateway.maxRunning()).isEqualTo(2);
    }
}
