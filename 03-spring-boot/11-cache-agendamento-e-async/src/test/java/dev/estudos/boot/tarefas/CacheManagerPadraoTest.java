package dev.estudos.boot.tarefas;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.CacheManager;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;

/** SB11-07: {@code spring.cache.type=simple} força o ConcurrentMapCacheManager (sem limite) mesmo com Caffeine no classpath. */
@SpringBootTest(properties = "spring.cache.type=simple")
class CacheManagerPadraoTest {
    @Autowired CacheManager cm;
    @Autowired PriceService prices;

    @Test void sb1107_simple_criaCachesSobDemanda() {
        assertThat(cm).isInstanceOf(ConcurrentMapCacheManager.class);
        prices.price("Z");
        assertThat(cm.getCacheNames()).contains("prices");
    }
}
