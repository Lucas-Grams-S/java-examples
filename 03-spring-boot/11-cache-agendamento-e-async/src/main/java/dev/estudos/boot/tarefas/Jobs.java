package dev.estudos.boot.tarefas;

import java.util.concurrent.atomic.AtomicInteger;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/** SB11-14: {@code fixedRate} e cron de 6 campos (com segundos); o scheduler padrão tem 1 thread. */
@Component
public class Jobs {
    final AtomicInteger ticks = new AtomicInteger();
    final AtomicInteger lentas = new AtomicInteger();

    @Scheduled(fixedRate = 50) void tick() { ticks.incrementAndGet(); }
    @Scheduled(cron = "0 0 2 * * *", zone = "America/Sao_Paulo") void nightly() {}
    @Scheduled(fixedDelayString = "${jobs.cleanup.delay:PT5M}") void cleanup() {}
}
