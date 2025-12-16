package com.example.ogani.service.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import com.example.ogani.entity.Category;
import com.example.ogani.exception.NotFoundException;
import com.example.ogani.model.request.CreateCategoryRequest;
import com.example.ogani.repository.CategoryRepository;
import com.example.ogani.service.CategoryService;

@Service
public class CategoryServiceImpl implements CategoryService {

    @Autowired
    private CategoryRepository categoryRepository;

    @Override
    public List<Category> findAll() {
        // Lấy danh sách tất cả danh mục, sắp xếp theo ID giảm dần.
        return categoryRepository.findAll(Sort.by("id").descending());
    }

    @Override
    public Category createCategory(CreateCategoryRequest request) {
        // Tạo một danh mục mới.
        Category category = new Category();
        category.setName(request.getName());
        category.setEnable(false); // Mặc định danh mục mới tạo sẽ không được kích hoạt.
        return categoryRepository.save(category);
    }

    @Override
    public Category updateCategory(long id, CreateCategoryRequest request) {
        // Tìm danh mục theo ID, nếu không tìm thấy thì ném ra lỗi.
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Not Found Category With Id: " + id));
        
        // Cập nhật tên danh mục.
        category.setName(request.getName());
        return categoryRepository.save(category);
    }

    @Override
    public void enableCategory(long id) {
        // Tìm danh mục theo ID, nếu không tìm thấy thì ném ra lỗi.
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Not Found Category With Id: " + id));
        
        // Đảo ngược trạng thái kích hoạt (enabled).
        category.setEnable(!category.isEnable());
        categoryRepository.save(category);
    }

    @Override
    public void deleteCategory(long id) {
        // Tìm danh mục theo ID, nếu không tìm thấy thì ném ra lỗi.
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Not Found Category With Id: " + id));
        
        // Xóa danh mục.
        categoryRepository.delete(category);
    }

    @Override
    public List<Category> getListEnabled() {
        // Lấy danh sách các danh mục được kích hoạt.
        return categoryRepository.findAllByEnableTrue();
    }
}
