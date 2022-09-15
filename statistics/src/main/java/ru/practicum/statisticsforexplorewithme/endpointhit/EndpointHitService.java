package ru.practicum.statisticsforexplorewithme.endpointhit;

import ru.practicum.statisticsforexplorewithme.endpointhit.dto.EndpointHitCreateDto;
import ru.practicum.statisticsforexplorewithme.endpointhit.dto.ViewStats;
import ru.practicum.statisticsforexplorewithme.endpointhit.requestparams.GetStatsParams;

import java.util.List;

public interface EndpointHitService {

    /**
     * Сохранить информацию посещения
     */
    void saveHit(EndpointHitCreateDto createDto);


    /**
     * Получение статистики по посещениям
     */
    List<ViewStats> getStats(GetStatsParams params);

}
