package com.example.ogani.service;

import com.example.ogani.model.request.AiRequest;
import com.example.ogani.model.response.AiResponse;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.util.List;

@Service
public class AiService {

    @Value("${gemini.api-key}")
    private String apiKey;

    @Value("${gemini.model}")
    private String model;

    private final WebClient webClient;

    public AiService(WebClient.Builder builder) {
        this.webClient = builder.baseUrl("https://generativelanguage.googleapis.com")
                .build();
    }

    public String generateContent(String prompt) {
        try {
            AiRequest request = new AiRequest(
                    List.of(
                            new AiRequest.Content(
                                    List.of(new AiRequest.Part(prompt))
                            )
                    )
            );

            AiResponse response = webClient.post()
                    .uri("/v1beta/models/{model}:generateContent?key={key}",
                            model, apiKey)
                    .bodyValue(request)
                    .retrieve()
                    .bodyToMono(AiResponse.class)
                    .block();

            if (response == null
                    || response.getCandidates() == null
                    || response.getCandidates().isEmpty()) {
                return "Xin lỗi, tôi không thể xử lý câu hỏi này lúc này.";
            }

            return response.getCandidates()
                    .get(0)
                    .getContent()
                    .getParts()
                    .get(0)
                    .getText();
                    
        } catch (WebClientResponseException.Forbidden e) {
            System.err.println("Gemini API 403 Error: " + e.getMessage());
            System.err.println("Check API key and model name: " + model);
            return "Xin lỗi, hệ thống AI đang gặp vấn đề với quyền truy cập. Vui lòng kiểm tra lại cấu hình API key hoặc thử lại sau.";
        } catch (WebClientResponseException e) {
            System.err.println("Gemini API Error (" + e.getStatusCode() + "): " + e.getMessage());
            return "Xin lỗi, hệ thống AI đang gặp sự cố. Vui lòng thử lại sau.";
        } catch (Exception e) {
            System.err.println("Unexpected error calling Gemini API: " + e.getMessage());
            e.printStackTrace();
            return "Xin lỗi, đã xảy ra lỗi không mong muốn. Vui lòng thử lại sau.";
        }
    }

    /**
     * Tạo nội dung AI với context từ database
     * @param userPrompt Câu hỏi của người dùng
     * @param databaseContext Dữ liệu từ database đã được format
     * @return Câu trả lời từ AI
     */
    public String generateContentWithContext(String userPrompt, String databaseContext) {
        // Tạo prompt có cấu trúc với context
        String structuredPrompt = buildStructuredPrompt(userPrompt, databaseContext);
        
        return generateContent(structuredPrompt);
    }

    /**
     * Xây dựng prompt có cấu trúc với database context
     */
    private String buildStructuredPrompt(String userPrompt, String databaseContext) {
        StringBuilder prompt = new StringBuilder();
        
        prompt.append("==================================================\n");
        prompt.append("ROLE: Bạn là Bot của cửa hàng Ogani (thực phẩm hữu cơ)\n");
        prompt.append("==================================================\n\n");
        
        prompt.append("█████ DỮ LIỆU TỪ DATABASE (NGUỒN DUY NHẤT) █████\n");
        prompt.append(databaseContext);
        prompt.append("████████████████ HẾT DỮ LIỆU ████████████████\n\n");
        
        prompt.append("═══════════════════════════════════════════════\n");
        prompt.append("CÂU HỎI KHÁCH HÀNG: " + userPrompt + "\n");
        prompt.append("═══════════════════════════════════════════════\n\n");
        
        prompt.append("╔══════════════ QUY TẮC BẮT BUỘC ══════════════╗\n");
        prompt.append("║                                               ║\n");
        prompt.append("║  ⚠️  CẢNH BÁO QUAN TRỌNG:                    ║\n");
        prompt.append("║  • CHỈ được dùng dữ liệu ở phần DATABASE      ║\n");
        prompt.append("║  • COPY CHÍNH XÁC tên, giá, số lượng         ║\n");
        prompt.append("║  • KHÔNG ĐƯỢC suy đoán hay bịa thông tin      ║\n");
        prompt.append("║  • KHÔNG được làm tròn số hay thay đổi giá    ║\n");
        prompt.append("║                                               ║\n");
        prompt.append("╚═══════════════════════════════════════════════╝\n\n");
        
        prompt.append("📋 HƯỚNG DẪN TRẢ LỜI:\n\n");
        prompt.append("1️⃣ Nếu hỏi về SẢN PHẨM CỤ THỂ:\n");
        prompt.append("   ✓ Tìm CHÍNH XÁC sản phẩm trong DATABASE\n");
        prompt.append("   ✓ Trả lời: 'Sản phẩm [TÊN CHÍNH XÁC] [TÌNH TRẠNG]. Giá: [GIÁ CHÍNH XÁC] VND'\n");
        prompt.append("   ✗ VÍ DỤ SAI: 'khoảng 50k', 'có thể hết hàng'\n");
        prompt.append("   ✓ VÍ DỤ ĐÚNG: 'Táo Fuji CÒN HÀNG (50 sản phẩm). Giá: 45,000 VND'\n\n");
        
        prompt.append("2️⃣ Nếu KHÔNG TÌM THẤY trong database:\n");
        prompt.append("   ✓ Nói rõ: 'Hiện tại cửa hàng không có sản phẩm này'\n");
        prompt.append("   ✓ Có thể gợi ý sản phẩm TRONG DATABASE\n\n");
        
        prompt.append("3️⃣ Nếu hỏi TƯ VẤN DINH DƯỠNG:\n");
        prompt.append("   ✓ Giải thích ngắn gọn\n");
        prompt.append("   ✓ Gợi ý sản phẩm CHÍNH XÁC từ DATABASE\n");
        prompt.append("   ✓ Nêu TÊN + GIÁ + TÌNH TRẠNG của sản phẩm gợi ý\n\n");
        
        prompt.append("4️⃣ FORMAT GIÁ:\n");
        prompt.append("   ✓ Viết CHÍNH XÁC như trong database (ví dụ: 45,000 VND)\n");
        prompt.append("   ✗ KHÔNG viết 'khoảng', 'tầm', '~'\n\n");
        
        prompt.append("5️⃣ TÌNH TRẠNG:\n");
        prompt.append("   ✓ Nếu database ghi 'CÒN HÀNG' → nói 'CÒN HÀNG'\n");
        prompt.append("   ✓ Nếu database ghi 'HẾT HÀNG' → nói 'HẾT HÀNG'\n");
        prompt.append("   ✗ KHÔNG nói 'có thể', 'nên', 'chắc'\n\n");
        
        prompt.append("6️⃣ GIỌNG ĐIỆU:\n");
        prompt.append("   • Thân thiện, ngắn gọn\n");
        prompt.append("   • Chỉ trả lời điều được hỏi\n");
        prompt.append("   • Tiếng Việt\n\n");
        
        prompt.append("════════════════════════════════════════════════\n");
        prompt.append("👉 TRẢI LỜI CỦA BẠN (theo đúng quy tắc trên):\n");
        prompt.append("════════════════════════════════════════════════\n");
        
        return prompt.toString();
    }
}

