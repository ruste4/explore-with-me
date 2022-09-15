package ru.practicum.explorewithme.event;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.AutoConfigureTestEntityManager;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.boot.test.context.SpringBootTest;
import ru.practicum.explorewithme.event.category.Category;
import ru.practicum.explorewithme.event.dto.EventCreateDto;
import ru.practicum.explorewithme.event.dto.EventFullDto;
import ru.practicum.explorewithme.event.dto.EventShortDto;
import ru.practicum.explorewithme.event.dto.EventUpdateDto;
import ru.practicum.explorewithme.event.exception.EventDateInvalidException;
import ru.practicum.explorewithme.event.exception.EventUpdatingIsProhibitedException;
import ru.practicum.explorewithme.event.exception.UserInActivatedException;
import ru.practicum.explorewithme.event.requestparams.SearchEventParams;
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
        EventCreateDto.Location location = new EventCreateDto.Location(12, 12);

        return EventCreateDto.builder()
                .annotation("annotation " + nanoTime)
                .category(generateEventCategoryAndGetId(String.valueOf(nanoTime)))
                .description("description " + nanoTime)
                .eventDate(LocalDateTime.now().plusDays(1))
                .location(location)
                .paid(false)
                .participantLimit(10l)
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
                .participantLimit(10l)
                .requestModeration(true)
                .title("title " + nanoTime)
                .createdOn(LocalDateTime.now().plusDays(1))
                .state(EventState.PENDING)
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

        assertThrows(UserInActivatedException.class, () -> eventService.addEvent(userId, eventCreateDto));
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
        User user = userSupplier.get();
        Long userId = testEntityManager.persistAndGetId(user, Long.class);
        Event event = eventSupplier.get();
        event.setInitiator(user);
        testEntityManager.persistAndFlush(event);
        String updatedDescription = "updatedDescription";
        String updatedTitle = "updatedTitle";
        EventUpdateDto eventUpdateDto = new EventUpdateDto();
        eventUpdateDto.setId(event.getId());
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
        eventUpdateDto.setId(event.getId());

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
        eventUpdateDto.setId(event.getId());
        eventUpdateDto.setEventDate(LocalDateTime.now().plusHours(1));

        assertThrows(
                EventDateInvalidException.class,
                () -> eventService.updateEventByInitiatorId(userId, eventUpdateDto)
        );
    }

    @Test
    public void getEventsByInitiatorIdSuccess() {
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

        long[] userIds = {userId1};
        Long[] categoryIds = {event2.getCategory().getId(), event1.getCategory().getId()};
        String[] states = {"PENDING"};

        SearchEventParams params = new SearchEventParams();
        params.setUsers(userIds);
        params.setCategories(categoryIds);
        params.setStates(states);
        params.setRangeStart(LocalDateTime.now().minusMonths(1));
        params.setRangeEnd(LocalDateTime.now().plusMonths(1));
        params.setFrom(0);
        params.setSize(10);

        assertAll(
                () -> assertEquals(eventService.searchEvents(params).size(), 2),
                () -> {
                    long[] userIdsWithTwoUsers = {userId1, userId2};
                    params.setUsers(userIdsWithTwoUsers);
                    assertEquals(eventService.searchEvents(params).size(), 3);
                }
        );
    }

//    @Test
//    public void getEventsSuccess() {
//        Map<Long, Event> eventMap = generateAndPersistEvent(3);
//        eventMap.get(1L).setParticipantLimit(0L);
//        eventMap.get(2L).setPaid(true);
//
//        String searchText = "anno";
//        Long[] searchCategory = eventMap.values().stream().map(
//                event -> event.getCategory().getId()
//        ).toArray(Long[]::new);
//
//        GetEventsParams params = GetEventsParams.builder()
//                .text(searchText)
//                .categoryIds(searchCategory)
//                .paid(false)
//                .rangeStart(LocalDateTime.now().minusWeeks(1))
//                .rangeEnd(LocalDateTime.now().plusWeeks(1))
//                .onlyAvailable(true)
////                .sort(EventSort.EVENT_DATE)
////                .from(0)
////                .size(10)
//                .build();
//
//        List<EventShortDto> found = eventService.getEvents(params);
//
//        System.out.println(1);
//    }

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

    ;

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