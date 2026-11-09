package com.example.orders;

import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

/** PP08: cache e agendamento. Em classe separada: {@code @EnableCaching} na classe principal exigiria CacheManager em {@code @WebMvcTest}. */
@Configuration
@EnableCaching
@EnableScheduling
class InfraConfig {}
