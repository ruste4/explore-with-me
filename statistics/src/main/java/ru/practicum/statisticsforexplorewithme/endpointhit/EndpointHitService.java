package ru.practicum.statisticsforexplorewithme.endpointhit;

import org.springframework.web.bind.annotation.RequestParam;
import ru.practicum.statisticsforexplorewithme.endpointhit.dto.EndpointHitCreateDto;
import ru.practicum.statisticsforexplorewithme.endpointhit.dto.EndpointHitDto;
import ru.practicum.statisticsforexplorewithme.endpointhit.dto.ViewStats;
import ru.practicum.statisticsforexplorewithme.endpointhit.requestparams.GetStatsParams;

import java.time.LocalDateTime;
import java.util.List;

public interface EndpointHitService {

    /**
     * Сохранить информацию посещения
     */
    EndpointHitDto saveHit(EndpointHitCreateDto createDto);


    /**
     * Получение статистики по посещениям
     */
    List<ViewStats> getStats(LocalDateTime start, LocalDateTime end, List<String> uris, boolean unique);

}
