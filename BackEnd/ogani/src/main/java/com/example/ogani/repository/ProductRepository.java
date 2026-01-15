package com.example.ogani.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.ogani.entity.Product;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    // ✅ Lấy danh sách sản phẩm mới nhất (native vì có LIMIT)
    @Query(value = "SELECT * FROM Product ORDER BY id DESC LIMIT :number", nativeQuery = true)
    List<Product> getListNewest(@Param("number") int number);

    // ✅ Lấy danh sách sản phẩm theo giá (native vì có LIMIT)
    @Query(value = "SELECT * FROM Product ORDER BY price LIMIT 8", nativeQuery = true)
    List<Product> getListByPrice();

    // ✅ Sản phẩm liên quan (PostgreSQL dùng RANDOM() thay vì RAND())
    @Query(value = "SELECT * FROM Product WHERE category_id = :id ORDER BY RANDOM() LIMIT 4", nativeQuery = true)
    List<Product> findRelatedProduct(@Param("id") long id);

    // ✅ Lấy tất cả sản phẩm theo danh mục
    @Query("SELECT p FROM Product p WHERE p.category.id = :id")
    List<Product> getListProductByCategory(@Param("id") long id);

    // ✅ Lấy sản phẩm theo khoảng giá trong danh mục
    @Query("SELECT p FROM Product p WHERE p.category.id = :id AND p.price BETWEEN :min AND :max")
    List<Product> getListProductByPriceRange(@Param("id") long id,
                                             @Param("min") int min,
                                             @Param("max") int max);

    // ✅ Tìm kiếm sản phẩm theo tên (không phân biệt hoa thường)
    @Query("SELECT p FROM Product p WHERE LOWER(p.name) LIKE LOWER(CONCAT('%', :keyword, '%')) ORDER BY p.id DESC")
    List<Product> searchProduct(@Param("keyword") String keyword);

    // ✅ Tìm kiếm sản phẩm theo tên + khoảng giá (không phân biệt hoa thường)
    @Query("SELECT p FROM Product p WHERE LOWER(p.name) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
           "AND p.price BETWEEN :min AND :max ORDER BY p.id DESC")
    List<Product> searchProductByPriceRange(@Param("keyword") String keyword,
                                            @Param("min") Integer min,
                                            @Param("max") Integer max);
}
