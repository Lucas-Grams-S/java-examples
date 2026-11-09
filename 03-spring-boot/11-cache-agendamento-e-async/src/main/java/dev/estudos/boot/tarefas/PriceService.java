package dev.estudos.boot.tarefas;

import java.util.concurrent.atomic.AtomicInteger;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

/** SB11-05a/SB11-11: cache e self-invocation. */
@Service
public class PriceService {
    private final AtomicInteger calls = new AtomicInteger();

    @Cacheable(cacheNames = "prices", key = "#sku", unless = "#result == null")
    public String price(String sku) { calls.incrementAndGet(); return "preço-" + sku; }

    /** this.price(): não passa pelo proxy, o cache é ignorado. */
    public String viaThis(String sku) { return price(sku); }

    @CacheEvict(cacheNames = "prices", allEntries = true)
    public void limpa() {}

    public int calls() { return calls.get(); }
}
