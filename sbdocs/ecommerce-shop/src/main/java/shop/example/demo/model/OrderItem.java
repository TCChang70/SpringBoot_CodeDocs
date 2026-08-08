package shop.example.demo.model;



import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "order_items")
@Schema(description = "訂單明細資料模型")
public class OrderItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(description = "明細 ID（自動產生）", example = "1", accessMode = Schema.AccessMode.READ_ONLY)
    private Long id;

    @Column(name = "product_id")
    @Schema(description = "商品 ID（快照）", example = "1")
    private Long productId;

    @Column(name = "product_name", nullable = false)
    @Schema(description = "商品名稱（下單當時的快照）", example = "iPhone 15 Pro", requiredMode = Schema.RequiredMode.REQUIRED)
    private String productName;

    @Column(nullable = false)
    @Schema(description = "單價（下單當時的快照）", example = "39900.0", requiredMode = Schema.RequiredMode.REQUIRED)
    private Double price;

    @Column(nullable = false)
    @Schema(description = "數量", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer quantity;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id")
    @JsonIgnoreProperties("items")
    @Schema(description = "所屬訂單（多對一關聯）")
    private Order order;

    public OrderItem() {}

    public OrderItem(Long productId, String productName, Double price, Integer quantity) {
        this.productId = productId;
        this.productName = productName;
        this.price = price;
        this.quantity = quantity;
    }

//    public Long getId() { return id; }
//    public void setId(Long id) { this.id = id; }
//    public Long getProductId() { return productId; }
//    public void setProductId(Long productId) { this.productId = productId; }
//    public String getProductName() { return productName; }
//    public void setProductName(String productName) { this.productName = productName; }
//    public Double getPrice() { return price; }
//    public void setPrice(Double price) { this.price = price; }
//    public Integer getQuantity() { return quantity; }
//    public void setQuantity(Integer quantity) { this.quantity = quantity; }
//    public Order getOrder() { return order; }
//    public void setOrder(Order order) { this.order = order; }
}
