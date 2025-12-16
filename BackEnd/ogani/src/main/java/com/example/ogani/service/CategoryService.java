package com.example.ogani.service;

import java.util.List;

import com.example.ogani.entity.Category;
import com.example.ogani.model.request.CreateCategoryRequest;

public interface CategoryService {
    /**
     * Lấy danh sách tất cả danh mục.
     */
    List<Category> findAll();

    /**
     * Lấy danh sách các danh mục được kích hoạt (enabled).
     */
    List<Category> getListEnabled();

    /**
     * Tạo mới một danh mục.
     *
     * @param request Thông tin danh mục cần tạo.
     * @return Danh mục đã được tạo.
     */
    Category createCategory(CreateCategoryRequest request);

    /**
     * Cập nhật thông tin một danh mục.
     *
     * @param id      ID của danh mục cần cập nhật.
     * @param request Thông tin cập nhật.
     * @return Danh mục đã được cập nhật.
     */
    Category updateCategory(long id, CreateCategoryRequest request);

    /**
     * Bật hoặc tắt trạng thái kích hoạt của danh mục.
     *
     * @param id ID của danh mục cần thay đổi trạng thái.
     */
    void enableCategory(long id);

    /**
     * Xóa một danh mục theo ID.
     *
     * @param id ID của danh mục cần xóa.
     */
    void deleteCategory(long id);
}
