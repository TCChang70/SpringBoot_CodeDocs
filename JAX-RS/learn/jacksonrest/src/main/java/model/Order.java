package model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDateTime;
import java.util.List;

public class Order {

    private int orderId;

    @JsonProperty("items")
    private List<OrderItem> items;

    private double total;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm")
    private LocalDateTime orderTime;

    public Order() {}

    public Order(int orderId, List<OrderItem> items, LocalDateTime orderTime) {
        this.orderId = orderId;
        this.items = items;
        this.total = items.stream()
                .mapToDouble(OrderItem::getSubtotal).sum();
        this.orderTime = orderTime;
    }

    // ── 巢狀類別 ────────────────────────────────────
    public static class OrderItem {
        private String productName;
        private int quantity;
        private double unitPrice;

        public OrderItem() {}
        public OrderItem(String productName, int quantity, double unitPrice) {
            this.productName = productName;
            this.quantity = quantity;
            this.unitPrice = unitPrice;
        }
        public double getSubtotal() { return quantity * unitPrice; }
        public String getProductName() { return productName; }
        public void setProductName(String productName) { this.productName = productName; }
        public int getQuantity() { return quantity; }
        public void setQuantity(int quantity) { this.quantity = quantity; }
        public double getUnitPrice() { return unitPrice; }
        public void setUnitPrice(double unitPrice) { this.unitPrice = unitPrice; }
		@Override
		public String toString() {
			return "OrderItem [productName=" + productName + ", quantity=" + quantity + ", unitPrice=" + unitPrice
					+ "]";
		}
        
    }

    // getters/setters
    public int getOrderId() { return orderId; }
    public void setOrderId(int orderId) { this.orderId = orderId; }
    public List<OrderItem> getItems() { return items; }
    public void setItems(List<OrderItem> items) { this.items = items; }
    public double getTotal() { return total; }
    public void setTotal(double total) { this.total = total; }
    public LocalDateTime getOrderTime() { return orderTime; }
    public void setOrderTime(LocalDateTime orderTime) { this.orderTime = orderTime; }

	@Override
	public String toString() {
		return "Order [orderId=" + orderId + ", items=" + items + ", total=" + total + ", orderTime=" + orderTime + "]";
	}
    
}
