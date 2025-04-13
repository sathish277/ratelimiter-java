package org.example.ratelimiter;

public interface RateLimiter {
    boolean isAllowed(String clientId, String apiKey);
}