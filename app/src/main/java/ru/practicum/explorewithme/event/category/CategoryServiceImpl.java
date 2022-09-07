package ru.practicum.explorewithme.event.category;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.explorewithme.event.category.dto.CategoryCreateDto;
import ru.practicum.explorewithme.event.category.dto.CategoryDto;
import ru.practicum.explorewithme.event.category.exception.CategoryNotFoundException;

import javax.transaction.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;

    @Override
    public CategoryDto add(CategoryCreateDto createDto) {
        Category category = CategoryMapper.toCategory(createDto);
        categoryRepository.save(category);

        return CategoryMapper.toCategoryDto(category);
    }

    @Override
    public CategoryDto findById(long id) {
        Category category = categoryRepository.findById(id).orElseThrow(
                () -> new CategoryNotFoundException(String.format("Category with id:%s not found", id))
        );

        return CategoryMapper.toCategoryDto(category);
    }
}
