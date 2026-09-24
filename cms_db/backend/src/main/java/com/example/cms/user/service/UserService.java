package com.example.cms.user.service;

import com.example.cms.common.BusinessException;
import com.example.cms.common.ErrorCode;
import com.example.cms.config.SecurityUtils;
import com.example.cms.user.dto.AdminCreateUserRequest;
import com.example.cms.user.dto.ChangePasswordRequest;
import com.example.cms.user.dto.UpdateProfileRequest;
import com.example.cms.user.dto.UpdateUserRequest;
import com.example.cms.user.dto.UserResponse;
import com.example.cms.user.entity.User;
import com.example.cms.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 使用者管理（FR-01-05 / FR-01-06）。
 * 分一般使用者（自己）與 admin（管理他人）兩類操作。
 */
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final SecurityUtils securityUtils;

    @Transactional(readOnly = true)
    public UserResponse getMe(Long userId) {
        return UserResponse.from(getUser(userId));
    }

    @Transactional
    public UserResponse updateProfile(Long userId, UpdateProfileRequest request) {
        User user = getUser(userId);
        user.setDisplayName(request.displayName());
        user.setAvatarUrl(request.avatarUrl());
        return UserResponse.from(userRepository.save(user));
    }

    @Transactional
    public void changePassword(Long userId, ChangePasswordRequest request) {
        User user = getUser(userId);
        if (!passwordEncoder.matches(request.oldPassword(), user.getPasswordHash())) {
            throw new BusinessException(ErrorCode.INVALID_CREDENTIALS);
        }
        user.setPasswordHash(passwordEncoder.encode(request.newPassword()));
        userRepository.save(user);
    }

    /* -------------------- admin 管理 -------------------- */

    @Transactional
    public UserResponse create(AdminCreateUserRequest request) {
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
        user.setDisplayName(request.displayName());
        user.setRole(request.role());
        user.setEnabled(true);
        return UserResponse.from(userRepository.save(user));
    }

    @Transactional(readOnly = true)
    public Page<UserResponse> list(Pageable pageable) {
        return userRepository.findAll(pageable).map(UserResponse::from);
    }

    @Transactional(readOnly = true)
    public UserResponse get(Long id) {
        return UserResponse.from(getUser(id));
    }

    @Transactional
    public UserResponse update(Long id, UpdateUserRequest request) {
        User user = getUser(id);
        if (request.role() != null) {
            user.setRole(request.role());
        }
        if (request.email() != null && !request.email().isBlank()) {
            userRepository.findByEmail(request.email())
                    .filter(other -> !other.getId().equals(id))
                    .ifPresent(other -> {
                        throw new BusinessException(ErrorCode.EMAIL_EXISTS);
                    });
            user.setEmail(request.email());
        }
        if (request.displayName() != null) {
            user.setDisplayName(request.displayName());
        }
        return UserResponse.from(userRepository.save(user));
    }

    @Transactional
    public void setEnabled(Long id, boolean enabled) {
        User user = getUser(id);
        if (!enabled && user.getId().equals(securityUtils.currentUserId())) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "無法停用目前登入的帳號");
        }
        user.setEnabled(enabled);
        userRepository.save(user);
    }

    @Transactional
    public void delete(Long id) {
        User user = getUser(id);
        if (userRepository.existsByAuthorId(id)) {
            throw new BusinessException(ErrorCode.USER_HAS_ARTICLES);
        }
        userRepository.delete(user);
    }

    private User getUser(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
    }
}