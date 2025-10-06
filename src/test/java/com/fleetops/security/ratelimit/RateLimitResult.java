package com.fleetops.security.ratelimit;

/**
 * Result of a rate limit check.
 * 
 * This is a value object that encapsulates the result of attempting to consume
 * a token from the rate limiter. It provides all necessary information for:
 * - Determining if the request should be allowed
 * - Providing rate limit headers to the client
 * - Logging and monitoring
 */
public class RateLimitResult {
    
    private final boolean allowed;
    private final long remainingTokens;
    private final long capacity;
    private final long retryAfterSeconds;
    private final long resetTimeEpochSecond;

    public RateLimitResult(
            boolean allowed,
            long remainingTokens,
            long capacity,
            long retryAfterSeconds,
            long resetTimeEpochSecond) {
        this.allowed = allowed;
        this.remainingTokens = remainingTokens;
        this.capacity = capacity;
        this.retryAfterSeconds = retryAfterSeconds;
        this.resetTimeEpochSecond = resetTimeEpochSecond;
    }

    /**
     * @return true if the request should be allowed, false if rate limited
     */
    public boolean isAllowed() {
        return allowed;
    }

    /**
     * @return number of tokens remaining in the bucket
     */
    public long getRemainingTokens() {
        return remainingTokens;
    }

    /**
     * @return maximum capacity of the bucket
     */
    public long getCapacity() {
        return capacity;
    }

    /**
     * @return number of seconds to wait before retrying (0 if allowed)
     */
    public long getRetryAfterSeconds() {
        return retryAfterSeconds;
    }

    /**
     * @return epoch second when the rate limit will reset
     */
    public long getResetTimeEpochSecond() {
        return resetTimeEpochSecond;
    }

    @Override
    public String toString() {
        return "RateLimitResult{" +
                "allowed=" + allowed +
                ", remainingTokens=" + remainingTokens +
                ", capacity=" + capacity +
                ", retryAfterSeconds=" + retryAfterSeconds +
                ", resetTimeEpochSecond=" + resetTimeEpochSecond +
                '}';
    }
}