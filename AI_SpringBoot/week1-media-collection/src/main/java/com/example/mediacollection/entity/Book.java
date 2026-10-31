package com.example.mediacollection.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 書籍實體類別，對應資料庫 books 資料表。
 *
 * Week 2 重要更新：採用 JPA 關聯映射
 *
 * 欄位說明：
 * - id: 自動遞增主鍵
 * - title: 書名（必填，最長 200 字元）
 * - author: 作者實體對象（@ManyToOne 關聯，取代 Week 1 的字符串）
 * - isbn: 國際標準書號（可選）
 * - genre: 書籍類型（如：小說、科幻、歷史）
 * - description: 書籍簡介
 * - publishYear: 出版年份
 * - isRead: 是否已閱讀（預設 false）
 * - createdAt / updatedAt: 建立/更新時間戳（由 JPA 生命週期自動管理）
 *
 * Week 2 新增 JPA 關聯說明：
 * - @ManyToOne author: 多本書籍 → 一個作者（外鍵 author_id 在此表）
 * - @JoinColumn(name = "author_id", nullable = false): 定義外鍵欄位名稱
 */
@Entity
@Table(name = "books")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Book {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 200)
    private String title;

    /**
     * N:1 關聯：多本書籍 → 一個作者。
     *
     * @ManyToOne: 此方為「多」，Author 為「一」。
     * @JoinColumn(name = "author_id", nullable = false): 定義外鍵欄位名稱。
     * fetch = FetchType.EAGER: 立即載入作者資訊（Week 3 會改為 LAZY 優化效能）。
     *
     * 資料庫映射：
     * - books 表新增 author_id 欄位（指向 authors.id）
     * - 當查詢書籍時自動載入作者資訊，無需另外查詢
     */
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "author_id", nullable = false)
    private Author author;

    @Column(length = 20)
    private String isbn;

    @Column(length = 50)
    private String genre;

    @Column(columnDefinition = "TEXT")
    private String description;

    private Integer publishYear;

    /**
     * 閱讀狀態；@Builder.Default 確保 Builder 模式下預設值生效。
     */
    @Builder.Default
    private Boolean isRead = false;

    @Column(updatable = false)
    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    /**
     * JPA 持久化前自動設定建立/更新時間。
     */
    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * JPA 更新前自動刷新 updatedAt 時間戳。
     */
    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
