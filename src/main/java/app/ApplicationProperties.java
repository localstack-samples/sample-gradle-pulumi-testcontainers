package app;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties
public record ApplicationProperties(String queue, String bucket) {}
