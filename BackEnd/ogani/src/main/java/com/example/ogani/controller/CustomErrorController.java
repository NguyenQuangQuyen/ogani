package com.example.ogani.controller;

import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

@Controller
public class CustomErrorController implements ErrorController {

    private static final String PATH = "/error";
    
    @RequestMapping(value = PATH)
    public void handleError(HttpServletRequest request, HttpServletResponse response) throws IOException {
        // Lấy status code
        Integer statusCode = (Integer) request.getAttribute("jakarta.servlet.error.status_code");
        
        // Nếu là lỗi 404 và URL chứa 'login' hoặc 'oauth', chuyển hướng về trang đăng nhập frontend
        String requestURI = (String) request.getAttribute("jakarta.servlet.error.request_uri");
        System.out.println("Error handling - Status: " + statusCode + ", URI: " + requestURI);
        
        if (statusCode != null && statusCode == 404 && 
           (requestURI != null && (requestURI.contains("login") || requestURI.contains("oauth")))) {
            // Chuyển hướng khi là lỗi liên quan đến login/oauth
            response.sendRedirect("http://localhost:5361/login");
        } else {
            // Trả về API error response cho các lỗi khác
            response.setContentType("application/json");
            response.setStatus(statusCode != null ? statusCode : 500);
            response.getWriter().write("{\"error\": \"" + statusCode + "\", \"message\": \"Error occurred\"}");
        }
    }
} 