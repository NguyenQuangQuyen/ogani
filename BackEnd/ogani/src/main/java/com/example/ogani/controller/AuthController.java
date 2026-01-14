package com.example.ogani.controller;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.ogani.entity.Role;
import com.example.ogani.entity.User;
import com.example.ogani.model.request.CreateUserRequest;
import com.example.ogani.model.request.LoginRequest;
import com.example.ogani.model.response.MessageResponse;
import com.example.ogani.model.response.UserInfoResponse;
import com.example.ogani.repository.RoleRepository;
import com.example.ogani.security.jwt.JwtUtils;
import com.example.ogani.security.service.UserDetailsImpl;
import com.example.ogani.service.UserService;

import io.swagger.v3.oas.annotations.Operation;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "http://localhost:4200", maxAge = 3600)
public class AuthController {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtUtils jwtUtils;

    @Autowired
    private UserService userService;

    @Autowired
    private RoleRepository roleRepository;

    /**
     * Endpoint để xử lý đăng nhập
     */
    @PostMapping("/login")
    @Operation(summary = "Đăng nhập")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest loginRequest) {
        try {
            userService.getUserByUsername(loginRequest.getUsername());

            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword()));

            SecurityContextHolder.getContext().setAuthentication(authentication);

            UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();

            String jwt = jwtUtils.generateTokenFromUsername(userDetails.getUsername());
            
            ResponseCookie jwtCookie = jwtUtils.generateJwtCookie(userDetails);

            List<String> roles = userDetails.getAuthorities().stream()
                    .map(item -> item.getAuthority())
                    .collect(Collectors.toList());

            return ResponseEntity.ok().header(HttpHeaders.SET_COOKIE, jwtCookie.toString())
                    .body(new UserInfoResponse(userDetails.getId(),
                            userDetails.getUsername(),
                            userDetails.getEmail(),
                            roles,
                            jwt));
        } catch (com.example.ogani.exception.NotFoundException e) {
            return ResponseEntity.badRequest().body(new MessageResponse("Sai tài khoản hoặc tài khoản không tồn tại"));
        } catch (BadCredentialsException e) {
            return ResponseEntity.badRequest().body(new MessageResponse("Sai mật khẩu"));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(new MessageResponse("Đã xảy ra lỗi khi đăng nhập: " + e.getMessage()));
        }
    }

    /**
     * Endpoint để xử lý đăng ký tài khoản
     */
    @PostMapping("/register")
    @Operation(summary = "Đăng ký")
    public ResponseEntity<?> register(@Valid @RequestBody CreateUserRequest request) {
        userService.register(request);

        return ResponseEntity.ok(new MessageResponse("Đăng ký tài khoản thành công"));
    }

    /**
     * Endpoint để xử lý đăng xuất
     */
    @PostMapping("/logout")
    @Operation(summary = "Đăng xuất")
    public ResponseEntity<?> logoutUser() {
        // Xóa JWT cookie
        ResponseCookie cookie = jwtUtils.getCleanJwtCookie();

        // Trả về thông báo đăng xuất thành công
        return ResponseEntity.ok().header(HttpHeaders.SET_COOKIE, cookie.toString())
                .body(new MessageResponse("You've been logged out!"));
    }

    /**
     * Endpoint để kiểm tra tất cả vai trò có trong hệ thống
     */
    @GetMapping("/roles")
    @Operation(summary = "Xem tất cả vai trò")
    public ResponseEntity<?> getAllRoles() {
        List<Role> roles = roleRepository.findAll();
        return ResponseEntity.ok(roles);
    }

    /**
     * Endpoint để xác thực người dùng đăng nhập bằng Google
     */
    @PostMapping("/google-signin")
    @Operation(summary = "Đăng nhập bằng Google")
    public ResponseEntity<?> googleSignIn(@RequestBody Map<String, String> request) {
        String idToken = request.get("idToken");
        if (idToken == null || idToken.isEmpty()) {
            return ResponseEntity.badRequest().body(new MessageResponse("Error: No ID token provided"));
        }

        try {
            // Trong môi trường thực tế, bạn sẽ xác thực token với Google API
            // Ở đây chúng ta giả định token đã được xác thực và chứa email của người dùng
            String email = "user@gmail.com"; // Trong thực tế, email sẽ được trích xuất từ token đã xác thực
            
            // Tìm người dùng bằng email
            User user = userService.getUserByEmail(email);
            if (user == null) {
                return ResponseEntity.status(404).body(new MessageResponse("Error: User not found with email: " + email));
            }
            
            // Tạo JWT token
            String jwt = jwtUtils.generateTokenFromUsername(user.getUsername());
            
            // Lấy danh sách các vai trò của người dùng
            List<String> roles = user.getRoles().stream()
                    .map(role -> role.getName())
                    .collect(Collectors.toList());
            
            // Trả về thông tin người dùng kèm JWT token
            return ResponseEntity.ok(new UserInfoResponse(user.getId(),
                    user.getUsername(),
                    user.getEmail(),
                    roles,
                    jwt));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(new MessageResponse("Error processing Google Sign-In: " + e.getMessage()));
        }
    }

    /**
     * Endpoint để làm mới token khi vai trò thay đổi
     */
    @PostMapping("/refresh-token")
    @Operation(summary = "Làm mới token khi vai trò thay đổi")
    public ResponseEntity<?> refreshToken() {
        // Lấy thông tin xác thực hiện tại
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        if (authentication != null && authentication.getPrincipal() instanceof UserDetailsImpl) {
            UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
            String username = userDetails.getUsername();
            
            // Lấy thông tin mới nhất từ cơ sở dữ liệu
            User freshUser = userService.getUserByUsername(username);
            
            // Tạo UserDetails mới từ thông tin mới nhất
            // Đây là bước quan trọng để cập nhật vai trò trong SecurityContext
            UserDetailsImpl updatedUserDetails = UserDetailsImpl.build(freshUser);
            
            // Tạo Authentication mới với thông tin đã cập nhật
            Authentication newAuth = new UsernamePasswordAuthenticationToken(
                updatedUserDetails, null, updatedUserDetails.getAuthorities());
            
            // Cập nhật SecurityContext với thông tin xác thực mới
            SecurityContextHolder.getContext().setAuthentication(newAuth);
            
            // Tạo JWT token mới
            String jwt = jwtUtils.generateTokenFromUsername(username);
            
            // Tạo JWT cookie mới
            ResponseCookie jwtCookie = jwtUtils.generateJwtCookie(updatedUserDetails);
            
            // Lấy danh sách vai trò mới
            List<String> roles = updatedUserDetails.getAuthorities().stream()
                    .map(item -> item.getAuthority())
                    .collect(Collectors.toList());
            
            // Trả về thông tin người dùng với token mới
            return ResponseEntity.ok().header(HttpHeaders.SET_COOKIE, jwtCookie.toString())
                    .body(new UserInfoResponse(updatedUserDetails.getId(),
                            updatedUserDetails.getUsername(),
                            updatedUserDetails.getEmail(),
                            roles,
                            jwt));
        }
        
        return ResponseEntity.status(401).body(new MessageResponse("User not authenticated"));
    }

    /**
     * Endpoint để lấy thông tin người dùng đã đăng nhập bằng OAuth2
     */
    @GetMapping(value = "/current-user", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Lấy thông tin người dùng hiện tại")
    public ResponseEntity<?> getCurrentUser() {
        // Lấy thông tin xác thực hiện tại
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        System.out.println("==== DEBUG getCurrentUser ====");
        System.out.println("Authentication: " + authentication);
        if (authentication != null) {
            System.out.println("Authentication.name: " + authentication.getName());
            System.out.println("Authentication.principal: " + authentication.getPrincipal());
            System.out.println("Authentication.isAuthenticated: " + authentication.isAuthenticated());
        }
        
        if (authentication != null && authentication.isAuthenticated()) {
            String username = authentication.getName();
            System.out.println("Username: " + username);
            
            try {
                // Lấy thông tin người dùng từ database
                System.out.println("Trying to get user by username: " + username);
                User user = null;
                
                // Đầu tiên thử tìm bằng username
                try {
                    user = userService.getUserByUsername(username);
                } catch (Exception e) {
                    System.out.println("User not found by username, trying with email");
                    // Nếu không tìm thấy bằng username, thử tìm bằng email
                    // Vì với OAuth2, authentication.getName() có thể trả về email
                    user = userService.getUserByEmail(username);
                }
                
                if (user == null) {
                    System.out.println("User not found in DB for username/email: " + username);
                    
                    // Đặc biệt xử lý cho OAuth2 user
                    if (authentication.getPrincipal() instanceof OAuth2User) {
                        OAuth2User oauth2User = (OAuth2User) authentication.getPrincipal();
                        String email = oauth2User.getAttribute("email");
                        if (email != null) {
                            System.out.println("Trying to get OAuth2 user by email: " + email);
                            user = userService.getUserByEmail(email);
                            if (user == null) {
                                // Nếu vẫn không tìm thấy, đăng ký người dùng mới
                                System.out.println("OAuth2 user not found by email, registering new user");
                                userService.registerOrUpdateUser(oauth2User);
                                user = userService.getUserByEmail(email);
                            }
                        }
                    }
                    
                    if (user == null) {
                        return ResponseEntity.status(404).body(new MessageResponse("User not found"));
                    }
                }
                
                System.out.println("User found: " + user.getUsername() + ", id: " + user.getId());
                
                // Tạo JWT token
                String jwt = jwtUtils.generateTokenFromUsername(user.getUsername());
                
                // Lấy danh sách vai trò
                List<String> roles = user.getRoles().stream()
                        .map(role -> role.getName())
                        .collect(Collectors.toList());
                
                System.out.println("User roles: " + roles);
                
                // Trả về thông tin người dùng
                return ResponseEntity.ok(new UserInfoResponse(user.getId(),
                        user.getUsername(),
                        user.getEmail(),
                        roles,
                        jwt));
            } catch (Exception e) {
                System.out.println("Error getting user info: " + e.getMessage());
                e.printStackTrace();
                return ResponseEntity.status(500).body(new MessageResponse("Error getting user information: " + e.getMessage()));
            }
        }
        
        System.out.println("Not authenticated");
        return ResponseEntity.status(401).body(new MessageResponse("Not authenticated"));
    }

    // Xử lý endpoint /login để chuyển hướng về frontend
    @GetMapping("/login")
    public void loginRedirect(HttpServletResponse response) throws IOException {
        // Chuyển hướng về trang đăng nhập trên frontend
        response.sendRedirect("http://localhost:4200/login");
    }

    // Xử lý lỗi OAuth
    @GetMapping("/oauth2/error")
    public void handleOAuthError(HttpServletResponse response) throws IOException {
        // Chuyển hướng về trang đăng nhập trên frontend
        response.sendRedirect("http://localhost:4200/login");
    }
}
