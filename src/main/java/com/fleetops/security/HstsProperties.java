package com.fleetops.security;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Setter
@Getter
@Configuration
@ConfigurationProperties(prefix = "security.hsts")
public class HstsProperties {
    /** Enable/disable HSTS header. */
    private boolean enabled = false;
    /** Max age in seconds. */
    private long maxAge = 0L;
    /** Include subdomains directive. */
    private boolean includeSubdomains = false;
    /** Preload directive (not initially used). */
    private boolean preload = false;

}

