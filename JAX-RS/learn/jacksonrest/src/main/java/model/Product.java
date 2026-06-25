package model;

import com.fasterxml.jackson.annotation.*;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

public class Product {

    private int id;

    @JsonProperty("product_name")
    @JsonAlias({"productName", "product_name", "name"})
    private String name;

    private double price;

    @JsonIgnore
    private String internalCode;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createdAt;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String remark;

    public Product() {
        this.createdAt =  Date.from(
        	    LocalDateTime.now().plusHours(8)
                .atZone(ZoneId.systemDefault()).toInstant()
        );
    }

    public Product(int id, String name, double price) {
        this();
        this.id = id;
        this.name = name;
        this.price = price;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }

    public String getInternalCode() { return internalCode; }
    public void setInternalCode(String internalCode) { this.internalCode = internalCode; }

    public Date getCreatedAt() { return createdAt; }
    public void setCreatedAt(Date createdAt) { this.createdAt = createdAt; }

    public String getRemark() { return remark; }
    public void setRemark(String remark) { this.remark = remark; }
}
