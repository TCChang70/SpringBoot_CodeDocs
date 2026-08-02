package demo.example.model;


import java.math.BigDecimal;

import jakarta.validation.constraints.*;

// 新增書籍時，客戶端傳入的資料格式（不含 id，因為 id 由資料庫自動產生）
public class BookCreateRequest {

    @NotBlank(message = "書名不得為空")
    @Size(max = 200, message = "書名長度不可超過 200")
    private String title;

    @NotBlank(message = "作者不得為空")
    private String author;

    @NotBlank(message = "ISBN 不得為空")
    @Pattern(regexp = "^[0-9-]{10,17}$", message = "ISBN 格式不正確")
    private String isbn;

    @NotNull(message = "價格不得為空")
    @Positive(message = "價格必須大於 0")
    private BigDecimal price;

    @NotNull(message = "庫存不得為空")
    @Min(value = 0, message = "庫存不可為負數")
    private Integer stock;

    private String category;  // 分類可為空（選填）

    // Getters and Setters
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getAuthor() { return author; }
    public void setAuthor(String author) { this.author = author; }
    public String getIsbn() { return isbn; }
    public void setIsbn(String isbn) { this.isbn = isbn; }
    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }
    public Integer getStock() { return stock; }
    public void setStock(Integer stock) { this.stock = stock; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
}
