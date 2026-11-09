package dev.estudos.boot.testes;

import java.util.concurrent.atomic.AtomicInteger;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

/** SB10-11: bean proxiado (CGLIB) por causa de {@code @Cacheable}; estado exposto por MÉTODO, nunca por campo. */
@Service
public class PriceService {
    final AtomicInteger calls = new AtomicInteger();   // campo: no proxy fica nulo
    @Cacheable("prices") public int price(String sku) { calls.incrementAndGet(); return sku.length() * 10; }
    public int calls() { return calls.get(); }
}
