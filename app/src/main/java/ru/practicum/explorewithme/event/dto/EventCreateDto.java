package ru.practicum.explorewithme.event.dto;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.*;
import ru.practicum.explorewithme.jsonserializer.LocalDateTimeDeserializer;
import ru.practicum.explorewithme.jsonserializer.LocalDateTimeSerializer;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

@Data
@Builder
public class EventCreateDto {

    @NotBlank
    private String annotation;

    @NotNull
    private Long category;

    @NotBlank
    private String description;

    @NotNull
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    private LocalDateTime eventDate;

    @NotNull
    private Location location;

    @NotNull
    private Boolean paid;

    @NotNull
    private Long participantLimit;

    @NotNull
    private Boolean requestModeration;

    @NotBlank
    private String title;

    @Data
    @AllArgsConstructor
    public static class Location {

        private int lat;

        private int lon;

    }
}
