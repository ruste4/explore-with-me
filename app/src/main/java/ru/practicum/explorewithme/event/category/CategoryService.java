package ru.practicum.explorewithme.event.category;

import ru.practicum.explorewithme.event.category.dto.CategoryCreateDto;
import ru.practicum.explorewithme.event.category.dto.CategoryDto;

import java.util.List;

public interface CategoryService {

    CategoryDto add(CategoryCreateDto createDto);

    CategoryDto updateCategory(CategoryDto categoryDto);

    void deleteCategory(long id);

    List<CategoryDto> getAllCategories(int from, int size);

    CategoryDto getCategoryById(long id);

}
