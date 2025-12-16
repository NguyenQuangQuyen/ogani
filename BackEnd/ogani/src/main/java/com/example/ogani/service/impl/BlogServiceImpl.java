package com.example.ogani.service.impl;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import com.example.ogani.entity.Blog;
import com.example.ogani.entity.Image;
import com.example.ogani.entity.Tag;
import com.example.ogani.entity.User;
import com.example.ogani.exception.NotFoundException;
import com.example.ogani.model.request.CreateBlogRequest;
import com.example.ogani.repository.BlogRepository;
import com.example.ogani.repository.ImageRepository;
import com.example.ogani.repository.UserRepository;
import com.example.ogani.repository.TagRepository;
import com.example.ogani.service.BlogService;
import java.sql.Timestamp;

@Service
public class BlogServiceImpl implements BlogService {

    @Autowired
    private BlogRepository blogRepository;

    @Autowired
    private TagRepository tagRepository;

    @Autowired
    private ImageRepository imageRepository;

    @Autowired
    private UserRepository userRepository;

    @Override
    public List<Blog> getList() {
        return blogRepository.findAll(Sort.by("id").descending());
    }

    @Override
    public Blog getBlog(long id) {
        return blogRepository.findById(id).orElseThrow(() -> new NotFoundException("Không tìm thấy Blog"));
    }

    @Override
    public Blog createBlog(CreateBlogRequest request) {
        Blog blog = new Blog();
        blog.setTitle(request.getTitle());
        blog.setDescription(request.getDescription());
        blog.setContent(request.getContent());

        // Lấy hình ảnh
        Image image = imageRepository.findById(request.getImageId())
                .orElseThrow(() -> new NotFoundException("Không tìm thấy hình ảnh"));
        blog.setImage(image);

        // Lấy người dùng
        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new NotFoundException("Không tìm thấy người dùng"));
        blog.setUser(user);

        // Gán thời gian tạo
        blog.setCreateAt(new Timestamp(System.currentTimeMillis()));

        // Lấy danh sách thẻ
        Set<Tag> tags = new HashSet<>();
        for (Long tagId : request.getTags()) {
            Tag tag = tagRepository.findById(tagId)
                    .orElseThrow(() -> new NotFoundException("Không tìm thấy thẻ"));
            tags.add(tag);
        }
        blog.setTags(tags);

        blogRepository.save(blog);
        return blog;
    }

    @Override
    public Blog updateBlog(long id, CreateBlogRequest request) {
        Blog blog = blogRepository.findById(id).orElseThrow(() -> new NotFoundException("Không tìm thấy Blog"));
        blog.setTitle(request.getTitle());
        blog.setDescription(request.getDescription());
        blog.setContent(request.getContent());

        // Lấy hình ảnh
        Image image = imageRepository.findById(request.getImageId())
                .orElseThrow(() -> new NotFoundException("Không tìm thấy hình ảnh"));
        blog.setImage(image);

        // Lấy danh sách thẻ
        Set<Tag> tags = new HashSet<>();
        for (Long tagId : request.getTags()) {
            Tag tag = tagRepository.findById(tagId)
                    .orElseThrow(() -> new NotFoundException("Không tìm thấy thẻ"));
            tags.add(tag);
        }
        blog.setTags(tags);

        blogRepository.save(blog);
        return blog;
    }

    @Override
    public void deleteBlog(long id) {
        Blog blog = blogRepository.findById(id).orElseThrow(() -> new NotFoundException("Không tìm thấy Blog"));
        blog.getTags().clear();
        blogRepository.delete(blog);
    }

    @Override
    public List<Blog> getListNewest(int limit) {
        return blogRepository.getListNewest(limit);
    }
}
