package ru.practicum.explorewithme.event;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.explorewithme.event.category.CategoryMapper;
import ru.practicum.explorewithme.event.category.CategoryService;
import ru.practicum.explorewithme.event.dto.EventCreateDto;
import ru.practicum.explorewithme.event.dto.EventFullDto;
import ru.practicum.explorewithme.event.dto.EventShortDto;
import ru.practicum.explorewithme.event.dto.EventUpdateDto;
import ru.practicum.explorewithme.event.exception.EventDateInvalidException;
import ru.practicum.explorewithme.event.exception.UserInActivatedException;
import ru.practicum.explorewithme.event.requestParams.GetEventsParams;
import ru.practicum.explorewithme.event.requestParams.SearchEventParams;
import ru.practicum.explorewithme.user.UserMapper;
import ru.practicum.explorewithme.user.UserService;
import ru.practicum.explorewithme.user.dto.UserFullDto;

import javax.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class EventServiceImpl implements EventService {

    private final UserService userService;
    private final CategoryService categoryService;

    private final EventRepository eventRepository;

    private static final int BAN_HOURS_BEFORE_EVENT = 2;

    @Override
    public List<EventShortDto> getEvents(GetEventsParams params) {
        return null;
    }

    @Override
    public EventFullDto getEventById(long id) {
        return null;
    }

    @Override
    public List<EventShortDto> getEventsByInitiatorId(long userId, int from, int size) {
        return null;
    }

    @Override
    public EventFullDto updateEventByInitiatorId(long userId, EventUpdateDto eventUpdateDto) {
        return null;
    }

    @Override
    public EventFullDto updateEventById(long eventId, EventUpdateDto eventUpdateDto) {
        return null;
    }

    @Override
    public EventFullDto addEvent(long userId, EventCreateDto eventCreateDto) {
        LocalDateTime now = LocalDateTime.now();
        boolean isValidEventDate = eventCreateDto.getEventDate().minusHours(BAN_HOURS_BEFORE_EVENT).isAfter(now);

        UserFullDto user = userService.findUserById(userId);

        if (!user.isActivated()) {
            throw new UserInActivatedException(String.format("User with id:%s is not activated", userId));
        }

        if (!isValidEventDate) {
            throw new EventDateInvalidException(
                    String.format(
                            "It is forbidden to create events no earlier than %s hours before the event",
                            BAN_HOURS_BEFORE_EVENT
                    )
            );
        }

        Event event = EventMapper.toEvent(eventCreateDto);

        event.setCategory(CategoryMapper.toCategory(categoryService.findById(eventCreateDto.getCategory())));

        event.setCreatedOn(now);

        event.setInitiator(UserMapper.toUser(user));

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
