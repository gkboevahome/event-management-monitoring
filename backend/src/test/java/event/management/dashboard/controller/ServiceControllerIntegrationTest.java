package event.management.dashboard.controller;

import event.management.dashboard.config.TestContainersConfig;
import event.management.dashboard.dto.ServiceDTO;
import event.management.dashboard.persistence.model.ServiceEntity;
import event.management.dashboard.persistence.repository.ServicesRepository;
import event.management.dashboard.persistence.service.ServiceEntityService;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.context.ImportTestcontainers;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.ObjectMapper;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.UUID;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ImportTestcontainers(TestContainersConfig.class)
@Transactional
class ServiceControllerIntegrationTest {

    @Autowired
    private ServicesRepository servicesRepository;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ServiceEntityService serviceEntityService;

    @Autowired
    private ObjectMapper objectMapper;

    private ServiceEntity existingService;

    @BeforeEach
    void setUp() {
        ServiceEntity service = new ServiceEntity();
        service.setName("Auth-Service");
        service.setEnvironment("Production");
        service.setOwner("Security Team");
        existingService = serviceEntityService.save(service);
    }

    @Test
    void createService_ShouldSaveInDockerAndReturnDTO() throws Exception {
        ServiceDTO newService = new ServiceDTO(null, "Payment-Service", "Staging", "Billing Team", null);

        mockMvc.perform(post("/api/services")
                        .header("Authorization", generateTestJwt("dashboard_system"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newService)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Payment-Service"))
                .andExpect(jsonPath("$.environment").value("Staging"));
    }

    @Test
    void createService_WithDuplicateName_ShouldThrowExceptionAndFail() throws Exception {
        ServiceDTO duplicateService = new ServiceDTO(null, "Auth-Service", "Production", "Security Team", null);

        mockMvc.perform(post("/api/services")
                        .header("Authorization", generateTestJwt("dashboard_system"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(duplicateService)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getAllServices_ShouldReturnListFromDocker() throws Exception {
        servicesRepository.save(ServiceEntity.builder()
                .name("other")
                .owner("Owner")
                .environment("Environment")
                .build());
        mockMvc.perform(get("/api/services")
                        .header("Authorization", generateTestJwt("dashboard_system")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].name").value("Auth-Service"));
    }


    @Test
    void getServiceByName_ShouldReturnMatchingServices() throws Exception {
        mockMvc.perform(get("/api/services/{name}", "Auth-Service")
                        .header("Authorization", generateTestJwt("dashboard_system")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].name").value("Auth-Service"));
    }

    @Test
    void updateService_ShouldModifyExistingRecord() throws Exception {
        ServiceDTO updatedDTO = new ServiceDTO(UUID.randomUUID(), "Auth-Service-V2", "Production", "New Owner", 0L);

        mockMvc.perform(put("/api/services/{id}", existingService.getId())
                        .header("Authorization", generateTestJwt("dashboard_system"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updatedDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Auth-Service-V2"))
                .andExpect(jsonPath("$.owner").value("New Owner"));
    }

    @Test
    void updateService_ShouldReturnNotFound_WhenIdDoesNotExist() throws Exception {
        ServiceDTO updatedDTO = new ServiceDTO(UUID.randomUUID(), "Non-Existent", "QA", "Nobody", 0L);
        UUID randomId = UUID.randomUUID();

        mockMvc.perform(put("/api/services/{id}", randomId)
                        .header("Authorization", generateTestJwt("dashboard_system"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updatedDTO)))
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteService_ShouldRemoveRecordFromDocker() throws Exception {
        mockMvc.perform(delete("/api/services/{id}", existingService.getId())
                        .header("Authorization", generateTestJwt("dashboard_system")))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/services")
                        .header("Authorization", generateTestJwt("dashboard_system")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    private String generateTestJwt(String username) {
        String secretString = "mySecretKeyMustBeAtLeast32CharactersLongForHS256!";
        SecretKey key = Keys.hmacShaKeyFor(secretString.getBytes(StandardCharsets.UTF_8));

        String token = Jwts.builder()
                .subject(username)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + 600000))
                .signWith(key, Jwts.SIG.HS256)
                .compact();
        return "Bearer " + token;

    }

}
