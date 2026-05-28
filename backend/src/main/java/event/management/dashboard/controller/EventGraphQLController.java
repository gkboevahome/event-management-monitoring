package event.management.dashboard.controller;

import event.management.dashboard.dto.ServiceDTO;
import event.management.dashboard.dto.ServiceMapper;
import event.management.dashboard.enums.EventType;
import event.management.dashboard.persistence.model.EventEntity;
import event.management.dashboard.persistence.service.EventEntityService;
import lombok.RequiredArgsConstructor;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.BatchMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:4000")
@Controller
public class EventGraphQLController {
    private final EventEntityService eventEntityService;
    private final ServiceMapper serviceMapper;

    @QueryMapping
    public List<EventEntity> getEvents(@Argument String serviceName, @Argument EventType type) {
        return eventEntityService.findByTypeAndORServiceName(type, serviceName);
    }

    @BatchMapping(typeName = "Event", field = "service")
    public Map<EventEntity, ServiceDTO> getServices(List<EventEntity> events) {
        return events.stream()
                .collect(Collectors.toMap(
                        Function.identity(),
                        event -> serviceMapper.toDTO(event.getService())
                ));
    }
}
