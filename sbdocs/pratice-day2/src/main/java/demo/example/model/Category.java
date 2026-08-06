package demo.example.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonManagedReference; // ← 避免遞迴：此端正常序列化
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "categories")
@Schema(description = "類別資料模型")
public class Category {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(description = "類別 ID（自動產生）", example = "1", accessMode = Schema.AccessMode.READ_ONLY)
    private Long id;

    @Column(nullable = false, unique = true)  // 類別名稱不可重複
    @Schema(description = "類別名稱（不可重複）", example = "電腦", requiredMode = Schema.RequiredMode.REQUIRED)
    private String name;

 // ✅ 方法一：使用 @JsonManagedReference + @JsonBackReference（見下方 Entity 範例）
 // ✅ 方法二：使用 @JsonIgnoreProperties
    @OneToMany(mappedBy = "category", fetch = FetchType.LAZY)
    @JsonIgnoreProperties("category")   // 序列化 products 時，忽略每個 product 的 category 欄位
    @Schema(description = "此類別下的商品清單（LAZY，預設不載入）", accessMode = Schema.AccessMode.READ_ONLY)
    private List<Product> products = new ArrayList<>();

    public Category() {}
    public Category(String name) { this.name = name; }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public List<Product> getProducts() { return products; }
    public void setProducts(List<Product> products) { this.products = products; }

    // ⚠️ 若有 toString()，切勿直接印出 products（會觸發 LAZY 載入並可能遞迴）
    @Override
    public String toString() {
        return "Category{id=" + id + ", name='" + name + "'}";
    }
}
