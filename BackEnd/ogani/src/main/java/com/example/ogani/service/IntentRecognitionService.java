package com.example.ogani.service;

import com.example.ogani.model.dto.UserIntent;
import com.example.ogani.model.dto.UserIntent.IntentType;
import org.springframework.stereotype.Service;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class IntentRecognitionService {

    /**
     * Phân tích câu hỏi của người dùng để nhận dạng intent và trích xuất parameters
     * Sử dụng priority-based detection để ưu tiên các intent cụ thể
     */
    public UserIntent recognizeIntent(String userPrompt) {
        UserIntent intent = new UserIntent();
        String lowerPrompt = userPrompt.toLowerCase().trim();

        // PRIORITY 1: Stock Check - kiểm tra trước vì quan trọng và cụ thể
        if (containsAny(lowerPrompt, "còn hàng", "tồn kho", "stock", "còn không", "hết hàng", "còn", "có bán không")) {
            intent.setIntentType(IntentType.STOCK_CHECK);
            // Trích xuất sản phẩm được hỏi nếu có
            String keyword = extractSearchKeyword(userPrompt, lowerPrompt);
            if (!keyword.isEmpty()) {
                intent.setKeyword(keyword);
            }
            return intent; // Return ngay để không bị override
        }

        // PRIORITY 2: Nutrition Advice - tư vấn dinh dưỡng
        if (containsAny(lowerPrompt, "giảm cân", "tăng cân", "ăn kiêng", "diet", "dinh dưỡng", 
                "sức khỏe", "healthy", "tốt cho", "nên ăn", "không nên ăn", "nutrition")) {
            intent.setIntentType(IntentType.NUTRITION_ADVICE);
            // Trích xuất mục tiêu (giảm cân, tăng cân, etc)
            if (containsAny(lowerPrompt, "giảm cân", "weight loss", "lose weight")) {
                intent.setKeyword("giảm cân");
            } else if (containsAny(lowerPrompt, "tăng cân", "weight gain", "gain weight")) {
                intent.setKeyword("tăng cân");
            } else if (containsAny(lowerPrompt, "ăn kiêng", "diet", "healthy")) {
                intent.setKeyword("ăn kiêng");
            }
            return intent;
        }

        // PRIORITY 3: Category Query
        if (containsAny(lowerPrompt, "danh mục", "loại", "phân loại", "category", "các loại")) {
            intent.setIntentType(IntentType.CATEGORY_QUERY);
            return intent;
        }

        // PRIORITY 4: Price Filter - kiểm tra khoảng giá
        PriceRange priceRange = extractPriceRange(lowerPrompt);
        if (priceRange.hasPrice) {
            intent.setIntentType(IntentType.PRICE_FILTER);
            intent.setMinPrice(priceRange.min);
            intent.setMaxPrice(priceRange.max);
            // Vẫn cố trích xuất keyword nếu có kết hợp
            String keyword = extractSearchKeyword(userPrompt, lowerPrompt);
            if (!keyword.isEmpty()) {
                intent.setIntentType(IntentType.PRODUCT_SEARCH); // Chuyển sang product search với price filter
                intent.setKeyword(keyword);
            }
            return intent;
        }

        // PRIORITY 5: Product Search - mặc định cho các câu hỏi về sản phẩm
        // Bao gồm cả câu hỏi chung như "có những sản phẩm nào", "bán gì", "shop có gì"
        if (containsAny(lowerPrompt, "tìm", "tìm kiếm", "search", "có ", "bán", "sản phẩm", "mua",
                "có gì", "có những", "bán gì", "những gì", "danh sách", "shop", "cửa hàng")) {
            intent.setIntentType(IntentType.PRODUCT_SEARCH);
            String keyword = extractSearchKeyword(userPrompt, lowerPrompt);
            
            // Debug log
            System.out.println("🎯 PRODUCT_SEARCH detected, keyword: '" + keyword + "'");
            
            intent.setKeyword(keyword); // keyword có thể rỗng nếu là câu hỏi chung
            return intent;
        }

        // DEFAULT: General Question
        intent.setIntentType(IntentType.GENERAL_QUESTION);
        return intent;
    }

    /**
     * Trích xuất từ khóa tìm kiếm từ câu hỏi
     */
    private String extractSearchKeyword(String originalPrompt, String lowerPrompt) {
        String keyword = "";
        
        // Kiểm tra xem có phải câu hỏi general listing không
        // "shop có gì", "bán những gì", "có những sản phẩm nào"
        if (isGeneralListingQuery(lowerPrompt)) {
            System.out.println("🔍 Detected GENERAL LISTING query - no specific keyword");
            return ""; // Trả về empty để gọi buildAllProductsContext
        }
        
        // Tìm pattern: "táo", "có táo", "sản phẩm táo"
        String[] patterns = {
            "(?:tìm|tìm kiếm|search)\\s+(?:sản phẩm)?\\s*(.+)",
            "(?:sản phẩm|có)\\s+(.+?)\\s*(?:còn|không|nào|\\?|$)",
            "(.+?)\\s*(?:còn hàng|còn không|có bán không|có không|giá bao nhiêu)"
        };

        for (String pattern : patterns) {
            Pattern p = Pattern.compile(pattern, Pattern.CASE_INSENSITIVE);
            Matcher m = p.matcher(lowerPrompt);
            if (m.find() && m.groupCount() > 0) {
                keyword = m.group(1).trim();
                
                // Loại bỏ stop words và từ không cần thiết
                keyword = removeStopWords(keyword);
                
                if (!keyword.isEmpty()) {
                    System.out.println("🔑 Extracted keyword: '" + keyword + "'");
                    return keyword;
                }
            }
        }
        
        System.out.println("🔍 No specific keyword found");
        return "";
    }
    
    /**
     * Kiểm tra xem có phải câu hỏi general listing không
     */
    private boolean isGeneralListingQuery(String lowerPrompt) {
        // Patterns cho câu hỏi chung về danh sách sản phẩm
        String[] generalPatterns = {
            "có những.*nào",
            "có những.*gì",
            "có.*gì",
            "bán.*gì",
            "bán những.*nào",
            "shop.*có.*gì",
            "cửa hàng.*có.*gì",
            "danh sách.*sản phẩm",
            "những.*sản phẩm.*nào"
        };
        
        for (String pattern : generalPatterns) {
            if (lowerPrompt.matches(".*" + pattern + ".*")) {
                return true;
            }
        }
        
        return false;
    }
    
    /**
     * Loại bỏ stop words khỏi keyword
     */
    private String removeStopWords(String keyword) {
        // Danh sách stop words cần loại bỏ
        String[] stopWords = {
            "không", "nào", "được", "chưa", "giá", "từ", "đến", "đồng", "vnd",
            "sản phẩm", "có", "còn", "vậy", "thế", "ạ", "à", "nhỉ", "nhé",
            "và", "bán", "những", "nữa", "của", "trong", "ở", "tại", "cho",
            "đang", "đã", "sẽ", "phải", "với", "về", "bao nhiêu", "nhiêu"
        };
        
        String cleaned = keyword;
        for (String stopWord : stopWords) {
            // Loại bỏ stop word ở đầu, cuối, và giữa
            cleaned = cleaned.replaceAll("(?i)\\b" + stopWord + "\\b", " ");
        }
        
        cleaned = cleaned.trim().replaceAll("\\s+", " ");
        
        return cleaned;
    }

    /**
     * Trích xuất khoảng giá từ câu hỏi
     */
    private PriceRange extractPriceRange(String lowerPrompt) {
        PriceRange range = new PriceRange();

        // Pattern: "từ X đến Y", "X - Y", "dưới X", "trên X", "khoảng X"
        // Số có thể có dấu phẩy, chấm hoặc k/K (nghìn)
        
        // Pattern: "từ X đến Y" hoặc "X đến Y"
        Pattern rangePattern = Pattern.compile("(?:từ|from)?\\s*(\\d+(?:[.,]\\d+)?[kK]?)\\s*(?:đến|to|-|->)\\s*(\\d+(?:[.,]\\d+)?[kK]?)");
        Matcher rangeMatcher = rangePattern.matcher(lowerPrompt);
        if (rangeMatcher.find()) {
            range.min = parsePrice(rangeMatcher.group(1));
            range.max = parsePrice(rangeMatcher.group(2));
            range.hasPrice = true;
            return range;
        }

        // Pattern: "dưới X", "dưới X đồng"
        Pattern underPattern = Pattern.compile("(?:dưới|under|<)\\s*(\\d+(?:[.,]\\d+)?[kK]?)");
        Matcher underMatcher = underPattern.matcher(lowerPrompt);
        if (underMatcher.find()) {
            range.max = parsePrice(underMatcher.group(1));
            range.min = 0;
            range.hasPrice = true;
            return range;
        }

        // Pattern: "trên X", "trên X đồng"
        Pattern overPattern = Pattern.compile("(?:trên|over|>)\\s*(\\d+(?:[.,]\\d+)?[kK]?)");
        Matcher overMatcher = overPattern.matcher(lowerPrompt);
        if (overMatcher.find()) {
            range.min = parsePrice(overMatcher.group(1));
            range.max = Integer.MAX_VALUE;
            range.hasPrice = true;
            return range;
        }

        // Pattern: "khoảng X", "tầm X"
        Pattern approxPattern = Pattern.compile("(?:khoảng|tầm|around)\\s*(\\d+(?:[.,]\\d+)?[kK]?)");
        Matcher approxMatcher = approxPattern.matcher(lowerPrompt);
        if (approxMatcher.find()) {
            int price = parsePrice(approxMatcher.group(1));
            range.min = (int) (price * 0.8); // ±20%
            range.max = (int) (price * 1.2);
            range.hasPrice = true;
            return range;
        }

        return range;
    }

    /**
     * Parse chuỗi giá thành số nguyên
     */
    private int parsePrice(String priceStr) {
        // Loại bỏ dấu phẩy, chấm
        priceStr = priceStr.replaceAll("[,.]", "");
        
        // Xử lý k/K (nghìn)
        if (priceStr.toLowerCase().endsWith("k")) {
            priceStr = priceStr.substring(0, priceStr.length() - 1);
            return Integer.parseInt(priceStr) * 1000;
        }
        
        return Integer.parseInt(priceStr);
    }

    /**
     * Kiểm tra chuỗi có chứa bất kỳ keyword nào
     */
    private boolean containsAny(String text, String... keywords) {
        for (String keyword : keywords) {
            if (text.contains(keyword)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Helper class cho khoảng giá
     */
    private static class PriceRange {
        Integer min;
        Integer max;
        boolean hasPrice = false;
    }
}
