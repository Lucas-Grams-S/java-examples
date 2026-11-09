package dev.estudos.boot.testes;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Configuration;

/** SB10-08: reproduz a mensagem de {@code @EnableCaching} sem {@code CacheManager} (o que um slice enfrentaria). */
class EnableNoMainTest {
    @Configuration @EnableCaching static class Ruim { @org.springframework.context.annotation.Bean Alvo alvo() { return new Alvo(); } }
    static class Alvo { @Cacheable("x") public int f() { return 1; } }

    @Test void sb1008_enableCachingSemCacheManager() {
        assertThatThrownBy(() -> new AnnotationConfigApplicationContext(Ruim.class).close())
            .hasStackTraceContaining("no CacheResolver specified");
    }
}
