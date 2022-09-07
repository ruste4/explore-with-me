package ru.practicum.explorewithme.event;

import ru.practicum.explorewithme.event.dto.EventCreateDto;
import ru.practicum.explorewithme.event.dto.EventFullDto;

public class EventMapper {
    public static Event toEvent(EventCreateDto createDto) {
        return Event.builder()
                .annotation(createDto.getAnnotation())
                .description(createDto.getDescription())
                .eventDate(createDto.getEventDate())
                .paid(createDto.getPaid())
                .participantLimit(createDto.getParticipantLimit())
                .requestModeration(createDto.getRequestModeration())
                .title(createDto.getTitle())
                .build();
    }

    public static EventFullDto toEventFullDto(Event event) {
        EventFullDto.Category category = new EventFullDto.Category(
                event.getCategory().getId(),
                event.getCategory().getName()
        );

        EventFullDto.User initiator = new EventFullDto.User(
                event.getInitiator().getId(),
                event.getInitiator().getName()
        );


        return EventFullDto.builder()
                .id(event.getId())
                .annotation(event.getAnnotation())
                .category(category)
                .createdOn(event.getCreatedOn())
                .description(event.getDescription())
                .eventDate(event.getEventDate())
                .initiator(initiator)
                .paid(event.isPaid())
                .participantLimit(event.getParticipantLimit())
                .publishedOn(event.getPublishedOn())
                .requestModeration(event.isRequestModeration())
                .state(event.getState())
                .title(event.getTitle())
                .build();
    }
}
