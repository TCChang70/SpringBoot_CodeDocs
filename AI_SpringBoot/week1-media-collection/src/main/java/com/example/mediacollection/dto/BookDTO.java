package com.example.mediacollection.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 書籍回應 DTO（Data Transfer Object）。
 *
 * 職責：將 Service 層資料傳遞給 Controller，再回傳給前端。
 * 設計原則：隔離 Entity 與 API 回應格式，避免直接暴露資料庫欄位。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookDTO {

    /** 書籍唯一識別碼 */
    private Long id;

    /** 書名 */
    private String title;

    /** 作者（嵌入式物件，含 id、name、biography） */
    private AuthorDTO author;

    /** 國際標準書號 */
    private String isbn;

    /** 書籍類型（小說/科幻/歷史/技術...） */
    private String genre;

    /** 書籍簡介 */
    private String description;

    /** 出版年份 */
    private Integer publishYear;

    /** 是否已閱讀 */
    private Boolean isRead;

    /** 資料建立時間 */
    private LocalDateTime createdAt;

    /** 資料最後更新時間 */
    private LocalDateTime updatedAt;
}
