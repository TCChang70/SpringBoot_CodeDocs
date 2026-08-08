package shop.example.demo.model;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "orders")
@Schema(description = "訂單主檔資料模型")
@Data
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(description = "訂單 ID（自動產生）", example = "1", accessMode = Schema.AccessMode.READ_ONLY)
    private Long id;

    @Column(name = "order_no", nullable = false, unique = true)
    @Schema(description = "訂單編號（不可重複）", example = "ORD-1722900000000", requiredMode = Schema.RequiredMode.REQUIRED)
    private String orderNo;

    @Column(name = "customer_name", nullable = false)
    @Schema(description = "客戶名稱", example = "Alice", requiredMode = Schema.RequiredMode.REQUIRED)
    private String customerName;

    @Column(name = "order_date")
    @Schema(description = "訂單日期", example = "2026-08-06T10:00:00")
    private LocalDateTime orderDate;

    @Column(name = "total_amount")
    @Schema(description = "訂單總金額", example = "47890.0")
    private Double totalAmount;

    @OneToMany(mappedBy = "order", fetch = FetchType.LAZY,
               cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnoreProperties("order")
    @Schema(description = "訂單明細清單", accessMode = Schema.AccessMode.READ_ONLY)
    private List<OrderItem> items = new ArrayList<>();

    public Order() {}

//    public Long getId() { return id; }
//    public void setId(Long id) { this.id = id; }
//    public String getOrderNo() { return orderNo; }
//    public void setOrderNo(String orderNo) { this.orderNo = orderNo; }
//    public String getCustomerName() { return customerName; }
//    public void setCustomerName(String customerName) { this.customerName = customerName; }
//    public LocalDateTime getOrderDate() { return orderDate; }
//    public void setOrderDate(LocalDateTime orderDate) { this.orderDate = orderDate; }
//    public Double getTotalAmount() { return totalAmount; }
//    public void setTotalAmount(Double totalAmount) { this.totalAmount = totalAmount; }
//    public List<OrderItem> getItems() { return items; }
//    public void setItems(List<OrderItem> items) { this.items = items; }

    // 方便新增明細
    public void addItem(OrderItem item) {
        items.add(item);
        item.setOrder(this);
    }

    @Override
    public String toString() {
        return "Order{id=" + id + ", orderNo='" + orderNo + "', customerName='" + customerName + "'}";
    }
}
