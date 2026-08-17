package com.demo.controller;

import java.util.*;
import org.springframework.stereotype.Component;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

@Component
public class JwtUtility {
    private static final String SECRET = "MySecretKey"; // 密鑰，實際專案應存放在配置檔中

    // 生成 Token
    public static String generateToken(String username) {
        return Jwts.builder()
                .setSubject(username)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + 600000)) // 有效期 10 分鐘
                .signWith(SignatureAlgorithm.HS512, SECRET)
                .compact();
    }

    // 驗證 Token
    public static boolean validateToken(String token) {
        try {
            String name = Jwts.parser()
                    .setSigningKey(SECRET)
                    .parseClaimsJws(token)
                    .getBody()
                    .getSubject();
            return name != null;
        } catch (Exception e) {
            System.out.println("validateToken error " + e.getMessage());
            return false;
        }
    }

    // 從 Token 提取使用者名稱
    public String extractUsername(String token) {
        return Jwts.parser()
                .setSigningKey(SECRET)
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
    }
}
