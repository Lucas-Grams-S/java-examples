package dev.estudos.boot.actuator;

import org.springframework.boot.health.contributor.Health;
import org.springframework.boot.health.contributor.HealthIndicator;
import org.springframework.stereotype.Component;

/** SB09-11: HealthIndicator próprio (pacote do Boot 4: {@code org.springframework.boot.health.contributor}). */
@Component("stock")
public class StockHealth implements HealthIndicator {
    volatile boolean ok = true;

    public void setOk(boolean ok) { this.ok = ok; }

    @Override public Health health() {
        return ok ? Health.up().withDetail("items", 42).build()
                  : Health.down().withDetail("reason", "empty").build();
    }
}
