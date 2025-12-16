package com.example.ogani.model.response;

import java.util.List;

/**
 * Response model for user information.
 * Contains basic details about the user and their roles.
 */
public class UserInfoResponse {

    /**
     * Unique identifier of the user.
     */
    private long id;

    /**
     * Username of the user.
     */
    private String username;

    /**
     * Email address of the user.
     */
    private String email;

    /**
     * List of roles assigned to the user.
     * Can be changed to Set<String> if roles need to be unique.
     */
    private List<String> roles;
    
    /**
     * JWT token for authentication.
     */
    private String token;

    // Constructor có tham số
    public UserInfoResponse(long id, String username, String email, List<String> roles) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.roles = roles;
    }
    
    // Constructor có tham số với token
    public UserInfoResponse(long id, String username, String email, List<String> roles, String token) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.roles = roles;
        this.token = token;
    }

    // Constructor không tham số
    public UserInfoResponse() {
    }

    // Getter và Setter
    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public List<String> getRoles() {
        return roles;
    }

    public void setRoles(List<String> roles) {
        this.roles = roles;
    }
    
    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }
}
