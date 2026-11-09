package com.example.orders.orders;

import java.time.Clock;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
class ClockConfig {
    @Bean @ConditionalOnMissingBean Clock clock() { return Clock.systemUTC(); }
}
