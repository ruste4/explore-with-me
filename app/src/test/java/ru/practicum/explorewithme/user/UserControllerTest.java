package ru.practicum.explorewithme.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.explorewithme.user.dto.UserCreateDto;
import ru.practicum.explorewithme.user.dto.UserDto;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Supplier;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.hamcrest.Matchers.is;

@WebMvcTest(controllers = UserController.class)
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class UserControllerTest {
    @Autowired
    private final ObjectMapper mapper;

    @MockBean
    private final UserService userService;

    @Autowired
    private final MockMvc mvc;

    private static AtomicLong userIdHolder;

    @BeforeAll
    public static void init() {
        userIdHolder = new AtomicLong();
    }

    private final Supplier<UserCreateDto> userCreateDtoSupplier = () -> {
        UserCreateDto result = new UserCreateDto();
        result.setName("UserName");
        result.setEmail(String.format("%s%s@mail.ru", result.getName(), userIdHolder.getAndIncrement()));
        return result;
    };

    private final Supplier<UserDto> userDtoSupplier = () -> {
        UserDto result = new UserDto();
        result.setName("UserName");
        result.setEmail(String.format("%s%s@mail.ru", result.getName(), userIdHolder.getAndIncrement()));
        return result;
    };

    @Test
    public void addUserSuccess() throws Exception {
        UserCreateDto userCreateDto = userCreateDtoSupplier.get();
        UserDto userDto = new UserDto(1L, userCreateDto.getEmail(), userCreateDto.getName());

        Mockito
                .when(userService.add(userCreateDto))
                .thenReturn(userDto);

        mvc.perform(post("/admin/users")
                        .content(mapper.writeValueAsString(userCreateDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(userDto.getId().intValue())))
                .andExpect(jsonPath("$.email", is(userDto.getEmail())))
                .andExpect(jsonPath("$.name", is(userDto.getName())));
    }

}