package demo.com.example.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import demo.com.example.model.*;
import jakarta.transaction.Transactional;
import lombok.*;
import java.util.*;

@Component
@AllArgsConstructor
public class DataInitializer implements CommandLineRunner {
//                                       ^^^^^^^^^^^^^^^^
//                  實作此介面的 run() 方法，會在應用啟動後自動執行一次

    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional   // 確保資料庫操作在同一個交易（transaction）中完成
    public void run(String... args) {

        // 1. 建立角色（使用 orElseGet 避免重複建立）
        Role adminRole = roleRepository.findByName("ROLE_ADMIN")
                .orElseGet(() -> {
                    Role r = new Role();
                    r.setName("ROLE_ADMIN");
                    return roleRepository.save(r);
                });
        // ↑ 如果 "ROLE_ADMIN" 已存在就直接用，否則建立新的

        Role userRole = roleRepository.findByName("ROLE_USER")
                .orElseGet(() -> {
                    Role r = new Role();
                    r.setName("ROLE_USER");
                    return roleRepository.save(r);
                });

        // 2. 建立管理員帳號（檢查是否已存在，避免重複）
        if (!userRepository.existsByUsername("admin")) {
            User admin = new User();
            admin.setUsername("admin");
            admin.setEmail("admin@example.com");
            admin.setPassword(passwordEncoder.encode("admin123"));  // ← BCrypt 加密
            admin.setRoles(Set.of(adminRole));
            userRepository.save(admin);
            System.out.println("✅ 預設管理員帳號已建立：admin / admin123");
        }
        if (!userRepository.existsByUsername("user")) {
            User u = new User();
            u.setUsername("user");
            u.setEmail("user@example.com");
            u.setPassword(passwordEncoder.encode("user123"));  // ← BCrypt 加密
            u.setRoles(Set.of(userRole));
            userRepository.save(u);
            System.out.println("✅ 預設一般帳號已建立：user / user123");
        }
    }
}
