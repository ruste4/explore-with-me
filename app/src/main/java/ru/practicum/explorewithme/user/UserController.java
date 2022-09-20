package ru.practicum.explorewithme.user;

import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.explorewithme.user.dto.UserCreateDto;
import ru.practicum.explorewithme.user.dto.UserDto;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping(path = "/admin/users")
@RequiredArgsConstructor
@Validated
public class UserController {

    private final UserService userService;

    @GetMapping
    public List<UserDto> search(
            @RequestParam("ids") long[] userIds,
            @RequestParam(defaultValue = "0") Integer from,
            @RequestParam(defaultValue = "10") Integer size
    ) {
        return userService.search(userIds, from, size);
    }

    @PostMapping
    public UserDto add(@RequestBody @Valid UserCreateDto userCreateDto) {
        return userService.add(userCreateDto);
    }

    @DeleteMapping("/{userId}")
    public UserDto delete(@PathVariable long userId) {
        return userService.deleteById(userId);
    }

    @PatchMapping("/{userId}/activate")
    public UserDto activate(@PathVariable long userId) {
        return userService.activateById(userId);
    }
}
