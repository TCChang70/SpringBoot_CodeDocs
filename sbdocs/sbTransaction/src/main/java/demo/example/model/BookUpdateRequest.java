package demo.example.model;

import java.math.BigDecimal;

import jakarta.validation.constraints.*;

// 修改書籍時的資料格式（所有欄位可選填，只更新有傳入的欄位）
public class BookUpdateRequest {

    @NotBlank(message = "書名不得為空")
    private String title;

    @NotBlank(message = "作者不得為空")
    private String author;

    @Pattern(regexp = "^[0-9-]{10,17}$", message = "ISBN 格式不正確")
    private String isbn;

    @Positive(message = "價格必須大於 0")
    private BigDecimal price;

    @Min(value = 0, message = "庫存不可為負數")
    private Integer stock;

    private String category;

    // Getters and Setters（同上）
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
