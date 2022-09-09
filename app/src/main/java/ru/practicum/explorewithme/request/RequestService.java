package ru.practicum.explorewithme.request;

import ru.practicum.explorewithme.request.dto.RequestCreateDto;
import ru.practicum.explorewithme.request.dto.RequestFullDto;

import java.util.List;

public interface RequestService {

    /**
     * Добавление запросов от текущего пользователя на участие в событии
     */
    RequestFullDto addRequestFromCurrentUserForEvent(long userId, RequestCreateDto createDto);

    /**
     * Получение информации о заявках текущего пользователя на участие в чужих событиях
     */
    List<RequestFullDto> getEventRequestsFromCurrentUser(long userId);

    /**
     * Отмена запроса текущего пользователя
     */
    RequestFullDto cancelEventRequestCurrentUser(long userId, long requestId);
}
