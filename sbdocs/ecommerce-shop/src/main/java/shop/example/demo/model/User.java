package shop.example.demo.model;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import lombok.Data;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

@Entity
@Table(name = "users")
@Schema(description = "帳號資料模型（JWT 登入用）")
@Data
public class User implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(description = "帳號 ID（自動產生）", example = "1", accessMode = Schema.AccessMode.READ_ONLY)
    private Long id;

    @Column(nullable = false, unique = true)
    @Schema(description = "帳號名稱（不可重複）", example = "admin", requiredMode = Schema.RequiredMode.REQUIRED)
    private String username;

    @Column(nullable = false)
    @Schema(description = "密碼（存入 BCrypt 加密後的雜湊值）", example = "admin123", accessMode = Schema.AccessMode.WRITE_ONLY)
    private String password;

    @Column(nullable = false)
    @Schema(description = "角色：ADMIN / USER", example = "ADMIN")
    private String role = "USER";

    @Column(name = "created_at")
    @Schema(description = "建立時間", accessMode = Schema.AccessMode.READ_ONLY)
    private LocalDateTime createdAt = LocalDateTime.now();

    public User() {}

    public User(String username, String password, String role) {
        this.username = username;
        this.password = password;
        this.role = role;
    }

    // ===== UserDetails 介面實作 =====

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + role));
    }

    @Override
    public boolean isAccountNonExpired() { return true; }

    @Override
    public boolean isAccountNonLocked() { return true; }

    @Override
    public boolean isCredentialsNonExpired() { return true; }

    @Override
    public boolean isEnabled() { return true; }
}
