將原本的購物車系統升級為 **Spring Security 6 \+ JWT 認證防護機制**，能讓學員掌握企業級的安全防護架構，同時理解 AI 如何協助撰寫複雜的無狀態（Stateless）認證過濾器與前端 Token 自動附加邏輯。

## **階段一：Spring Security 6 \+ JWT 安全組態 (SecurityConfig)**

在 Spring Boot 3.x 中，WebSecurityConfigurerAdapter 已全面廢棄，改採 Lambda 導向的 SecurityFilterChain Bean 配置。

### **1\. 給 AI 的提示詞 (Prompt)**

**\[Context\]** 我正在開發 Spring Boot 3.2 購物車系統，需導入 Spring Security 6 與 JWT 無狀態認證。  
**\[Task\]** 請撰寫 SecurityConfig 組態類別，配置自訂的 JwtAuthenticationFilter。  
**\[Constraint\]**

1. 使用 Spring Boot 3.x 規範（SecurityFilterChain Bean，不可使用已廢棄的類別）。  
2. 將 Session 管理設為 SessionCreationPolicy.STATELESS。  
3. 開放 /api/v1/auth/\*\* 與 Swagger API 文件路徑，其餘 /api/v1/cart/\*\* 需通過認證。  
4. 正確配置 CORS 跨域設定，允許前端帶入 Authorization Header。

### **2\. AI 產出：SecurityConfig.java**

Java  
package com.example.cart.config;

import com.example.cart.security.JwtAuthenticationFilter;  
import lombok.RequiredArgsConstructor;  
import org.springframework.context.annotation.Bean;  
import org.springframework.context.annotation.Configuration;  
import org.springframework.security.config.annotation.web.builders.HttpSecurity;  
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;  
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;  
import org.springframework.security.config.http.SessionCreationPolicy;  
import org.springframework.security.web.SecurityFilterChain;  
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;  
import org.springframework.web.cors.CorsConfiguration;  
import org.springframework.web.cors.CorsConfigurationSource;  
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration  
@EnableWebSecurity  
@RequiredArgsConstructor  
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthFilter;

    @Bean  
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {  
        http  
            .csrf(AbstractHttpConfigurer::disable)  
            .cors(cors \-\> cors.configurationSource(corsConfigurationSource()))  
            .sessionManagement(session \-\> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))  
            .authorizeHttpRequests(auth \-\> auth  
                .requestMatchers("/api/v1/auth/\*\*", "/v3/api-docs/\*\*", "/swagger-ui/\*\*").permitAll()  
                .requestMatchers("/api/v1/cart/\*\*").hasAnyRole("USER", "ADMIN")  
                .anyRequest().authenticated()  
            )  
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();  
    }

    @Bean  
    public CorsConfigurationSource corsConfigurationSource() {  
        CorsConfiguration config \= new CorsConfiguration();  
        config.setAllowedOrigins(List.of("http\://localhost:5173")); // 前端 Vite/Vue 預設 Port  
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));  
        config.setAllowedHeaders(List.of("Authorization", "Content-Type"));  
        config.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source \= new UrlBasedCorsConfigurationSource();  
        source.registerCorsConfiguration("/\*\*", config);  
        return source;  
    }  
}

## **階段二：JWT 過濾器與安全上下文建置**

### **1\. 給 AI 的提示詞 (Prompt)**

**\[Context\]** 我需要建立一個 JWT 請求攔截器 JwtAuthenticationFilter。  
**\[Task\]** 繼承 OncePerRequestFilter，從 HTTP Authorization Header 解析 Bearer \<JWT\_TOKEN\>。  
**\[Constraint\]**

1. 解析 Token 取得 userId 與 roles 後，構建 UsernamePasswordAuthenticationToken 並存入 SecurityContextHolder。  
2. 若 Token 無效或過期，不中斷 Request 鏈，交由 Spring Security 觸發 401 Unauthorized。

### **2\. AI 产出：JwtAuthenticationFilter.java**

Java  
package com.example.cart.security;

