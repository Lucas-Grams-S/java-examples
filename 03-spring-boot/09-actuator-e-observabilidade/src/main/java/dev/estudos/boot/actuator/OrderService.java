package dev.estudos.boot.actuator;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

/** SB09-12: métrica de negócio com tag de baixa cardinalidade; SB09-13: cache "prices". */
@Service
public class OrderService {
    private final Counter placed;

    OrderService(MeterRegistry registry) {
        this.placed = Counter.builder("orders.placed").tag("channel", "web").register(registry);
    }

    public void place() { placed.increment(); }

    @Cacheable("prices")
    public int price(String sku) { return sku.length() * 10; }
}
