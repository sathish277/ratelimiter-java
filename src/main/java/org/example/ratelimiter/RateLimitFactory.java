package org.example.ratelimiter;

import java.util.Map;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import org.example.ratelimiter.algorithms.TokenBucketRateLimiter;

public class RateLimitFactory {
    private final JedisPool redisPool;

    public RateLimitFactory(String redisHost, int redisPort) {
        JedisPoolConfig poolConfig = new JedisPoolConfig();
        this.redisPool = new JedisPool(poolConfig, redisHost, redisPort);
    }

    public RateLimiter create(String algorithm, Map<String, Integer> clientLimits, long timeWindow, int capacity,
            double refillRate) {
        if ("token_bucket".equalsIgnoreCase(algorithm)) {
            return new TokenBucketRateLimiter(redisPool, capacity, refillRate);
        } else {
            throw new IllegalArgumentException("Invalid rate limiter algorithm: " + algorithm);
        }
    }

    public void close() {
        redisPool.close();
    }
}
