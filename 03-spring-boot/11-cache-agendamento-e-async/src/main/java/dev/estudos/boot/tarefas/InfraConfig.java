package dev.estudos.boot.tarefas;

import java.util.concurrent.atomic.AtomicInteger;
import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Configuration;
import org.springframework.resilience.annotation.EnableResilientMethods;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

/** SB11-05: habilita os recursos fora da classe principal (ver SB10-08). */
@Configuration
@EnableCaching @EnableScheduling @EnableResilientMethods
class InfraConfig {
}

/**
 * SB11-13: handler próprio de erros em {@code @Async void}. Fica numa classe à parte: um {@code AsyncConfigurer}
 * é instanciado muito cedo e não pode declarar métodos {@code @Bean} (o Spring falha com "Illegal factory instance").
 */
@Configuration
@EnableAsync
class AsyncConfig implements AsyncConfigurer {
    private final AsyncErrors counter;
    AsyncConfig(AsyncErrors errors) { this.counter = errors; }

    @Override public AsyncUncaughtExceptionHandler getAsyncUncaughtExceptionHandler() {
        return (ex, method, params) -> counter.count.incrementAndGet();
    }
}

/** Contador de falhas de {@code @Async void} (para o teste observar). */
@org.springframework.stereotype.Component
class AsyncErrors { final AtomicInteger count = new AtomicInteger(); }
