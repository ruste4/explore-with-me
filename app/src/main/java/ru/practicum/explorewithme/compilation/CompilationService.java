package ru.practicum.explorewithme.compilation;

import ru.practicum.explorewithme.compilation.dto.CompilationCreateDto;
import ru.practicum.explorewithme.compilation.dto.CompilationDto;

import java.util.List;

public interface CompilationService {

    /**
     * Добавление новой подборки
     */
    CompilationDto addCompilation(CompilationCreateDto createDto);

    /**
     * Удаление подборки по id
     */
    void deleteCompilationById(long id);

    /**
     * Удаление события из подборки
     */
    void deleteEventFromCompilation(long compilationId, long eventId);

    /**
     * Добавление события в подборку
     */
    void addEventToCompilation(long compilationIdm, long eventId);

    /**
     * Открепить подборку по id
     */
    void unpinnedCompilationById(long id);

    /**
     * Прикрепить подборку по id
     */
    void pinnedCompilationById(long id);

    /**
     * Получить все подборки
     *
     * @param pinned true - вернет закрепленные подборки,
     *               false - незакрепленные подборки,
     *               null - вернет закрепленные / незакрепленные подборки
     */
    List<CompilationDto> getAll(Boolean pinned, int from, int size);

    /**
     * Получить подборку событий по id
     */
    CompilationDto getById(long id);

}
