package com.example.mediacollection.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 作者回應 DTO（Data Transfer Object）。
 *
 * 職責：將 Author Entity 轉為 API 回應格式，隔離資料庫層實現。
 * 設計原則：包含作者所有資訊，但不含 books 清單（避免序列化時無限遞迴）。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthorDTO {

    /** 作者唯一識別碼 */
    private Long id;

    /** 作者名稱 */
    private String name;

    /** 作者簡介 */
    private String biography;

    /** 建立時間 */
    private LocalDateTime createdAt;

    /** 最後更新時間 */
    private LocalDateTime updatedAt;
}
