package event.management.dashboard.service;

import event.management.dashboard.config.EventGeneratorSchedulerConfig;
import event.management.dashboard.exception.CustomSchedulerException;
import event.management.dashboard.persistence.model.EventEntity;
import event.management.dashboard.persistence.model.ServiceEntity;
import event.management.dashboard.persistence.service.EventEntityService;
import event.management.dashboard.persistence.service.ServiceEntityService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import tools.jackson.core.json.JsonReadFeature;
import tools.jackson.databind.DeserializationFeature;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;

import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.function.Predicate;

@Slf4j
@RequiredArgsConstructor
@Component
public class EventGeneratorService {
    private final EventEntityService eventEntityService;
    private final ServiceEntityService serviceEntityService;
    private final EventGeneratorSchedulerConfig eventGeneratorSchedulerConfig;
    private final ObjectMapper objectMapper = JsonMapper.builder()
            .disable(DeserializationFeature.FAIL_ON_TRAILING_TOKENS)
            .enable(JsonReadFeature.ALLOW_UNESCAPED_CONTROL_CHARS)
            .enable(JsonReadFeature.ALLOW_BACKSLASH_ESCAPING_ANY_CHARACTER)
            .build();

    @Scheduled(fixedRateString = "#{eventGeneratorSchedulerConfig.getFixedRateString()}")
    public void generateEvents() {
        Random random = new Random();
        List<EventGeneratorSchedulerConfig.EventEntityMock> eventMocks = eventGeneratorSchedulerConfig.getEventMocks();
        EventGeneratorSchedulerConfig.EventEntityMock eventMock = eventMocks.get(random.nextInt(eventMocks.size()));

        try {
            generateMockEvent(random, eventMock);
        } catch (Exception exception) {
            log.error("Exception occurred while generating event {}}", eventMock, exception);
            throw new CustomSchedulerException("Exception occurred while generating event");
        }
    }

    private void generateMockEvent(Random random, EventGeneratorSchedulerConfig.EventEntityMock eventMock) throws Exception {
        // Find or create service
        EventGeneratorSchedulerConfig.ServiceEntityMock serviceMock = eventMock.getService();
        Optional<ServiceEntity> serviceEntity = serviceEntityService.findByNameAndEnvironmentAndOwner(serviceMock.getName(), serviceMock.getEnvironment(), serviceMock.getOwner());
        ServiceEntity service = serviceEntity.orElseGet(() -> serviceEntityService.save(ServiceEntity.builder()
                .name(serviceMock.getName())
                .owner(serviceMock.getOwner())
                .environment(serviceMock.getEnvironment())
                .build()));

        // Create the event
        Predicate<String> isAccountParam = message -> message.contains("accountId");
        Predicate<String> isUserParam = message -> message.contains("userId");
        String message = eventMock.getMessage();
        if (isAccountParam.test(message)) {
            message = String.format(message, random.nextInt());
        }
        if (isUserParam.test(message)) {
            message = String.format(message, random.nextInt(), random.nextInt());
        }
        JsonNode parsedJson = objectMapper.readTree(message);
        String payload = objectMapper.writeValueAsString(parsedJson);

        eventEntityService.save(EventEntity.builder()
                .service(service)
                .type(eventMock.getType())
                .message(payload).build());

    }
}
