package com.example.cms.config;

import com.example.cms.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

/**
 * 提供目前登入使用者資訊的小工具。
 */
@Component
@RequiredArgsConstructor
public class SecurityUtils {

    public UserPrincipal currentPrincipal() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !(auth.getPrincipal() instanceof UserPrincipal principal)) {
            throw new com.example.cms.common.BusinessException(com.example.cms.common.ErrorCode.UNAUTHORIZED);
        }
        return principal;
    }

    public Long currentUserId() {
        return currentPrincipal().getId();
    }
}