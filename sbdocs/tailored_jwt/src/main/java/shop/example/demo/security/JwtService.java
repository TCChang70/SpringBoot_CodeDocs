package shop.example.demo.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import shop.example.demo.model.User;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Component
public class JwtService {

    private final SecretKey key;
    private final long expirationMs;

    public JwtService(@Value("${jwt.secret}") String secret,
                      @Value("${jwt.expiration-ms}") long expirationMs) {
        // HS256 需要至少 32 bytes 的密鑰
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.expirationMs = expirationMs;
    }

    public String generateToken(User user) {
        Date now = new Date();
        return Jwts.builder()
                .subject(user.getUsername())                 // 主體＝帳號名稱
                .claim("role", user.getRole())               // 額外宣告：角色
                .issuedAt(now)
                .expiration(new Date(now.getTime() + expirationMs))
                .signWith(key)                               // HS256 + 密鑰簽章
                .compact();
    }

    public String extractUsername(String token) {
        return parse(token).getSubject();
    }

    public String extractRole(String token) {
        return parse(token).get("role", String.class);
    }

    // 自含驗證：簽章正確 + 未過期
    public boolean isValid(String token) {
        return parse(token).getExpiration().after(new Date());
    }

    private Claims parse(String token) {
        return Jwts.parser()
                .verifyWith(key)                             // 驗證簽章
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
