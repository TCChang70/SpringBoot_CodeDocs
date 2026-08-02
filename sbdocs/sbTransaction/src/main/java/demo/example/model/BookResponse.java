package demo.example.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
// 回傳給客戶端的資料格式（控制哪些欄位回傳）
public class BookResponse {

    private Long id;
    private String title;
    private String author;
    private String isbn;
    private BigDecimal price;
    private Integer stock;
    private String category;
    private LocalDateTime createdAt;
    // 注意：可依需求選擇性排除欄位，例如管理後台可加 stock，前台可不加

    // 靜態工廠方法：從 Entity 轉換成 DTO（方便在 Service/Controller 中呼叫）
    public static BookResponse from(Book book) {
        BookResponse response = new BookResponse();
        response.id = book.getId();
        response.title = book.getTitle();
        response.author = book.getAuthor();
        response.isbn = book.getIsbn();
        response.price = book.getPrice();
        response.stock = book.getStock();
        response.category = book.getCategory();
        response.createdAt = book.getCreatedAt();
        return response;
    }

    // 批次轉換（Controller 的 getAll() 使用）
    public static List<BookResponse> fromList(List<Book> books) {
        return books.stream()
                .map(BookResponse::from)
                .toList();
    }

    // Getters（不需要 Setters，因為 Response 物件只讀）
    public Long getId() { return id; }
    public String getTitle() { return title; }
    public String getAuthor() { return author; }
    public String getIsbn() { return isbn; }
    public BigDecimal getPrice() { return price; }
    public Integer getStock() { return stock; }
    public String getCategory() { return category; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}
