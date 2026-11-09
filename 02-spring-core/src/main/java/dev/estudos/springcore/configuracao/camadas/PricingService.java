package dev.estudos.springcore.configuracao.camadas;

import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

/** Lê a propriedade de forma tipada, via {@link Environment} (erro claro se faltar). */
@Service
public class PricingService {
    private final int maxItems;
    private final String currency;
    public PricingService(Environment env) {
        this.maxItems = env.getRequiredProperty("orders.max-items", Integer.class);
        this.currency = env.getProperty("orders.currency", "BRL");
    }
    public int maxItems() { return maxItems; }
    public String currency() { return currency; }
}
