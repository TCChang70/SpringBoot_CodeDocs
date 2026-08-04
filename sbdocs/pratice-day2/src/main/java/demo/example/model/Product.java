package demo.example.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import jakarta.persistence.*;

@Entity
@Table(name = "products")       // 對應資料庫中的 products 表
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)  // MySQL AUTO_INCREMENT
    private Long id;

    @Column(nullable = false)   // NOT NULL：商品名稱必填
    private String name;

    @Column(nullable = false)   // NOT NULL：價格必填
    private Double price;

    private Integer stock;      // 允許 null：庫存可以不設定

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    @JsonIgnoreProperties("products")   // 序列化 category 時，忽略其 products 欄位
    private Category category;

    public Category getCategory() { return category; }
    public void setCategory(Category category) { this.category = category; }

    // ★ JPA 必須有無參數建構子（JPA 反射建立物件時使用）
    public Product() {}

    // 帶參數建構子，方便在測試或 Service 中快速建立物件
    public Product(String name, Double price, Integer stock, Category category) {
        this.name = name;
        this.price = price;
        this.stock = stock;
        this.category = category;
    }

    // Getter / Setter
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public Double getPrice() { return price; }
    public void setPrice(Double price) { this.price = price; }
    public Integer getStock() { return stock; }
    public void setStock(Integer stock) { this.stock = stock; }
    
}
