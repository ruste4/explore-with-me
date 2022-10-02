package ru.practicum.explorewithme.event.category.exception;

import ru.practicum.explorewithme.exception.NotFoundException;

public class CategoryNotFoundException extends NotFoundException {
    public CategoryNotFoundException(String reason) {
        super("Category not found", reason);
    }
}
