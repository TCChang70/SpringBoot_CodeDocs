package com.example.cms.media.service;

import com.example.cms.common.BusinessException;
import com.example.cms.common.ErrorCode;
import com.example.cms.config.SecurityUtils;
import com.example.cms.media.entity.Media;
import com.example.cms.media.repository.MediaRepository;
import com.example.cms.media.storage.MediaStorage;
import com.example.cms.security.UserPrincipal;
import com.example.cms.user.entity.User;
import com.example.cms.user.entity.UserRole;
import com.example.cms.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * 多媒體刪除權限測試（SD §5.2：uploader 本人或 admin 可刪除；editor 不得刪除他人檔案）。
 * 離線單元測試，不需資料庫，也不觸及 @Value 上傳驗證邏輯。
 */
@ExtendWith(MockitoExtension.class)
class MediaServiceTest {

    @Mock
    private MediaRepository mediaRepository;
    @Mock
    private MediaStorage mediaStorage;
    @Mock
    private UserRepository userRepository;
    @Mock
    private SecurityUtils securityUtils;

    private MediaService mediaService;

    @BeforeEach
    void setUp() {
        mediaService = new MediaService(mediaRepository, mediaStorage, userRepository, securityUtils);
    }

    private User user(long id, UserRole role) {
        User user = new User();
        user.setId(id);
        user.setUsername("user" + id);
        user.setRole(role);
        return user;
    }

    private Media ownedBy(User uploader) {
        Media media = new Media();
        media.setId(10L);
        media.setUploader(uploader);
        media.setFilePath("media/abc.png");
        return media;
    }

    private void loginAs(User user) {
        when(securityUtils.currentPrincipal()).thenReturn(new UserPrincipal(user));
    }

    @Test
    @DisplayName("上傳者本人可刪除自己的檔案")
    void delete_byUploader_success() {
        User uploader = user(1L, UserRole.author);
        Media media = ownedBy(uploader);
        loginAs(uploader);
        when(mediaRepository.findById(10L)).thenReturn(java.util.Optional.of(media));

        assertThatCode(() -> mediaService.delete(10L)).doesNotThrowAnyException();
        verify(mediaStorage).delete("media/abc.png");
        verify(mediaRepository).delete(media);
    }

    @Test
    @DisplayName("admin 可刪除任何檔案")
    void delete_byAdmin_success() {
        User uploader = user(1L, UserRole.author);
        User admin = user(9L, UserRole.admin);
        Media media = ownedBy(uploader);
        loginAs(admin);
        when(mediaRepository.findById(10L)).thenReturn(java.util.Optional.of(media));

        assertThatCode(() -> mediaService.delete(10L)).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("editor 不得刪除他人檔案（SD §5.2 僅限 uploader 或 admin）")
    void delete_byEditorForbidden() {
        User uploader = user(1L, UserRole.author);
        User editor = user(8L, UserRole.editor);
        Media media = ownedBy(uploader);
        loginAs(editor);
        when(mediaRepository.findById(10L)).thenReturn(java.util.Optional.of(media));

        assertThatThrownBy(() -> mediaService.delete(10L))
                .isInstanceOf(BusinessException.class)
                .extracting("code").isEqualTo(ErrorCode.FORBIDDEN.getCode());
    }

    @Test
    @DisplayName("非上傳者的 author 不可刪除")
    void delete_byOtherAuthorForbidden() {
        User uploader = user(1L, UserRole.author);
        User other = user(2L, UserRole.author);
        Media media = ownedBy(uploader);
        loginAs(other);
        when(mediaRepository.findById(10L)).thenReturn(java.util.Optional.of(media));

        assertThatThrownBy(() -> mediaService.delete(10L))
                .isInstanceOf(BusinessException.class)
                .extracting("code").isEqualTo(ErrorCode.FORBIDDEN.getCode());
    }
}