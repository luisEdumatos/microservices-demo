package br.com.products.api.modules.category.service;

import br.com.products.api.config.exception.SuccessResponse;
import br.com.products.api.config.exception.ValidationException;
import br.com.products.api.modules.category.dto.CategoryRequest;
import br.com.products.api.modules.category.dto.CategoryResponse;
import br.com.products.api.modules.category.model.Category;
import br.com.products.api.modules.category.repository.CategoryRepository;
import br.com.products.api.modules.product.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

import static org.springframework.util.ObjectUtils.isEmpty;

@Service
public class CategoryService {

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private ProductRepository productRepository;

    public List<CategoryResponse> findAll() {
        return categoryRepository
                .findAll()
                .stream()
                .map(CategoryResponse::of)
                .collect(Collectors.toList());
    }

    public CategoryResponse findById(Integer id) {
        Category category = categoryRepository
                .findById(id)
                .orElseThrow(() -> new ValidationException("There's no category for the given ID."));
        return CategoryResponse.of(category);
    }

    public List<CategoryResponse> findByDescription(String description) {
        if (isEmpty(description)) {
            throw new ValidationException("The category description must be informed. ");
        }

        return categoryRepository
                .findByDescriptionIgnoreCaseContaining(description)
                .stream()
                .map(CategoryResponse::of)
                .collect(Collectors.toList());
    }

    public CategoryResponse save(CategoryRequest request) {
        validateCategoryNameInformed(request);
        var category = categoryRepository.save(Category.of(request));
        return CategoryResponse.of(category);
    }

    public CategoryResponse update(CategoryRequest request, Integer id) {
        validateCategoryNameInformed(request);
        findById(id);
        var category = Category.of(request);
        category.setId(id);
        categoryRepository.save(category);
        return CategoryResponse.of(category);
    }

    private void validateCategoryNameInformed(CategoryRequest request) {
        if (isEmpty(request.getDescription())) {
            throw new ValidationException("The category description was not informed.");
        }
    }

    public SuccessResponse delete(Integer id) {
        if (productRepository.existsByCategoryId(id)) {
            throw new ValidationException("You cannot delete this category because it's already defined by a product. ");
        }
        categoryRepository.deleteById(id);
        return SuccessResponse.create("The category was deleted.");
    }

}
