package com.example.mediacollection.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 書籍建立/更新請求 DTO。
 *
 * 職責：接收前端傳入的 JSON 請求體，用於 POST（新增）與 PUT（更新）操作。
 * 設計原則：不含 id、createdAt、updatedAt 等系統欄位，防止前端竄改。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookRequestDTO {

    /** 書名（必填） */
    private String title;

    /** 作者 ID（必填，指向 authors 表的 id） */
    private Long authorId;

    /** 國際標準書號（選填） */
    private String isbn;

    /** 書籍類型，如：小說、科幻、技術 */
    private String genre;

    /** 書籍簡介 */
    private String description;

    /** 出版年份 */
    private Integer publishYear;

    /** 是否已閱讀（預設 false） */
    private Boolean isRead;
}
