package event.management.dashboard.persistence.service;

import event.management.dashboard.persistence.model.ServiceEntity;
import event.management.dashboard.persistence.repository.ServicesRepository;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ServiceEntityService {
    private final ServicesRepository servicesRepository;

    @Transactional
    public ServiceEntity save(ServiceEntity event){
        return servicesRepository.save(event);
    }

    public List<ServiceEntity> findByName(@NonNull String name){
        return servicesRepository.findAllByName(name);
    }
    public Optional<ServiceEntity> findByNameAndEnvironmentAndOwner(@NonNull String name, @NonNull String environment, @NonNull String owner) {
        return servicesRepository.findByNameAndEnvironmentAndOwner(name,environment,owner);
    }

    public List<ServiceEntity> findAll() {
        return servicesRepository.findAll();
    }

    public Optional<ServiceEntity> findById(UUID id) {
        return servicesRepository.findById(id);
    }

    public void deleteById(UUID id) {
        servicesRepository.deleteById(id);
    }
}
