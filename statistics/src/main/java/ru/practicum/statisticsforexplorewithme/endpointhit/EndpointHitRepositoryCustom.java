package ru.practicum.statisticsforexplorewithme.endpointhit;

import ru.practicum.statisticsforexplorewithme.endpointhit.dto.ViewStats;
import ru.practicum.statisticsforexplorewithme.endpointhit.requestparams.GetStatsParams;

import java.util.List;

public interface EndpointHitRepositoryCustom {
    List<Object[]>getStats(GetStatsParams params);
}
