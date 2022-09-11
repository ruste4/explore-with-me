package ru.practicum.explorewithme.request;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.explorewithme.event.Event;
import ru.practicum.explorewithme.event.EventRepository;
import ru.practicum.explorewithme.event.exception.EventNotFoundException;
import ru.practicum.explorewithme.request.dto.RequestCreateDto;
import ru.practicum.explorewithme.request.dto.RequestFullDto;
import ru.practicum.explorewithme.request.exception.RequestNotFoundException;
import ru.practicum.explorewithme.user.User;
import ru.practicum.explorewithme.user.UserRepository;
import ru.practicum.explorewithme.user.exception.UserNotFoundException;

import javax.transaction.Transactional;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class RequestServiceImpl implements RequestService {

    private final RequestRepository requestRepository;
    private final EventRepository eventRepository;

    private final UserRepository userRepository;

    @Override
    public RequestFullDto addEventRequest(long userId, RequestCreateDto createDto) {
        Event event = findEventById(createDto.getEvent());
        return null;
    }

    /*
        1) нельзя добавить повторный запрос
        2) инициатор события не может добавить запрос на участие в своём событии
        3) нельзя участвовать в неопубликованном событии
        4) если у события достигнут лимит запросов на участие - необходимо вернуть ошибку
        5) если для события отключена пре-модерация запросов на участие, то запрос должен автоматически перейти в состояние подтвержденного
    */

    @Override
    public List<RequestFullDto> getEventRequestsFromCurrentUser(long userId) {

        User requester = findUserById(userId);

        return requestRepository
                .findAllByRequester(requester)
                .stream()
                .map(RequestMapper::toRequestFullDto)
                .collect(Collectors.toList());
    }

    @Override
    public RequestFullDto cancelEventRequestCurrentUser(long userId, long requestId) {
        return null;
    }

    private Event findEventById(long id) {
        return eventRepository.findById(id).orElseThrow(
                () -> new EventNotFoundException(String.format("Event with id:%s not found", id))
        );
    }

    private Request findRequestById(long id) {
        return requestRepository.findById(id).orElseThrow(
                () -> new RequestNotFoundException(String.format("Request with id:%s not found", id))
        );
    }

    private User findUserById(long id) {
        return userRepository.findById(id).orElseThrow(
                () -> new UserNotFoundException(String.format("User with id:%s not found", id))
        );
    }
}
