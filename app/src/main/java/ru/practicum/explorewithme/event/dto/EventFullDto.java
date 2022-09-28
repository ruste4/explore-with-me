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

    private long participantLimit;

    private LocalDateTime publishedOn;

    private boolean requestModeration;

    private EventState state;

    private String title;

    private long confirmedRequests;

    private int views;

    private Location location;

    @Data
    @AllArgsConstructor
    public static class Location {

        private double lat;

        private double lon;

    }

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
