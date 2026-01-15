package com.example.ogani.service.impl;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import com.example.ogani.entity.Category;
import com.example.ogani.entity.Image;
import com.example.ogani.entity.Product;
import com.example.ogani.exception.NotFoundException;
import com.example.ogani.model.request.CreateProductRequest;
import com.example.ogani.model.request.CreateOrderDetailRequest;
import com.example.ogani.repository.CategoryRepository;
import com.example.ogani.repository.ImageRepository;
import com.example.ogani.repository.ProductRepository;
import com.example.ogani.service.ProductService;

@Service
public class ProductServiceImpl implements ProductService {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private ImageRepository imageRepository;

    @Override
    public List<Product> getList() {
        return productRepository.findAll(Sort.by("id").descending());
    }

    @Override
    public Product getProduct(long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Not Found Product With Id: " + id));
    }

    @Override
    public Product createProduct(CreateProductRequest request) {
        Product product = new Product();
        product.setName(request.getName());
        product.setDescription(request.getDescription());

        // Sửa lỗi: Chuyển đổi Double sang long
        product.setPrice(Math.round(request.getPrice())); // Sử dụng Math.round()

        product.setQuantity(request.getQuantity());
        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new NotFoundException("Not Found Category With Id: " + request.getCategoryId()));
        product.setCategory(category);

        Set<Image> images = new HashSet<>();
        for (long imageId : request.getImageIds()) {
            Image image = imageRepository.findById(imageId)
                    .orElseThrow(() -> new NotFoundException("Not Found Image With Id: " + imageId));
            images.add(image);
        }
        product.setImages(images);
        productRepository.save(product);
        return product;
    }

    @Override
    public Product updateProduct(long id, CreateProductRequest request) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Not Found Product With Id: " + id));
        product.setName(request.getName());
        product.setDescription(request.getDescription());

        // Sửa lỗi: Chuyển đổi Double sang long
        product.setPrice(Math.round(request.getPrice())); // Sử dụng Math.round()

        product.setQuantity(request.getQuantity());
        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new NotFoundException("Not Found Category With Id: " + request.getCategoryId()));
        product.setCategory(category);

        Set<Image> images = new HashSet<>();
        for (long imageId : request.getImageIds()) {
            Image image = imageRepository.findById(imageId)
                    .orElseThrow(() -> new NotFoundException("Not Found Image With Id: " + imageId));
            images.add(image);
        }
        product.setImages(images);
        productRepository.save(product);

        return product;
    }

    @Override
    public void deleteProduct(long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Not Found Product With Id: " + id));

        // Sửa lỗi: Xóa hình ảnh liên kết trước khi xóa sản phẩm
        product.getImages().clear();

        productRepository.delete(product);
    }

    @Override
    public List<Product> getListNewst(int number) {
        return productRepository.getListNewest(number);
    }

    @Override
    public List<Product> getListByPrice() {
        return productRepository.getListByPrice();
    }

    @Override
    public List<Product> findRelatedProduct(long id) {
        return productRepository.findRelatedProduct(id);
    }

    @Override
    public List<Product> getListProductByCategory(long id) {
        return productRepository.getListProductByCategory(id);
    }

    @Override
    public List<Product> getListByPriceRange(long id, int min, int max) {
        return productRepository.getListProductByPriceRange(id, min, max);
    }

    @Override
    public List<Product> searchProduct(String keyword) {
        return productRepository.searchProduct(keyword);
    }

    @Override
    public List<Product> searchProductByPriceRange(String keyword, Integer min, Integer max) {
        return productRepository.searchProductByPriceRange(keyword, min, max);
    }

    // ==================== Stock Management Methods ====================

    @Override
    public void validateStock(List<CreateOrderDetailRequest> orderDetails) {
        if (orderDetails == null || orderDetails.isEmpty()) {
            return;
        }
        
        for (CreateOrderDetailRequest item : orderDetails) {
            if (item.getProductId() != null) {
                Product product = productRepository.findById(item.getProductId())
                    .orElseThrow(() -> new NotFoundException("Không tìm thấy sản phẩm với ID: " + item.getProductId()));
                
                if (product.getQuantity() < item.getQuantity()) {
                    throw new IllegalArgumentException(
                        "Số lượng sản phẩm \"" + product.getName() + "\" không đủ. " +
                        "Còn lại: " + product.getQuantity() + ", Yêu cầu: " + item.getQuantity()
                    );
                }
            }
        }
    }

    @Override
    public void deductStock(List<CreateOrderDetailRequest> orderDetails) {
        if (orderDetails == null || orderDetails.isEmpty()) {
            return;
        }
        
        for (CreateOrderDetailRequest item : orderDetails) {
            if (item.getProductId() != null && item.getQuantity() != null) {
                Product product = productRepository.findById(item.getProductId())
                    .orElseThrow(() -> new NotFoundException("Không tìm thấy sản phẩm với ID: " + item.getProductId()));
                
                int newQuantity = product.getQuantity() - item.getQuantity();
                if (newQuantity < 0) {
                    throw new IllegalArgumentException(
                        "Không đủ tồn kho cho sản phẩm: " + product.getName()
                    );
                }
                product.setQuantity(newQuantity);
                productRepository.save(product);
            }
        }
    }

    @Override
    public void restoreStock(Long productId, int quantity) {
        if (productId == null) {
            return;
        }
        
        Product product = productRepository.findById(productId)
            .orElseThrow(() -> new NotFoundException("Không tìm thấy sản phẩm với ID: " + productId));
        
        product.setQuantity(product.getQuantity() + quantity);
        productRepository.save(product);
    }

    @Override
    public int getAvailableStock(long productId) {
        Product product = productRepository.findById(productId)
            .orElseThrow(() -> new NotFoundException("Không tìm thấy sản phẩm với ID: " + productId));
        return product.getQuantity();
    }
}
