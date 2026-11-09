package dev.estudos.boot.actuator;

import java.util.concurrent.atomic.AtomicInteger;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/** SB09-13: tarefa listada em /actuator/scheduledtasks. */
@Component
public class Jobs {
    final AtomicInteger ticks = new AtomicInteger();

    @Scheduled(fixedRate = 200) void tick() { ticks.incrementAndGet(); }
}
