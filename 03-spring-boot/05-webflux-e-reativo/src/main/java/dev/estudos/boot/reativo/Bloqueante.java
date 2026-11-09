package dev.estudos.boot.reativo;

import java.util.concurrent.Callable;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

/** SB05-14: isolar código bloqueante em boundedElastic. */
public final class Bloqueante {
    private Bloqueante() {}

    /** Devolve o nome da thread onde o código legado executou. */
    public static Mono<String> executar(Callable<String> legado) {
        return Mono.fromCallable(legado).subscribeOn(Schedulers.boundedElastic());
    }

    public static String legado() throws InterruptedException {
        Thread.sleep(30);
        return Thread.currentThread().getName();
    }
}
