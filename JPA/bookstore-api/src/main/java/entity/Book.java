package entity;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "books")
@Schema(description = "書籍實體")
public class Book {

 @Id
 @GeneratedValue(strategy = GenerationType.IDENTITY)
 @Schema(description = "主鍵（自動產生，寫入時不需填）", example = "1", accessMode = Schema.AccessMode.READ_ONLY)
 private Long id;

 @Column(nullable = false, length = 200)
 @Schema(description = "書名", example = "Java 程式設計", requiredMode = Schema.RequiredMode.REQUIRED)
 private String title;

 @Column(nullable = false, length = 100)
 @Schema(description = "作者", example = "張三", requiredMode = Schema.RequiredMode.REQUIRED)
 private String author;

 @Column(length = 20)
 @Schema(description = "ISBN", example = "978-111-222-333")
 private String isbn;

 @Column(nullable = false)
 @Schema(description = "價格", example = "599.0", requiredMode = Schema.RequiredMode.REQUIRED)
 private Double price;

 @Column(name = "publish_date")
 @Schema(description = "出版日期（格式 yyyy-MM-dd）", example = "2024-05-01")
 private LocalDate publishDate;

 @Column(length = 50)
 @Schema(description = "分類（可用於查詢篩選）", example = "程式設計")
 private String category;

 @Column(name = "stock")
 @Schema(description = "庫存數量", example = "50")
 private Integer stock;

 @Column(name = "created_at", updatable = false)
 @Schema(description = "建立時間（自動產生，不需填）", accessMode = Schema.AccessMode.READ_ONLY)
 private LocalDateTime createdAt;

 @Column(name = "updated_at")
 @Schema(description = "更新時間（自動產生，不需填）", accessMode = Schema.AccessMode.READ_ONLY)
 private LocalDateTime updatedAt;

 @PrePersist
 protected void onCreate() {
     createdAt = LocalDateTime.now();
     updatedAt = LocalDateTime.now();
 }

 @PreUpdate
 protected void onUpdate() {
     updatedAt = LocalDateTime.now();
 }

 // ===== Getters & Setters =====

 public Long getId() { return id; }
 public void setId(Long id) { this.id = id; }

 public String getTitle() { return title; }
 public void setTitle(String title) { this.title = title; }

 public String getAuthor() { return author; }
 public void setAuthor(String author) { this.author = author; }

 public String getIsbn() { return isbn; }
 public void setIsbn(String isbn) { this.isbn = isbn; }

 public Double getPrice() { return price; }
 public void setPrice(Double price) { this.price = price; }

 public LocalDate getPublishDate() { return publishDate; }
 public void setPublishDate(LocalDate publishDate) { this.publishDate = publishDate; }

 public String getCategory() { return category; }
 public void setCategory(String category) { this.category = category; }

 public Integer getStock() { return stock; }
 public void setStock(Integer stock) { this.stock = stock; }

 public LocalDateTime getCreatedAt() { return createdAt; }
 public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

 public LocalDateTime getUpdatedAt() { return updatedAt; }
 public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}