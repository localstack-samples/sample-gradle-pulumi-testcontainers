package app;

import java.util.UUID;

public record Message(UUID uuid, String content) {}
