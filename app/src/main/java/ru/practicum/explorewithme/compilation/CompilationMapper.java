package ru.practicum.explorewithme.compilation;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.practicum.explorewithme.compilation.dto.CompilationCreateDto;
import ru.practicum.explorewithme.compilation.dto.CompilationDto;
import ru.practicum.explorewithme.request.RequestRepository;

import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class CompilationMapper {

    private final RequestRepository requestRepository;

    public Compilation toCompilation(CompilationCreateDto createDto) {
        return Compilation.builder()
                .pinned(createDto.getPinned())
                .title(createDto.getTitle())
                .build();
    }

    public CompilationDto toCompilationDto(Compilation compilation) {
        List<CompilationDto.Event> events = compilation.getEvents().stream()
                .map(e -> {
                    CompilationDto.Category category = new CompilationDto.Category(
                            e.getCategory().getId(),
                            e.getCategory().getName()
                    );

                    int confirmedRequest = requestRepository.findByEvent(e).size();

                    CompilationDto.User initiator = new CompilationDto.User(
                            e.getInitiator().getId(),
                            e.getInitiator().getName()
                    );

                    return CompilationDto.Event.builder()
                            .id(e.getId())
                            .annotation(e.getAnnotation())
                            .category(category)
                            .confirmedRequests(confirmedRequest)
                            .eventDate(e.getEventDate())
                            .initiator(initiator)
                            .paid(e.isPaid())
                            .title(e.getTitle())
                            .build();
                }).collect(Collectors.toList());


        return CompilationDto.builder()
                .id(compilation.getId())
                .title(compilation.getTitle())
                .pinned(compilation.isPinned())
                .events(events)
                .build();
    }
}
