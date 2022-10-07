package ru.practicum.explorewithme.user;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import ru.practicum.explorewithme.user.dto.UserCreateDto;
import ru.practicum.explorewithme.user.dto.UserDto;
import ru.practicum.explorewithme.user.dto.UserFullDto;
import ru.practicum.explorewithme.user.exception.UserNotFoundException;

import javax.transaction.Transactional;
import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    @Override
    public List<UserDto> search(long[] ids, int from, int size) {
        PageRequest pageRequest = PageRequest.of(from, size);
        Page<User> result = userRepository.findAllByIdIn(ids, pageRequest);

        return result.map(UserMapper::toUserDto).toList();
    }

    @Override
    public UserFullDto findUserById(long userId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new UserNotFoundException(userId));

        return UserMapper.toUserFullDto(user);
    }

    @Override
    public UserDto add(UserCreateDto userCreateDto) {
        log.info("Add User with email: {}, name: {}", userCreateDto.getEmail(), userCreateDto.getName());
        User user = UserMapper.toUser(userCreateDto);
        userRepository.save(user);
        log.info("User with email: {} added, assigned id:{}", userCreateDto.getEmail(), user.getId());
        return UserMapper.toUserDto(user);
    }

    @Override
    public UserDto deleteById(long userId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new UserNotFoundException(userId));
        userRepository.deleteById(userId);

        return UserMapper.toUserDto(user);
    }

    @Override
    public UserDto activateById(long userId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new UserNotFoundException(userId));

        user.setActivated(true);

        return UserMapper.toUserDto(user);
    }
}
