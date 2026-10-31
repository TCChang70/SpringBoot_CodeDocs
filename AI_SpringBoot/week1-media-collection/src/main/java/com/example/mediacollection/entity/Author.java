package com.example.mediacollection.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 作者實體類別，對應資料庫 authors 資料表。
 *
 * 欄位說明：
 * - id: 自動遞增主鍵
 * - name: 作者名稱（必填，最長 100 字元）
 * - biography: 作者簡介（TEXT 類型）
 * - books: 該作者著作的書籍清單（1:N 反向關聯）
 * - createdAt / updatedAt: 建立/更新時間戳
 *
 * JPA 關聯設計（Week 2 核心）：
 * - @OneToMany(mappedBy = "author")
 *   → 在 Book 側已定義 @ManyToOne author，此處透過 mappedBy 建立雙向映射
 *   → 資料庫外鍵實際在 books 表的 author_id 欄位
 *   → Author 端只是「虛擬」的導航屬性，不影響資料庫結構
 *
 * - cascade = CascadeType.ALL
 *   → 刪除作者時自動刪除該作者的所有書籍（慎用！通常應為 REMOVE 而非 ALL）
 *
 * - orphanRemoval = true
 *   → 當書籍從作者的清單中移除時，自動刪除該書籍
 */
@Entity
@Table(name = "authors")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Author {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String biography;

    /**
     * 1:N 關聯：一個作者對應多本書籍。
     *
     * mappedBy = "author" → Book 類別的 author 欄位定義了外鍵，此處無需再定義
     * cascade = CascadeType.ALL → 級聯操作（新增/更新/刪除時同步）
     * orphanRemoval = true → 移除書籍時自動刪除（孤立資料清理）
     *
     * fetch = FetchType.LAZY（Week 3 效能優化會講）
     *   → 預設延遲載入，使用時才查詢書籍清單
     *   → 避免 N+1 查詢問題
     */
    @OneToMany(mappedBy = "author", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<Book> books;

    @Column(updatable = false)
    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
