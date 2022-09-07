package ru.practicum.explorewithme.event.requestParams;

import lombok.Data;
import ru.practicum.explorewithme.event.EventSort;

import java.time.LocalDateTime;

@Data
public class GetEventsParams {
    /**
     *  Текст для поиска в содержимом аннотации и подробном описании события
     */
    private String text;

    /**
     * Список идентификаторов категорий в которых будет вестись поиск
     */
    private long[] categoryIds;

    /**
     * Поиск только платных/бесплатных событий. Если null - не использовать данный фильтр
     */
    private Boolean paid;

    /**
     * Дата и время не раньше которых должно произойти событие
     */
    private LocalDateTime rangeStart;

    /**
     * Дата и время не позже которых должно произойти событие
     */
    private LocalDateTime rangeEnd;

    /**
     * Только события у которых не исчерпан лимит запросов на участие
     */
    private boolean onlyAvailable;

    /**
     * Вариант сортировки: по дате события или по количеству просмотров (EVENT_DATE, VIEWS)
     */
    private EventSort sort;

    /**
     * Количество событий, которые нужно пропустить для формирования текущего набора
     */
    private int from;

    /**
     * Количество событий в наборе
     */
    private int size;

}
