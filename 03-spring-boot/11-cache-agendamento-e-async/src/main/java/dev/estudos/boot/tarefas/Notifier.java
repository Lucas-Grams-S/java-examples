package dev.estudos.boot.tarefas;

import java.util.concurrent.CompletableFuture;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

/** SB11-05b/SB11-13: @Async e self-invocation; erros em retorno void e em CompletableFuture. */
@Service
public class Notifier {
    @Async public CompletableFuture<String> where() {
        return CompletableFuture.completedFuture(Thread.currentThread().getName());
    }
    public String viaThis() { return where().join(); }   // this.where(): síncrono, na thread do chamador

    @Async public void falhaVoid() { throw new IllegalStateException("boom"); }
    @Async public CompletableFuture<Void> falhaFuture() { throw new IllegalStateException("boom"); }
}
