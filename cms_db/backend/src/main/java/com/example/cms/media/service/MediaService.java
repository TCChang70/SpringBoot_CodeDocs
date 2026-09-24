package com.example.cms.media.service;

import com.example.cms.common.BusinessException;
import com.example.cms.common.ErrorCode;
import com.example.cms.config.SecurityUtils;
import com.example.cms.media.dto.MediaResponse;
import com.example.cms.media.entity.Media;
import com.example.cms.media.repository.MediaRepository;
import com.example.cms.media.storage.MediaStorage;
import com.example.cms.user.entity.User;
import com.example.cms.user.entity.UserRole;
import com.example.cms.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.Set;

/**
 * 多媒體管理（FR-06）。
 */
@Service
@RequiredArgsConstructor
public class MediaService {

    private final MediaRepository mediaRepository;
    private final MediaStorage mediaStorage;
    private final UserRepository userRepository;
    private final SecurityUtils securityUtils;

    @Value("${app.media.allowed-types}")
    private String[] allowedTypes;

    @Value("${app.media.max-size}")
    private long maxSize;

    @Transactional
    public MediaResponse upload(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "請選擇要上傳的檔案");
        }
        // NFR-02 上傳驗證：MIME 類型與大小（FR-06-04）
        String contentType = file.getContentType();
        if (!java.util.Arrays.asList(allowedTypes).contains(contentType)) {
            throw new BusinessException(ErrorCode.VALIDATION_FAILED, "不允許的檔案類型：" + contentType);
        }
        if (file.getSize() > maxSize) {
            throw new BusinessException(ErrorCode.VALIDATION_FAILED, "檔案大小超過上限");
        }

        Long uploaderId = securityUtils.currentUserId();
        User uploader = userRepository.findById(uploaderId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        String key = mediaStorage.store(file.getOriginalFilename(), file);
        Media media = new Media();
        media.setUploader(uploader);
        media.setFileName(file.getOriginalFilename());
        media.setFilePath(key);
        media.setFileType(contentType);
        media.setFileSize(file.getSize());
        return MediaResponse.from(mediaRepository.save(media));
    }

    @Transactional(readOnly = true)
    public Page<MediaResponse> list(Pageable pageable) {
        Page<Media> page;
        if (isAdminOrEditor()) {
            page = mediaRepository.findAllByOrderByCreatedAtDesc(pageable);
        } else {
            page = mediaRepository.findByUploaderIdOrderByCreatedAtDesc(securityUtils.currentUserId(), pageable);
        }
        return page.map(MediaResponse::from);
    }

    @Transactional(readOnly = true)
    public Resource loadFile(Long id) {
        Media media = getMedia(id);
        return mediaStorage.load(media.getFilePath());
    }

    @Transactional
    public MediaResponse updateAltText(Long id, String altText) {
        Media media = getMedia(id);
        requireOwnerOrAdmin(media);
        media.setAltText(altText);
        return MediaResponse.from(mediaRepository.save(media));
    }

    @Transactional
    public void delete(Long id) {
        Media media = getMedia(id);
        requireOwnerOrAdminOnly(media); // SD §5.2：uploader 本人或 admin
        mediaStorage.delete(media.getFilePath());
        mediaRepository.delete(media);
    }

    private void requireOwnerOrAdmin(Media media) {
        if (isAdminOrEditor()) {
            return;
        }
        if (media.getUploader() == null || !media.getUploader().getId().equals(securityUtils.currentUserId())) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }
    }

    /** 刪除僅允許上傳者本人或 admin（editor 不得刪除他人檔案）。 */
    private void requireOwnerOrAdminOnly(Media media) {
        User current = securityUtils.currentPrincipal().getUser();
        if (current.getRole() == UserRole.admin) {
            return;
        }
        if (media.getUploader() == null || !media.getUploader().getId().equals(current.getId())) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }
    }

    private boolean isAdminOrEditor() {
        User current = securityUtils.currentPrincipal().getUser();
        return current.getRole() == UserRole.admin || current.getRole() == UserRole.editor;
    }

    private Media getMedia(Long id) {
        return mediaRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.MEDIA_NOT_FOUND));
    }
}