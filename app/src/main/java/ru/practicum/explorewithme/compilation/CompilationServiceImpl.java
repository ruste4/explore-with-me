package ru.practicum.explorewithme.compilation;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.explorewithme.compilation.dto.CompilationCreateDto;
import ru.practicum.explorewithme.compilation.dto.CompilationDto;
import ru.practicum.explorewithme.compilation.exception.CompilationNotFoundException;
import ru.practicum.explorewithme.compilation.exception.EventAlreadyExistAtCompilationException;
import ru.practicum.explorewithme.event.Event;
import ru.practicum.explorewithme.event.EventRepository;
import ru.practicum.explorewithme.event.exception.EventAtCompilationNotFoundException;
import ru.practicum.explorewithme.event.exception.EventNotFoundException;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class CompilationServiceImpl implements CompilationService {

    private final CompilationRepository compilationRepository;

    private final EventRepository eventRepository;

    private final CompilationMapper mapper;

    @Override
    public CompilationDto addCompilation(CompilationCreateDto createDto) {
        Compilation compilation = mapper.toCompilation(createDto);
        List<Event> events = findEventByIds(createDto.getEvents());

        compilation.setEvents(events);
        compilationRepository.save(compilation);

        return mapper.toCompilationDto(compilation);
    }

    @Override
    public void deleteCompilationById(long id) {
        compilationRepository.deleteById(id);
    }

    @Override
    public void deleteEventFromCompilation(long compilationId, long eventId) {
        Compilation compilation = findCompilationById(compilationId);
        Event event = findEventById(eventId);

        List<Event> events = compilation.getEvents();
        if (events.contains(event)) {
            events.remove(event);
        } else {
            throw new EventAtCompilationNotFoundException(eventId, compilationId);
        }
    }

    @Override
    public void addEventToCompilation(long compilationId, long eventId) {
        Compilation compilation = findCompilationById(compilationId);
        Event event = findEventById(eventId);

        List<Event> events = compilation.getEvents();
        if (events.contains(event)) {
            throw new EventAlreadyExistAtCompilationException(eventId, compilationId);
        } else {
            events.add(event);
        }
    }

    @Override
    public void unpinnedCompilationById(long id) {
        Compilation compilation = findCompilationById(id);
        compilation.setPinned(false);
    }

    @Override
    public void pinnedCompilationById(long id) {
        Compilation compilation = findCompilationById(id);
        compilation.setPinned(true);
    }

    @Override
    public List<CompilationDto> getAll(Boolean pinned, int from, int size) {
        List<Compilation> res = compilationRepository.findAll();

        return res.stream().map(mapper::toCompilationDto).collect(Collectors.toList());
    }

    @Override
    public CompilationDto getById(long id) {
        Compilation res = findCompilationById(id);

        return mapper.toCompilationDto(res);
    }

    private Compilation findCompilationById(long id) {
        return compilationRepository.findById(id).orElseThrow(() -> new CompilationNotFoundException(id));
    }

    private Event findEventById(Long id) {
        return eventRepository.findById(id).orElseThrow(() -> new EventNotFoundException(id));
    }

    private List<Event> findEventByIds(List<Long> ids) {
        return eventRepository.findAllById(ids);
    }
}
