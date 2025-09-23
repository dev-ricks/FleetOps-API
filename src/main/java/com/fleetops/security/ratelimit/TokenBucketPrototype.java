package com.fleetops.security.ratelimit;

public class TokenBucketPrototype {

    private int tokenCapacity;
    private int availableTokenCount;
    private long lastRefillTime;

    public TokenBucketPrototype(int tokenCapacity, long lastRefillTime) {
        this.tokenCapacity = tokenCapacity;
        this.availableTokenCount = tokenCapacity;
        this.lastRefillTime = lastRefillTime;
    }

    public static Object builder() {
        return null;
    }

    public boolean isAllowed() {
        return availableTokenCount >= 0;
    }

    public void decrement() {
        availableTokenCount--;
    }

    public int capacity() {
        return tokenCapacity;
    }
}
