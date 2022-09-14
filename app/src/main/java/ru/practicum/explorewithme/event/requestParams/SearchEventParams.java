package ru.practicum.explorewithme.event.requestParams;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class SearchEventParams {
    /**
     * Список id пользователей, чьи события нужно найти
     */
    private long[] users;

    /**
     * Список состояний в которых находятся искомые события
     */
    private String[] states;

    /**
     * Список id категорий в которых будет вестись поиск
     */
    private Long[] categories;

    /**
     * Дата и время не раньше которых должно произойти событие
     */
    private LocalDateTime rangeStart;

    /**
     * Дата и время не позже которых должно произойти событие
     */
    private LocalDateTime rangeEnd;

    /**
     * Количество событий, которые нужно пропустить для формирования текущего набора
     */
    private int from;

    /**
     * Количество событий в наборе
     */
    private int size;
}
