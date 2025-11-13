package com.fleetops.security.ratelimit;

import java.time.Clock;
import java.time.Duration;

public class RateLimitService {

    public RateLimitService() {

    }

    public RateLimitService(Clock fixedClock, int defaultCapacity, Duration defaultRefillPeriod) {

    }

    public RateLimitResult tryConsume(String key) {
        return null;
    }
}
