package com.example.cms.user.controller;

import com.example.cms.common.ApiResponse;
import com.example.cms.common.PageResult;
import com.example.cms.config.SecurityUtils;
import com.example.cms.user.dto.AdminCreateUserRequest;
import com.example.cms.user.dto.ChangePasswordRequest;
import com.example.cms.user.dto.UpdateProfileRequest;
import com.example.cms.user.dto.UpdateUserRequest;
import com.example.cms.user.dto.UserResponse;
import com.example.cms.user.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 使用者管理端點：/users/me（登入者）、/users/{id}（admin）。
 */
@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final SecurityUtils securityUtils;

    @GetMapping("/me")
    public ApiResponse<UserResponse> me() {
        return ApiResponse.success(userService.getMe(securityUtils.currentUserId()));
    }

    @PutMapping("/me")
    public ApiResponse<UserResponse> updateProfile(@Valid @RequestBody UpdateProfileRequest request) {
        return ApiResponse.success(userService.updateProfile(securityUtils.currentUserId(), request));
    }

    @PutMapping("/me/password")
    public ApiResponse<Void> changePassword(@Valid @RequestBody ChangePasswordRequest request) {
        userService.changePassword(securityUtils.currentUserId(), request);
        return ApiResponse.success();
    }

    /* -------------------- admin -------------------- */

    @GetMapping
    @PreAuthorize("hasRole('admin')")
    public ApiResponse<PageResult<UserResponse>> list(@PageableDefault(size = 20) Pageable pageable) {
        return ApiResponse.success(PageResult.from(userService.list(pageable)));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('admin')")
    public ApiResponse<UserResponse> get(@PathVariable Long id) {
        return ApiResponse.success(userService.get(id));
    }

    @PostMapping
    @PreAuthorize("hasRole('admin')")
    public ApiResponse<UserResponse> create(@Valid @RequestBody AdminCreateUserRequest request) {
        return ApiResponse.success(userService.create(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('admin')")
    public ApiResponse<UserResponse> update(@PathVariable Long id, @Valid @RequestBody UpdateUserRequest request) {
        return ApiResponse.success(userService.update(id, request));
    }

    @PatchMapping("/{id}/disable")
    @PreAuthorize("hasRole('admin')")
    public ApiResponse<Void> disable(@PathVariable Long id) {
        userService.setEnabled(id, false);
        return ApiResponse.success();
    }

    @PatchMapping("/{id}/enable")
    @PreAuthorize("hasRole('admin')")
    public ApiResponse<Void> enable(@PathVariable Long id) {
        userService.setEnabled(id, true);
        return ApiResponse.success();
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('admin')")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        userService.delete(id);
        return ApiResponse.success();
    }
}