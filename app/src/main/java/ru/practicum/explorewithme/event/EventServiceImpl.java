package ru.practicum.explorewithme.event;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import ru.practicum.explorewithme.event.category.Category;
import ru.practicum.explorewithme.event.category.CategoryRepository;
import ru.practicum.explorewithme.event.category.exception.CategoryNotFoundException;
import ru.practicum.explorewithme.event.dto.EventCreateDto;
import ru.practicum.explorewithme.event.dto.EventFullDto;
import ru.practicum.explorewithme.event.dto.EventShortDto;
import ru.practicum.explorewithme.event.dto.EventUpdateDto;
import ru.practicum.explorewithme.event.exception.*;
import ru.practicum.explorewithme.event.requestParams.GetEventsParams;
import ru.practicum.explorewithme.event.requestParams.SearchEventParams;
import ru.practicum.explorewithme.user.User;
import ru.practicum.explorewithme.user.UserRepository;
import ru.practicum.explorewithme.user.exception.UserNotFoundException;

import javax.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class EventServiceImpl implements EventService {

    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;

    private final EventRepository eventRepository;

    private static final int BAN_HOURS_BEFORE_EVENT = 2;

    @Override
    public List<EventShortDto> getEvents(GetEventsParams params) {
        return null;
    }

    @Override
    public EventFullDto getEventById(long id) {
        Event event = eventRepository.findById(id).orElseThrow(
                () -> new EventNotFoundException(String.format("Event with id:%s not found", id)));

        return EventMapper.toEventFullDto(event);
    }

    @Override
    public List<EventShortDto> getEventsByInitiatorId(long userId, int from, int size) {
        User initiator = userRepository.findById(userId).orElseThrow(
                () -> new UserNotFoundException(String.format("User with id:%s not found", userId))
        );

        PageRequest pageRequest = PageRequest.of(from, size);

        return eventRepository.findAllByInitiator(initiator, pageRequest).map(EventMapper::toEventShortDto).toList(); //todo в этом месте каждому EventShortDto згенерируй поля confirmedRequests, views
    }

    @Override
    public EventFullDto updateEventByInitiatorId(long userId, EventUpdateDto eventUpdateDto) {
        LocalDateTime now = LocalDateTime.now();

        User initiator = userRepository.findById(userId).orElseThrow(
                () -> new UserNotFoundException(String.format("User with id:%s not found", userId))
        );

        long eventId = eventUpdateDto.getId();

        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new EventNotFoundException(String.format("Event with id:%s not found", eventId)));

        boolean isInitiator = initiator.getId().equals(event.getInitiator().getId());

        boolean isValidEventState = event.getState().equals(EventState.PENDING)
                || event.getState().equals(EventState.CANCELED);

        if (!isInitiator) {
            throw new UserIsNotInitiatorException(String.format(
                    "the user with id:%s is not the initiator of the event with id:%s",
                    initiator.getId(),
                    event.getId()));
        }

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

        return EventMapper.toEventFullDto(event);
    }

    @Override
    public EventFullDto updateEventById(long eventId, EventUpdateDto eventUpdateDto) {
        return null;
    }

    @Override
    public EventFullDto addEvent(long userId, EventCreateDto eventCreateDto) {
        LocalDateTime now = LocalDateTime.now();
        boolean isValidEventDate = eventCreateDto.getEventDate().minusHours(BAN_HOURS_BEFORE_EVENT).isAfter(now);

        User user = userRepository.findById(userId).orElseThrow(
                () -> new UserNotFoundException(String.format("User with id:%s not found", userId))
        );

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

        EventFullDto eventFullDto = EventMapper.toEventFullDto(event);

        //todo посчитай количестов запросов и запиши в eventFullDto

        //todo посчитай количество просмотров из сервера статистики и запиши в eventFullDto

        return eventFullDto;

    }

    @Override
    public EventFullDto getEventCurrentUserById(long userId, long eventId) {
        return null;
    }

    @Override
    public EventFullDto cancelEventAddedCurrentUserById(long userId, long eventId) {
        return null;
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
        return null;
    }
}
