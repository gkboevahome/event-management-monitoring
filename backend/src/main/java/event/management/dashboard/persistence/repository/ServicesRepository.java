package event.management.dashboard.persistence.repository;

import event.management.dashboard.persistence.model.ServiceEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ServicesRepository extends JpaRepository<ServiceEntity, UUID> {

    Optional<ServiceEntity> findByNameAndEnvironmentAndOwner(String name, String environment, String owner);

    List<ServiceEntity> findAllByName(String name);
}
