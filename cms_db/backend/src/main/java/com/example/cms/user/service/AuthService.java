package com.example.cms.user.service;

import com.example.cms.common.BusinessException;
import com.example.cms.common.ErrorCode;
import com.example.cms.security.CustomUserDetailsService;
import com.example.cms.security.JwtService;
import com.example.cms.security.UserPrincipal;
import com.example.cms.user.dto.LoginRequest;
import com.example.cms.user.dto.LoginResponse;
import com.example.cms.user.dto.RegisterRequest;
import com.example.cms.user.dto.UserResponse;
import com.example.cms.user.entity.User;
import com.example.cms.user.entity.UserRole;
import com.example.cms.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 註冊與登入（FR-01-01 ~ FR-01-04）。
 */
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final CustomUserDetailsService userDetailsService;

    @Transactional
    public UserResponse register(RegisterRequest request) {
        if (userRepository.existsByUsername(request.username())) {
            throw new BusinessException(ErrorCode.USERNAME_EXISTS);
        }
        if (userRepository.existsByEmail(request.email())) {
            throw new BusinessException(ErrorCode.EMAIL_EXISTS);
        }

        User user = new User();
        user.setUsername(request.username());
        user.setEmail(request.email());
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        user.setRole(UserRole.author); // 預設角色 author（FR-01-04）
        userRepository.save(user);
        return UserResponse.from(user);
    }

    public LoginResponse login(LoginRequest request) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.username(), request.password()));
        } catch (DisabledException e) {
            throw new BusinessException(ErrorCode.ACCOUNT_DISABLED);
        } catch (BadCredentialsException e) {
            throw new BusinessException(ErrorCode.INVALID_CREDENTIALS);
        }

        User user = userRepository.findByUsername(request.username())
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
        UserPrincipal principal = (UserPrincipal) userDetailsService.loadUserByUsername(user.getUsername());
        String token = jwtService.generateToken(principal, user.getRole().name());
        return new LoginResponse(token, "Bearer", jwtService.getExpirationMs(), UserResponse.from(user));
    }
}