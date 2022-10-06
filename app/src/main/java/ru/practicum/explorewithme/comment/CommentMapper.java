package ru.practicum.explorewithme.comment;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import ru.practicum.explorewithme.comment.dto.CommentCreateDto;
import ru.practicum.explorewithme.comment.dto.CommentFullDto;
import ru.practicum.explorewithme.event.Event;
import ru.practicum.explorewithme.event.EventRepository;
import ru.practicum.explorewithme.event.exception.EventNotFoundException;

@Component
@AllArgsConstructor
public class CommentMapper {

    private EventRepository eventRepository;

    public Comment toComment(CommentCreateDto createDto) {
        Event event =  findEventById(createDto.getEventId());

        return Comment.builder()
                .event(event)
                .text(createDto.getText())
                .build();
    }

    public CommentFullDto toCommentFullDto(Comment comment) {
        return CommentFullDto.builder()
                .id(comment.getId())
                .user(comment.getUser().getId())
                .event(comment.getEvent().getId())
                .createdOn(comment.getCreatedOn())
                .text(comment.getText())
                .build();
    }

    private Event findEventById(long eventId) {
        return eventRepository.findById(eventId).orElseThrow(() -> new EventNotFoundException(eventId));
    }

}
