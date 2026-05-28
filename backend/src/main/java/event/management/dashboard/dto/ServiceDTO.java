package event.management.dashboard.dto;

import java.util.UUID;

public record ServiceDTO(UUID id, String name, String environment, String owner, Long version) {
}
