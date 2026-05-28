package event.management.dashboard.persistence.service;

import event.management.dashboard.enums.EventType;
import event.management.dashboard.persistence.model.EventEntity;
import event.management.dashboard.persistence.repository.EventsRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ActiveProfiles("test")
@ExtendWith(MockitoExtension.class)
public class EventEntityServiceTest {

    @Mock
    private EventsRepository eventsRepository;
    @InjectMocks
    private EventEntityService eventEntityService;

    @Test
    void givenNoServiceName_whenFindByTypeAndORServiceName_thenReturnResult() {
        // given
        EventType error = EventType.ERROR;
        when(eventsRepository.findByType(eq(error))).thenReturn(List.of(mock(EventEntity.class), mock(EventEntity.class)));

        // when
        List<EventEntity> entities = eventEntityService.findByTypeAndORServiceName(error, "fake");

        // then
        assertFalse(entities.isEmpty());
        assertEquals(2, entities.size());
        verify(eventsRepository).findByType(eq(error));
        verifyNoMoreInteractions(eventsRepository);
    }

    @Test
    void givenNullServiceName_whenFindByTypeAndORServiceName_thenReturnResult() {
        // given
        EventType error = EventType.ERROR;
        when(eventsRepository.findByType(eq(error))).thenReturn(List.of(mock(EventEntity.class), mock(EventEntity.class)));

        // when
        List<EventEntity> entities = eventEntityService.findByTypeAndORServiceName(error, null);

        // then
        assertFalse(entities.isEmpty());
        assertEquals(2, entities.size());
        verify(eventsRepository).findByType(eq(error));
        verifyNoMoreInteractions(eventsRepository);
    }

    @Test
    void givenServiceNameAndEventType_whenFindByTypeAndORServiceName_thenReturnResult() {
        // given
        EventType error = EventType.ERROR;
        var name = "fake";
        when(eventsRepository.findByTypeAndServiceName(eq(error), eq(name))).thenReturn(List.of(mock(EventEntity.class), mock(EventEntity.class)));

        // when
        List<EventEntity> entities = eventEntityService.findByTypeAndORServiceName(error, name);

        // then
        assertFalse(entities.isEmpty());
        assertEquals(2, entities.size());
        verify(eventsRepository).findByTypeAndServiceName(eq(error), eq(name));
        verifyNoMoreInteractions(eventsRepository);
    }

}
