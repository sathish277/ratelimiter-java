package org.example.ratelimiter.algorithms;

import org.example.ratelimiter.AbstractRateLimiter;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class TokenBucketRateLimiter extends AbstractRateLimiter {
    private final int capacity;
    private final double refillRate; // Tokens per second

    public TokenBucketRateLimiter(JedisPool redisPool, int capacity, double refillRate) {
        super(redisPool);
        this.capacity = capacity;
        this.refillRate = refillRate;
    }

    @Override
    public boolean processRequest(Jedis jedis, String key) {
        Map<String, String> bucket = jedis.hgetAll(key);

        long now = Instant.now().toEpochMilli();
        double currentTokens;
        long lastRefill;

        if (bucket.isEmpty()) { // Initialize if the bucket doesn't exist
            currentTokens = capacity;
            lastRefill = now;
        } else {
            currentTokens = Double.parseDouble(bucket.get("tokens"));
            lastRefill = Long.parseLong(bucket.get("last_refill"));
        }

        // Refill tokens based on elapsed time
        long elapsedMillis = now - lastRefill;
        double newTokens = refillRate * TimeUnit.MILLISECONDS.toSeconds(elapsedMillis);
        currentTokens = Math.min(capacity, currentTokens + newTokens);

        if (currentTokens >= 1) {
            // Allow request and update tokens
            jedis.hset(key, "tokens", String.valueOf(currentTokens - 1));
            jedis.hset(key, "last_refill", String.valueOf(now));
            return true;
        } else {
            // Reject request (rate limited)
            jedis.hset(key, "last_refill", String.valueOf(now)); // Still update last refill time
            return false;
        }
    }
}
