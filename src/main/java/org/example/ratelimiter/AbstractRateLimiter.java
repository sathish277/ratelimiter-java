package org.example.ratelimiter;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import org.example.ratelimiter.RateLimiter;

public abstract class AbstractRateLimiter implements RateLimiter {
    protected final JedisPool redisPool;
    protected final String prefix = "rate_limiter:";

    public AbstractRateLimiter(JedisPool redisPool) {
        this.redisPool = redisPool;
    }

    @Override
    public boolean isAllowed(String clientId, String key) {
        try (Jedis jedis = redisPool.getResource()) {
            return processRequest(jedis, key);
        } catch (Exception e) {
            e.printStackTrace();
            return false; // Fail closed on error
        }
    }

    public abstract boolean processRequest(Jedis jedis, String key);

}
