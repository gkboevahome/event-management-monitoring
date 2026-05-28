package event.management.dashboard.persistence.service;

import event.management.dashboard.enums.EventType;
import event.management.dashboard.persistence.model.EventEntity;
import event.management.dashboard.persistence.repository.EventsRepository;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class EventEntityService {
    private final EventsRepository eventsRepository;

    public List<EventEntity> findByTypeAndORServiceName(@NonNull EventType type,  String serviceName) {
        if (serviceName == null || serviceName.isBlank()) {
            return eventsRepository.findByType(type);
        }
        return eventsRepository.findByTypeAndServiceName(type, serviceName);
    }

    @Transactional
    public EventEntity save(EventEntity entity) {
        return eventsRepository.save(entity);
    }
}
