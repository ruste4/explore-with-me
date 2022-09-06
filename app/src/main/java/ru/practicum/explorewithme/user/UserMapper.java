package ru.practicum.explorewithme.user;

import ru.practicum.explorewithme.user.dto.UserCreateDto;
import ru.practicum.explorewithme.user.dto.UserDto;

public class UserMapper {
    public static User toUser(UserCreateDto userCreateDto) {
        return new User(null, userCreateDto.getEmail(), userCreateDto.getName(), false);
    }

    public static UserDto toUserDto(User user) {
        return new UserDto(user.getId(), user.getEmail(), user.getName());
    }
}
