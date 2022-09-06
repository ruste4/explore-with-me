package ru.practicum.explorewithme.user;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import ru.practicum.explorewithme.user.dto.UserCreateDto;
import ru.practicum.explorewithme.user.dto.UserDto;
import ru.practicum.explorewithme.user.exception.UserNotFoundException;

import javax.transaction.Transactional;
import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class UserServiceImpl implements UserService{

    private final UserRepository userRepository;

    @Override
    public List<UserDto> search(long[] ids, int from, int size) {
        PageRequest pageRequest = PageRequest.of(from, size);
        Page<User> result = userRepository.findAllByIdIn(ids, pageRequest);

        return result.map(UserMapper::toUserDto).toList();
    }

    @Override
    public UserDto add(UserCreateDto userCreateDto) {
        User user = UserMapper.toUser(userCreateDto);
        userRepository.save(user);
        return UserMapper.toUserDto(user);
    }

    @Override
    public UserDto deleteById(long userId) {
        User user = userRepository.findById(userId).orElseThrow(
                () -> new UserNotFoundException(String.format("User with id:%s not found", userId))
        );
        userRepository.deleteById(userId);
        return UserMapper.toUserDto(user);
    }

    @Override
    public UserDto activateById(long userId) {
        User user = userRepository.findById(userId).orElseThrow(
                () -> new UserNotFoundException(String.format("User with id:%s not found", userId))
        );

        user.setActivated(true);

        return UserMapper.toUserDto(user);
    }
}
