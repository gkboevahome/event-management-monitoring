package event.management.dashboard.controller;

import event.management.dashboard.dto.ServiceDTO;
import event.management.dashboard.dto.ServiceMapper;
import event.management.dashboard.persistence.model.ServiceEntity;
import event.management.dashboard.persistence.service.ServiceEntityService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RequiredArgsConstructor
@RestController
@CrossOrigin(origins = "http://localhost:4000")
@RequestMapping("/api/services")
public class ServiceController {

    private final ServiceEntityService serviceEntityService;
    private final ServiceMapper mapper;

    @PostMapping
    public ServiceDTO createService(@RequestBody ServiceDTO serviceDTO) {
        ServiceEntity entity = serviceEntityService.save(mapper.toEntity(serviceDTO));
        return mapper.toDTO(entity);
    }

    @GetMapping
    public List<ServiceDTO> getAllServices() {
        List<ServiceEntity> services = serviceEntityService.findAll();
        return services.stream().map(mapper::toDTO).toList();
    }

    @GetMapping("/{name}")
    public ResponseEntity<List<ServiceDTO>> getServiceByName(@PathVariable String name) {
        List<ServiceDTO> services = serviceEntityService.findByName(name)
                .stream()
                .map(mapper::toDTO)
                .toList();
        return ResponseEntity.ok(services);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ServiceDTO> updateService(@PathVariable UUID id, @RequestBody ServiceDTO updated) {
        return serviceEntityService.findById(id).map(service -> {
            service.setName(updated.name());
            service.setEnvironment(updated.environment());
            service.setOwner(updated.owner());
            ServiceEntity entity = serviceEntityService.save(service);
            return ResponseEntity.ok(mapper.toDTO(entity));
        }).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteService(@PathVariable UUID id) {
        serviceEntityService.deleteById(id);
        return ResponseEntity.noContent().build();
    }

}
