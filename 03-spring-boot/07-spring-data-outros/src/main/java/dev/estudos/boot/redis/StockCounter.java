package dev.estudos.boot.redis;

import java.time.Duration;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

@Service
public class StockCounter {
    private final StringRedisTemplate redis;
    StockCounter(StringRedisTemplate redis) { this.redis = redis; }

    public long reserve(String sku) { return redis.opsForValue().increment("stock:reserved:" + sku); }
    public void cacheOrder(String id, String json) { redis.opsForValue().set("order:" + id, json, Duration.ofMinutes(10)); }
    public String order(String id) { return redis.opsForValue().get("order:" + id); }
    public Long ttlSeconds(String id) { return redis.getExpire("order:" + id); }
}
