package ru.practicum.explorewithme.compilation;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.AutoConfigureTestEntityManager;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.boot.test.context.SpringBootTest;
import ru.practicum.explorewithme.compilation.dto.CompilationCreateDto;
import ru.practicum.explorewithme.compilation.dto.CompilationDto;
import ru.practicum.explorewithme.compilation.exception.CompilationNotFoundException;
import ru.practicum.explorewithme.compilation.exception.EventAlreadyExistAtCompilationException;
import ru.practicum.explorewithme.event.Event;
import ru.practicum.explorewithme.event.EventState;
import ru.practicum.explorewithme.event.Location;
import ru.practicum.explorewithme.event.category.Category;
import ru.practicum.explorewithme.event.exception.EventNotFoundException;
import ru.practicum.explorewithme.user.User;

import javax.transaction.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@AutoConfigureTestDatabase
@AutoConfigureTestEntityManager
@Transactional
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class CompilationServiceTest {

    @Autowired
    private final CompilationService compilationService;

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

    @Test
    public void addCompilationSuccess() {
        List<Event> events = new ArrayList<>(generateAndPersistEvent(5).values());
        List<Long> eventIds = events.stream().map(Event::getId).collect(Collectors.toList());
        CompilationCreateDto createDto = CompilationCreateDto.builder()
                .events(eventIds)
                .pinned(false)
                .title("Test title")
                .build();

        CompilationDto createdCompilation = compilationService.addCompilation(createDto);

        Compilation foundCompilation = testEntityManager.find(Compilation.class, createdCompilation.getId());

        assertAll(
                () -> assertEquals(foundCompilation.getEvents().size(), createDto.getEvents().size()),
                () -> assertEquals(foundCompilation.getTitle(), createDto.getTitle()),
                () -> assertEquals(foundCompilation.isPinned(), createDto.getPinned())
        );
    }

    @Test
    public void deleteCompilationByIdSuccess() {
        Compilation compilation = generateAndPersistCompilation();

        assertAll(
                () -> assertNotNull(testEntityManager.find(Compilation.class, compilation.getId())),
                () -> compilationService.deleteCompilationById(compilation.getId()),
                () -> assertNull(testEntityManager.find(Compilation.class, compilation.getId()))
        );
    }

    @Test
    public void deleteCompilationByIdFailCompilationNotFound() {
        assertThrows(
                Exception.class,
                () -> compilationService.deleteCompilationById(Long.MAX_VALUE)
        );
    }


    @Test
    public void deleteEventFromCompilationSuccess() {
        Compilation compilation = generateAndPersistCompilation();
        Event deleteEvent = compilation.getEvents().get(0);

        compilationService.deleteEventFromCompilation(compilation.getId(), deleteEvent.getId());

        assertFalse(testEntityManager.find(Compilation.class, compilation.getId()).getEvents().contains(deleteEvent));
    }

    @Test
    public void deleteEventFromCompilationFailCompilationNotFound() {
        Event event = generateAndPersistEvent(1).values().stream().findFirst().get();

        assertThrows(
                CompilationNotFoundException.class,
                () -> compilationService.deleteEventFromCompilation(Long.MAX_VALUE, event.getId())
        );
    }

    @Test
    public void deleteEventFromCompilationFailEventNotFound() {
        Compilation compilation = generateAndPersistCompilation();

        assertThrows(
                EventNotFoundException.class,
                () -> compilationService.deleteEventFromCompilation(compilation.getId(), Long.MAX_VALUE)
        );
    }

    @Test
    public void addEventToCompilationSuccess() {
        Compilation compilation = generateAndPersistCompilation();
        Event event = generateAndPersistEvent(1).values().stream().findFirst().get();

        compilationService.addEventToCompilation(compilation.getId(), event.getId());

        Compilation foundCompilation = testEntityManager.find(Compilation.class, compilation.getId());

        assertTrue(foundCompilation.getEvents().contains(event));
    }

    @Test
    public void addEventToCompilationFailEventAlreadyExist() {
        Compilation compilation = generateAndPersistCompilation();
        Event event = generateAndPersistEvent(1).values().stream().findFirst().get();

        compilationService.addEventToCompilation(compilation.getId(), event.getId());

        assertThrows(
                EventAlreadyExistAtCompilationException.class,
                () -> compilationService.addEventToCompilation(compilation.getId(), event.getId())
        );
    }

    @Test
    public void updatePinnedCompilationById() {
        Compilation compilation = generateAndPersistCompilation();

        assertAll(
                () -> {
                    compilationService.pinnedCompilationById(compilation.getId());
                    assertTrue(testEntityManager.find(Compilation.class, compilation.getId()).isPinned());
                },
                () -> {
                    compilationService.unpinnedCompilationById(compilation.getId());
                    assertFalse(testEntityManager.find(Compilation.class, compilation.getId()).isPinned());
                }
        );
    }

    private Compilation generateAndPersistCompilation() {
        long nanoTime = System.nanoTime();
        Compilation compilation = Compilation.builder()
                .title("title " + nanoTime)
                .pinned(false)
                .events(new ArrayList<>(generateAndPersistEvent(5).values()))
                .build();

        return testEntityManager.persist(compilation);

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
                    .location(new Location(17.4, 20.1))
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