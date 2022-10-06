package ru.practicum.explorewithme.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import ru.practicum.explorewithme.client.StatisticClient;
import ru.practicum.explorewithme.client.dto.ViewStats;
import ru.practicum.explorewithme.event.category.Category;
import ru.practicum.explorewithme.event.category.CategoryRepository;
import ru.practicum.explorewithme.event.category.exception.CategoryNotFoundException;
import ru.practicum.explorewithme.event.dto.EventCreateDto;
import ru.practicum.explorewithme.event.dto.EventFullDto;
import ru.practicum.explorewithme.event.dto.EventShortDto;
import ru.practicum.explorewithme.event.dto.EventUpdateDto;
import ru.practicum.explorewithme.event.exception.*;
import ru.practicum.explorewithme.request.Request;
import ru.practicum.explorewithme.request.RequestMapper;
import ru.practicum.explorewithme.request.RequestRepository;
import ru.practicum.explorewithme.request.RequestStatus;
import ru.practicum.explorewithme.request.dto.RequestFullDto;
import ru.practicum.explorewithme.request.exception.ParticipantLimitExceededException;
import ru.practicum.explorewithme.request.exception.RequestNotFoundException;
import ru.practicum.explorewithme.user.User;
import ru.practicum.explorewithme.user.UserRepository;
import ru.practicum.explorewithme.user.exception.UserNotActivatedException;
import ru.practicum.explorewithme.user.exception.UserNotFoundException;

