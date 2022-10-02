package ru.practicum.explorewithme.compilation.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class CompilationDto {

    private Long id;

    private String title;

    private boolean pinned;

    private List<Event> events;

    @Data
    @Builder
    public static class Event {

        private Long id;

        private String annotation;

        private Category category;

        private Integer confirmedRequests;

        private LocalDateTime eventDate;

        private User initiator;

        private Boolean paid;

        private String title;

        private Integer views;

    }

    @Data
    @AllArgsConstructor
    public static class User {

        private Long id;

        private String name;

    }

    @Data
    @AllArgsConstructor
    public static class Category {

        private Long id;

        private String name;

    }
}
