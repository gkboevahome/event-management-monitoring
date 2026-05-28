package event.management.dashboard.dto;

import event.management.dashboard.enums.EventType;

import java.time.Instant;
import java.util.UUID;

public record EventDTO(UUID id, ServiceDTO service, EventType type, String message, Long version, Instant createdAt) {
}