import javax.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class EventServiceImpl implements EventService {

    private final UserRepository userRepository;

    private final CategoryRepository categoryRepository;

    private final EventRepository eventRepository;

    private final RequestRepository requestRepository;

    private final StatisticClient statisticClient;

    private static final int BAN_HOURS_BEFORE_EVENT = 2;

    @Override
    public List<EventShortDto> getEvents(
            String text,
            List<Long> categories,
            Boolean paid,
            LocalDateTime rangeStart,
            LocalDateTime rangeEnd,
            Boolean onlyAvailable,
            EventSort sort,
            int from,
            int size
    ) {
        switch (sort) {
            case EVENT_DATE:
                return getEventsSortByEventDate(text, categories, paid, rangeStart, rangeEnd, onlyAvailable, from, size);
            case VIEWS:
                return getEventsSortByViews(text, categories, paid, rangeStart, rangeEnd, onlyAvailable, from, size);
            default:
                return getEventsWithoutSort(text, categories, paid, rangeStart, rangeEnd, onlyAvailable, from, size);
        }

    }

    private List<EventShortDto> getEventsWithoutSort(
            String text,
            List<Long> categories,
            Boolean paid,
            LocalDateTime rangeStart,
            LocalDateTime rangeEnd,
            Boolean onlyAvailable,
            int from,
            int size
    ) {
        PageRequest request = PageRequest.of(from, size);

        List<Event> events = eventRepository.findAll(
                EventSpecs
                        .hasTextInAnnotationOrDescription(text)
                        .and(EventSpecs.hasEventCategory(categories))
                        .and(EventSpecs.isPaid(paid))
                        .and(EventSpecs.betweenDates(rangeStart, rangeEnd))
                        .and(EventSpecs.isEventAvailable(onlyAvailable)),
                request
        ).toList();

        return addViewsAndRequestsForShortEventDto(events);
    }

    private List<EventShortDto> getEventsSortByViews(
            String text,
            List<Long> categories,
            Boolean paid,
            LocalDateTime rangeStart,
            LocalDateTime rangeEnd,
            Boolean onlyAvailable,
            int from,
            int size
    ) {
        List<Event> events = eventRepository.findAll(
                EventSpecs
                        .hasTextInAnnotationOrDescription(text)
                        .and(EventSpecs.hasEventCategory(categories))
                        .and(EventSpecs.isPaid(paid))
                        .and(EventSpecs.betweenDates(rangeStart, rangeEnd))
                        .and(EventSpecs.isEventAvailable(onlyAvailable))
        );

        List<EventShortDto> res = addViewsAndRequestsForShortEventDto(events);

        res.sort((Comparator.comparingInt(EventShortDto::getViews).reversed()));

        size = from + size;

        if (size > events.size()) {
            size = events.size();
        }

        return res.subList(from, size);
    }

    private List<EventShortDto> getEventsSortByEventDate(
            String text,
            List<Long> categories,
            Boolean paid,
            LocalDateTime rangeStart,
            LocalDateTime rangeEnd,
            Boolean onlyAvailable,
            int from,
            int size
    ) {
        PageRequest request = PageRequest.of(from, size, Sort.by("eventDate"));

        List<Event> events = eventRepository.findAll(
                EventSpecs
                        .hasTextInAnnotationOrDescription(text)
                        .and(EventSpecs.hasEventCategory(categories))
                        .and(EventSpecs.isPaid(paid))
                        .and(EventSpecs.betweenDates(rangeStart, rangeEnd))
                        .and(EventSpecs.isEventAvailable(onlyAvailable)),
                request
        ).toList();

        return addViewsAndRequestsForShortEventDto(events);
    }

    private List<EventShortDto> addViewsAndRequestsForShortEventDto(List<Event> events) {
        Map<String, ViewStats> viewStatsMap = new HashMap<>();

        List<EventShortDto> res = events.stream()
                .map(e -> {
                    viewStatsMap.put("/events/" + e.getId(), null);
                    EventShortDto shortDto = EventMapper.toEventShortDto(e);
                    shortDto.setConfirmedRequests(getConfirmedRequestsCountForEvent(e));
                    return shortDto;
                }).collect(Collectors.toList());

        Event eventWithMinCreatedOn = events.stream().min(Comparator.comparing(Event::getCreatedOn)).orElseThrow();

        statisticClient.getStats(
                eventWithMinCreatedOn.getCreatedOn(),
                LocalDateTime.now(),
                viewStatsMap.keySet(),
                false
        ).forEach(vs -> viewStatsMap.put(vs.getUri(), vs));

        res.forEach(e -> {
            String eventUrl = "/events/" + e.getId();
            ViewStats viewStats = viewStatsMap.get(eventUrl);

            if (viewStats != null) {
                e.setViews(viewStats.getHits());
            } else {
                e.setViews(0);
            }
        });

        return res;
    }

    private EventFullDto addViewsAndRequestsForEventFullDto(Event event) {
        Set<String> uri = Set.of("/events/" + event.getId());
        LocalDateTime rangeStart = event.getCreatedOn();
        LocalDateTime rangeEnd = LocalDateTime.now();

        Optional<ViewStats> vs = statisticClient.getStats(rangeStart, rangeEnd, uri, false)
                .stream().findFirst();

        EventFullDto res = EventMapper.toEventFullDto(event);

        vs.ifPresent(viewStats -> res.setViews(viewStats.getHits()));

        res.setConfirmedRequests(getConfirmedRequestsCountForEvent(event));

        return res;
    }

    private List<EventFullDto> addViewsAndRequestsForEventFullDto(List<Event> events) {
        Map<String, ViewStats> viewStatsMap = new HashMap<>();

        List<EventFullDto> res = events.stream()
                .map(e -> {
                    viewStatsMap.put("/events/" + e.getId(), null);
                    EventFullDto fullDto = EventMapper.toEventFullDto(e);
                    fullDto.setConfirmedRequests(getConfirmedRequestsCountForEvent(e));
                    return fullDto;
                }).collect(Collectors.toList());

        Event eventWithMinCreatedOn = events.stream().min(Comparator.comparing(Event::getCreatedOn)).orElseThrow();

        statisticClient.getStats(
                eventWithMinCreatedOn.getCreatedOn(),
                LocalDateTime.now(),
                viewStatsMap.keySet(),
                false
        ).forEach(vs -> viewStatsMap.put(vs.getUri(), vs));

        res.forEach(e -> {
            String eventUrl = "/events/" + e.getId();
            ViewStats viewStats = viewStatsMap.get(eventUrl);

            if (viewStats != null) {
                e.setViews(viewStats.getHits());
            } else {
                e.setViews(0);
            }
        });

        return res;
    }

    @Override
    public EventFullDto getEventById(long id) {
        return addViewsAndRequestsForEventFullDto(findEventById(id));
    }

    @Override
    public List<EventShortDto> getEventsByInitiatorId(long userId, int from, int size) {
        log.info("Get events for initiator with id:{}, from: {}, size:{}", userId, from, size);

        User initiator = findUserById(userId);

        PageRequest request = PageRequest.of(from, size);

        List<Event> events = eventRepository.findAllByInitiator(initiator, request).toList();

        return addViewsAndRequestsForShortEventDto(events);
    }

    @Override
    public EventFullDto updateEventByInitiatorId(long userId, EventUpdateDto eventUpdateDto) {

        log.info(
                "Update event with id:{} at user with id:{}. Update data: {}",
                eventUpdateDto.getEventId(), userId, eventUpdateDto
        );

        long eventId = eventUpdateDto.getEventId();
        Event event = findEventById(eventId);

        isInitiatorOrException(event, userId);

        return updateEventById(eventId, eventUpdateDto);
    }

    @Override
    public EventFullDto updateEventById(long eventId, EventUpdateDto eventUpdateDto) {
        LocalDateTime now = LocalDateTime.now();

        Event event = findEventById(eventId);

        boolean isValidEventState = event.getState() == EventState.PENDING
                || event.getState() == EventState.CANCELED;


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

        if (eventUpdateDto.getAnnotation() != null) {
            event.setAnnotation(eventUpdateDto.getAnnotation());
        }

        if (eventUpdateDto.getDescription() != null) {
            event.setDescription(eventUpdateDto.getDescription());
        }

        if (eventUpdateDto.getEventDate() != null) {
            boolean isValidEventDate = eventUpdateDto.getEventDate().minusHours(BAN_HOURS_BEFORE_EVENT).isAfter(now);

            if (!isValidEventDate) {
                throw new EventDateInvalidException(BAN_HOURS_BEFORE_EVENT);
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

        return addViewsAndRequestsForEventFullDto(event);
    }

    @Override
    public EventFullDto addEvent(long userId, EventCreateDto eventCreateDto) {
        LocalDateTime now = LocalDateTime.now();
        boolean isValidEventDate = eventCreateDto.getEventDate().minusHours(BAN_HOURS_BEFORE_EVENT).isAfter(now);

        User user = findUserById(userId);

        if (!user.isActivated()) {
            throw new UserNotActivatedException(userId);
        }

        if (!isValidEventDate) {
            throw new EventDateInvalidException(BAN_HOURS_BEFORE_EVENT);
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

        return addViewsAndRequestsForEventFullDto(event);

    }

    @Override
    public EventFullDto getEventCurrentUserById(long userId, long eventId) {
        log.info("Get event with id:{} for current user with id:{}", eventId, userId);
        Event event = findEventById(eventId);

        isInitiatorOrException(event, userId);

        return addViewsAndRequestsForEventFullDto(event);
    }

    @Override
    public EventFullDto cancelEventAddedCurrentUserById(long userId, long eventId) {
        log.info("Cancel event with id:{} added current user with id:{}", eventId, userId);

        Event event = findEventById(eventId);

        isInitiatorOrException(event, userId);

        event.setState(EventState.CANCELED);

        long confirmedRequestCount = getConfirmedRequestsCountForEvent(event);
        EventFullDto fullDto = EventMapper.toEventFullDto(event);
        fullDto.setConfirmedRequests(confirmedRequestCount);

        return addViewsAndRequestsForEventFullDto(event);
    }

    @Override
    public List<RequestFullDto> getRequestsForEventCurrentUserById(long userId, long eventId) {
        Event event = findEventById(eventId);

        isInitiatorOrException(event, userId);

        List<Request> requests = requestRepository.findAllByEvent(event);

        return requests.stream().map(RequestMapper::toRequestFullDto).collect(Collectors.toList());
    }

    @Override
    public RequestFullDto confirmRequestOnEventCurrentUser(long userId, long eventId, long reqId) {
        Event event = findEventById(eventId);
        isInitiatorOrException(event, userId);
        Request request = findRequestById(reqId);

        if (event.getParticipantLimit() == 0 || !event.isRequestModeration()) {
            request.setStatus(RequestStatus.CONFIRMED);
            return RequestMapper.toRequestFullDto(request);
        }

        List<Request> requests = findRequestsByEvent(event);
        int requestCount = requests.size();

        if (event.getParticipantLimit() <= requestCount) {
            throw new ParticipantLimitExceededException(String.format(
                    "Exceeding limit of participants for event with id:%s. Request with id:%s cannot be confirmed",
                    eventId, reqId
            ));
        }

        if (event.getParticipantLimit() <= requestCount + 1) {
            rejectAllPendingRequestByEvent(event);
        }

        request.setStatus(RequestStatus.CONFIRMED);

        return RequestMapper.toRequestFullDto(request);
    }

    private void rejectAllPendingRequestByEvent(Event event) {
        findRequestsByEvent(event)
                .forEach((r) -> {
                    if (r.getStatus().equals(RequestStatus.PENDING)) {
                        r.setStatus(RequestStatus.REJECTED);
                    }
                });
    }

    @Override
    public RequestFullDto rejectRequestOnEventCurrentUser(long userId, long eventId, long reqId) {
        Event event = findEventById(eventId);
        isInitiatorOrException(event, userId);
        Request request = findRequestById(reqId);
        request.setStatus(RequestStatus.REJECTED);

        return RequestMapper.toRequestFullDto(request);
    }

    @Override
    public EventFullDto publishEvent(long eventId) {
        log.info("Publish event with id:{}", eventId);
        Event event = findEventById(eventId);
        event.setState(EventState.PUBLISHED);

        return addViewsAndRequestsForEventFullDto(event);
    }

    @Override
    public EventFullDto rejectEvent(long eventId) {
        log.info("Reject event with id:{}", eventId);
        Event event = findEventById(eventId);
        event.setState(EventState.CANCELED);

        return addViewsAndRequestsForEventFullDto(event);
    }

    @Override
    public List<EventFullDto> searchEvents(
            List<Long> users,
            List<String> states,
            List<Long> categories,
            LocalDateTime rangeStart,
            LocalDateTime rangeEnd,
            Integer from,
            Integer size
    ) {
        PageRequest request = PageRequest.of(from, size);
        Specification<Event> spec = EventSpecs
                .hasInitiationIds(users)
                .and(EventSpecs.hasEventCategory(categories));

        List<Event> events = eventRepository.findAll(spec, request)
                .filter(e -> states.contains(e.getState().getVal())).toList();

        return addViewsAndRequestsForEventFullDto(events);
    }

    private Event findEventById(long id) {
        return eventRepository.findById(id).orElseThrow(() -> new EventNotFoundException(id));
    }

    private User findUserById(long id) {
        return userRepository.findById(id).orElseThrow(() -> new UserNotFoundException(id));
    }

    private List<Request> findRequestsByEvent(Event event) {
        return requestRepository.findAllByEvent(event);
    }

    private Request findRequestById(long reqId) {
        return requestRepository.findById(reqId).orElseThrow(() -> new RequestNotFoundException(reqId));
    }

    private void isInitiatorOrException(Event event, long userId) {
        boolean isInitiator = event.getInitiator().getId().equals(userId);

        if (!isInitiator) {
            throw new UserIsNotInitiatorOfEventException(userId, event.getId());
        }
    }

    private long getConfirmedRequestsCountForEvent(Event event) {
        List<Request> requests = requestRepository.findByEvent(event);
        return requests.stream().filter(r -> r.getStatus().equals(RequestStatus.CONFIRMED)).count();
    }
}
