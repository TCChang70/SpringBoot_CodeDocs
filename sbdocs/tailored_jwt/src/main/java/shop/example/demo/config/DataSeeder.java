package shop.example.demo.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;
import shop.example.demo.model.User;
import shop.example.demo.repository.UserRepository;

@Configuration
public class DataSeeder {

    // 啟動時建立預設帳號（密碼經 BCrypt 加密，避免直接在 data.sql 放明文）
    @Bean
    public CommandLineRunner seedUsers(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        return args -> {
            if (!userRepository.existsByUsername("admin")) {
                userRepository.save(new User("admin", passwordEncoder.encode("admin123"), "ADMIN"));
                System.out.println("已建立預設帳號：admin / admin123（ADMIN）");
            }
            if (!userRepository.existsByUsername("user")) {
                userRepository.save(new User("user", passwordEncoder.encode("user123"), "USER"));
                System.out.println("已建立預設帳號：user / user123（USER）");
            }
        };
    }
}
