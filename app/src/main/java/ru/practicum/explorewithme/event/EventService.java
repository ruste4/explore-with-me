package ru.practicum.explorewithme.event;

import ru.practicum.explorewithme.event.dto.EventCreateDto;
import ru.practicum.explorewithme.event.dto.EventFullDto;
import ru.practicum.explorewithme.event.dto.EventShortDto;
import ru.practicum.explorewithme.event.dto.EventUpdateDto;
import ru.practicum.explorewithme.event.requestparams.GetEventsParams;
import ru.practicum.explorewithme.event.requestparams.SearchEventParams;

import java.util.List;

public interface EventService {

    /**
     * Получение событий с возможностью фильтрации
     */
    List<EventShortDto> getEvents(GetEventsParams params);

    /**
     * Получение подробной информации о событии по его идентификатору
     */
    EventFullDto getEventById(long id);

    /**
     * Получение событий, добавленных определенным пользователем
     */
    List<EventShortDto> getEventsByInitiatorId(long userId, int from, int size);

    /**
     *  Изменение события добавленного текущим пользователем
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
     */
    void getRequestsForEventCurrentUserById(long userId, long eventId); //todo когда доделаешь функционал с запросами на участие, доработай этот метод с возвращением списка запросов для данного события

    /**
     * Подтверждение чужой заявки на участие в событии текущего пользователя
     */
    void confirmRequestOnEventCurrentUser(long userId, long eventId, long reqId); //todo когда доделаешь функционал с запросами на участие, доработай этот метод с возвращением события для данного события

    /**
     * Отклонение чужой заявки на участие в событии текущего пользователя
     */
    void rejectRequestOnEventCurrentUser(long userId, long eventId, long reqId); //todo когда доделаешь функционал с запросами на участие, доработай этот метод с возвращением события для данного события

    /**
     * Поиск событий
     */
    List<EventFullDto> searchEvents(SearchEventParams params);
}
