package com.example.ogani.service;

import java.util.List;

import com.example.ogani.entity.Product;
import com.example.ogani.model.request.CreateProductRequest;

public interface ProductService {
    
    List<Product> getList();

    List<Product> getListNewst(int number);

    List<Product> getListByPrice();

    List<Product> findRelatedProduct(long id);

    List<Product> getListProductByCategory(long id);

    List<Product> getListByPriceRange(long id,int min, int max);

    List<Product> searchProduct(String keyword);

    List<Product> searchProductByPriceRange(String keyword, Integer min, Integer max);

    Product getProduct(long id);

    Product createProduct(CreateProductRequest request);

    Product updateProduct(long id, CreateProductRequest request);

    void deleteProduct(long id);

}
