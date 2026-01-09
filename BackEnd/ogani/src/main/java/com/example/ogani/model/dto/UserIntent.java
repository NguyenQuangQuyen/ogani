package com.example.ogani.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserIntent {
    
    private IntentType intentType;
    private String keyword;
    private Integer minPrice;
    private Integer maxPrice;
    private Long categoryId;
    private Long productId;
    
    public enum IntentType {
        PRODUCT_SEARCH,      // Tìm kiếm sản phẩm theo từ khóa
        PRICE_FILTER,        // Lọc theo khoảng giá
        CATEGORY_QUERY,      // Hỏi về danh mục
        STOCK_CHECK,         // Kiểm tra tồn kho
        PRODUCT_DETAIL,      // Hỏi chi tiết sản phẩm cụ thể
        NUTRITION_ADVICE,    // Tư vấn dinh dưỡng + gợi ý sản phẩm
        GENERAL_QUESTION     // Câu hỏi chung, không liên quan database
    }
}
