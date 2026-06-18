package com.example.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.security.Key;
import java.util.Date;

public class JwtUtil {

    private static final String SECRET_ENV = System.getenv("JWT_SECRET");
    private static final String SECRET = (SECRET_ENV != null && SECRET_ENV.length() >= 32)
                                           ? SECRET_ENV
                                           : "ChangeThisSecretInProduction-min32chars!";
    private static final Key KEY = Keys.hmacShaKeyFor(SECRET.getBytes());
    private static final long EXPIRY_MS = 2 * 60 * 60 * 1000L;

    public static String generateToken(String username, String role) {
        return Jwts.builder()
                   .setSubject(username)
                   .claim("role", role)
                   .setIssuedAt(new Date())
                   .setExpiration(new Date(System.currentTimeMillis() + EXPIRY_MS))
                   .signWith(KEY)
                   .compact();
    }

    public static Claims parseToken(String token) {
        return Jwts.parserBuilder()
                   .setSigningKey(KEY)
                   .build()
                   .parseClaimsJws(token)
                   .getBody();
    }

    public static boolean isValid(String token) {
        try {
            parseToken(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }
}
