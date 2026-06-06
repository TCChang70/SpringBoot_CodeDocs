package demo.com.example.model;

import jakarta.persistence.*;
import lombok.*;
import java.util.*;

@Getter
@Setter
@NoArgsConstructor          // Lombok：自動產生無參數建構子
@Entity                     // JPA：標示這是資料庫對應的實體
@Table(name = "users")      // 對應資料庫的 "users" 資料表
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)  // 自動遞增 ID
    private Long id;

    @Column(nullable = false, unique = true)  // 非空、不重複
    private String username;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String password;   // ⚠️ 儲存 BCrypt 加密後的密碼，絕對不能明文存入

    // 多對多關聯：一個使用者可以有多個角色
    @ManyToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    @JoinTable(
        name = "users_roles",                                      // 中間關聯表名稱
        joinColumns        = @JoinColumn(name = "user_id"),        // 本表外鍵
        inverseJoinColumns = @JoinColumn(name = "role_id")         // 對方外鍵
    )
    private Set<Role> roles = new HashSet<>();
}
