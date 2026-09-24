package com.example.cms.media.storage;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

/**
 * 本機磁碟儲存實作。
 * 檔案存放於 {@code app.media.storage-location}/{yyyy/MM}/{uuid}.{ext}。
 */
@Component
public class LocalFileStorage implements MediaStorage {

    private final Path root;

    public LocalFileStorage(@Value("${app.media.storage-location}") String storageLocation) {
        this.root = Path.of(storageLocation).toAbsolutePath().normalize();
    }

    @PostConstruct
    void init() throws IOException {
        Files.createDirectories(root);
    }

    @Override
    public String store(String originalFilename, MultipartFile file) {
        try {
            String ext = extension(originalFilename);
            String sub = java.time.LocalDate.now().toString().replace("-", "/");
            Path dir = root.resolve(sub);
            Files.createDirectories(dir);
            String key = UUID.randomUUID().toString().replace("-", "") + ext;
            try (InputStream in = file.getInputStream()) {
                Files.copy(in, dir.resolve(key), StandardCopyOption.REPLACE_EXISTING);
            }
            return sub + "/" + key;
        } catch (IOException e) {
            throw new com.example.cms.common.BusinessException(
                    com.example.cms.common.ErrorCode.INTERNAL_ERROR, "檔案儲存失敗");
        }
    }

    @Override
    public Resource load(String key) {
        try {
            Path file = root.resolve(key).normalize();
            if (!file.startsWith(root)) {
                throw new com.example.cms.common.BusinessException(
                        com.example.cms.common.ErrorCode.MEDIA_NOT_FOUND);
            }
            Resource resource = new UrlResource(file.toUri());
            if (!resource.exists() || !resource.isReadable()) {
                throw new com.example.cms.common.BusinessException(
                        com.example.cms.common.ErrorCode.MEDIA_NOT_FOUND);
            }
            return resource;
        } catch (MalformedURLException e) {
            throw new com.example.cms.common.BusinessException(
                    com.example.cms.common.ErrorCode.MEDIA_NOT_FOUND);
        }
    }

    @Override
    public void delete(String key) {
        try {
            Path file = root.resolve(key).normalize();
            if (file.startsWith(root)) {
                Files.deleteIfExists(file);
            }
        } catch (IOException ignored) {
            // 靜默忽略
        }
    }

    private String extension(String filename) {
        if (filename == null) {
            return "";
        }
        int idx = filename.lastIndexOf('.');
        return idx >= 0 ? filename.substring(idx).toLowerCase() : "";
    }
}