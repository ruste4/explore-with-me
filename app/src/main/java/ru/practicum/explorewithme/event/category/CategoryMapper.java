package ru.practicum.explorewithme.event.category;

import ru.practicum.explorewithme.event.category.dto.CategoryCreateDto;
import ru.practicum.explorewithme.event.category.dto.CategoryDto;

public class CategoryMapper {
    public static CategoryDto toCategoryDto(Category category) {
        return new CategoryDto(category.getId(), category.getName());
    }

    public static Category toCategory(CategoryDto categoryDto) {
        return new Category(categoryDto.getId(), categoryDto.getName());
    }

    public static Category toCategory(CategoryCreateDto createDto) {
        return new Category(null, createDto.getName());
    }
}
