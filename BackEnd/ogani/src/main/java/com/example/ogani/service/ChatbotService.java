package com.example.ogani.service;

import com.example.ogani.entity.Category;
import com.example.ogani.entity.Product;
import com.example.ogani.model.dto.UserIntent;
import com.example.ogani.model.dto.UserIntent.IntentType;
import com.example.ogani.model.response.ChatbotResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ChatbotService {

    @Autowired
    private IntentRecognitionService intentRecognitionService;

    @Autowired
    private ProductService productService;

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private AiService aiService;

    /**
     * Xử lý câu hỏi từ người dùng với khả năng truy vấn database
     */
    public ChatbotResponse processQuery(String userPrompt) {
        System.out.println("========================================");
        System.out.println("🔍 USER PROMPT: " + userPrompt);
        
        // Bước 1: Nhận dạng intent
        UserIntent intent = intentRecognitionService.recognizeIntent(userPrompt);
        System.out.println("📊 DETECTED INTENT: " + intent.getIntentType());
        System.out.println("🔑 KEYWORD: '" + intent.getKeyword() + "'");

        // Bước 2: Lấy dữ liệu từ database dựa trên intent
        String databaseContext = fetchDatabaseContext(intent);
        
        if (databaseContext != null && !databaseContext.isEmpty()) {
            System.out.println("✅ DATABASE CONTEXT LENGTH: " + databaseContext.length() + " characters");
            System.out.println("📄 CONTEXT PREVIEW (first 200 chars):");
            System.out.println(databaseContext.substring(0, Math.min(200, databaseContext.length())));
        } else {
            System.out.println("⚠️ NO DATABASE CONTEXT!");
        }

        // Bước 3: Gọi AI với context
        String aiResponse;
        if (databaseContext != null && !databaseContext.isEmpty()) {
            aiResponse = aiService.generateContentWithContext(userPrompt, databaseContext);
        } else {
            aiResponse = aiService.generateContent(userPrompt);
        }
        
        System.out.println("🤖 AI RESPONSE: " + aiResponse);
        System.out.println("========================================");

        // Bước 4: Tạo response
        ChatbotResponse response = new ChatbotResponse();
        response.setMessage(aiResponse);
        response.setHasData(databaseContext != null && !databaseContext.isEmpty());
        response.setIntent(intent.getIntentType().toString());

        return response;
    }

    /**
     * Lấy dữ liệu từ database dựa trên intent
     */
    private String fetchDatabaseContext(UserIntent intent) {
        IntentType type = intent.getIntentType();

        switch (type) {
            case PRODUCT_SEARCH:
                return buildProductSearchContext(intent);

            case PRICE_FILTER:
                return buildPriceFilterContext(intent);

            case CATEGORY_QUERY:
                return buildCategoryContext();

            case STOCK_CHECK:
                return buildStockCheckContext(intent);

            case PRODUCT_DETAIL:
                return buildProductDetailContext(intent);

            case NUTRITION_ADVICE:
                return buildNutritionAdviceContext(intent);

            case GENERAL_QUESTION:
            default:
                return null;
        }
    }

    /**
     * Xây dựng context cho product search
     */
    private String buildProductSearchContext(UserIntent intent) {
        String keyword = intent.getKeyword();
        if (keyword == null || keyword.isEmpty()) {
            return buildAllProductsContext();
        }

        List<Product> products;
        
        // Nếu có cả keyword và price range
        if (intent.getMinPrice() != null || intent.getMaxPrice() != null) {
            Integer min = intent.getMinPrice() != null ? intent.getMinPrice() : 0;
            Integer max = intent.getMaxPrice() != null ? intent.getMaxPrice() : Integer.MAX_VALUE;
            products = productService.searchProductByPriceRange(keyword, min, max);
        } else {
            products = productService.searchProduct(keyword);
        }

        if (products.isEmpty()) {
            return "Không tìm thấy sản phẩm nào với từ khóa: " + keyword;
        }

        return formatProductList(products, "Kết quả tìm kiếm cho '" + keyword + "'");
    }

    /**
     * Xây dựng context cho price filter
     */
    private String buildPriceFilterContext(UserIntent intent) {
        Integer min = intent.getMinPrice() != null ? intent.getMinPrice() : 0;
        Integer max = intent.getMaxPrice() != null ? intent.getMaxPrice() : Integer.MAX_VALUE;

        List<Product> products;
        if (intent.getCategoryId() != null) {
            products = productService.getListByPriceRange(intent.getCategoryId(), min, max);
        } else {
            // Lấy tất cả sản phẩm và filter theo giá
            products = productService.getList().stream()
                    .filter(p -> p.getPrice() >= min && p.getPrice() <= max)
                    .collect(Collectors.toList());
        }

        if (products.isEmpty()) {
            return String.format("Không có sản phẩm nào trong khoảng giá %,d - %,d VND", min, max);
        }

        return formatProductList(products, 
            String.format("Sản phẩm trong khoảng giá %,d - %,d VND", min, max));
    }

    /**
     * Xây dựng context cho category query
     */
    private String buildCategoryContext() {
        List<Category> categories = categoryService.getListEnabled();
        
        if (categories.isEmpty()) {
            return "Hiện tại chưa có danh mục sản phẩm nào.";
        }

        StringBuilder context = new StringBuilder("Danh sách các danh mục sản phẩm:\n");
        for (Category category : categories) {
            context.append("- ").append(category.getName()).append("\n");
        }

        return context.toString();
    }

    /**
     * Xây dựng context cho stock check
     */
    private String buildStockCheckContext(UserIntent intent) {
        // Nếu có keyword, tìm sản phẩm theo tên
        if (intent.getKeyword() != null && !intent.getKeyword().isEmpty()) {
            List<Product> products = productService.searchProduct(intent.getKeyword());
            
            if (products.isEmpty()) {
                return "Không tìm thấy sản phẩm '" + intent.getKeyword() + "' trong cửa hàng.";
            }
            
            // Hiển thị stock status của các sản phẩm tìm được
            StringBuilder context = new StringBuilder("Tình trạng tồn kho:\n\n");
            for (Product p : products) {
                String status = p.getQuantity() > 0 ? "Còn hàng (" + p.getQuantity() + " sản phẩm)" : "Hết hàng";
                context.append(String.format("- %s: %s - Giá: %,d VND\n", 
                    p.getName(), status, p.getPrice()));
            }
            return context.toString();
        }
        
        if (intent.getProductId() != null) {
            Product product = productService.getProduct(intent.getProductId());
            String status = product.getQuantity() > 0 ? "còn " + product.getQuantity() + " sản phẩm" : "hết hàng";
            return String.format("Sản phẩm '%s' %s trong kho. Giá: %,d VND", 
                product.getName(), status, product.getPrice());
        }

        // Nếu không có keyword hay productId, trả về tổng quan
        List<Product> products = productService.getList();
        long inStock = products.stream().filter(p -> p.getQuantity() > 0).count();
        long outOfStock = products.stream().filter(p -> p.getQuantity() == 0).count();

        return String.format("Tổng quan kho hàng: %d sản phẩm còn hàng, %d sản phẩm hết hàng.", 
            inStock, outOfStock);
    }

    /**
     * Xây dựng context cho nutrition advice
     */
    private String buildNutritionAdviceContext(UserIntent intent) {
        String goal = intent.getKeyword(); // "giảm cân", "tăng cân", etc
        
        // Lấy tất cả sản phẩm từ categories phù hợp
        List<Product> allProducts = productService.getList();
        
        // Tạo context với danh sách sản phẩm cho AI phân tích
        StringBuilder context = new StringBuilder();
        context.append("MỤC TIÊU: ").append(goal != null ? goal : "cải thiện sức khỏe").append("\n\n");
        context.append("CÁC SẢN PHẨM HIỆN CÓ TRONG CỬA HÀNG:\n\n");
        
        for (int i = 0; i < Math.min(allProducts.size(), 20); i++) {
            Product p = allProducts.get(i);
            context.append(String.format("%d. %s", i + 1, p.getName()));
            if (p.getCategory() != null) {
                context.append(String.format(" (Danh mục: %s)", p.getCategory().getName()));
            }
            context.append(String.format(" - Giá: %,d VND", p.getPrice()));
            context.append(String.format(" - %s\n", 
                p.getQuantity() > 0 ? "Còn hàng" : "Hết hàng"));
        }
        
        if (allProducts.size() > 20) {
            context.append(String.format("\n... và %d sản phẩm khác.\n", allProducts.size() - 20));
        }
        
        return context.toString();
    }

    /**
     * Xây dựng context cho product detail
     */
    private String buildProductDetailContext(UserIntent intent) {
        if (intent.getProductId() == null) {
            return null;
        }

        Product product = productService.getProduct(intent.getProductId());
        return formatProductDetail(product);
    }

    /**
     * Xây dựng context cho tất cả sản phẩm
     */
    private String buildAllProductsContext() {
        List<Product> products = productService.getList(); // Lấy tất cả sản phẩm
        
        System.out.println("📦 buildAllProductsContext() called");
        System.out.println("📊 Total products from database: " + products.size());
        
        if (products.isEmpty()) {
            System.out.println("⚠️ WARNING: Database returned 0 products!");
            return "DATABASE TRỐNG - Không có sản phẩm nào trong cơ sở dữ liệu.";
        }
        
        // Nếu có nhiều sản phẩm, hiển thị 20 items
        int displayCount = Math.min(products.size(), 20);
        
        StringBuilder context = new StringBuilder();
        context.append(String.format("TỔNG SỐ SẢN PHẨM: %d sản phẩm\n\n", products.size()));
        context.append(String.format("DANH SÁCH %d SẢN PHẨM:\n\n", displayCount));
        
        for (int i = 0; i < displayCount; i++) {
            Product p = products.get(i);
            context.append(String.format("%d. %s\n", i + 1, p.getName()));
            context.append(String.format("   - Giá: %,d VND\n", p.getPrice()));
            context.append(String.format("   - Tồn kho: %s\n", 
                p.getQuantity() > 0 ? "Còn hàng (" + p.getQuantity() + ")" : "Hết hàng"));
            if (p.getCategory() != null) {
                context.append(String.format("   - Danh mục: %s\n", p.getCategory().getName()));
            }
            context.append("\n");
        }
        
        if (products.size() > displayCount) {
            context.append(String.format("... và %d sản phẩm khác.\n", products.size() - displayCount));
        }
        
        System.out.println("✅ Context built successfully, length: " + context.length());
        
        return context.toString();
    }

    /**
     * Format danh sách sản phẩm thành text với format rõ ràng
     */
    private String formatProductList(List<Product> products, String title) {
        StringBuilder context = new StringBuilder();
        context.append("╔════════════════════════════════════════\n");
        context.append("║ ").append(title.toUpperCase()).append("\n");
        context.append("╠════════════════════════════════════════\n");
        context.append(String.format("║ Tổng số: %d sản phẩm\n", products.size()));
        context.append("╚════════════════════════════════════════\n\n");
        
        int count = Math.min(products.size(), 10);
        for (int i = 0; i < count; i++) {
            Product p = products.get(i);
            context.append("─────────────────────────────────\n");
            context.append(String.format("SẢN PHẨM #%d\n", i + 1));
            context.append(String.format("├─ TÊN: %s\n", p.getName()));
            context.append(String.format("├─ GIÁ: %,d VND\n", p.getPrice()));
            
            if (p.getQuantity() > 0) {
                context.append(String.format("├─ TÌNH TRẠNG: CÒN HÀNG\n"));
                context.append(String.format("├─ SỐ LƯỢNG TỒN: %d sản phẩm\n", p.getQuantity()));
            } else {
                context.append("├─ TÌNH TRẠNG: HẾT HÀNG\n");
                context.append("├─ SỐ LƯỢNG TỒN: 0\n");
            }
            
            if (p.getCategory() != null) {
                context.append(String.format("└─ DANH MỤC: %s\n", p.getCategory().getName()));
            }
            context.append("\n");
        }

        if (products.size() > 10) {
            context.append(String.format("... VÀ CÒN %d SẢN PHẨM KHÁC NỮA.\n", products.size() - 10));
        }

        return context.toString();
    }

    /**
     * Format chi tiết sản phẩm
     */
    private String formatProductDetail(Product product) {
        StringBuilder context = new StringBuilder();
        context.append("Thông tin chi tiết sản phẩm:\n\n");
        context.append("Tên: ").append(product.getName()).append("\n");
        context.append("Giá: ").append(String.format("%,d VND", product.getPrice())).append("\n");
        context.append("Tồn kho: ").append(product.getQuantity()).append("\n");
        
        if (product.getCategory() != null) {
            context.append("Danh mục: ").append(product.getCategory().getName()).append("\n");
        }
        
        if (product.getDescription() != null && !product.getDescription().isEmpty()) {
            context.append("Mô tả: ").append(product.getDescription()).append("\n");
        }

        return context.toString();
    }
}
