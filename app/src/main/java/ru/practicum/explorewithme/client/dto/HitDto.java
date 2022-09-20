package ru.practicum.explorewithme.client.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class HitDto {

    private Long id;

    private String app;

    private String uri;

    private String ip;

    private LocalDateTime timeStamp;

}
