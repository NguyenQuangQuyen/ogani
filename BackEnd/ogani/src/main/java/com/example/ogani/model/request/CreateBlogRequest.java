package com.example.ogani.model.request;

import java.util.Set;

public class CreateBlogRequest {
    private String title;
    private String description;
    private String content;
    private Long imageId;
    private String username; 
    private Set<Long> tags;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Long getImageId() {
        return imageId;
    }

    public void setImageId(Long imageId) {
        this.imageId = imageId;
    }

    public String getUsername() { 
        return username;
    }

    public void setUsername(String username) { 
        this.username = username;
    }

    public Set<Long> getTags() {
        return tags;
    }

    public void setTags(Set<Long> tags) {
        this.tags = tags;
    }
}
