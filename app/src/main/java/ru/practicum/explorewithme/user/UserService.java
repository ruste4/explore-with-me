package ru.practicum.explorewithme.user;

import ru.practicum.explorewithme.user.dto.UserCreateDto;
import ru.practicum.explorewithme.user.dto.UserDto;
import ru.practicum.explorewithme.user.dto.UserFullDto;

import java.util.List;

public interface UserService {

    List<UserDto> search(long[] ids, int from, int size);

    UserFullDto findUserById(long userId);

    UserDto add(UserCreateDto userCreateDto);

    UserDto deleteById(long userId);

    UserDto activateById(long userId);

}
