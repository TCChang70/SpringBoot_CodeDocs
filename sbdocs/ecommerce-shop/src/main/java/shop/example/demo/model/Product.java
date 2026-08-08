package shop.example.demo.model;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "products")
@Schema(description = "3C 商品資料模型")
@Data
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(description = "商品 ID（自動產生）", example = "1", accessMode = Schema.AccessMode.READ_ONLY)
    private Long id;

    @Column(nullable = false)
    @Schema(description = "商品名稱", example = "iPhone 15 Pro", requiredMode = Schema.RequiredMode.REQUIRED)
    private String name;

    @Column(nullable = false)
    @Schema(description = "品牌", example = "Apple", requiredMode = Schema.RequiredMode.REQUIRED)
    private String brand;

    @Column(nullable = false)
    @Schema(description = "價格", example = "39900.0", requiredMode = Schema.RequiredMode.REQUIRED)
    private Double price;

    @Schema(description = "庫存數量（可為 null）", example = "20")
    private Integer stock;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    @JsonIgnoreProperties("products")
    @Schema(description = "所屬分類（多對一關聯）")
    private Category category;

    public Category getCategory() { return category; }
    public void setCategory(Category category) { this.category = category; }

    // ★ JPA 必須有無參數建構子（JPA 反射建立物件時使用）
    public Product() {}

    // 帶參數建構子，方便在測試或 Service 中快速建立物件
    public Product(String name, String brand, Double price, Integer stock, Category category) {
        this.name = name;
        this.brand = brand;
        this.price = price;
        this.stock = stock;
        this.category = category;
    }

//    public Long getId() { return id; }
//    public void setId(Long id) { this.id = id; }
//    public String getName() { return name; }
//    public void setName(String name) { this.name = name; }
//    public String getBrand() { return brand; }
//    public void setBrand(String brand) { this.brand = brand; }
//    public Double getPrice() { return price; }
//    public void setPrice(Double price) { this.price = price; }
//    public Integer getStock() { return stock; }
//    public void setStock(Integer stock) { this.stock = stock; }
}
