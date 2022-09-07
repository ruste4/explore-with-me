package ru.practicum.explorewithme.event.dto;

import lombok.*;
import ru.practicum.explorewithme.event.EventState;

import java.time.LocalDateTime;

@Data
@Builder
public class EventFullDto {

    private long id;

    private String annotation;

    private Category category;

    private LocalDateTime createdOn;

    private String description;

    private LocalDateTime eventDate;

    private User initiator;

    private boolean paid;

    private int participantLimit;

    private LocalDateTime publishedOn;

    private boolean requestModeration;

    private EventState state;

    private String title;

    private int confirmedRequests;

    private int views;

    @Data
    @AllArgsConstructor
    public static class Category {

        private long id;

        private String name;

    }

    @Data
    @AllArgsConstructor
    public static class User {

        private long id;

        private String name;

    }
}
