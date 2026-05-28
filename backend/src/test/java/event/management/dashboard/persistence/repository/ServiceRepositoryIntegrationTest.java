package event.management.dashboard.persistence.repository;


import event.management.dashboard.config.TestContainersConfig;
import event.management.dashboard.persistence.model.ServiceEntity;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.context.ImportTestcontainers;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.AssertionsKt.assertNotNull;

@ActiveProfiles("test")
@SpringBootTest
@ImportTestcontainers(TestContainersConfig.class)
public class ServiceRepositoryIntegrationTest {
    @Autowired
    private ServicesRepository servicesRepository;

    @Test
    @Transactional
    void givenCorrectServiceDataWhenSaveIsCalLedEntityIsSavedCorrectly() {
        // given
        ServiceEntity service = ServiceEntity.builder()
                .name("Name")
                .owner("Owner")
                .environment("Environment")
                .build();

        // when
        ServiceEntity createdService = servicesRepository.save(service);

        // then
        assertNotNull(createdService);
        assertNotNull(createdService.getId());
        assertEquals(0, createdService.getVersion());
        assertEquals(service.getName(), createdService.getName());
        assertEquals(service.getOwner(), createdService.getOwner());
        assertEquals(service.getEnvironment(), createdService.getEnvironment());
    }

    @Test
    void givenServiceWithoutNameWhenSaveIsCalLedDataIntegrityViolationExceptionIsThrown() {
        // given
        ServiceEntity service = ServiceEntity.builder()
                .owner("Test Owner")
                .environment("Test Environment")
                .build();

        // when then
        assertThrows(DataIntegrityViolationException.class, () -> servicesRepository.save(service));
    }

    @Test
    void givenServiceWithoutOwnerThenSaveIsCalLedDataIntegrityViolationExceptionIsThrown() {
        // given
        ServiceEntity service = ServiceEntity.builder()
                .name("Test Service")
                .environment("Test Environment")
                .build();

        // when then
        assertThrows(DataIntegrityViolationException.class, () -> servicesRepository.save(service));
    }

    @Test
    void givenServiceWithoutEnvironmentThenSaveIsCalLedDataIntegrityViolationExceptionIsThrown() {
        // given
        ServiceEntity service = ServiceEntity.builder()
                .name("Test Service")
                .owner("Test Owner")
                .build();

        // when then
        assertThrows(DataIntegrityViolationException.class, () -> servicesRepository.save(service));
    }

    @Test
    @Transactional
    void givenExistingServiceWithUniqueConstraintViolationThenSaveIsCalLedDataIntegrityViolationExceptionIsThrown() {
        // given
        ServiceEntity service = ServiceEntity.builder()
                .name("Name")
                .owner("Owner")
                .environment("Environment")
                .build();

        servicesRepository.saveAndFlush(service);

        ServiceEntity serviceAnotherTry = ServiceEntity.builder()
                .name(service.getName())
                .owner(service.getOwner())
                .environment(service.getEnvironment())
                .build();

        // when then
        assertThrows(DataIntegrityViolationException.class, () -> servicesRepository.saveAndFlush(serviceAnotherTry));
    }

    @Test
    @Transactional
    void givenExistingServiceWithOutUniqueConstraintViolationThenSaveIsCalLedEntityIsSavedCorrectly() {
        // given
        ServiceEntity service = ServiceEntity.builder()
                .name("Name1")
                .owner("Owner")
                .environment("Environment")
                .build();

        servicesRepository.saveAndFlush(service);

        ServiceEntity serviceAnotherTry = ServiceEntity.builder()
                .name("Name2")
                .owner(service.getOwner())
                .environment(service.getEnvironment())
                .build();

        // when
        ServiceEntity createdService = servicesRepository.saveAndFlush(serviceAnotherTry);

        // then
        assertNotNull(createdService);
        assertNotNull(createdService.getId());
        assertEquals(0, createdService.getVersion());
        assertEquals(serviceAnotherTry.getName(), createdService.getName());
        assertEquals(serviceAnotherTry.getOwner(), createdService.getOwner());
        assertEquals(serviceAnotherTry.getEnvironment(), createdService.getEnvironment());
    }

    @Test
    @Transactional
    void givenServiceDataWhenFindByNameIsCalledThenResultIsNotEmpty() {
        // given
        String name1 = "Name1";
        ServiceEntity service1 = servicesRepository.save(ServiceEntity.builder()
                .name(name1)
                .owner("Owner")
                .environment("Environment")
                .build());

        ServiceEntity service2 = servicesRepository.save(ServiceEntity.builder()
                .name(name1)
                .owner("Owner")
                .environment("Environment1")
                .build());

        ServiceEntity service3 = servicesRepository.save(ServiceEntity.builder()
                .name("other")
                .owner("Owner")
                .environment("Environment")
                .build());

        // when
        List<ServiceEntity> services = servicesRepository.findAllByName(name1);

        // then
        assertFalse(services.isEmpty());
        assertEquals(2, services.size());

        List<UUID> uuids = services.stream().map(ServiceEntity::getId).toList();
        assertTrue(uuids.contains(service1.getId()));
        assertTrue(uuids.contains(service2.getId()));
    }

    @Test
    @Transactional
    void givenServiceDataWhenFindByNameIsCalledThenResultIsEmpty() {
        // given
        String name1 = "Name1";
        servicesRepository.save(ServiceEntity.builder()
                .name(name1)
                .owner("Owner")
                .environment("Environment")
                .build());

        servicesRepository.save(ServiceEntity.builder()
                .name(name1)
                .owner("Owner")
                .environment("Environment1")
                .build());

        servicesRepository.save(ServiceEntity.builder()
                .name("other")
                .owner("Owner")
                .environment("Environment")
                .build());

        // when
        List<ServiceEntity> services = servicesRepository.findAllByName("fake");

        // then
        assertTrue(services.isEmpty());
    }

    @Test
    @Transactional
    void givenServiceDataWhenFindByNameAndEnvironmentIsCalledThenResultIsNotEmpty() {
        // given
        String name1 = "Name1";
        String environment1 = "Environment1";
        String owner1 = "Owner1";
        ServiceEntity service1 = servicesRepository.save(ServiceEntity.builder()
                .name(name1)
                .owner(owner1)
                .environment(environment1)
                .build());

        servicesRepository.save(ServiceEntity.builder()
                .name("other")
                .owner("Owner")
                .environment("Environment")
                .build());

        // when
        Optional<ServiceEntity> service = servicesRepository.findByNameAndEnvironmentAndOwner(name1, environment1, owner1);

        // then
        assertFalse(service.isEmpty());
        assertEquals(service1.getId(), service.get().getId());
    }

    @Test
    @Transactional
    void givenServiceDataWhenFindByNameAndEnvironmentIsCalledThenResultIsEmpty() {
        // given
        String name1 = "Name1";
        String environment1 = "Environment1";
        servicesRepository.save(ServiceEntity.builder()
                .name(name1)
                .owner("Owner")
                .environment(environment1)
                .build());

        servicesRepository.save(ServiceEntity.builder()
                .name("other")
                .owner("Owner")
                .environment("Environment")
                .build());

        // when
        Optional<ServiceEntity> service = servicesRepository.findByNameAndEnvironmentAndOwner("fake", "fake", "fake");

        // then
        assertTrue(service.isEmpty());
    }
}
