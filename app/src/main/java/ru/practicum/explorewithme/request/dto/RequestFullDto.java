package ru.practicum.explorewithme.request.dto;

import lombok.Builder;
import lombok.Data;
import ru.practicum.explorewithme.request.RequestStatus;

import java.time.LocalDateTime;

@Data
@Builder
public class RequestFullDto {

    private Long id;

    private Long event;

    private Long requester;

    private RequestStatus status;

    private LocalDateTime created;

}
