package com.example.ogani.service;

import java.util.List;

import com.example.ogani.entity.User;
import com.example.ogani.model.request.ChangePasswordRequest;
import com.example.ogani.model.request.CreateUserRequest;
import com.example.ogani.model.request.UpdateProfileRequest;
import org.springframework.security.oauth2.core.user.OAuth2User;

public interface UserService {
    
    void register(CreateUserRequest request);

    User getUserByUsername(String username);
    
    User getUserByEmail(String email);

    User updateUser(UpdateProfileRequest request);

    User updateUser(User user);

    void changePassword(ChangePasswordRequest request);
    
    List<User> getAllUsers();
    
    void updateUserRole(String username, List<String> roles);
    
    void deleteUser(String username);
    
    User registerOAuth2User(String email, String name);
    
    User registerNewOAuth2User(User user);
    
    User saveUser(User user);
    
    void registerOrUpdateUser(OAuth2User oAuth2User);
}
