package ru.practicum.explorewithme.request;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.explorewithme.event.Event;
import ru.practicum.explorewithme.event.EventRepository;
import ru.practicum.explorewithme.event.EventState;
import ru.practicum.explorewithme.event.exception.EventNotFoundException;
import ru.practicum.explorewithme.request.dto.RequestFullDto;
import ru.practicum.explorewithme.request.exception.*;
import ru.practicum.explorewithme.user.User;
import ru.practicum.explorewithme.user.UserRepository;
import ru.practicum.explorewithme.user.exception.UserNotFoundException;

import javax.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class RequestServiceImpl implements RequestService {

    private final RequestRepository requestRepository;
    private final EventRepository eventRepository;

    private final UserRepository userRepository;

    @Override
    public RequestFullDto addEventRequest(long userId, long eventId) {
        Event event = findEventById(eventId);
        User user = findUserById(userId);

        checkRequestLimit(event);
        checkEventStateIsPublished(event);
        checkUserNotEventInitiator(user, event);
        checkEventRequestNotAlreadyExist(userId, eventId);

        Request newRequest = new Request();
        newRequest.setEvent(event);
        newRequest.setRequester(user);
        newRequest.setCreated(LocalDateTime.now());

        if (!event.isRequestModeration()) {
            newRequest.setStatus(RequestStatus.CONFIRMED);
        } else {
            newRequest.setStatus(RequestStatus.PENDING);
        }

        return RequestMapper.toRequestFullDto(requestRepository.save(newRequest));
    }

    @Override
    public List<RequestFullDto> getEventRequestsFromCurrentUser(long userId) {
        log.info("Get request from current user with id:{}", userId);

        User requester = findUserById(userId);

        return requestRepository
                .findAllByRequester(requester)
                .stream()
                .map(RequestMapper::toRequestFullDto)
                .collect(Collectors.toList());
    }

    @Override
    public RequestFullDto cancelEventRequestCurrentUser(long userId, long requestId) {
        User user = findUserById(userId);
        Request request = findRequestById(requestId);

        if (!request.getRequester().equals(user)) {
            throw new UserNotRequesterForEventRequestException(userId, requestId);
        }

        request.setStatus(RequestStatus.CANCELED);

        return RequestMapper.toRequestFullDto(request);
    }

    private Event findEventById(long id) {
        return eventRepository.findById(id).orElseThrow(() -> new EventNotFoundException(id));
    }

    private Request findRequestById(long id) {
        return requestRepository.findById(id).orElseThrow(() -> new RequestNotFoundException(id));
    }

    private User findUserById(long id) {
        return userRepository.findById(id).orElseThrow(() -> new UserNotFoundException(id));
    }

    /**
     * Проверить на отсутствие похожего запроса
     *
     * @throws RequestAlreadyExistException если событие было добавлено ранее
     */
    private void checkEventRequestNotAlreadyExist(long userId, long eventId) {
        Optional<RequestFullDto> foundDuplicate = getEventRequestsFromCurrentUser(userId)
                .stream()
                .filter(r -> Objects.equals(r.getEvent(), eventId))
                .findFirst();

        if (foundDuplicate.isPresent()) {
            throw new RequestAlreadyExistException(eventId, userId);
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
            throw new RequesterIsInitiatorEventException(requester.getId(), event.getId());
        }
    }

    /**
     * Проверить на то, что событие было опубликовано
     *
     * @throws RequestUnpublishedEventException если событие не опубликовано
     */
    private void checkEventStateIsPublished(Event event) {
        if (!event.getState().equals(EventState.PUBLISHED)) {
            throw new RequestUnpublishedEventException();
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
                    String.format("Participant limit exceeded for event with id:%s", event.getId())
            );
        }
    }

}
