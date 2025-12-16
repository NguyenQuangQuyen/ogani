package com.example.ogani.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.example.ogani.entity.User;
import com.example.ogani.security.jwt.JwtUtils;
import com.example.ogani.service.UserService;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth/oauth2")
public class OAuthSuccessController {
    
    private static final Logger logger = LoggerFactory.getLogger(OAuthSuccessController.class);

    @Autowired
    private UserService userService;
    
    @Autowired
    private JwtUtils jwtUtils;
    
    @GetMapping("/success")
    public ResponseEntity<?> handleOAuthLoginSuccess(HttpServletRequest request) {
        logger.info("OAuth2 login success handler called");
        
        // Lấy thông tin người dùng từ session hoặc security context
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        if (authentication == null || !(authentication.getPrincipal() instanceof OAuth2User)) {
            logger.error("Authentication is null or not an OAuth2User");
            return ResponseEntity.status(401).body("Authentication failed: No OAuth2 user found");
        }
        
        OAuth2User oauth2User = (OAuth2User) authentication.getPrincipal();
        logger.info("OAuth2User attributes: {}", oauth2User.getAttributes());
        String email = oauth2User.getAttribute("email");
        logger.info("User email from OAuth2: {}", email);
        
        try {
            // Lấy user từ database theo email
            User user = userService.getUserByEmail(email);
            if (user == null) {
                logger.error("User not found in database for email: {}", email);
                return ResponseEntity.status(404).body("User not found");
            }
            
            logger.info("User found in database: {}", user.getUsername());
            
            // Tạo JWT token
            String jwt = jwtUtils.generateTokenFromUsername(user.getUsername());
            logger.info("JWT token generated");
            
            // Tạo response object
            Map<String, Object> response = new HashMap<>();
            response.put("token", jwt);
            response.put("user", user);
            response.put("roles", user.getRoles());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error in OAuth2 login success handler", e);
            return ResponseEntity.status(500).body("Error processing OAuth2 login: " + e.getMessage());
        }
    }
    
    @GetMapping("/redirect")
    public void redirectToFrontend(HttpServletResponse response) throws IOException {
        logger.info("Redirecting to frontend login page");
        response.sendRedirect("http://localhost:4200/login");
    }
} 