package event.management.dashboard.config;

import event.management.dashboard.enums.EventType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.annotation.Validated;

import java.util.List;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString
@Validated
@Configuration
@ConfigurationProperties(prefix = "event-generator-scheduler")
public class EventGeneratorSchedulerConfig {
    @NotBlank
    private String fixedRateString;
    @NotEmpty
    private List<EventEntityMock> eventMocks;

    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Getter
    @Setter
    @ToString
    public static class EventEntityMock {
        @NotBlank
        private EventType type;
        @NotBlank
        private String message;
        @NotNull
        private ServiceEntityMock service;
    }

    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Getter
    @Setter
    @ToString
    public static class ServiceEntityMock {
        @NotBlank
        private String name;
        @NotBlank
        private String environment;
        @NotBlank
        private String owner;
    }
}
