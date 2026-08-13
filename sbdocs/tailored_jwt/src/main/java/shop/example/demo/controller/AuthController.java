package shop.example.demo.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import shop.example.demo.dto.AuthResponse;
import shop.example.demo.dto.LoginRequest;
import shop.example.demo.dto.RegisterRequest;
import shop.example.demo.model.User;
import shop.example.demo.repository.UserRepository;
import shop.example.demo.security.JwtService;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "帳號驗證 API", description = "註冊、登入、取得目前登入者資訊（JWT）")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;

    public AuthController(AuthenticationManager authenticationManager,
                          UserRepository userRepository,
                          JwtService jwtService,
                          PasswordEncoder passwordEncoder) {
        this.authenticationManager = authenticationManager;
        this.userRepository = userRepository;
        this.jwtService = jwtService;
        this.passwordEncoder = passwordEncoder;
    }

    // POST /api/auth/register → 註冊帳號（密碼加密存入）
    @PostMapping("/register")
    @Operation(summary = "註冊帳號", description = "建立新帳號，密碼以 BCrypt 加密儲存")
    public ResponseEntity<?> register(@RequestBody RegisterRequest request) {
        if (request.username() == null || request.username().isBlank()
                || request.password() == null || request.password().isBlank()) {
            return ResponseEntity.badRequest().body("帳號與密碼不可為空白");
        }
        if (userRepository.existsByUsername(request.username())) {
            return ResponseEntity.badRequest().body("帳號已存在: " + request.username());
        }
        String role = (request.role() == null || request.role().isBlank())
                ? "USER" : request.role().toUpperCase();
        User user = new User(request.username(), passwordEncoder.encode(request.password()), role);
        userRepository.save(user);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(Map.of("message", "註冊成功", "username", user.getUsername(), "role", user.getRole()));
    }

    // POST /api/auth/login → 登入並回傳 JWT Token
    @PostMapping("/login")
    @Operation(summary = "登入", description = "驗證帳號密碼，成功回傳 JWT Token")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.username(), request.password()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("帳號或密碼錯誤");
        }
        User user = userRepository.findByUsername(request.username()).orElseThrow();
        String token = jwtService.generateToken(user);
        return ResponseEntity.ok(new AuthResponse(token, user.getUsername(), user.getRole()));
    }

    // GET /api/auth/me → 取得目前登入者資訊（需帶 Bearer Token）
    @GetMapping("/me")
    @Operation(summary = "取得目前登入者", description = "透過 JWT 回傳目前登入的帳號資訊")
    public ResponseEntity<?> me(Authentication authentication) {
        User user = userRepository.findByUsername(authentication.getName()).orElse(null);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("找不到帳號");
        }
        return ResponseEntity.ok(Map.of(
                "id", user.getId(),
                "username", user.getUsername(),
                "role", user.getRole()));
    }
}
