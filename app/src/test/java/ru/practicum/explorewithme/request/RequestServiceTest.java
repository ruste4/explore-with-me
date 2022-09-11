package ru.practicum.explorewithme.request;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.AutoConfigureTestEntityManager;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.boot.test.context.SpringBootTest;
import ru.practicum.explorewithme.event.Event;
import ru.practicum.explorewithme.event.EventState;
import ru.practicum.explorewithme.event.category.Category;
import ru.practicum.explorewithme.request.dto.RequestFullDto;
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

    @Autowired
    private final RequestService requestService;

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
                    .participantLimit(10)
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

}