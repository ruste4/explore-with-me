package ru.practicum.explorewithme.event;

import ru.practicum.explorewithme.event.dto.EventCreateDto;
import ru.practicum.explorewithme.event.dto.EventFullDto;
import ru.practicum.explorewithme.event.dto.EventShortDto;
import ru.practicum.explorewithme.event.dto.EventUpdateDto;
import ru.practicum.explorewithme.event.requestparams.GetEventsParams;
import ru.practicum.explorewithme.request.dto.RequestFullDto;

import java.time.LocalDateTime;
import java.util.List;

public interface EventService {

    /**
     * Получение событий с возможностью фильтрации
     */
    List<EventShortDto> getEvents(
            String text,
            List<Long> categories,
            Boolean paid,
            LocalDateTime rangeStart,
            LocalDateTime rangeEnd,
            Boolean onlyAvailable,
            EventSort sort,
            int from,
            int size
    );

    /**
     * Получение подробной информации о событии по его идентификатору
     */
    EventFullDto getEventById(long id);

    /**
     * Получение событий, добавленных определенным пользователем
     */
    List<EventShortDto> getEventsByInitiatorId(long userId, int from, int size);

    /**
     * Изменение события добавленного текущим пользователем
     */
    EventFullDto updateEventByInitiatorId(long userId, EventUpdateDto eventUpdateDto);

    /**
     * Редактирование события по id
     */
    EventFullDto updateEventById(long eventId, EventUpdateDto eventUpdateDto);

    /**
     * Добавление нового события
     */
    EventFullDto addEvent(long userId, EventCreateDto eventCreateDto);

    /**
     * Получение полной информации о событии добавленном текущим пользователем
     */
    EventFullDto getEventCurrentUserById(long userId, long eventId);

    /**
     * Отмена события добавленного текущим пользователем
     */
    EventFullDto cancelEventAddedCurrentUserById(long userId, long eventId);

    /**
     * Получение информации о запросах на участие в событии текущего пользователя
     *
     * @return
     */
    List<RequestFullDto> getRequestsForEventCurrentUserById(long userId, long eventId);

    /**
     * Подтверждение чужой заявки на участие в событии текущего пользователя
     */
    RequestFullDto confirmRequestOnEventCurrentUser(long userId, long eventId, long reqId);

    /**
     * Отклонение чужой заявки на участие в событии текущего пользователя
     */
    RequestFullDto rejectRequestOnEventCurrentUser(long userId, long eventId, long reqId);

    /**
     * Опубликовать событие
     *
     * @return
     */
    EventFullDto publishEvent(long eventId);

    /**
     * Отклонить событие
     *
     * @return
     */
    EventFullDto rejectEvent(long eventId);

    /**
     * Поиск событий
     */
    List<EventFullDto> searchEvents(
            List<Long> users,
            List<String> states,
            List<Long> categories,
            LocalDateTime rangeStart,
            LocalDateTime rangeEnd,
            Integer from,
            Integer size
    );
}
