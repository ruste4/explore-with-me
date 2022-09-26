package ru.practicum.explorewithme.request;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.AutoConfigureTestEntityManager;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import ru.practicum.explorewithme.client.StatisticClient;
import ru.practicum.explorewithme.client.dto.ViewStats;
import ru.practicum.explorewithme.event.Event;
import ru.practicum.explorewithme.event.EventService;
import ru.practicum.explorewithme.event.EventState;
import ru.practicum.explorewithme.event.Location;
import ru.practicum.explorewithme.event.category.Category;
import ru.practicum.explorewithme.event.dto.EventShortDto;
import ru.practicum.explorewithme.request.dto.RequestFullDto;
import ru.practicum.explorewithme.request.exception.*;
import ru.practicum.explorewithme.user.User;

import javax.transaction.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@AutoConfigureTestDatabase
@AutoConfigureTestEntityManager
@Transactional
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class RequestServiceTest {

    @MockBean
    private StatisticClient statisticClient;

    @Autowired
    private final RequestService requestService;

    @Autowired
    private final EventService eventService;

    @Autowired
    private final TestEntityManager testEntityManager;

    private static AtomicLong userIdHolder;

    @BeforeAll
    public static void init() {
        userIdHolder = new AtomicLong();
    }

    @BeforeEach
    public void beforeEachEventServiceTest() {
        testEntityManager.clear();
    }


    private Map<Long, Event> generateAndPersistEvent(int quantity) {
        Map<Long, Event> res = new HashMap<>();

        for (int i = 0; i < quantity; i++) {
            long nanoTime = System.nanoTime();

            Event event = Event.builder()
                    .annotation("annotation " + nanoTime)
                    .category(generateAndPersistEventCategory(String.valueOf(nanoTime)))
                    .description("description " + nanoTime)
                    .eventDate(LocalDateTime.now().plusDays(1))
                    .paid(false)
                    .initiator(generateAndPersistUser())
                    .participantLimit(10l)
                    .location(new Location(14.2, 18.4))
                    .requestModeration(true)
                    .title("title " + nanoTime)
                    .createdOn(LocalDateTime.now().plusDays(1))
                    .state(EventState.PENDING)
                    .build();

            testEntityManager.persistAndFlush(event);
            res.put(event.getId(), event);
        }

        return res;
    };

    private Category generateAndPersistEventCategory(String name) {
        Category category = new Category(null, name);
        return testEntityManager.persistAndFlush(category);
    }

    private User generateAndPersistUser() {
        User result = new User();
        result.setName("UserName");
        result.setEmail(String.format("%s%s@mail.ru", result.getName(), userIdHolder.getAndIncrement()));
        result.setActivated(true);
        return testEntityManager.persistAndFlush(result);
    }

    @Test
    public void getEventRequestsFromCurrentUserSuccess() {
        Map<Long, Event> eventMap = generateAndPersistEvent(3);
        User requester = generateAndPersistUser();
        List<Request> requests = new ArrayList<>();

        for (Event event : eventMap.values()) {
            Request request = Request.builder()
                    .event(event)
                    .requester(requester)
                    .status(RequestStatus.PENDING)
                    .created(LocalDateTime.now())
                    .build();

            requests.add(
                    testEntityManager.persist(request)
            );
        }

        testEntityManager.flush();

        List<RequestFullDto> foundEvents = requestService.getEventRequestsFromCurrentUser(requester.getId());

        assertAll(
                () -> assertEquals(foundEvents.size(), requests.size()),
                () -> assertEquals(foundEvents.get(0).getRequester(), requester.getId()),
                () -> assertEquals(foundEvents.get(1).getRequester(), requester.getId()),
                () -> assertEquals(foundEvents.get(2).getRequester(), requester.getId())
        );
    }

    @Test
    public void addEventRequestSuccess() {
        Map<Long, Event> eventMap = generateAndPersistEvent(1);
        Event event = eventMap.values().stream().findFirst().get();
        event.setState(EventState.PUBLISHED);
        User requester = generateAndPersistUser();

        RequestFullDto createdRequest = requestService.addEventRequest(requester.getId(), event.getId());
        Request foundRequest = testEntityManager.find(Request.class, createdRequest.getId());

        assertAll(
                () -> assertEquals(foundRequest.getRequester().getId(), requester.getId()),
                () -> assertEquals(foundRequest.getEvent().getId(), event.getId())
        );

    }
    @Test
    public void addEventRequestFailParticipantLimitExceeded() {
        Map<Long, Event> eventMap = generateAndPersistEvent(1);
        Event event = eventMap.values().stream().findFirst().get();
        event.setState(EventState.PUBLISHED);
        event.setParticipantLimit(1l);
        User firstRequester = generateAndPersistUser();
        User secondRequester = generateAndPersistUser();

        requestService.addEventRequest(firstRequester.getId(), event.getId());

        assertThrows(ParticipantLimitExceededException.class,
                () -> requestService.addEventRequest(secondRequester.getId(), event.getId())
        );
    }

    @Test
    public void addEventRequestFailRequestUnpublishedEvent() {
        Map<Long, Event> eventMap = generateAndPersistEvent(1);
        Event event = eventMap.values().stream().findFirst().get();
        User requester = generateAndPersistUser();

        assertThrows(RequestUnpublishedEventException.class,
                () -> requestService.addEventRequest(requester.getId(), event.getId())
        );
    }

    @Test
    public void addEventRequestFailUserIsInitiatorEvent() {
        Map<Long, Event> eventMap = generateAndPersistEvent(1);
        Event event = eventMap.values().stream().findFirst().get();
        event.setState(EventState.PUBLISHED);

        assertThrows(RequesterIsInitiatorEventException.class,
                () -> requestService.addEventRequest(event.getInitiator().getId(), event.getId())
        );
    }

    @Test
    public void addEventRequestFailRequestAlreadyExist() {
        Map<Long, Event> eventMap = generateAndPersistEvent(1);
        Event event = eventMap.values().stream().findFirst().get();
        event.setState(EventState.PUBLISHED);
        User requester = generateAndPersistUser();
        requestService.addEventRequest(requester.getId(), event.getId());

        assertThrows(RequestAlreadyExistException.class,
                () -> requestService.addEventRequest(requester.getId(), event.getId())
        );
    }

    @Test
    public void addEventRequestSuccessRequestStatePublishedWithRequestModerationOf() {
        Map<Long, Event> eventMap = generateAndPersistEvent(1);
        Event event = eventMap.values().stream().findFirst().get();
        event.setState(EventState.PUBLISHED);
        event.setRequestModeration(false);
        User requester = generateAndPersistUser();
        RequestFullDto addedRequest = requestService.addEventRequest(requester.getId(), event.getId());

        Request foundRequest = testEntityManager.find(Request.class, addedRequest.getId());

        assertEquals(foundRequest.getStatus(), RequestStatus.CONFIRMED);
    }

    @Test
    public void addEventRequestSuccessRequestStatePendingWithRequestModerationOn() {
        Map<Long, Event> eventMap = generateAndPersistEvent(1);
        Event event = eventMap.values().stream().findFirst().get();
        event.setState(EventState.PUBLISHED);
        event.setRequestModeration(true);
        User requester = generateAndPersistUser();
        RequestFullDto addedRequest = requestService.addEventRequest(requester.getId(), event.getId());

        Request foundRequest = testEntityManager.find(Request.class, addedRequest.getId());

        assertEquals(foundRequest.getStatus(), RequestStatus.PENDING);
    }

    @Test
    public void cancelEventRequestCurrentUserSuccess() {
        Map<Long, Event> eventMap = generateAndPersistEvent(1);
        Event event = eventMap.values().stream().findFirst().get();
        event.setState(EventState.PUBLISHED);
        User requester = generateAndPersistUser();

        Request request = Request.builder()
                .event(event)
                .requester(requester)
                .status(RequestStatus.PENDING)
                .created(LocalDateTime.now())
                .build();

        testEntityManager.persistAndFlush(request);

        RequestFullDto canceledReq = requestService.cancelEventRequestCurrentUser(requester.getId(), request.getId());

        assertEquals(canceledReq.getStatus(), RequestStatus.CANCELED);
    }

    @Test
    public void cancelEventRequestCurrentUserFailUserIsNotRequester() {
        Map<Long, Event> eventMap = generateAndPersistEvent(1);
        Event event = eventMap.values().stream().findFirst().get();
        event.setState(EventState.PUBLISHED);
        User requester = generateAndPersistUser();
        User otherUser = generateAndPersistUser();

        Request request = Request.builder()
                .event(event)
                .requester(requester)
                .status(RequestStatus.PENDING)
                .created(LocalDateTime.now())
                .build();

        testEntityManager.persistAndFlush(request);

        assertThrows(UserNotRequesterForEventRequestException.class,
                () -> requestService.cancelEventRequestCurrentUser(otherUser.getId(), request.getId())
        );
    }

    @Test
    public void testConfirmedRequestCountWithGetEventsByInitiatorId() {
        Map<Long, Event> eventMap = generateAndPersistEvent(1);
        Event event = eventMap.values().stream().findFirst().get();
        event.setState(EventState.PUBLISHED);
        event.setParticipantLimit(1l);
        User firstRequester = generateAndPersistUser();
        User secondRequester = generateAndPersistUser();

        Mockito
                .when(statisticClient.getStats(Mockito.any(), Mockito.any(), Mockito.anySet(), Mockito.anyBoolean()))
                .thenReturn(List.of(new ViewStats("test", "events/test", 3)));

        Request firstRequest = Request.builder()
                .event(event)
                .requester(firstRequester)
                .status(RequestStatus.CONFIRMED)
                .created(LocalDateTime.now())
                .build();

        Request secondRequest = Request.builder()
                .event(event)
                .requester(secondRequester)
                .status(RequestStatus.CONFIRMED)
                .created(LocalDateTime.now())
                .build();

        testEntityManager.persist(firstRequest);
        testEntityManager.persistAndFlush(secondRequest);

        List<EventShortDto> found = eventService.getEventsByInitiatorId(event.getInitiator().getId(), 0, 10);

        assertEquals(found.get(0).getConfirmedRequests(), 2);
    }
}