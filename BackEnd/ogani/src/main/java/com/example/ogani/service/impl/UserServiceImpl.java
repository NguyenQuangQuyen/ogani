package com.example.ogani.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Optional;

import com.example.ogani.entity.Role;
import com.example.ogani.entity.User;
import com.example.ogani.exception.BadRequestException;
import com.example.ogani.exception.NotFoundException;
import com.example.ogani.model.request.ChangePasswordRequest;
import com.example.ogani.model.request.CreateUserRequest;
import com.example.ogani.model.request.UpdateProfileRequest;
import com.example.ogani.repository.RoleRepository;
import com.example.ogani.repository.UserRepository;
import com.example.ogani.service.UserService;
import org.springframework.security.oauth2.core.user.OAuth2User;
import jakarta.persistence.EntityManager;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PasswordEncoder encoder;

    @Autowired
    private EntityManager entityManager;

    @Override
    public void register(CreateUserRequest request) {
        System.out.println("Register request: " + request.getUsername() + ", roles: " + request.getRole());
        
        boolean usernameExists = userRepository.existsByUsername(request.getUsername());
        boolean emailExists = userRepository.existsByEmail(request.getEmail());

        if (usernameExists && emailExists) {
            throw new BadRequestException("Tên tài khoản và email đã được sử dụng");
        } else if (usernameExists) {
            throw new BadRequestException("Tên tài khoản đã tồn tại");
        } else if (emailExists) {
            throw new BadRequestException("Email đã được sử dụng");
        }

        // Tạo người dùng mới
        User user = new User();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setPassword(encoder.encode(request.getPassword()));

        // Gán vai trò cho người dùng
        Set<String> strRoles = (request.getRole() != null && !request.getRole().isEmpty()) 
                ? request.getRole() 
                : Set.of("ROLE_USER"); // Nếu không có role, mặc định là "ROLE_USER"
        System.out.println("Using roles: " + strRoles);
        Set<Role> roles = new HashSet<>();

        strRoles.forEach(role -> {
            try {
                Role userRole = roleRepository.findByName(role)
                        .orElseThrow(() -> new RuntimeException("Error: Role " + role + " is not found."));
                roles.add(userRole);
                System.out.println("Added role: " + role);
            } catch (Exception e) {
                System.err.println("Error adding role: " + role + " - " + e.getMessage());
                e.printStackTrace();
            }
        });

        user.setRoles(roles);
        try {
            userRepository.save(user);
            System.out.println("User saved successfully: " + user.getUsername());
        } catch (Exception e) {
            System.err.println("Error saving user: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }

    @Override
    public User getUserByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new NotFoundException("Error: User not found with username: " + username));
    }

    @Override
    public User updateUser(UpdateProfileRequest request) {
        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new NotFoundException("Error: User not found with username: " + request.getUsername()));

        user.setFirstname(request.getFirstname());
        user.setLastname(request.getLastname());
        user.setEmail(request.getEmail());
        user.setCountry(request.getCountry());
        user.setState(request.getState());
        user.setAddress(request.getAddress());
        user.setPhone(request.getPhone());

        userRepository.save(user);
        return user;
    }

    @Override
    public void changePassword(ChangePasswordRequest request) {
        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new NotFoundException("Error: User not found with username: " + request.getUsername()));

        // Kiểm tra mật khẩu cũ
        if (!encoder.matches(request.getOldPassword(), user.getPassword())) {
            throw new BadRequestException("Error: Old password is incorrect!");
        }

        // Cập nhật mật khẩu mới
        user.setPassword(encoder.encode(request.getNewPassword()));
        userRepository.save(user);
    }
    
    @Override
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }
    
    @Override
    public void updateUserRole(String username, List<String> roleNames) {
        System.out.println("==== DEBUG - updateUserRole ====");
        System.out.println("Updating roles for user: " + username);
        System.out.println("New roles: " + roleNames);
        
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new NotFoundException("Error: User not found with username: " + username));
        
        System.out.println("Found user in database: " + user.getUsername());
        System.out.println("Current roles: " + user.getRoles());
        
        Set<Role> roles = new HashSet<>();
        
        roleNames.forEach(roleName -> {
            try {
                Role role = roleRepository.findByName(roleName)
                        .orElseThrow(() -> new RuntimeException("Error: Role " + roleName + " is not found."));
                roles.add(role);
                System.out.println("Added role: " + role.getName());
            } catch (Exception e) {
                System.err.println("Error adding role: " + roleName + " - " + e.getMessage());
            }
        });
        
        user.setRoles(roles);
        userRepository.save(user);
        
        // Refresh cache của Hibernate để đảm bảo mọi thay đổi được nhìn thấy ngay lập tức
        userRepository.flush();
        
        // Log vai trò sau khi cập nhật
        User updatedUser = userRepository.findByUsername(username).orElse(null);
        if (updatedUser != null) {
            System.out.println("User roles after update: " + updatedUser.getRoles());
        }
        
        System.out.println("Roles updated successfully for user: " + username);
    }
    
    @Override
    @org.springframework.transaction.annotation.Transactional
    public void deleteUser(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new NotFoundException("Error: User not found with username: " + username));

        try {
            Long userId = user.getId();
            System.out.println("Deleting user with ID: " + userId + ", username: " + username);
            
            // Xóa tất cả các dữ liệu liên quan trước khi xóa user
            System.out.println("Clearing related data for user: " + username);
            
            // Kiểm tra và xóa các bảng có liên kết trực tiếp
            try {
                // Kiểm tra nếu bảng cart/cart_items tồn tại và xóa
                boolean hasCartTable = !entityManager.createNativeQuery(
                        "SELECT 1 FROM information_schema.tables WHERE table_name = 'cart_items'")
                        .getResultList().isEmpty();
                
                if (hasCartTable) {
                    entityManager.createNativeQuery("DELETE FROM cart_items WHERE cart_id IN (SELECT id FROM cart WHERE user_id = :userId)")
                            .setParameter("userId", userId)
                            .executeUpdate();
                    
                    entityManager.createNativeQuery("DELETE FROM cart WHERE user_id = :userId")
                            .setParameter("userId", userId)
                            .executeUpdate();
                }
            } catch (Exception e) {
                System.err.println("Non-critical error deleting cart data: " + e.getMessage());
            }
            
            // Xóa order_details và orders
            try {
                entityManager.createNativeQuery(
                        "DELETE FROM order_details WHERE order_id IN (SELECT id FROM orders WHERE user_id = :userId)")
                        .setParameter("userId", userId)
                        .executeUpdate();
                
                entityManager.createNativeQuery("DELETE FROM orders WHERE user_id = :userId")
                        .setParameter("userId", userId)
                        .executeUpdate();
                
                System.out.println("Deleted related orders for user: " + username);
            } catch (Exception e) {
                System.err.println("Non-critical error deleting orders: " + e.getMessage());
            }
            
            // Xóa user_roles
            try {
                entityManager.createNativeQuery("DELETE FROM user_roles WHERE user_id = :userId")
                        .setParameter("userId", userId)
                        .executeUpdate();
                
                System.out.println("Deleted user roles for user: " + username);
            } catch (Exception e) {
                System.err.println("Non-critical error deleting user roles: " + e.getMessage());
            }
            
            // Kiểm tra và xóa các bảng khác có thể có liên kết
            try {
                // Kiểm tra và xóa reviews nếu có
                boolean hasReviewsTable = !entityManager.createNativeQuery(
                        "SELECT 1 FROM information_schema.tables WHERE table_name = 'reviews'")
                        .getResultList().isEmpty();
                if (hasReviewsTable) {
                    entityManager.createNativeQuery("DELETE FROM reviews WHERE user_id = :userId")
                            .setParameter("userId", userId)
                            .executeUpdate();
                }
                
                // Kiểm tra và xóa comments nếu có
                boolean hasCommentsTable = !entityManager.createNativeQuery(
                        "SELECT 1 FROM information_schema.tables WHERE table_name = 'comments'")
                        .getResultList().isEmpty();
                if (hasCommentsTable) {
                    entityManager.createNativeQuery("DELETE FROM comments WHERE user_id = :userId")
                            .setParameter("userId", userId)
                            .executeUpdate();
                }
                
                // Kiểm tra và xóa wishlist nếu có
                boolean hasWishlistTable = !entityManager.createNativeQuery(
                        "SELECT 1 FROM information_schema.tables WHERE table_name = 'wishlist'")
                        .getResultList().isEmpty();
                if (hasWishlistTable) {
                    entityManager.createNativeQuery("DELETE FROM wishlist WHERE user_id = :userId")
                            .setParameter("userId", userId)
                            .executeUpdate();
                }
            } catch (Exception e) {
                System.err.println("Non-critical error deleting related data: " + e.getMessage());
            }
            
            // Xóa user trực tiếp từ database
            try {
                // Sử dụng native query để xóa trực tiếp
                int deleted = entityManager.createNativeQuery("DELETE FROM users WHERE id = :userId")
                        .setParameter("userId", userId)
                        .executeUpdate();
                
                System.out.println("User deleted from database: " + (deleted > 0 ? "successful" : "failed"));
                
                if (deleted <= 0) {
                    // Nếu không xóa được bằng native query, thử sử dụng repository
                    System.out.println("Trying alternative deletion method...");
                    userRepository.deleteById(userId);
                    System.out.println("Alternative deletion method completed");
                }
            } catch (Exception e) {
                System.err.println("Error in user deletion: " + e.getMessage());
                e.printStackTrace();
                throw e; 
            }
            
            System.out.println("User completely deleted: " + username + " with ID: " + userId);
            
        } catch (Exception e) {
            System.err.println("Critical error deleting user: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Failed to delete user: " + e.getMessage(), e);
        }
    }
    
    @Override
    public User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElse(null); // Return null if user not found
    }
    
    @Override
    public User registerOAuth2User(String email, String name) {
        // Tạo người dùng mới từ Google OAuth2
        User user = new User();
        
        // Tạo username từ email (loại bỏ @domain.com)
        String username = email.split("@")[0] + "_google";
        int count = 0;
        
        // Kiểm tra xem username đã tồn tại chưa. Nếu có, thêm số đằng sau
        while (userRepository.existsByUsername(username + (count > 0 ? count : ""))) {
            count++;
        }
        
        username = username + (count > 0 ? count : "");
        
        user.setUsername(username);
        user.setEmail(email);
        user.setPassword(encoder.encode("GoogleAuth" + System.currentTimeMillis())); // Password ngẫu nhiên
        
        // Nếu có tên từ google, sử dụng làm firstname
        if (name != null && !name.isEmpty()) {
            String[] parts = name.split(" ");
            if (parts.length > 1) {
                user.setFirstname(parts[0]);
                user.setLastname(parts[parts.length - 1]);
            } else {
                user.setFirstname(name);
            }
        }
        
        // Gán role ROLE_USER cho người dùng
        Set<Role> roles = new HashSet<>();
        roles.add(roleRepository.findByName("ROLE_USER")
                .orElseThrow(() -> new RuntimeException("Error: Role ROLE_USER is not found.")));
        user.setRoles(roles);
        
        // Lưu người dùng vào database
        return userRepository.save(user);
    }
    
    @Override
    public User registerNewOAuth2User(User user) {
        System.out.println("Registering new OAuth2 user: " + user.getUsername());
        
        // Mã hóa mật khẩu
        user.setPassword(encoder.encode(user.getPassword()));
        
        // Gán role ROLE_USER cho người dùng nếu chưa có roles
        if (user.getRoles() == null || user.getRoles().isEmpty()) {
            Set<Role> roles = new HashSet<>();
            Role userRole = roleRepository.findByName("ROLE_USER")
                .orElseThrow(() -> new RuntimeException("Error: Role ROLE_USER is not found."));
            roles.add(userRole);
            user.setRoles(roles);
        }
        
        // Lưu người dùng vào database
        return userRepository.save(user);
    }
    
    @Override
    public User updateUser(User user) {
        System.out.println("Updating user: " + user.getUsername());
        
        // Lưu user đã được cập nhật
        return userRepository.save(user);
    }
    
    @Override
    public User saveUser(User user) {
        return userRepository.save(user);
    }
    
    @Override
    public void registerOrUpdateUser(OAuth2User oAuth2User) {
        try {
            // Lấy thông tin từ OAuth2User
            String email = oAuth2User.getAttribute("email");
            String name = oAuth2User.getAttribute("name");
            String googleId = oAuth2User.getAttribute("sub"); // Google sử dụng "sub" làm ID
            String pictureUrl = oAuth2User.getAttribute("picture");
            
            // Nếu không có email, không thể tiếp tục
            if (email == null || email.isEmpty()) {
                System.out.println("OAuth2 user missing required email attribute");
                throw new RuntimeException("OAuth2 account must have an email");
            }
            
            System.out.println("Processing OAuth2 user with email: " + email);
            System.out.println("Google profile picture URL: " + pictureUrl);
            
            // Kiểm tra xem user đã tồn tại chưa
            Optional<User> existingUser = userRepository.findByEmail(email);
            
            if (existingUser.isPresent()) {
                // Nếu user đã tồn tại, cập nhật thông tin
                User user = existingUser.get();
                user.setGoogleId(googleId);
                
                // Chỉ cập nhật avatar và profileImage khi đăng nhập bằng Google
                if (pictureUrl != null && !pictureUrl.isEmpty()) {
                    // Đảm bảo URL hình ảnh không có lỗi
                    String cleanPictureUrl = sanitizeGoogleImageUrl(pictureUrl);
                    
                    user.setAvatar(cleanPictureUrl);
                    user.setProfileImage(cleanPictureUrl);
                    System.out.println("Updated user profile image with Google picture URL: " + cleanPictureUrl);
                }
                
                user.setProvider("google");
                
                // Chỉ cập nhật username nếu cần thiết
                if (user.getUsername() == null || user.getUsername().isEmpty()) {
                    // Tạo username từ phần đầu email
                    String baseUsername = email.split("@")[0];
                    user.setUsername(baseUsername);
                    System.out.println("Updated username to: " + baseUsername);
                }
                
                // Cập nhật firstname, lastname nếu không có
                if (name != null) {
                    String[] nameParts = name.split(" ");
                    if (user.getFirstname() == null || user.getFirstname().isEmpty()) {
                        user.setFirstname(nameParts[0]);
                    }
                    if (nameParts.length > 1 && (user.getLastname() == null || user.getLastname().isEmpty())) {
                        user.setLastname(nameParts[nameParts.length - 1]);
                    }
                }
                
                System.out.println("Updating existing user: " + user.getUsername());
                userRepository.save(user);
            } else {
                // Nếu user chưa tồn tại, tạo mới
                User newUser = new User();
                newUser.setEmail(email);
                newUser.setGoogleId(googleId);
                
                // Lưu URL ảnh đại diện từ Google (nếu có)
                if (pictureUrl != null && !pictureUrl.isEmpty()) {
                    // Đảm bảo URL hình ảnh không có lỗi
                    String cleanPictureUrl = sanitizeGoogleImageUrl(pictureUrl);
                    
                    newUser.setAvatar(cleanPictureUrl);
                    newUser.setProfileImage(cleanPictureUrl);
                    System.out.println("Set profile image for new user: " + cleanPictureUrl);
                }
                
                newUser.setProvider("google");
                newUser.setEnabled(true);
                
                // Tạo username từ email (chỉ lấy phần trước @)
                String baseUsername = email.split("@")[0];
                
                // Kiểm tra xem username đã tồn tại chưa
                if (userRepository.existsByUsername(baseUsername)) {
                    // Thêm số ngẫu nhiên vào cuối username
                    baseUsername = baseUsername + "_" + System.currentTimeMillis() % 10000;
                }
                
                newUser.setUsername(baseUsername);
                System.out.println("Created username for new user: " + baseUsername);
                
                // Tạo firstname, lastname từ tên đầy đủ
                if (name != null) {
                    String[] nameParts = name.split(" ");
                    newUser.setFirstname(nameParts[0]);
                    if (nameParts.length > 1) {
                        newUser.setLastname(nameParts[nameParts.length - 1]);
                    }
                } else {
                    // Nếu không có tên, dùng phần đầu email làm tên
                    newUser.setFirstname(baseUsername);
                }
                
                // Tạo mật khẩu ngẫu nhiên cho tài khoản (sẽ không được sử dụng vì đăng nhập qua Google)
                newUser.setPassword(encoder.encode(String.valueOf(System.currentTimeMillis())));
                
                // Gán vai trò mặc định: ROLE_USER
                Role userRole = roleRepository.findByName("ROLE_USER")
                        .orElseThrow(() -> new RuntimeException("Error: Role ROLE_USER not found"));
                Set<Role> roles = new HashSet<>();
                roles.add(userRole);
                newUser.setRoles(roles);
                
                System.out.println("Creating new user for OAuth2: " + newUser.getUsername());
                userRepository.save(newUser);
            }
        } catch (Exception e) {
            System.out.println("Error processing OAuth2 user: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Error processing OAuth2 user data: " + e.getMessage(), e);
        }
    }
    
    // Hàm xử lý và làm sạch URL hình ảnh từ Google
    private String sanitizeGoogleImageUrl(String url) {
        if (url == null) {
            return null;
        }
        
        // Đảm bảo URL không chứa các ký tự có thể gây vấn đề
        String cleanUrl = url.trim();
        
        // Google thường trả về URL có kích thước giới hạn (s96-c), 
        // thử cập nhật để lấy kích thước lớn hơn (s400-c)
        if (cleanUrl.contains("s96-c")) {
            cleanUrl = cleanUrl.replace("s96-c", "s400-c");
            System.out.println("Upgraded Google image resolution: " + cleanUrl);
        }
        
        return cleanUrl;
    }
}
