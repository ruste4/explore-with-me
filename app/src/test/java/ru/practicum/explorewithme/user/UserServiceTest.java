package ru.practicum.explorewithme.user;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.AutoConfigureTestEntityManager;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import ru.practicum.explorewithme.user.dto.UserCreateDto;
import ru.practicum.explorewithme.user.dto.UserDto;
import ru.practicum.explorewithme.user.exception.UserNotFoundException;

import javax.transaction.Transactional;

import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@AutoConfigureTestDatabase
@AutoConfigureTestEntityManager
@Transactional
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class UserServiceTest {
    @Autowired
    private final UserService userService;

    @Autowired
    private final TestEntityManager testEntityManager;

    private static AtomicLong userIdHolder;

    @BeforeAll
    public static void init() {
        userIdHolder = new AtomicLong();
    }

    @BeforeEach
    public void beforeEachUserServiceTest() {
        testEntityManager.clear();
    }

    private final Supplier<UserCreateDto> userCreateDtoSupplier = () -> {
        UserCreateDto result = new UserCreateDto();
        result.setName("UserName");
        result.setEmail(String.format("%s%s@mail.ru", result.getName(), userIdHolder.getAndIncrement()));
        return result;
    };

    private final Supplier<User> userSupplier = () -> {
        User result = new User();
        result.setName("UserName");
        result.setEmail(String.format("%s%s@mail.ru", result.getName(), userIdHolder.getAndIncrement()));
        return result;
    };

    @Test
    public void addUserSuccess() {
        UserCreateDto userCreateDto = userCreateDtoSupplier.get();
        UserDto createdUser = userService.add(userCreateDto);
        User foundUser = testEntityManager.find(User.class, createdUser.getId());

        assertAll("Tests for user fields",
                () -> assertNotNull(foundUser.getId(), "id not null"),
                () -> assertEquals(userCreateDto.getName(), foundUser.getName(), "the names are identical"),
                () -> assertEquals(userCreateDto.getEmail(), foundUser.getEmail(), "the emails are identical"),
                () -> assertFalse(foundUser.isActivated())
        );
    }

    @Test
    public void addUserFailDuplicateEmail() {
        UserCreateDto userCreateDto = userCreateDtoSupplier.get();
        UserCreateDto userCreateDtoWithDuplicateEmail = userCreateDtoSupplier.get();
        userCreateDtoWithDuplicateEmail.setEmail(userCreateDto.getEmail());

        userService.add(userCreateDto);

        assertThrows(DataIntegrityViolationException.class, () -> userService.add(userCreateDtoWithDuplicateEmail));
    }

    @Test
    public void addUserFailNoEmail() {
        UserCreateDto userCreateDto = userCreateDtoSupplier.get();
        userCreateDto.setEmail(null);

        assertThrows(DataIntegrityViolationException.class, () -> userService.add(userCreateDto));
    }

    @Test
    public void addUserFailNoName() {
        UserCreateDto userCreateDto = userCreateDtoSupplier.get();
        userCreateDto.setName(null);

        assertThrows(DataIntegrityViolationException.class, () -> userService.add(userCreateDto));
    }

    @Test
    public void deleteByIdSuccess() {
        User user = userSupplier.get();
        Long userId = testEntityManager.persistAndGetId(user, Long.class);
        testEntityManager.flush();

        userService.deleteById(userId);

        assertNull(testEntityManager.find(User.class, userId));
    }

    @Test
    public void deleteByIdFailUserNotFound() {
        assertThrows(UserNotFoundException.class, () -> userService.deleteById(Long.MAX_VALUE));
    }

    @Test
    public void activateUserSuccess() {
        User user = userSupplier.get();
        Long userId = testEntityManager.persistAndGetId(user, Long.class);
        testEntityManager.flush();

        userService.activateById(userId);

        assertTrue(testEntityManager.find(User.class, userId).isActivated());
    }

    @Test
    public void activateUserFailUserNotFound() {
        assertThrows(UserNotFoundException.class, () -> userService.activateById(Long.MAX_VALUE));
    }

    @Test
    public void search2User() {
        User firstUser = userSupplier.get();
        Long firstUserId = testEntityManager.persistAndGetId(firstUser, Long.class);

        User secondUser = userSupplier.get();
        Long secondUserId = testEntityManager.persistAndGetId(secondUser, Long.class);

        User thirdUser = userSupplier.get();
        Long thirdUserId = testEntityManager.persistAndGetId(thirdUser, Long.class);

        long[] ids = {firstUserId, thirdUserId};

        List<UserDto> searchResult = userService.search(ids, 0, 10);

        assertAll(
                () -> assertEquals(searchResult.get(0).getEmail(), firstUser.getEmail()),
                () -> assertEquals(searchResult.get(1).getEmail(), thirdUser.getEmail())
        );
    }

    @Test
    public void searchEmptyList() {
        User firstUser = userSupplier.get();
        testEntityManager.persist(firstUser);

        User secondUser = userSupplier.get();
        testEntityManager.persist(secondUser);

        User thirdUser = userSupplier.get();
        testEntityManager.persist(thirdUser);

        long[] ids = {Long.MAX_VALUE, Long.MAX_VALUE - 1};

        List<UserDto> searchResult = userService.search(ids, 0, 10);

        assertTrue(searchResult.isEmpty());
    }
}