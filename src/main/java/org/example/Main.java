package org.example;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.example.ratelimiter.AbstractRateLimiter;
import org.example.ratelimiter.RateLimitFactory;
import org.example.ratelimiter.algorithms.TokenBucketRateLimiter;
import org.example.ratelimiter.RateLimiter;
import redis.clients.jedis.JedisPoolConfig;

public class Main {
    public static void main(String[] args) {
        RateLimitFactory factory = new RateLimitFactory("localhost", 6379);

        // Example: Using Time Window
        Map<String, Integer> clientLimits = new HashMap<>();
        clientLimits.put("premium", 200);
        clientLimits.put("free", 50);

        RateLimiter tokenBucketLimiter = factory.create("token_bucket", null, 0, 100, 10); // 10 tokens/sec

        String clientType = "premium"; // Or "free", or get it from the request context
        String clientId = "client1";
        String apiKey = "apiKey123";
        String key = clientType + ":" + clientId + ":" + apiKey; // Construct the key based on client type

        for (int i = 0; i < 250; i++) { // Test exceeding limits
            if (tokenBucketLimiter.isAllowed(clientId, key)) {
                System.out.println(clientType + " Request allowed by Token Bucket");
            } else {
                System.out.println(clientType + " Request rejected (rate limited) by Token Bucket");
            }

            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        factory.close();
    }
}