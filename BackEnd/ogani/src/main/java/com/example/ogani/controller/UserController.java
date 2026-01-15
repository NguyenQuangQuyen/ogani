package com.example.ogani.controller;

import java.util.List;
import java.util.stream.Collectors;
import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.example.ogani.entity.User;
import com.example.ogani.model.request.ChangePasswordRequest;
import com.example.ogani.model.request.UpdateProfileRequest;
import com.example.ogani.model.request.UpdateRoleRequest;
import com.example.ogani.model.response.MessageResponse;
import com.example.ogani.model.response.UserInfoResponse;
import com.example.ogani.security.jwt.JwtUtils;
import com.example.ogani.security.service.UserDetailsImpl;
import com.example.ogani.service.UserService;

import io.swagger.v3.oas.annotations.Operation;
import org.springframework.dao.DataIntegrityViolationException;
import com.example.ogani.exception.NotFoundException;

@RestController
@RequestMapping("/api/user")
@CrossOrigin(origins = "https://hgr0a62zxby.sn.mynetname.net:1411", maxAge = 3600, allowedHeaders = "*", methods = {})
public class UserController {

    @Autowired
    private UserService userService;
    
    @Autowired
    private JwtUtils jwtUtils;
    
    @GetMapping("/current-user-info")
    @Operation(summary="Lấy thông tin người dùng hiện tại từ token")
    public ResponseEntity<?> getCurrentUserInfo() {
        // Lấy thông tin người dùng đang đăng nhập từ SecurityContext
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        System.out.println("==== DEBUG BACKEND - getCurrentUserInfo ====");
        System.out.println("Authentication: " + authentication);
        
        if (authentication != null && authentication.getPrincipal() instanceof UserDetailsImpl) {
            UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
            System.out.println("UserDetailsImpl from SecurityContext: " + userDetails.getUsername());
            System.out.println("Authorities from SecurityContext: " + userDetails.getAuthorities());
            
            try {
                // Lấy thông tin người dùng mới nhất từ cơ sở dữ liệu thay vì từ SecurityContext
                User freshUser = userService.getUserByUsername(userDetails.getUsername());
                System.out.println("FreshUser from DB: " + freshUser);
                System.out.println("FreshUser roles from DB: " + freshUser.getRoles());
                
                // Kiểm tra và debug về đối tượng Role trong freshUser
                freshUser.getRoles().forEach(role -> {
                    System.out.println("Role: " + role.getName() + ", Type: " + role.getClass().getName());
                });
                
                // Chuyển đổi Set<Role> thành List<String>
                List<String> roles = freshUser.getRoles().stream()
                        .map(role -> role.getName())
                        .collect(Collectors.toList());
                
                System.out.println("Final roles list: " + roles);
                
                // Tạo token mới để đảm bảo phản ánh quyền mới nhất
                String newToken = jwtUtils.generateTokenFromUsername(freshUser.getUsername());
                System.out.println("Generated new token for user: " + newToken);
                
                // Tạo đối tượng phản hồi
                UserInfoResponse response = new UserInfoResponse(
                        freshUser.getId(),
                        freshUser.getUsername(),
                        freshUser.getEmail(),
                        roles,
                        newToken
                );
                
                System.out.println("Final response object: " + response);
                System.out.println("Response roles: " + response.getRoles());
                
                // Trả về thông tin người dùng mới nhất với token mới
                return ResponseEntity.ok(response);
            } catch (Exception e) {
                System.err.println("Error getting user info: " + e.getMessage());
                e.printStackTrace();
                return ResponseEntity.status(500).body(new MessageResponse("Error getting user info: " + e.getMessage()));
            }
        }
        
        return ResponseEntity.status(401).body(new MessageResponse("User not authenticated"));
    }

    @GetMapping("/")
    @Operation(summary="Lấy ra user bằng username")
    public ResponseEntity<User> getuser(@RequestParam("username") String username){
        User user = userService.getUserByUsername(username);
        return ResponseEntity.ok(user);
    }

    @PutMapping("/update")
    @Operation(summary="Cập nhật user")
    public ResponseEntity<User> updateProfile(@RequestBody UpdateProfileRequest request){
        User user = userService.updateUser(request);
        return ResponseEntity.ok(user);
    }

    @PutMapping("/password")
    @Operation(summary="Đổi mật khẩu")
    public ResponseEntity<?> changePassword(@RequestBody ChangePasswordRequest request){
        userService.changePassword(request);
        return ResponseEntity.ok(new MessageResponse("Change Password Success!"));
    }
    
    @GetMapping("/all")
    @Operation(summary="Lấy danh sách tất cả người dùng")
    public ResponseEntity<List<User>> getAllUsers() {
        List<User> users = userService.getAllUsers();
        return ResponseEntity.ok(users);
    }
    
    @PutMapping("/role")
    @Operation(summary="Cập nhật vai trò của người dùng")
    public ResponseEntity<?> updateUserRole(@RequestBody UpdateRoleRequest request) {
        // Lấy thông tin về người dùng đang đăng nhập
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentUsername = null;
        
        if (authentication != null && authentication.getPrincipal() instanceof UserDetailsImpl) {
            UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
            currentUsername = userDetails.getUsername();
        }
        
        // Cập nhật vai trò
        userService.updateUserRole(request.getUsername(), request.getRoles());
        
        // Kiểm tra xem người dùng có đang cập nhật vai trò của chính mình không
        boolean isSelfUpdate = currentUsername != null && currentUsername.equals(request.getUsername());
        
        if (isSelfUpdate) {
            // Nếu đang cập nhật chính mình, cần gợi ý client làm mới token
            return ResponseEntity.ok(new MessageResponse("User role updated successfully. Please refresh your token for changes to take effect."));
        } else {
            return ResponseEntity.ok(new MessageResponse("User role updated successfully"));
        }
    }
    
