package demo.com.example.model;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {
    // Spring Data JPA 依照方法名稱自動產生 SQL：
    
    Optional<User> findByUsernameOrEmail(String username, String email);
    //  → SELECT * FROM users WHERE username = ? OR email = ?

    Optional<User> findByUsername(String username);
    //  → SELECT * FROM users WHERE username = ?

    boolean existsByUsername(String username);
    //  → SELECT COUNT(*) > 0 FROM users WHERE username = ?

    boolean existsByEmail(String email);
    //  → SELECT COUNT(*) > 0 FROM users WHERE email = ?
}
