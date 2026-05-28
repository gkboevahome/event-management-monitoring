package event.management.dashboard.persistence.repository;

import event.management.dashboard.enums.EventType;
import event.management.dashboard.persistence.model.EventEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface EventsRepository extends JpaRepository<EventEntity, UUID> {
    List<EventEntity> findByTypeAndServiceName(EventType type, String serviceName);
    List<EventEntity> findByType(EventType type);
}
