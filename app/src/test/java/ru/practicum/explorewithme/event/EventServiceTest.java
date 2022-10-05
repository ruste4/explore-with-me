package ru.practicum.explorewithme.event;

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
import ru.practicum.explorewithme.event.category.Category;
import ru.practicum.explorewithme.event.dto.EventCreateDto;
import ru.practicum.explorewithme.event.dto.EventFullDto;
import ru.practicum.explorewithme.event.dto.EventShortDto;
import ru.practicum.explorewithme.event.dto.EventUpdateDto;
import ru.practicum.explorewithme.event.exception.EventDateInvalidException;
import ru.practicum.explorewithme.event.exception.EventUpdatingIsProhibitedException;
import ru.practicum.explorewithme.user.exception.UserNotActivatedException;
import ru.practicum.explorewithme.user.User;

import javax.transaction.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@AutoConfigureTestDatabase
@AutoConfigureTestEntityManager
@Transactional
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class EventServiceTest {

    @MockBean
    private StatisticClient statisticClient;

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

    private final Supplier<EventCreateDto> createDtoSupplier = () -> {
        long nanoTime = System.nanoTime();
        EventCreateDto.Location location = new EventCreateDto.Location(12.1, 12.2);

        return EventCreateDto.builder()
                .annotation("annotation " + nanoTime)
                .category(generateEventCategoryAndGetId(String.valueOf(nanoTime)))
                .description("description " + nanoTime)
                .eventDate(LocalDateTime.now().plusDays(1))
                .location(location)
                .paid(false)
                .participantLimit(10L)
                .requestModeration(true)
                .title("title " + nanoTime)
                .build();
    };

    private final Supplier<Event> eventSupplier = () -> {
        long nanoTime = System.nanoTime();

        return Event.builder()
                .annotation("annotation " + nanoTime)
                .category(generateEventCategory(String.valueOf(nanoTime)))
                .description("description " + nanoTime)
                .eventDate(LocalDateTime.now().plusDays(1))
                .paid(false)
                .participantLimit(10L)
                .requestModeration(true)
                .title("title " + nanoTime)
                .createdOn(LocalDateTime.now().plusDays(1))
                .state(EventState.PENDING)
                .location(new Location(12.5, 13.4))
                .build();
    };

    private final Supplier<User> userSupplier = () -> {
        User result = new User();
        result.setName("UserName");
        result.setEmail(String.format("%s%s@mail.ru", result.getName(), userIdHolder.getAndIncrement()));
        result.setActivated(true);
        return result;
    };

    @Test
    public void addEventSuccess() {
        Mockito
                .when(statisticClient.getStats(Mockito.any(), Mockito.any(), Mockito.anySet(), Mockito.anyBoolean()))
                .thenReturn(List.of(new ViewStats("test", "events/test", 3)));

        User user = userSupplier.get();
        Long userId = testEntityManager.persistAndGetId(user, Long.class);
        EventCreateDto eventCreateDto = createDtoSupplier.get();

        EventFullDto createdEvent = eventService.addEvent(userId, eventCreateDto);

        Event foundEvent = testEntityManager.find(Event.class, createdEvent.getId());

        assertAll(
                () -> assertEquals(foundEvent.getAnnotation(), createdEvent.getAnnotation()),
                () -> assertEquals(foundEvent.getCategory().getName(), createdEvent.getCategory().getName()),
                () -> assertEquals(foundEvent.getDescription(), createdEvent.getDescription()),
                () -> assertEquals(foundEvent.getEventDate(), createdEvent.getEventDate()),
                () -> assertEquals(foundEvent.isPaid(), createdEvent.isPaid()),
                () -> assertEquals(foundEvent.getParticipantLimit(), createdEvent.getParticipantLimit()),
                () -> assertEquals(foundEvent.isRequestModeration(), createdEvent.isRequestModeration()),
                () -> assertEquals(foundEvent.getTitle(), createdEvent.getTitle())
        );
    }

    @Test
    public void addEventFailInvalidEventDate() {
        User user = userSupplier.get();
        Long userId = testEntityManager.persistAndGetId(user, Long.class);
        EventCreateDto eventCreateDto = createDtoSupplier.get();
        eventCreateDto.setEventDate(LocalDateTime.now().plusHours(1));

        assertThrows(EventDateInvalidException.class, () -> eventService.addEvent(userId, eventCreateDto));
    }

    @Test
    public void addEventFailInactiveUser() {
        User user = userSupplier.get();
        user.setActivated(false);
        Long userId = testEntityManager.persistAndGetId(user, Long.class);
        EventCreateDto eventCreateDto = createDtoSupplier.get();

        assertThrows(UserNotActivatedException.class, () -> eventService.addEvent(userId, eventCreateDto));
    }

    private long generateEventCategoryAndGetId(String name) {
        return generateEventCategory(name).getId();
    }

    private Category generateEventCategory(String name) {
        Category category = new Category(null, name);
        return testEntityManager.persist(category);
    }

    @Test
    public void updateEventByInitiatorIdSuccess() {
        Mockito
                .when(statisticClient.getStats(Mockito.any(), Mockito.any(), Mockito.anySet(), Mockito.anyBoolean()))
                .thenReturn(List.of(new ViewStats("test", "events/test", 3)));

        User user = userSupplier.get();
        Long userId = testEntityManager.persistAndGetId(user, Long.class);
        Event event = eventSupplier.get();
        event.setInitiator(user);
        testEntityManager.persistAndFlush(event);
        String updatedDescription = "updatedDescription";
        String updatedTitle = "updatedTitle";
        EventUpdateDto eventUpdateDto = new EventUpdateDto();
        eventUpdateDto.setEventId(event.getId());
        eventUpdateDto.setDescription(updatedDescription);
        eventUpdateDto.setTitle(updatedTitle);
        eventService.updateEventByInitiatorId(userId, eventUpdateDto);
        Event foundEvent = testEntityManager.find(Event.class, event.getId());

        assertAll(
                () -> assertEquals(foundEvent.getDescription(), eventUpdateDto.getDescription()),
                () -> assertEquals(foundEvent.getTitle(), eventUpdateDto.getTitle())
        );
    }

    @Test
    public void updateEventByInitiatorIdFailInvalidState() {
        User user = userSupplier.get();
        Long userId = testEntityManager.persistAndGetId(user, Long.class);
        Event event = eventSupplier.get();
        event.setInitiator(user);
        event.setState(EventState.PUBLISHED);
        testEntityManager.persistAndFlush(event);
        EventUpdateDto eventUpdateDto = new EventUpdateDto();
        eventUpdateDto.setEventId(event.getId());

        assertThrows(
                EventUpdatingIsProhibitedException.class,
                () -> eventService.updateEventByInitiatorId(userId, eventUpdateDto)
        );
    }

    @Test
    public void updateEventByInitiatorIdFailInvalidEventDate() {
        User user = userSupplier.get();
        Long userId = testEntityManager.persistAndGetId(user, Long.class);
        Event event = eventSupplier.get();
        event.setInitiator(user);
        testEntityManager.persistAndFlush(event);
        EventUpdateDto eventUpdateDto = new EventUpdateDto();
        eventUpdateDto.setEventId(event.getId());
        eventUpdateDto.setEventDate(LocalDateTime.now().plusHours(1));

        assertThrows(
                EventDateInvalidException.class,
                () -> eventService.updateEventByInitiatorId(userId, eventUpdateDto)
        );
    }

    @Test
    public void getEventsByInitiatorIdSuccess() {
        Mockito
                .when(statisticClient.getStats(Mockito.any(), Mockito.any(), Mockito.anySet(), Mockito.anyBoolean()))
                .thenReturn(List.of(new ViewStats("test", "events/test", 3)));

        User user1 = userSupplier.get();
        Long userId1 = testEntityManager.persistAndGetId(user1, Long.class);
        User user2 = userSupplier.get();
        Long userId2 = testEntityManager.persistAndGetId(user2, Long.class);
        EventCreateDto eventCreateDto1 = createDtoSupplier.get();
        EventCreateDto eventCreateDto2 = createDtoSupplier.get();
        EventCreateDto eventCreateDto3 = createDtoSupplier.get();

        EventFullDto createdEvent1 = eventService.addEvent(userId1, eventCreateDto1);
        EventFullDto createdEvent2 = eventService.addEvent(userId1, eventCreateDto2);
        EventFullDto createdEvent3 = eventService.addEvent(userId2, eventCreateDto3);

        List<EventShortDto> resultByUser1 = eventService.getEventsByInitiatorId(userId1, 0, 10);
        List<EventShortDto> resultByUser2 = eventService.getEventsByInitiatorId(userId2, 0, 10);

        assertAll(
                () -> assertEquals(resultByUser1.size(), 2),
                () -> assertEquals(resultByUser2.size(), 1),
                () -> assertEquals(resultByUser1.get(0).getAnnotation(), createdEvent1.getAnnotation()),
                () -> assertEquals(resultByUser1.get(1).getAnnotation(), createdEvent2.getAnnotation()),
                () -> assertEquals(resultByUser2.get(0).getAnnotation(), createdEvent3.getAnnotation())
        );
    }

    @Test
    public void getEventCurrentUserByIdSuccess() {
        Mockito
                .when(statisticClient.getStats(Mockito.any(), Mockito.any(), Mockito.anySet(), Mockito.anyBoolean()))
                .thenReturn(List.of(new ViewStats("test", "events/test", 3)));

        User user = userSupplier.get();
        Long userId = testEntityManager.persistAndGetId(user, Long.class);
        Event event = eventSupplier.get();
        event.setInitiator(user);
        Long eventId = testEntityManager.persistAndGetId(event, Long.class);

        EventFullDto foundEvent = eventService.getEventCurrentUserById(userId, eventId);

        assertEquals(foundEvent.getAnnotation(), event.getAnnotation());
    }

    @Test
    public void cancelEventAddedCurrentUserByIdSuccess() {
        Mockito
                .when(statisticClient.getStats(Mockito.any(), Mockito.any(), Mockito.anySet(), Mockito.anyBoolean()))
                .thenReturn(List.of(new ViewStats("test", "events/test", 3)));

        User user = userSupplier.get();
        Long userId = testEntityManager.persistAndGetId(user, Long.class);
        Event event = eventSupplier.get();
        event.setInitiator(user);
        Long eventId = testEntityManager.persistAndGetId(event, Long.class);

        eventService.cancelEventAddedCurrentUserById(userId, eventId);

        assertEquals(testEntityManager.find(Event.class, eventId).getState(), EventState.CANCELED);
    }

    @Test
    public void searchEventsSuccess() {
        Mockito
                .when(statisticClient.getStats(Mockito.any(), Mockito.any(), Mockito.anySet(), Mockito.anyBoolean()))
                .thenReturn(List.of(new ViewStats("test", "events/test", 3)));

        // Добавить первое событие первого пользователя
        User user1 = userSupplier.get();
        Long userId1 = testEntityManager.persistAndGetId(user1, Long.class);
        Event event1 = eventSupplier.get();
        event1.setInitiator(user1);
        Long eventId1 = testEntityManager.persistAndGetId(event1, Long.class);

        // Добавить второе событие первого пользователя
        Event event2 = eventSupplier.get();
        event2.setInitiator(user1);
        Long eventId2 = testEntityManager.persistAndGetId(event2, Long.class);

        //Добавляем первое событие второго пользователя
        User user2 = userSupplier.get();
        Long userId2 = testEntityManager.persistAndGetId(user2, Long.class);
        Event event3 = eventSupplier.get();
        event3.setInitiator(user2);
        event3.setCategory(event2.getCategory());
        Long eventId3 = testEntityManager.persistAndGetId(event3, Long.class);

        List<Long> userIds = new java.util.ArrayList<>(List.of(userId1));
        List<Long> categoryIds = List.of(event2.getCategory().getId(), event1.getCategory().getId());
        List<String> states = List.of("PENDING");


        assertAll(
                () -> assertEquals(
                        eventService.searchEvents(
                                userIds, states, categoryIds,
                                LocalDateTime.now().minusMonths(1), LocalDateTime.now().plusMonths(1),
                                0, 20
                        ).size(), 2),

                () -> {
                    userIds.add(userId2);
                    assertEquals(eventService.searchEvents(
                            userIds, states, categoryIds,
                            LocalDateTime.now().minusMonths(1), LocalDateTime.now().plusMonths(1),
                            0, 20
                    ).size(), 3);
                }
        );
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
                    .participantLimit(10L)
                    .requestModeration(true)
                    .title("title " + nanoTime)
                    .createdOn(LocalDateTime.now().plusDays(1))
                    .state(EventState.PENDING)
                    .build();

            testEntityManager.persistAndFlush(event);
            res.put(event.getId(), event);
        }

        return res;
    }

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

}