package ru.practicum.explorewithme.event.category;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import ru.practicum.explorewithme.event.category.dto.CategoryCreateDto;
import ru.practicum.explorewithme.event.category.dto.CategoryDto;
import ru.practicum.explorewithme.event.category.exception.CategoryNotFoundException;

import javax.transaction.Transactional;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;

    @Override
    public CategoryDto add(CategoryCreateDto createDto) {
        log.info("Add category: {}", createDto.getName());
        Category category = CategoryMapper.toCategory(createDto);
        categoryRepository.save(category);
        log.info("Category with name: {} added, assigned id:{}", category.getName(), category.getId());

        return CategoryMapper.toCategoryDto(category);
    }

    @Override
    public CategoryDto updateCategory(CategoryDto categoryDto) {
        log.info("Update category with id:{}. Updated data: {}", categoryDto.getId(), categoryDto);

        Category category = findCategoryById(categoryDto.getId());

        category.setName(categoryDto.getName());

        return CategoryMapper.toCategoryDto(category);
    }

    @Override
    public void deleteCategory(long id) {
        log.info("Delete category by id:{}", id);

        categoryRepository.deleteById(id);
    }

    @Override
    public List<CategoryDto> getAllCategories(int from, int size) {
        log.info("Get all categories. From: {}, size: {}.", from, size);

        PageRequest pageRequest = PageRequest.of(from, size);

        List<Category> categories = categoryRepository.findAll(pageRequest).toList();

        return categories.stream().map(CategoryMapper::toCategoryDto).collect(Collectors.toList());
    }

    @Override
    public CategoryDto getCategoryById(long id) {
        log.info("Get category by id:{}", id);
        Category category = findCategoryById(id);

        return CategoryMapper.toCategoryDto(category);
    }

    private Category findCategoryById(long id) {
        return categoryRepository.findById(id).orElseThrow(
                () -> new CategoryNotFoundException(String.format("Category with id:%s not found", id))
        );
    }
}
