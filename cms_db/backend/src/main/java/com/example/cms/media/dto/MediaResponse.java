package com.example.cms.media.dto;

import com.example.cms.media.entity.Media;

import java.time.LocalDateTime;

/**
 * 多媒體回應（FR-06）。
 */
public record MediaResponse(
        Long id,
        String fileName,
        String filePath,
        String fileType,
        long fileSize,
        String altText,
        String url,
        String uploaderName,
        LocalDateTime createdAt
) {
    public static MediaResponse from(Media media) {
        String uploader = media.getUploader() == null ? null
                : (media.getUploader().getDisplayName() == null || media.getUploader().getDisplayName().isBlank()
                        ? media.getUploader().getUsername()
                        : media.getUploader().getDisplayName());
        return new MediaResponse(
                media.getId(),
                media.getFileName(),
                media.getFilePath(),
                media.getFileType(),
                media.getFileSize() == null ? 0 : media.getFileSize(),
                media.getAltText(),
                "/media/files/" + media.getId(),
                uploader,
                media.getCreatedAt());
    }
}