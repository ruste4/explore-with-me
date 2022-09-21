package ru.practicum.explorewithme.user;

import ru.practicum.explorewithme.user.dto.UserCreateDto;
import ru.practicum.explorewithme.user.dto.UserDto;
import ru.practicum.explorewithme.user.dto.UserFullDto;

public class UserMapper {
    public static User toUser(UserCreateDto userCreateDto) {
        return new User(null, userCreateDto.getEmail(), userCreateDto.getName(), true);
    }

    public static UserDto toUserDto(User user) {
        return new UserDto(user.getId(), user.getEmail(), user.getName());
    }

    public static UserFullDto toUserFullDto(User user) {
        UserFullDto result = new UserFullDto();
        result.setId(user.getId());
        result.setEmail(user.getEmail());
        result.setName(user.getName());
        result.setActivated(user.isActivated());
        return result;
    }

    public static User toUser(UserFullDto fullDto) {
        return new User(fullDto.getId(), fullDto.getEmail(), fullDto.getName(), fullDto.isActivated());
    }
}
