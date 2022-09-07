package ru.practicum.explorewithme.event.dto;

import lombok.*;

import java.time.LocalDateTime;

@Data
public class EventShortDto {

    private long id;

    private String annotation;

    private Category category;

    private LocalDateTime eventDate;

    private User initiator;

    private boolean paid;

    private String title;

    private int confirmedRequests;

    private int views;

    @Data
    public static class Category {

        private long id;

        private String name;

    }

    @Data
    public static class User {

        private long id;

        private String name;

    }

}
