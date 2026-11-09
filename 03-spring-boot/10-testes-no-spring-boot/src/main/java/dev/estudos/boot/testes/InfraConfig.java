package dev.estudos.boot.testes;

import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Configuration;

/** SB10-08: não é varrida pelos filtros de slice (só tipos web/JPA), então os slices não exigem CacheManager. */
@Configuration
@EnableCaching
class InfraConfig {}
