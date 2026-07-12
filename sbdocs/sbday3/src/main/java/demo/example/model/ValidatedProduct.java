package demo.example.model;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;

public class ValidatedProduct {
    
    @NotBlank(message = "產品名稱不能為空")
    @Size(min = 2, max = 100, message = "產品名稱長度必須在 2-100 之間")
    private String name;
    
    @Size(max = 500, message = "產品描述不能超過 500 字元")
    private String description;
    
    @NotNull(message = "價格不能為空")
    @DecimalMin(value = "0.01", message = "價格必須大於 0")
    private BigDecimal price;
    
    @Min(value = 0, message = "庫存不能為負數")
    private int stock;
    
    @Email(message = "電子郵件格式不正確")
    private String contactEmail;
    
    // Getter 和 Setter
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }
    public int getStock() { return stock; }
    public void setStock(int stock) { this.stock = stock; }
    public String getContactEmail() { return contactEmail; }
    public void setContactEmail(String contactEmail) { this.contactEmail = contactEmail; }
}
