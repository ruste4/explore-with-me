package ru.practicum.explorewithme.compilation.exception;

import ru.practicum.explorewithme.exception.NotFoundException;

public class CompilationNotFoundException extends NotFoundException {
    public CompilationNotFoundException(long compilationId) {
        super("Compilation not found", String.format("Compilation with id:%s not found", compilationId));
    }
}
