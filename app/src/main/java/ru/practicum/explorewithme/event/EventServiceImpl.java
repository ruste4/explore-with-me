package ru.practicum.explorewithme.event;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import ru.practicum.explorewithme.event.category.Category;
import ru.practicum.explorewithme.event.category.CategoryRepository;
import ru.practicum.explorewithme.event.category.exception.CategoryNotFoundException;
import ru.practicum.explorewithme.event.dto.EventCreateDto;
import ru.practicum.explorewithme.event.dto.EventFullDto;
import ru.practicum.explorewithme.event.dto.EventShortDto;
import ru.practicum.explorewithme.event.dto.EventUpdateDto;
import ru.practicum.explorewithme.event.exception.*;
import ru.practicum.explorewithme.event.requestparams.GetEventsParams;
import ru.practicum.explorewithme.event.requestparams.SearchEventParams;
import ru.practicum.explorewithme.request.RequestRepository;
import ru.practicum.explorewithme.request.RequestStatus;
import ru.practicum.explorewithme.user.User;
import ru.practicum.explorewithme.user.UserRepository;
import ru.practicum.explorewithme.user.exception.UserNotFoundException;

import javax.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class EventServiceImpl implements EventService {

    private final UserRepository userRepository;

    private final CategoryRepository categoryRepository;

    private final EventRepository eventRepository;

    private final RequestRepository requestRepository;

    private static final int BAN_HOURS_BEFORE_EVENT = 2;

    @Override
    public List<EventShortDto> getEvents(GetEventsParams params) {
        switch (params.getSort()) {
            case EVENT_DATE:
                Specification<Event> spec = EventSpecs
                        .hasTextInAnnotationOrDescription(params.getText())
                        .and(EventSpecs.hasEventCategory(params.getCategoryIds()))
                        .and(EventSpecs.isPaid(params.getPaid()))
                        .and(EventSpecs.betweenDates(params.getRangeStart(), params.getRangeEnd()))
                        .and(EventSpecs.isEventAvailable(params.isOnlyAvailable()));

                PageRequest pg = PageRequest.of(params.getFrom(), params.getSize(), Sort.by("eventDate"));

                List<Long> eventIds = new ArrayList<>(); // todo собираем id для отправки на сервер статистики

                List<EventShortDto> res = eventRepository.findAll(spec, pg).map(e -> {
                            int confirmedRequestCount = getConfirmedRequestsCountForEvent(e);
                            EventShortDto shortDto = EventMapper.toEventShortDto(e);
                            shortDto.setConfirmedRequests(confirmedRequestCount);
                            eventIds.add(e.getId());
                            return shortDto;
                        }).toList();

                // todo отправляем запрос на view

                res.forEach(e -> e.setViews(ThreadLocalRandom.current().nextInt(0, 10)));  // todo пока заполним рандомно

                return res;

            case VIEWS:
                Map<Long, Integer> viewMap = new HashMap<>();

                List<EventShortDto> events = eventRepository.findAll(
                        EventSpecs
                                .hasTextInAnnotationOrDescription(params.getText())
                                .and(EventSpecs.hasEventCategory(params.getCategoryIds()))
                                .and(EventSpecs.isPaid(params.getPaid()))
                                .and(EventSpecs.betweenDates(params.getRangeStart(), params.getRangeEnd()))
                                .and(EventSpecs.isEventAvailable(params.isOnlyAvailable()))
                ).stream().map(event -> {
                    int confirmedRequestCount = getConfirmedRequestsCountForEvent(event);
                    EventShortDto shortDto = EventMapper.toEventShortDto(event);
                    shortDto.setConfirmedRequests(confirmedRequestCount);
                    viewMap.put(event.getId(), 0);
                    return shortDto;
                }).collect(Collectors.toList());

                // TODO 1) Отправляем eventIds на сервер статистики вместе с даныыми для пагинации
                //      2) На стороне сервера статиски делаем подборку просмотров Map(eventId, viewCount). айдишники нужно отсортирвать по количеству просмотров, и вернуть часть, согласно пагинации
                //      3) После получения ответа от сервера статистики записать параметры view в event согласно их idи отсортировать по events.viewCount
                return events;

            default:
                return eventRepository.findAll(
                        EventSpecs
                                .hasTextInAnnotationOrDescription(params.getText())
                                .and(EventSpecs.hasEventCategory(params.getCategoryIds()))
                                .and(EventSpecs.isPaid(params.getPaid()))
                                .and(EventSpecs.betweenDates(params.getRangeStart(), params.getRangeEnd()))
                                .and(EventSpecs.isEventAvailable(params.isOnlyAvailable()))
                ).stream().map(event -> {
                    int confirmedRequestCount = getConfirmedRequestsCountForEvent(event);
                    EventShortDto shortDto = EventMapper.toEventShortDto(event);
                    shortDto.setConfirmedRequests(confirmedRequestCount);

                    return shortDto;
                }).collect(Collectors.toList());

        }

    }

    @Override
    public EventFullDto getEventById(long id) {
        Event event = findEventById(id);
        int confirmedRequestCount = getConfirmedRequestsCountForEvent(event);
        EventFullDto fullDto = EventMapper.toEventFullDto(event);
        fullDto.setConfirmedRequests(confirmedRequestCount);
        return fullDto;
    }

    @Override
    public List<EventShortDto> getEventsByInitiatorId(long userId, int from, int size) {
        User initiator = findUserById(userId);

        PageRequest pageRequest = PageRequest.of(from, size);

        return eventRepository
                .findAllByInitiator(initiator, pageRequest)
                .map(event -> {
                    int confirmedRequestCount = getConfirmedRequestsCountForEvent(event);
                    EventShortDto shortDto = EventMapper.toEventShortDto(event);
                    shortDto.setConfirmedRequests(confirmedRequestCount);

                    return shortDto;
                })
                .toList(); // todo в этом месте каждому EventShortDto згенерируй views
    }

    @Override
    public EventFullDto updateEventByInitiatorId(long userId, EventUpdateDto eventUpdateDto) {

        long eventId = eventUpdateDto.getId();

        Event event = findEventById(eventId);

        isInitiatorOrException(event, userId);

        return updateEventById(eventId, eventUpdateDto);
    }

    @Override
    public EventFullDto updateEventById(long eventId, EventUpdateDto eventUpdateDto) {
        LocalDateTime now = LocalDateTime.now();

        Event event = findEventById(eventId);

        boolean isValidEventState = event.getState().equals(EventState.PENDING)
                || event.getState().equals(EventState.CANCELED);


        if (!isValidEventState) {
            throw new EventUpdatingIsProhibitedException("To update an event, its status must be CANCELED or PENDING");
        }

        if (eventUpdateDto.getCategory() != null) {
            long categoryId = eventUpdateDto.getCategory();

            Category category = categoryRepository.findById(categoryId).orElseThrow(
                    () -> new CategoryNotFoundException(String.format("Category with id:%s not found", categoryId))
            );

            event.setCategory(category);
        }

        if (eventUpdateDto.getDescription() != null) {
            event.setDescription(eventUpdateDto.getDescription());
        }

        if (eventUpdateDto.getEventDate() != null) {
            boolean isValidEventDate = eventUpdateDto.getEventDate().minusHours(BAN_HOURS_BEFORE_EVENT).isAfter(now);

            if (!isValidEventDate) {
                throw new EventDateInvalidException(String.format(
                        "It is forbidden to update events date no earlier than %s hours before the event",
                        BAN_HOURS_BEFORE_EVENT));
            }

            event.setEventDate(eventUpdateDto.getEventDate());
        }

        if (eventUpdateDto.getPaid() != null) {
            event.setPaid(eventUpdateDto.getPaid());
        }

        if (eventUpdateDto.getParticipantLimit() != null) {
            event.setParticipantLimit(eventUpdateDto.getParticipantLimit());
        }

        if (eventUpdateDto.getTitle() != null) {
            event.setTitle(eventUpdateDto.getTitle());
        }

        int confirmedRequestCount = getConfirmedRequestsCountForEvent(event);
        EventFullDto fullDto = EventMapper.toEventFullDto(event);
        fullDto.setConfirmedRequests(confirmedRequestCount);
        //todo запиши view
        return fullDto;
    }

    @Override
    public EventFullDto addEvent(long userId, EventCreateDto eventCreateDto) {
        LocalDateTime now = LocalDateTime.now();
        boolean isValidEventDate = eventCreateDto.getEventDate().minusHours(BAN_HOURS_BEFORE_EVENT).isAfter(now);

        User user = findUserById(userId);

        if (!user.isActivated()) {
            throw new UserInActivatedException(String.format("User with id:%s is not activated", userId));
        }

        if (!isValidEventDate) {
            throw new EventDateInvalidException(String.format(
                    "It is forbidden to create events no earlier than %s hours before the event",
                    BAN_HOURS_BEFORE_EVENT));
        }

        Event event = EventMapper.toEvent(eventCreateDto);

        event.setCategory(categoryRepository.findById(eventCreateDto.getCategory()).orElseThrow(
                () -> new CategoryNotFoundException(
                        String.format("Category with id:%s not found", eventCreateDto.getCategory())
                )
        ));

        event.setCreatedOn(now);

        event.setInitiator(user);

        event.setState(EventState.PENDING);

        eventRepository.save(event);

        return EventMapper.toEventFullDto(event);

    }

    @Override
    public EventFullDto getEventCurrentUserById(long userId, long eventId) {
        Event event = findEventById(eventId);

        isInitiatorOrException(event, userId);

        int confirmedRequestCount = getConfirmedRequestsCountForEvent(event);
        EventFullDto fullDto = EventMapper.toEventFullDto(event);
        fullDto.setConfirmedRequests(confirmedRequestCount);

        return fullDto;
    }

    @Override
    public EventFullDto cancelEventAddedCurrentUserById(long userId, long eventId) {
        Event event = findEventById(eventId);

        isInitiatorOrException(event, userId);

        event.setState(EventState.CANCELED);

        int confirmedRequestCount = getConfirmedRequestsCountForEvent(event);
        EventFullDto fullDto = EventMapper.toEventFullDto(event);
        fullDto.setConfirmedRequests(confirmedRequestCount);

        return EventMapper.toEventFullDto(event);
    }

    @Override
    public void getRequestsForEventCurrentUserById(long userId, long eventId) {

    }

    @Override
    public void confirmRequestOnEventCurrentUser(long userId, long eventId, long reqId) {

    }

    @Override
    public void rejectRequestOnEventCurrentUser(long userId, long eventId, long reqId) {

    }

    @Override
    public List<EventFullDto> searchEvents(SearchEventParams params) {
        PageRequest pageRequest = PageRequest.of(params.getFrom(), params.getSize());
        return eventRepository.findAll(EventSpecs
                        .hasInitiationIds(params.getUsers())
                        .and(EventSpecs.hasEventStates(params.getStates()))
                        .and(EventSpecs.hasEventCategory(params.getCategories())),
                pageRequest
        ).map(event -> {
            int confirmedRequestCount = getConfirmedRequestsCountForEvent(event);
            EventFullDto fullDto = EventMapper.toEventFullDto(event);
            fullDto.setConfirmedRequests(confirmedRequestCount);

            return fullDto;
        }).toList();
    }

    private Event findEventById(long id) {
        return eventRepository.findById(id).orElseThrow(
                () -> new EventNotFoundException(String.format("Event with id:%s not found", id))
        );
    }

    private User findUserById(long id) {
        return userRepository.findById(id).orElseThrow(
                () -> new UserNotFoundException(String.format("User with id:%s not found", id))
        );
    }

    private void isInitiatorOrException(Event event, long userId) {
        boolean isInitiator = event.getInitiator().getId().equals(userId);

        if (!isInitiator) {
            throw new UserIsNotInitiatorException(
                    String.format("User with id:%s is not the initiator of the event with id:%s", userId, event.getId())
            );
        }
    }

    private int getConfirmedRequestsCountForEvent(Event event) {
        return requestRepository.findAllByEventAndStatus(event, RequestStatus.CONFIRMED).size();
    }
}