    @DeleteMapping("/{username}")
    @Operation(summary="Xóa người dùng hoàn toàn khỏi hệ thống")
    public ResponseEntity<?> deleteUser(@PathVariable String username) {
        try {
            System.out.println("Controller: Received delete request for user: " + username);
            
            // Gọi service để thực hiện xóa tài khoản hoàn toàn
            userService.deleteUser(username);
            System.out.println("Controller: User deleted successfully: " + username);
            
            return ResponseEntity.ok(new MessageResponse("User completely deleted from system"));
        } catch (NotFoundException e) {
            // Người dùng không tồn tại
            System.err.println("Controller: User not found: " + e.getMessage());
            return ResponseEntity.status(404).body(new MessageResponse("User not found: " + e.getMessage()));
        } catch (Exception e) {
            // Ghi log chi tiết và trả về lỗi
            System.err.println("Controller: Error deleting user: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(500).body(new MessageResponse("Error deleting user: " + e.getMessage()));
        }
    }

    /**
     * API upload ảnh đại diện người dùng
     */
    @PostMapping("/upload-avatar")
    @Operation(summary = "Upload ảnh đại diện", description = "Upload ảnh đại diện cho người dùng")
    public ResponseEntity<?> uploadAvatar(@RequestParam("file") MultipartFile file, 
                                          @RequestParam("username") String username) {
        try {
            if (file.isEmpty()) {
                return ResponseEntity.badRequest().body(new MessageResponse("File trống"));
            }
            
            // Kiểm tra loại file
            if (!file.getContentType().startsWith("image/")) {
                return ResponseEntity.badRequest().body(new MessageResponse("Chỉ chấp nhận file ảnh"));
            }
            
            // Lấy thông tin người dùng
            User user = userService.getUserByUsername(username);
            if (user == null) {
                return ResponseEntity.status(404).body(new MessageResponse("Không tìm thấy người dùng"));
            }
            
            // Sử dụng đường dẫn tuyệt đối đến thư mục static/photos
            String basePath = new File("").getAbsolutePath();
            String uploadDir = basePath + "/src/main/resources/static/photos/";
            
            // Log thông tin đường dẫn để debug
            System.out.println("Base path: " + basePath);
            System.out.println("Upload directory: " + uploadDir);
            
            File uploadPath = new File(uploadDir);
            if (!uploadPath.exists()) {
                boolean created = uploadPath.mkdirs();
                System.out.println("Created directory: " + created);
            }
            
            // Log thông tin về thư mục upload
            System.out.println("Upload directory exists: " + uploadPath.exists());
            System.out.println("Upload directory is directory: " + uploadPath.isDirectory());
            System.out.println("Upload directory can write: " + uploadPath.canWrite());
            
            // Tạo tên file duy nhất
            String fileName = "avatar_" + username + "_" + System.currentTimeMillis() + "." + getFileExtension(file.getOriginalFilename());
            String filePath = uploadDir + fileName;
            
            System.out.println("Saving file to: " + filePath);
            
            // Lưu file
            File dest = new File(filePath);
            file.transferTo(dest);
            
            System.out.println("File saved successfully: " + dest.exists() + ", size: " + dest.length());
            
            // Cập nhật URL ảnh đại diện trong thông tin người dùng
            String imageUrl = "/api/image/file/" + fileName;
            
            // Lưu ý: Kiểm tra nếu người dùng là từ Google
            boolean isGoogleUser = "google".equals(user.getProvider());
            System.out.println("User provider: " + user.getProvider() + ", isGoogleUser: " + isGoogleUser);
            
            // Cập nhật cả profileImage và avatar (nếu có)
            user.setProfileImage(imageUrl);
            
            // Nếu người dùng đăng nhập bằng Google, cập nhật cả trường avatar
            if (isGoogleUser) {
                System.out.println("Overriding Google avatar with uploaded image for user: " + username);
                
                // Ghi đè URL avatar từ Google bằng ảnh mới tải lên
                user.setAvatar(imageUrl);
            }
            
            userService.saveUser(user);
            
            System.out.println("User profile image updated: " + imageUrl);
            
            // Trả về URL của ảnh đại diện và hướng dẫn cách hiển thị
            Map<String, String> response = new HashMap<>();
            response.put("imageUrl", imageUrl);
            response.put("fullUrl", "https://hgr0a62zxby.sn.mynetname.net:2003" + imageUrl);
            response.put("message", "Sử dụng fullUrl để hiển thị ảnh hoặc thêm baseUrl vào imageUrl");
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            // Log lỗi chi tiết
            System.err.println("Error uploading avatar: " + e.getMessage());
            e.printStackTrace();
            
            return ResponseEntity.status(500).body(new MessageResponse("Lỗi khi upload ảnh: " + e.getMessage()));
        }
    }
    
    /**
     * Phương thức lấy phần mở rộng của file
     */
    private String getFileExtension(String fileName) {
        if (fileName == null) {
            return "";
        }
        int lastDotIndex = fileName.lastIndexOf(".");
        if (lastDotIndex < 0) {
            return "";
        }
        return fileName.substring(lastDotIndex + 1);
    }
}
