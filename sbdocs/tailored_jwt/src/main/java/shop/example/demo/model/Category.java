package shop.example.demo.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "categories")
@Schema(description = "3C 商品分類資料模型")
@Data
public class Category {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(description = "分類 ID（自動產生）", example = "1", accessMode = Schema.AccessMode.READ_ONLY)
    private Long id;

    @Column(nullable = false, unique = true)
    @Schema(description = "分類名稱（不可重複）", example = "手機", requiredMode = Schema.RequiredMode.REQUIRED)
    private String name;

    @OneToMany(mappedBy = "category", fetch = FetchType.LAZY)
    @JsonIgnoreProperties("category")
    @Schema(description = "此分類下的商品清單（LAZY，預設不載入）", accessMode = Schema.AccessMode.READ_ONLY)
    private List<Product> products = new ArrayList<>();

    public Category() {}
    public Category(String name) { this.name = name; }

//    public Long getId() { return id; }
//    public void setId(Long id) { this.id = id; }
//    public String getName() { return name; }
//    public void setName(String name) { this.name = name; }
//    public List<Product> getProducts() { return products; }
//    public void setProducts(List<Product> products) { this.products = products; }

    // ⚠️ 若有 toString()，切勿直接印出 products（會觸發 LAZY 載入並可能遞迴）
    @Override
    public String toString() {
        return "Category{id=" + id + ", name='" + name + "'}";
    }
}
