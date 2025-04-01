package io.will.poc.kafka.model;

public record Farewell(
        String message,
        Integer remainingMinutes
) {
}
