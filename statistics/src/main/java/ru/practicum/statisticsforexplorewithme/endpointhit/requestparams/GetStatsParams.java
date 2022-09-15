package ru.practicum.statisticsforexplorewithme.endpointhit.requestparams;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class GetStatsParams {
    // Таймстемп (количество секунд от эпохи unix) начала диапазона за который нужно выгрузить статистику
    @Builder.Default
    private LocalDateTime start = LocalDateTime.MIN;

    // Таймстемп (количество секунд от эпохи unix) конца диапазона за который нужно выгрузить статистику
    @Builder.Default
    private  LocalDateTime end = LocalDateTime.MAX;

    // Список uri для которых нужно выгрузить статистику
    private List<String> uris;

    // Нужно ли учитывать только уникальные посещения (только с уникальным ip)
    private boolean unique;

}
