package ru.practicum.statisticsforexplorewithme.endpointhit.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class EndpointHitCreateDto {

    private String app;

    private String uri;

    private String ip;

}
