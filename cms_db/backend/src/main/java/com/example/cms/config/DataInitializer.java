package com.example.cms.config;

import com.example.cms.user.entity.User;
import com.example.cms.user.entity.UserRole;
import com.example.cms.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * 首次啟動時建立預設管理員（admin / admin123）。
 * 僅在使用者資料表為空時執行，請於正式環境立即變更密碼或改由 Migration 建立。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        if (userRepository.count() > 0) {
            return;
        }
        User admin = new User();
        admin.setUsername("admin");
        admin.setEmail("admin@cms.local");
        admin.setPasswordHash(passwordEncoder.encode("admin123"));
        admin.setDisplayName("系統管理員");
        admin.setRole(UserRole.admin);
        userRepository.save(admin);
        log.info("已建立預設管理員帳號：admin（密碼 admin123，請立即變更）");
    }
}