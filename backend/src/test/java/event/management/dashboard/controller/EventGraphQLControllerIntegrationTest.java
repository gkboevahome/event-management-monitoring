package event.management.dashboard.controller;

import event.management.dashboard.config.TestContainersConfig;
import event.management.dashboard.enums.EventType;
import event.management.dashboard.persistence.model.EventEntity;
import event.management.dashboard.persistence.model.ServiceEntity;
import event.management.dashboard.persistence.repository.EventsRepository;
import event.management.dashboard.persistence.repository.ServicesRepository;
import event.management.dashboard.persistence.service.EventEntityService;
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
import java.util.Map;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ImportTestcontainers(TestContainersConfig.class)
@Transactional
class EventGraphQLControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private EventsRepository eventsRepository;

    @Autowired
    private ServicesRepository servicesRepository;

    private String authHeader;

    @BeforeEach
    void setUp() {
        String secretString = "mySecretKeyMustBeAtLeast32CharactersLongForHS256!";
        SecretKey key = Keys.hmacShaKeyFor(secretString.getBytes(StandardCharsets.UTF_8));

        String token = Jwts.builder()
                .subject("dashboard_system")
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + 600000))
                .signWith(key, Jwts.SIG.HS256)
                .compact();

        this.authHeader = "Bearer " + token;

        // 2. Подготвяме тестови данни в Docker Postgres базата
        ServiceEntity service = new ServiceEntity();
        service.setName("Payment Service");
        service.setEnvironment("Production");
        service.setOwner("Finance Team");
        ServiceEntity savedService = servicesRepository.saveAndFlush(service);

        EventEntity event = new EventEntity();
        event.setType(EventType.ERROR);
        event.setService(savedService);
        event.setMessage("{\"text\": \"Something went wrong in payment processing\"}");
        eventsRepository.saveAndFlush(event);
    }

    @Test
    void getEvents_WithRealJwt_ShouldReturnGraphQLDataAndResolveBatchMapping() throws Exception {
        // 1. Дефинираме GraphQL заявката като чист текст
        // Важно: Изискваме 'service' обекта, за да задействаме вашия @BatchMapping метод!
        String graphQLQuery = """
                query {
                    getEvents(serviceName: "Payment Service", type: ERROR) {
                        id
                        type
                        service {
                            name
                            environment
                            owner
                        }
                    }
                }
                """;

        // 2. Спецификацията на GraphQL изисква заявката да бъде изпратена като JSON тяло с ключ "query"
        Map<String, String> requestBody = Map.of("query", graphQLQuery);

        // 3. Изпълняваме заявката като стандартен POST към "/graphql"
        mockMvc.perform(post("/graphql")
                        .header("Authorization", authHeader)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestBody)))
                .andExpect(status().isOk())
                // В GraphQL отговорът винаги се съдържа в обект "data"
                .andExpect(jsonPath("$.data.getEvents", hasSize(1)))
                .andExpect(jsonPath("$.data.getEvents[0].type").value("ERROR"))
                // Валидираме, че @BatchMapping е попълнил вложеното DTO правилно
                .andExpect(jsonPath("$.data.getEvents[0].service.name").value("Payment Service"))
                .andExpect(jsonPath("$.data.getEvents[0].service.environment").value("Production"));
    }
}
