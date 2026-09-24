package com.example.cms.media.storage;

import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

/**
 * 檔案儲存抽象（SD §7 NFR-04）。
 * 目前實作 {@link LocalFileStorage}；可擴充物件儲存（S3 / MinIO）實作。
 */
public interface MediaStorage {

    /**
     * 儲存檔案並回傳可辨識之儲存 key（寫入 media.file_path）。
     */
    String store(String originalFilename, MultipartFile file);

    /**
     * 載入資源（供下載／預覽）。
     */
    Resource load(String key);

    /**
     * 刪除實體檔案（不存在時靜默忽略）。
     */
    void delete(String key);
}