package ru.practicum.explorewithme.request;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.explorewithme.event.Event;
import ru.practicum.explorewithme.event.EventRepository;
import ru.practicum.explorewithme.event.EventState;
import ru.practicum.explorewithme.event.exception.EventNotFoundException;
import ru.practicum.explorewithme.request.dto.RequestCreateDto;
import ru.practicum.explorewithme.request.dto.RequestFullDto;
import ru.practicum.explorewithme.request.exception.*;
import ru.practicum.explorewithme.user.User;
import ru.practicum.explorewithme.user.UserRepository;
import ru.practicum.explorewithme.user.exception.UserNotFoundException;

import javax.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
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
        User user = findUserById(userId);

        checkRequestLimit(event);
        checkEventStateIsPublished(event);
        checkUserNotEventInitiator(user, event);
        checkEventRequestNotAlreadyExist(userId, createDto.getEvent());

        Request newRequest = new Request();
        newRequest.setEvent(event);
        newRequest.setRequester(user);
        newRequest.setCreated(LocalDateTime.now());

        if (!event.isRequestModeration()) {
            newRequest.setStatus(RequestStatus.PUBLISHED);
        } else {
            newRequest.setStatus(RequestStatus.PENDING);
        }

        return RequestMapper.toRequestFullDto(requestRepository.save(newRequest));
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

    /**
     * Проверить на отсутсвие похожего запроса
     *
     * @throws RequestAlreadyExistException если событие было добавлено ранее
     */
    private void checkEventRequestNotAlreadyExist(long userId, long eventId) {
        Optional<RequestFullDto> foundDuplicate = getEventRequestsFromCurrentUser(userId)
                .stream()
                .filter(r -> r.getEvent().equals(eventId))
                .findFirst();

        if (foundDuplicate.isPresent()) {
            throw new RequestAlreadyExistException(
                    String.format("Request for event with id:%s on user with id:%s already exist", eventId, userId)
            );
        }
    }

    /**
     * Проверить на то, что запрашивающий не является инициатором события
     *
     * @throws RequesterIsInitiatorEventException если запрашивающий на участие в событии пользователь является
     *                                            инициатором
     */
    private void checkUserNotEventInitiator(User requester, Event event) {
        boolean isEventInitiator = event.getInitiator().getId().equals(requester.getId());

        if (isEventInitiator) {
            throw new RequesterIsInitiatorEventException("Requester cannot be initiator of event");
        }
    }

    /**
     * Проверить на то, что событие было опубликовано
     *
     * @throws RequestUnpublishedEventException если событие не опубликовано
     */
    private void checkEventStateIsPublished(Event event) {
        if (!event.getState().equals(EventState.PUBLISHED)) {
            throw new RequestUnpublishedEventException("Request to an unpublished event is prohibited");
        }
    }

    /**
     * Проверить лимит запросов на событие
     *
     * @throws ParticipantLimitExceededException если лимит на количество запросов на событие превышен
     */
    private void checkRequestLimit(Event event) {
        int requestCount = requestRepository.findAllByEvent(event).size();
        if (event.getParticipantLimit() > 0 && requestCount >= event.getParticipantLimit()) {
            throw new ParticipantLimitExceededException(
                    String.format("Participant limit exceeded fro event with id:%s", event.getId())
            );
        }
    }

}
