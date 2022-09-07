package ru.practicum.explorewithme.event.category;

import ru.practicum.explorewithme.event.category.dto.CategoryCreateDto;
import ru.practicum.explorewithme.event.category.dto.CategoryDto;

public interface CategoryService {
    CategoryDto add(CategoryCreateDto createDto);
    CategoryDto findById(long id);
}
