package ru.practicum.explorewithme.event.dto;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDateTime;

@Getter
@Setter
@ToString
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class EventUpdateDto {
    @EqualsAndHashCode.Include
    private long id;

    private Long category;

    private String description;

    private LocalDateTime eventDate;

    private Boolean paid;

    private Integer participantLimit;

    private String title;

}
