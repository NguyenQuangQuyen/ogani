package com.example.ogani.service;

import java.util.List;

import com.example.ogani.entity.Product;
import com.example.ogani.model.request.CreateProductRequest;
import com.example.ogani.model.request.CreateOrderDetailRequest;

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

    // Stock management methods
    void validateStock(List<CreateOrderDetailRequest> orderDetails);
    
    void deductStock(List<CreateOrderDetailRequest> orderDetails);
    
    void restoreStock(Long productId, int quantity);
    
    int getAvailableStock(long productId);

}