import jakarta.servlet.FilterChain;  
import jakarta.servlet.ServletException;  
import jakarta.servlet.http.HttpServletRequest;  
import jakarta.servlet.http.HttpServletResponse;  
import lombok.RequiredArgsConstructor;  
import org.springframework.lang.NonNull;  
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;  
import org.springframework.security.core.authority.SimpleGrantedAuthority;  
import org.springframework.security.core.context.SecurityContextHolder;  
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;  
import org.springframework.stereotype.Component;  
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;  
import java.util.List;

@Component  
@RequiredArgsConstructor  
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;

    @Override  
    protected void doFilterInternal(  
            @NonNull HttpServletRequest request,  
            @NonNull HttpServletResponse response,  
            @NonNull FilterChain filterChain  
    ) throws ServletException, IOException {

        final String authHeader \= request.getHeader("Authorization");  
          
        if (authHeader \== null \|\| \!authHeader.startsWith("Bearer ")) {  
            filterChain.doFilter(request, response);  
            return;  
        }

        final String jwt \= authHeader.substring(7);  
        if (jwtService.isTokenValid(jwt)) {  
            Long userId \= jwtService.extractUserId(jwt);  
            List\<String\> roles \= jwtService.extractRoles(jwt);

            var authorities \= roles.stream()  
                    .map(role \-\> new SimpleGrantedAuthority("ROLE\_" \+ role))  
                    .toList();

            UsernamePasswordAuthenticationToken authToken \= new UsernamePasswordAuthenticationToken(  
                    userId, // 將 userId 作為 Principal 傳遞  
                    null,  
                    authorities  
            );  
            authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

            // 設定安全上下文，後續 Controller 即可透過 @AuthenticationPrincipal 取得 userId  
            SecurityContextHolder.getContext().setAuthentication(authToken);  
        }

        filterChain.doFilter(request, response);  
    }  
}

## **階段三：Controller 重構與動態 User 身份注入**

經過 JWT 過濾器處理後，原本手動帶入 HTTP Header (@RequestHeader("X-User-Id")) 的做法可被安全的 Security Context 替代。

Java  
package com.example.cart.controller;

import com.example.cart.dto.AddToCartRequest;  
import com.example.cart.dto.CartResponse;  
import com.example.cart.service.CartService;  
import jakarta.validation.Valid;  
import lombok.RequiredArgsConstructor;  
import org.springframework.http.ResponseEntity;  
import org.springframework.security.core.annotation.AuthenticationPrincipal;  
import org.springframework.web.bind.annotation.\*;

@RestController  
@RequestMapping("/api/v1/cart")  
@RequiredArgsConstructor  
public class CartController {

    private final CartService cartService;

    @PostMapping("/items")  
    public ResponseEntity\<CartResponse\> addToCart(  
            @AuthenticationPrincipal Long userId, // 從安全上下文中安全注入當前登入者的 ID  
            @Valid @RequestBody AddToCartRequest request) {  
        return ResponseEntity.ok(cartService.addToCart(userId, request));  
    }  
}

## **階段四：前端 (Vue 3 \+ Axios) JWT 攔截器對接**

### **1\. 給 AI 的提示詞 (Prompt)**

**\[Context\]** 前端已改用 JWT 進行認證。  
**\[Task\]** 撰寫 Axios 實例配置（apiClient.js），自動將 localStorage 中的 Token 附加至 Request Header，並攔截 401 錯誤導向登入頁。

### **2\. AI 產出：Axios 攔截器實作 (apiClient.js)**

JavaScript  
import axios from 'axios';

const apiClient \= axios.create({  
  baseURL: 'http\://localhost:8080/api/v1',  
  headers: {  
    'Content-Type': 'application/json'  
  }  
});

// 1\. Request 攔截器：自動夾帶 JWT  
apiClient.interceptors.request.use(  
  (config) \=\> {  
    const token \= localStorage.getItem('access\_token');  
    if (token) {  
      config.headers.Authorization \= \`Bearer \${token}\`;  
    }  
    return config;  
  },  
  (error) \=\> Promise.reject(error)  
);

// 2\. Response 攔截器：處理 Token 過期 (401 Unauthorized)  
apiClient.interceptors.response.use(  
  (response) \=\> response,  
  (error) \=\> {  
    if (error.response && error.response.status \=== 401) {  
      localStorage.removeItem('access\_token');  
      window.location.href \= '/login?msg=session\_expired';  
    }  
    return Promise.reject(error);  
  }  
);

export default apiClient;  
