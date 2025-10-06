package com.fleetops.security.ratelimit;

public class RateLimitResult {
    public RateLimitResult(boolean b, int i, int i1, int i2, long l1) {

    }

    public boolean isAllowed() {
        return false;
    }

    public long getRetryAfterSeconds() {
        return 0;
    }

    public long getRemainingTokens() {
        return 0;
    }

    public long getCapacity() {
        return 0;
    }

    public long getResetTimeEpochSecond() {
        return 0;
    }
}
