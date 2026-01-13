package com.example.ogani.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;

import com.example.ogani.security.jwt.AuthEntryPointJwt;
import com.example.ogani.security.jwt.AuthTokenFilter;
import com.example.ogani.security.service.UserDetailsServiceImpl;
import com.example.ogani.security.service.OAuth2UserServiceImpl;
import com.example.ogani.service.UserService;

import java.util.Arrays;

@Configuration
@EnableMethodSecurity
public class WebSecurityConfig {
    @Autowired
    private UserDetailsServiceImpl userDetailsService;
  
    @Autowired
    private AuthEntryPointJwt unauthorizedHandler;
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    @Autowired
    private ClientRegistrationRepository clientRegistrationRepository;
  
    @Bean
    public AuthTokenFilter authenticationJwtTokenFilter() {
      return new AuthTokenFilter();
    }
  
    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
         
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder);
     
        return authProvider;
    }
    
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
      return authConfig.getAuthenticationManager();
    }
  
    @Bean
    public OAuth2UserService<OAuth2UserRequest, OAuth2User> oauth2UserService() {
        return new OAuth2UserServiceImpl(userService);
    }
    
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Arrays.asList("http://localhost:5361"));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("Authorization", "Content-Type", "X-Requested-With", 
                "accept", "Origin", "Access-Control-Request-Method", "Access-Control-Request-Headers", "X-XSRF-TOKEN"));
        configuration.setExposedHeaders(Arrays.asList("Authorization", "Access-Control-Allow-Origin", "Access-Control-Allow-Credentials"));
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L);
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
        
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource())) // Cấu hình CORS chi tiết
                .csrf(csrf -> csrf.disable()) // Tắt CSRF
                .sessionManagement(session -> session
                    .sessionCreationPolicy(SessionCreationPolicy.ALWAYS)) // Sử dụng ALWAYS thay vì STATELESS
                .exceptionHandling(exception -> exception.authenticationEntryPoint(unauthorizedHandler)) // Xử lý lỗi 401
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/auth/**").permitAll()
                        .requestMatchers("/oauth2/authorization/google").permitAll() // Cho phép truy cập API OAuth2 login
                        .requestMatchers("/login/oauth2/code/**").permitAll() // Cho phép redirect URL của OAuth2
                        .requestMatchers("/api/user/**").permitAll() // Cho phép truy cập API người dùng
                        .requestMatchers("/api/image/**").permitAll() // Cho phép truy cập API image
                        .requestMatchers("/**").permitAll()
                        .anyRequest().authenticated()
                )
                .oauth2Login(oauth2 -> oauth2
                    .userInfoEndpoint(userInfo -> userInfo.userService(oauth2UserService()))
                    .authorizationEndpoint(authorization -> authorization
                        .baseUri("/oauth2/authorization")
                        .authorizationRequestResolver(
                            new CustomAuthorizationRequestResolver(
                                clientRegistrationRepository,
                                "/oauth2/authorization"
                            )
                        )
                    )
                    .failureHandler((request, response, exception) -> {
                        // Xử lý khi đăng nhập OAuth2 thất bại hoặc bị hủy
                        System.out.println("OAuth2 login failed: " + exception.getMessage());
                        // Chuyển hướng về trang đăng nhập của frontend
                        response.sendRedirect("http://localhost:5361/login");
                    })
                    .successHandler((request, response, authentication) -> {
                        // Lưu thông tin xác thực vào SecurityContextHolder
                        SecurityContextHolder.getContext().setAuthentication(authentication);
                        
                        // Log thông tin xác thực để debug
                        if (authentication != null && authentication.getPrincipal() instanceof OAuth2User) {
                            OAuth2User oauth2User = (OAuth2User) authentication.getPrincipal();
                            System.out.println("Authentication successful for user: " + oauth2User.getAttribute("email"));
                            System.out.println("OAuth2User attributes: " + oauth2User.getAttributes());
                        }
                        
                        // Chuyển hướng người dùng đã đăng nhập về trang frontend
                        response.sendRedirect("http://localhost:5361/login/oauth2/success");
                    })
                )
                .authenticationProvider(authenticationProvider()) // Cấu hình Provider
                .addFilterBefore(authenticationJwtTokenFilter(), UsernamePasswordAuthenticationFilter.class); // Thêm JWT Filter

        return http.build();
    }
}
