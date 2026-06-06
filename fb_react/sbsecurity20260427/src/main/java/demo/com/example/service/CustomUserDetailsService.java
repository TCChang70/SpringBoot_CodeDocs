package demo.com.example.service;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import demo.com.example.model.*;
import lombok.*;
import java.util.*;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor   // Lombok：自動產生包含所有 final 欄位的建構子（相當於 @Autowired）
public class CustomUserDetailsService implements UserDetailsService {
    //                                       ^^^^^^^^^^^^^^^^^^^^^^^^
    //                              實作 Spring Security 提供的介面

    private UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String usernameOrEmail)
            throws UsernameNotFoundException {

        // 1. 從資料庫查詢使用者（同時支援帳號或 Email 登入）
        User user = userRepository.findByUsernameOrEmail(usernameOrEmail, usernameOrEmail)
                .orElseThrow(() ->
                    new UsernameNotFoundException("User not exists by Username or Email"));
                    //                ↑ 找不到使用者時拋出例外，Spring Security 會處理

        // 2. 將 Role 轉換為 Spring Security 認識的 GrantedAuthority 格式
        Set<GrantedAuthority> authorities = user.getRoles().stream()
                .map(role -> new SimpleGrantedAuthority(role.getName()))
                //                                       ↑ 例如 "ROLE_ADMIN"
                .collect(Collectors.toSet());

        // 3. 回傳 Spring Security 的 UserDetails 物件
        //    使用完整路徑避免與自訂 User 類別命名衝突
        return new org.springframework.security.core.userdetails.User(
                usernameOrEmail,    // 登入用帳號
                user.getPassword(), // BCrypt 加密後的密碼
                authorities         // 角色/權限清單
        );
    }
}
