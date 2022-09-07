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
import ru.practicum.explorewithme.event.exception.EventDateInvalidException;
import ru.practicum.explorewithme.event.exception.UserInActivatedException;
import ru.practicum.explorewithme.user.User;

import javax.transaction.Transactional;

import java.time.LocalDateTime;
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

    private Supplier<EventCreateDto> createDtoSupplier = () -> {
        long nanoTime = System.nanoTime();
        EventCreateDto.Location location = new EventCreateDto.Location(12, 12);

        return EventCreateDto.builder()
                .annotation("annotation " + nanoTime)
                .category(generateEventCategoryAndGetId(String.valueOf(nanoTime)))
                .description("description " + nanoTime)
                .eventDate(LocalDateTime.now().plusDays(1))
                .location(location)
                .paid(false)
                .participantLimit(10)
                .requestModeration(true)
                .title("title " + nanoTime)
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
        Category category = new Category(null, name);
        return testEntityManager.persistAndGetId(category, Long.class);
    }

}