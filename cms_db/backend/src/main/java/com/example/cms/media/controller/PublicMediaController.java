package com.example.cms.media.controller;

import com.example.cms.media.service.MediaService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

/**
 * 公開檔案存取端點（SD §4.2 Media），提供內嵌（inline）預覽與下載。
 */
@RestController
@RequiredArgsConstructor
public class PublicMediaController {

    private final MediaService mediaService;

    @GetMapping("/media/files/{id}")
    public ResponseEntity<Resource> file(@PathVariable Long id) {
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(mediaService.loadFile(id));
    }
}