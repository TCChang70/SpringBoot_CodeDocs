package demo.com.example.controller;

import java.util.stream.Collectors;

import javax.naming.AuthenticationException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import java.util.*;

@Controller   // ← 注意：不是 @RestController（因為要回傳 HTML 頁面，不是 JSON）
public class AuthController {
	  // ✅ 新增注入
	@Autowired
    private  AuthenticationManager authenticationManager;
    // GET /login → 渲染 templates/login.html
    @GetMapping("/login")
    public String loginPage() {
        return "login";    // Thymeleaf 會到 templates/ 資料夾找 login.html
    }

    // GET /welcome → 渲染 templates/welcome.html
    @GetMapping("/welcome")
    public String welcome() {
        return "welcome";
    }
    @GetMapping("/api/me")
    public ResponseEntity<Map<String, Object>> getCurrentUser(Authentication authentication) {

        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(401).build();
        }

        List<String> roles = authentication.getAuthorities()
                .stream()
                .map(GrantedAuthority::getAuthority) // 回傳 "ROLE_USER", "ROLE_ADMIN"
                .collect(Collectors.toList());

        Map<String, Object> userInfo = new HashMap<>();
        userInfo.put("username", authentication.getName());
        userInfo.put("roles", roles);

        return ResponseEntity.ok(userInfo);
    }
    // ✅ 新增：自訂 POST /login 處理
    @PostMapping("/login")
    public String processLogin(
            @RequestParam String username,
            @RequestParam String password,
            HttpServletRequest request,
            RedirectAttributes redirectAttributes) {

        try {
        	System.out.println("username:"+username);
        	System.out.println("password:"+password);
            // ① 建立「待驗證的憑證物件」
            UsernamePasswordAuthenticationToken token =
                new UsernamePasswordAuthenticationToken(username, password);

            // ② 呼叫 AuthenticationManager 驗證
            //    底層會呼叫 CustomUserDetailsService.loadUserByUsername()
            //    再用 BCryptPasswordEncoder.matches() 比對密碼
            Authentication authentication = authenticationManager.authenticate(token);

            // ③ 把驗證結果寫入 SecurityContext（這一步讓 Spring Security 認識你已登入）
            SecurityContextHolder.getContext().setAuthentication(authentication);

            // ④ 把 SecurityContext 存入 Session（讓後續請求維持登入狀態）
            HttpSession session = request.getSession(true);
            session.setAttribute(
                HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY,
                SecurityContextHolder.getContext()
            );

            return "redirect:/welcome";

        } catch (Exception e) {
            // 驗證失敗（帳號不存在或密碼錯誤）
            return "redirect:/login?error=true";
        }
    }

}
