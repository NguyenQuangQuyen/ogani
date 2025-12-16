package com.example.ogani.security.service;

import java.util.ArrayList;
import java.util.Collection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import com.example.ogani.entity.User;
import com.example.ogani.service.UserService;

@Service
public class OAuth2UserServiceImpl extends DefaultOAuth2UserService {

    private final UserService userService;
    private static final Logger logger = LoggerFactory.getLogger(OAuth2UserServiceImpl.class);

    @Autowired
    public OAuth2UserServiceImpl(UserService userService) {
        this.userService = userService;
    }

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        // Gọi phương thức loadUser của lớp cha để lấy thông tin OAuth2User
        OAuth2User oAuth2User = super.loadUser(userRequest);
        
        try {
            logger.info("Processing OAuth2 user: {}", oAuth2User.getAttributes());
            
            // Đăng ký hoặc cập nhật thông tin người dùng
            userService.registerOrUpdateUser(oAuth2User);
            
            // Tạo danh sách quyền
            Collection<GrantedAuthority> authorities = new ArrayList<>();
            authorities.add(new SimpleGrantedAuthority("ROLE_USER"));
            
            // Trả về DefaultOAuth2User với thông tin đã lấy được
            return new DefaultOAuth2User(
                authorities,
                oAuth2User.getAttributes(),
                userRequest.getClientRegistration().getProviderDetails().getUserInfoEndpoint().getUserNameAttributeName()
            );
        } catch (Exception e) {
            logger.error("Error processing OAuth2 user", e);
            throw new OAuth2AuthenticationException(e.getMessage());
        }
    }
} 