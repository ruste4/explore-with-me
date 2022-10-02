package ru.practicum.explorewithme.compilation.exception;

import ru.practicum.explorewithme.exception.NotFoundException;

public class CompilationNotFoundException extends NotFoundException {
    public CompilationNotFoundException(String reason) {
        super("Compilation not found", reason);
    }
}
