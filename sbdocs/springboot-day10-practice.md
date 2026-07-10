# Day 10 — 訂單系統整合實作 實作練習

## 學習目標
- 透過實作掌握完整的訂單系統開發
- 理解分層架構與業務邏輯實作
- 學習異常處理、輸入驗證、DTO 設計
- 掌握企業級應用的最佳實踐

---

## 練習 1：基本訂單系統實作 ⭐

### 1.1 資料庫設計與 Migration
**目標**：建立完整的資料庫結構

**步驟**：
1. 建立 Flyway 遷移腳本
2. 建立基本的 Entity 類別
3. 建立 Repository 介面
4. 驗證資料庫結構

**實作**：

```sql
-- V1__create_order_system_tables.sql
CREATE TABLE customers (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    email VARCHAR(150) NOT NULL,
    phone VARCHAR(20),
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_customers_email (email)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE products (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(200) NOT NULL,
    description TEXT,
    price DECIMAL(10,2) NOT NULL,
    stock INT NOT NULL DEFAULT 0,
    category VARCHAR(50),
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE orders (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    customer_id BIGINT NOT NULL,
    order_date DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    total_amount DECIMAL(12,2) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    notes TEXT,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_orders_customer FOREIGN KEY (customer_id) REFERENCES customers(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE order_items (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_id BIGINT NOT NULL,
    product_id BIGINT NOT NULL,
    quantity INT NOT NULL,
    unit_price DECIMAL(10,2) NOT NULL,
    subtotal DECIMAL(12,2) NOT NULL,
    CONSTRAINT fk_order_items_order FOREIGN KEY (order_id) REFERENCES orders(id) ON DELETE CASCADE,
    CONSTRAINT fk_order_items_product FOREIGN KEY (product_id) REFERENCES products(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- V2__seed_data.sql
INSERT INTO customers (name, email, phone) VALUES
('Alice Chen', 'alice@test.com', '0912-345-678'),
('Bob Wang', 'bob@test.com', '0923-456-789'),
('Charlie Lin', 'charlie@test.com', '0934-567-890');

INSERT INTO products (name, description, price, stock, category) VALUES
('Java 程式設計', 'Java 基礎與進階教程', 680.00, 50, '書籍'),
('Spring Boot 實戰', 'Spring Boot 完全指南', 550.00, 30, '書籍'),
('資料庫系統概論', '資料庫設計與實作', 420.00, 20, '書籍'),
('演算法導論', '經典演算法教材', 780.00, 15, '書籍'),
('程式設計馬拉松', '程式設計競賽題目解析', 320.00, 25, '書籍');
```

### 1.2 Entity 類別實作
**目標**：建立完整的 Entity 類別

**實作**：

```java
package com.example.order.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "customers")
public class Customer {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false, length = 150, unique = true)
    private String email;

    @Column(length = 20)
    private String phone;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    public Customer() {}

    public Customer(String name, String email) {
        this.name = name;
        this.email = email;
    }

    // getters and setters
}
```

```java
package com.example.order.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "products")
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 200)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    @Column(nullable = false)
    private Integer stock;

    @Column(length = 50)
    private String category;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public Product() {}

    public Product(String name, BigDecimal price, Integer stock) {
        this.name = name;
        this.price = price;
        this.stock = stock;
    }

    // getters and setters
}
```

```java
package com.example.order.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "orders")
public class Order {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    @Column(name = "order_date", nullable = false)
    private LocalDateTime orderDate;

    @Column(name = "total_amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal totalAmount;

    @Column(nullable = false)
    private String status;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderItem> items = new ArrayList<>();

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        orderDate = LocalDateTime.now();
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (status == null) status = "PENDING";
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public Order() {}

    // helpers
    public void addItem(OrderItem item) {
        items.add(item);
        item.setOrder(this);
    }

    public void removeItem(OrderItem item) {
        items.remove(item);
        item.setOrder(null);
    }

    // getters and setters
}
```

```java
package com.example.order.model;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "order_items")
public class OrderItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(nullable = false)
    private Integer quantity;

    @Column(name = "unit_price", nullable = false, precision = 10, scale = 2)
    private BigDecimal unitPrice;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal subtotal;

    public OrderItem() {}

    public void calculateSubtotal() {
        this.subtotal = this.unitPrice.multiply(BigDecimal.valueOf(this.quantity));
    }

    // getters and setters
}
```

### 1.3 Repository 實作
**目標**：建立資料存取層

**實作**：

```java
package com.example.order.repository;

import com.example.order.model.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, Long> {
    Optional<Customer> findByEmail(String email);
    boolean existsByEmail(String email);
}
```

```java
package com.example.order.repository;

import com.example.order.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
    List<Product> findByStockGreaterThan(int minStock);
    List<Product> findByCategory(String category);
    
    @Query("SELECT p FROM Product p WHERE p.stock > 0 AND p.name LIKE %:keyword%")
    List<Product> searchByName(@Param("keyword") String keyword);
}
```

```java
package com.example.order.repository;

import com.example.order.model.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    List<Order> findByCustomerId(Long customerId);
    List<Order> findByStatus(String status);
    
    @Query("SELECT o FROM Order o JOIN FETCH o.items WHERE o.id = :id")
    Optional<Order> findByIdWithItems(@Param("id") Long id);
    
    @Query("SELECT o FROM Order o JOIN FETCH o.customer WHERE o.id = :id")
    Optional<Order> findByIdWithCustomer(@Param("id") Long id);
}
```

---

## 練習 2：異常處理與輸入驗證 ⭐⭐

### 2.1 自訂異常類別
**目標**：建立完整的異常處理機制

**實作**：

```java
package com.example.order.exception;

public class BusinessException extends RuntimeException {
    private final String errorCode;
    private final Object[] args;
    
    public BusinessException(String errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
        this.args = null;
    }
    
    public BusinessException(String errorCode, String message, Object[] args) {
        super(message);
        this.errorCode = errorCode;
        this.args = args;
    }
    
    public BusinessException(String errorCode, String message, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
        this.args = null;
    }
    
    public String getErrorCode() {
        return errorCode;
    }
    
    public Object[] getArgs() {
        return args;
    }
}
```

```java
package com.example.order.exception;

public class ResourceNotFoundException extends BusinessException {
    public ResourceNotFoundException(String resourceType, Long id) {
        super("RESOURCE_NOT_FOUND", resourceType + " 不存在，ID: " + id);
    }
    
    public ResourceNotFoundException(String resourceType, String identifier) {
        super("RESOURCE_NOT_FOUND", resourceType + " 不存在: " + identifier);
    }
}
```

```java
package com.example.order.exception;

public class InsufficientStockException extends BusinessException {
    private final String productName;
    private final int requestedQuantity;
    private final int availableStock;
    
    public InsufficientStockException(String productName, int requestedQuantity, int availableStock) {
        super("INSUFFICIENT_STOCK", 
            "庫存不足: " + productName + "，請求數量: " + requestedQuantity + "，可用庫存: " + availableStock);
        this.productName = productName;
        this.requestedQuantity = requestedQuantity;
        this.availableStock = availableStock;
    }
    
    public String getProductName() {
        return productName;
    }
    
    public int getRequestedQuantity() {
        return requestedQuantity;
    }
    
    public int getAvailableStock() {
        return availableStock;
    }
}
```

### 2.2 錯誤回應格式
**目標**：建立統一的錯誤回應格式

**實作**：

```java
package com.example.order.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.time.LocalDateTime;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class ErrorResponse {
    private String code;
    private String message;
    private LocalDateTime timestamp;
    private String path;
    private List<FieldError> fieldErrors;
    
    public ErrorResponse() {
        this.timestamp = LocalDateTime.now();
    }
    
    public ErrorResponse(String code, String message) {
        this();
        this.code = code;
        this.message = message;
    }
    
    // Builder pattern
    public static Builder builder() {
        return new Builder();
    }
    
    public static class Builder {
        private final ErrorResponse response = new ErrorResponse();
        
        public Builder code(String code) {
            response.code = code;
            return this;
        }
        
        public Builder message(String message) {
            response.message = message;
            return this;
        }
        
        public Builder path(String path) {
            response.path = path;
            return this;
        }
        
        public Builder fieldErrors(List<FieldError> fieldErrors) {
            response.fieldErrors = fieldErrors;
            return this;
        }
        
        public ErrorResponse build() {
            return response;
        }
    }
    
    // Getters and setters
}

// 欄位錯誤
class FieldError {
    private String field;
    private String message;
    private Object rejectedValue;
    
    public FieldError(String field, String message, Object rejectedValue) {
        this.field = field;
        this.message = message;
        this.rejectedValue = rejectedValue;
    }
    
    // Getters and setters
}
```

### 2.3 全域異常處理器
**目標**：建立統一的異常處理機制

**實作**：

```java
package com.example.order.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;
import java.util.List;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {
    
    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);
    
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ErrorResponse> handleBusinessException(BusinessException ex) {
        logger.warn("Business exception: {} - {}", ex.getErrorCode(), ex.getMessage());
        
        ErrorResponse error = ErrorResponse.builder()
            .code(ex.getErrorCode())
            .message(ex.getMessage())
            .build();
        
        return ResponseEntity.badRequest().body(error);
    }
    
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleResourceNotFoundException(ResourceNotFoundException ex) {
        logger.warn("Resource not found: {}", ex.getMessage());
        
        ErrorResponse error = ErrorResponse.builder()
            .code(ex.getErrorCode())
            .message(ex.getMessage())
            .build();
        
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }
    
    @ExceptionHandler(InsufficientStockException.class)
    public ResponseEntity<ErrorResponse> handleInsufficientStockException(InsufficientStockException ex) {
        logger.warn("Insufficient stock: {} - requested: {}, available: {}", 
            ex.getProductName(), ex.getRequestedQuantity(), ex.getAvailableStock());
        
        ErrorResponse error = ErrorResponse.builder()
            .code(ex.getErrorCode())
            .message(ex.getMessage())
            .build();
        
        return ResponseEntity.badRequest().body(error);
    }
    
    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex,
            org.springframework.http.HttpHeaders headers,
            HttpStatus status,
            org.springframework.web.context.request.WebRequest request) {
        
        List<FieldError> fieldErrors = ex.getBindingResult().getFieldErrors();
        String message = fieldErrors.stream()
            .map(error -> error.getField() + ": " + error.getDefaultMessage())
            .collect(Collectors.joining(", "));
        
        ErrorResponse error = ErrorResponse.builder()
            .code("VALIDATION_ERROR")
            .message(message)
            .build();
        
        return ResponseEntity.badRequest().body(error);
    }
    
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleException(Exception ex) {
        logger.error("Unexpected exception", ex);
        
        ErrorResponse error = ErrorResponse.builder()
            .code("SYSTEM_ERROR")
            .message("系統錯誤，請稍後再試")
            .build();
        
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }
}
```

### 2.4 輸入驗證
**目標**：實作 Bean Validation 與業務規則驗證

**實作**：

```java
package com.example.order.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;

public class CreateOrderRequest {
    
    @NotNull(message = "客戶ID不能為空")
    private Long customerId;
    
    @NotEmpty(message = "訂單項目不能為空")
    @Valid
    private List<OrderItemRequest> items;
    
    private String notes;
    
    // getters and setters
}

class OrderItemRequest {
    
    @NotNull(message = "商品ID不能為空")
    private Long productId;
    
    @NotNull(message = "數量不能為空")
    @Min(value = 1, message = "數量必須大於0")
    private Integer quantity;
    
    // getters and setters
}
```

```java
package com.example.order.validator;

import com.example.order.dto.CreateOrderRequest;
import com.example.order.dto.OrderItemRequest;
import com.example.order.model.Product;
import com.example.order.repository.ProductRepository;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;
import java.util.List;

@Component
public class OrderRequestValidator implements Validator {
    
    private final ProductRepository productRepository;
    
    public OrderRequestValidator(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }
    
    @Override
    public boolean supports(Class<?> clazz) {
        return CreateOrderRequest.class.isAssignableFrom(clazz);
    }
    
    @Override
    public void validate(Object target, Errors errors) {
        CreateOrderRequest request = (CreateOrderRequest) target;
        
        // 驗證訂單項目
        List<OrderItemRequest> items = request.getItems();
        if (items != null && !items.isEmpty()) {
            for (int i = 0; i < items.size(); i++) {
                OrderItemRequest item = items.get(i);
                
                // 驗證商品是否存在
                Product product = productRepository.findById(item.getProductId()).orElse(null);
                if (product == null) {
                    errors.rejectValue("items[" + i + "].productId", "PRODUCT_NOT_FOUND", 
                        "商品不存在: " + item.getProductId());
                } else {
                    // 驗證庫存是否足夠
                    if (product.getStock() < item.getQuantity()) {
                        errors.rejectValue("items[" + i + "].quantity", "INSUFFICIENT_STOCK", 
                            "庫存不足: " + product.getName() + "，目前庫存: " + product.getStock());
                    }
                }
            }
        }
    }
}
```

---

## 練習 3：完整 DTO 設計與資料轉換 ⭐⭐

### 3.1 請求與回應 DTO
**目標**：建立完整的 DTO 設計

**實作**：

```java
package com.example.order.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

// 請求 DTO
public class CreateOrderRequest {
    private Long customerId;
    private List<OrderItemRequest> items;
    private String notes;
    
    // getters and setters
}

class OrderItemRequest {
    private Long productId;
    private Integer quantity;
    
    // getters and setters
}

// 回應 DTO
public class OrderResponse {
    private Long id;
    private CustomerResponse customer;
    private List<OrderItemResponse> items;
    private BigDecimal totalAmount;
    private String status;
    private String notes;
    private LocalDateTime orderDate;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    // Builder pattern
    @Builder
    public OrderResponse(Long id, CustomerResponse customer, List<OrderItemResponse> items,
                        BigDecimal totalAmount, String status, String notes,
                        LocalDateTime orderDate, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.customer = customer;
        this.items = items;
        this.totalAmount = totalAmount;
        this.status = status;
        this.notes = notes;
        this.orderDate = orderDate;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }
    
    // getters
}

class CustomerResponse {
    private Long id;
    private String name;
    private String email;
    private String phone;
    
    @Builder
    public CustomerResponse(Long id, String name, String email, String phone) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.phone = phone;
    }
    
    // getters
}

class OrderItemResponse {
    private Long id;
    private ProductResponse product;
    private Integer quantity;
    private BigDecimal unitPrice;
    private BigDecimal subtotal;
    
    @Builder
    public OrderItemResponse(Long id, ProductResponse product, Integer quantity,
                           BigDecimal unitPrice, BigDecimal subtotal) {
        this.id = id;
        this.product = product;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
        this.subtotal = subtotal;
    }
    
    // getters
}

class ProductResponse {
    private Long id;
    private String name;
    private String description;
    private BigDecimal price;
    private Integer stock;
    private String category;
    
    @Builder
    public ProductResponse(Long id, String name, String description, BigDecimal price,
                          Integer stock, String category) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.price = price;
        this.stock = stock;
        this.category = category;
    }
    
    // getters
}
```

### 3.2 資料轉換器（Mapper）
**目標**：實作 Entity 與 DTO 的轉換

**實作**：

```java
package com.example.order.mapper;

import com.example.order.dto.*;
import com.example.order.model.*;
import org.springframework.stereotype.Component;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class OrderMapper {
    
    public OrderResponse toResponse(Order order) {
        return OrderResponse.builder()
            .id(order.getId())
            .customer(toResponse(order.getCustomer()))
            .items(toResponseList(order.getItems()))
            .totalAmount(order.getTotalAmount())
            .status(order.getStatus())
            .notes(order.getNotes())
            .orderDate(order.getOrderDate())
            .createdAt(order.getCreatedAt())
            .updatedAt(order.getUpdatedAt())
            .build();
    }
    
    public CustomerResponse toResponse(Customer customer) {
        return CustomerResponse.builder()
            .id(customer.getId())
            .name(customer.getName())
            .email(customer.getEmail())
            .phone(customer.getPhone())
            .build();
    }
    
    public List<OrderItemResponse> toResponseList(List<OrderItem> items) {
        return items.stream()
            .map(this::toResponse)
            .collect(Collectors.toList());
    }
    
    public OrderItemResponse toResponse(OrderItem item) {
        return OrderItemResponse.builder()
            .id(item.getId())
            .product(toResponse(item.getProduct()))
            .quantity(item.getQuantity())
            .unitPrice(item.getUnitPrice())
            .subtotal(item.getSubtotal())
            .build();
    }
    
    public ProductResponse toResponse(Product product) {
        return ProductResponse.builder()
            .id(product.getId())
            .name(product.getName())
            .description(product.getDescription())
            .price(product.getPrice())
            .stock(product.getStock())
            .category(product.getCategory())
            .build();
    }
    
    public Order toEntity(CreateOrderRequest request, Customer customer) {
        Order order = new Order();
        order.setCustomer(customer);
        order.setNotes(request.getNotes());
        order.setStatus("PENDING");
        
        List<OrderItem> items = new ArrayList<>();
        for (OrderItemRequest itemRequest : request.getItems()) {
            OrderItem item = new OrderItem();
            item.setOrder(order);
            item.setQuantity(itemRequest.getQuantity());
            items.add(item);
        }
        order.setItems(items);
        
        return order;
    }
    
    public OrderItem toEntity(OrderItemRequest request, Order order, Product product) {
        OrderItem item = new OrderItem();
        item.setOrder(order);
        item.setProduct(product);
        item.setQuantity(request.getQuantity());
        item.setUnitPrice(product.getPrice());
        item.calculateSubtotal();
        
        return item;
    }
    
    public List<Order> toEntityList(List<OrderResponse> responses) {
        // 實際應用中需要根據業務需求實作
        return new ArrayList<>();
    }
}
```

---

## 練習 4：Service 層完整實作 ⭐⭐

### 4.1 完整的 Service 層
**目標**：實作完整的業務邏輯層

**實作**：

```java
package com.example.order.service;

import com.example.order.dto.CreateOrderRequest;
import com.example.order.dto.OrderItemRequest;
import com.example.order.dto.OrderResponse;
import com.example.order.exception.BusinessException;
import com.example.order.exception.InsufficientStockException;
import com.example.order.exception.ResourceNotFoundException;
import com.example.order.mapper.OrderMapper;
import com.example.order.model.Customer;
import com.example.order.model.Order;
import com.example.order.model.OrderItem;
import com.example.order.model.Product;
import com.example.order.repository.CustomerRepository;
import com.example.order.repository.OrderRepository;
import com.example.order.repository.ProductRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class OrderService {
    
    private final OrderRepository orderRepo;
    private final CustomerRepository customerRepo;
    private final ProductRepository productRepo;
    private final OrderMapper orderMapper;
    
    public OrderService(OrderRepository orderRepo,
                        CustomerRepository customerRepo,
                        ProductRepository productRepo,
                        OrderMapper orderMapper) {
        this.orderRepo = orderRepo;
        this.customerRepo = customerRepo;
        this.productRepo = productRepo;
        this.orderMapper = orderMapper;
    }
    
    public OrderResponse createOrder(CreateOrderRequest request) {
        // 1. 驗證客戶
        Customer customer = customerRepo.findById(request.getCustomerId())
            .orElseThrow(() -> new ResourceNotFoundException("客戶", request.getCustomerId()));
        
        // 2. 建立訂單
        Order order = orderMapper.toEntity(request, customer);
        
        // 3. 處理訂單項目
        BigDecimal total = BigDecimal.ZERO;
        List<OrderItem> items = new ArrayList<>();
        
        for (OrderItemRequest itemRequest : request.getItems()) {
            Product product = productRepo.findById(itemRequest.getProductId())
                .orElseThrow(() -> new ResourceNotFoundException("商品", itemRequest.getProductId()));
            
            // 驗證庫存
            if (product.getStock() < itemRequest.getQuantity()) {
                throw new InsufficientStockException(
                    product.getName(), itemRequest.getQuantity(), product.getStock());
            }
            
            // 扣除庫存
            product.setStock(product.getStock() - itemRequest.getQuantity());
            productRepo.save(product);
            
            // 建立訂單項目
            OrderItem item = orderMapper.toEntity(itemRequest, order, product);
            items.add(item);
            total = total.add(item.getSubtotal());
        }
        
        order.setItems(items);
        order.setTotalAmount(total);
        
        // 4. 儲存訂單
        Order savedOrder = orderRepo.save(order);
        
        return orderMapper.toResponse(savedOrder);
    }
    
    @Transactional(readOnly = true)
    public OrderResponse findById(Long id) {
        Order order = orderRepo.findByIdWithItems(id)
            .orElseThrow(() -> new ResourceNotFoundException("訂單", id));
        
        return orderMapper.toResponse(order);
    }
    
    @Transactional(readOnly = true)
    public Page<OrderResponse> findAll(Pageable pageable) {
        Page<Order> orders = orderRepo.findAll(pageable);
        List<OrderResponse> responses = orders.getContent().stream()
            .map(orderMapper::toResponse)
            .collect(Collectors.toList());
        
        return new PageImpl<>(responses, pageable, orders.getTotalElements());
    }
    
    @Transactional(readOnly = true)
    public Page<OrderResponse> findByCustomerId(Long customerId, Pageable pageable) {
        Page<Order> orders = orderRepo.findByCustomerId(customerId, pageable);
        List<OrderResponse> responses = orders.getContent().stream()
            .map(orderMapper::toResponse)
            .collect(Collectors.toList());
        
        return new PageImpl<>(responses, pageable, orders.getTotalElements());
    }
    
    @Transactional(readOnly = true)
    public Page<OrderResponse> findByStatus(String status, Pageable pageable) {
        Page<Order> orders = orderRepo.findByStatus(status, pageable);
        List<OrderResponse> responses = orders.getContent().stream()
            .map(orderMapper::toResponse)
            .collect(Collectors.toList());
        
        return new PageImpl<>(responses, pageable, orders.getTotalElements());
    }
    
    public void cancelOrder(Long orderId) {
        Order order = orderRepo.findByIdWithItems(orderId)
            .orElseThrow(() -> new ResourceNotFoundException("訂單", orderId));
        
        // 驗證訂單狀態
        if (!"PENDING".equals(order.getStatus())) {
            throw new BusinessException("INVALID_ORDER_STATUS", 
                "只能取消 PENDING 狀態的訂單，目前狀態: " + order.getStatus());
        }
        
        // 歸還庫存
        for (OrderItem item : order.getItems()) {
            Product product = item.getProduct();
            product.setStock(product.getStock() + item.getQuantity());
            productRepo.save(product);
        }
        
        // 更新訂單狀態
        order.setStatus("CANCELLED");
        orderRepo.save(order);
    }
    
    public OrderResponse updateOrderStatus(Long orderId, String newStatus) {
        Order order = orderRepo.findById(orderId)
            .orElseThrow(() -> new ResourceNotFoundException("訂單", orderId));
        
        // 驗證狀態轉換
        validateStatusTransition(order.getStatus(), newStatus);
        
        order.setStatus(newStatus);
        Order updatedOrder = orderRepo.save(order);
        
        return orderMapper.toResponse(updatedOrder);
    }
    
    private void validateStatusTransition(String currentStatus, String newStatus) {
        boolean validTransition = false;
        
        switch (currentStatus) {
            case "PENDING":
                validTransition = "CONFIRMED".equals(newStatus) || "CANCELLED".equals(newStatus);
                break;
            case "CONFIRMED":
                validTransition = "PROCESSING".equals(newStatus) || "CANCELLED".equals(newStatus);
                break;
            case "PROCESSING":
                validTransition = "SHIPPED".equals(newStatus);
                break;
            case "SHIPPED":
                validTransition = "DELIVERED".equals(newStatus);
                break;
            default:
                validTransition = false;
        }
        
        if (!validTransition) {
            throw new BusinessException("INVALID_STATUS_TRANSITION", 
                "無效的狀態轉換: " + currentStatus + " -> " + newStatus);
        }
    }
}
```

### 4.2 Repository 介面擴展
**目標**：擴展 Repository 以支援更多查詢

**實作**：

```java
package com.example.order.repository;

import com.example.order.model.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    
    List<Order> findByCustomerId(Long customerId);
    
    List<Order> findByStatus(String status);
    
    Page<Order> findByCustomerId(Long customerId, Pageable pageable);
    
    Page<Order> findByStatus(String status, Pageable pageable);
    
    @Query("SELECT o FROM Order o JOIN FETCH o.items WHERE o.id = :id")
    Optional<Order> findByIdWithItems(@Param("id") Long id);
    
    @Query("SELECT o FROM Order o JOIN FETCH o.customer WHERE o.id = :id")
    Optional<Order> findByIdWithCustomer(@Param("id") Long id);
    
    @Query("SELECT o FROM Order o JOIN FETCH o.customer JOIN FETCH o.items WHERE o.id = :id")
    Optional<Order> findByIdWithCustomerAndItems(@Param("id") Long id);
    
    @Query("SELECT o FROM Order o WHERE o.customer.id = :customerId AND o.status = :status")
    List<Order> findByCustomerIdAndStatus(@Param("customerId") Long customerId, 
                                         @Param("status") String status);
    
    @Query("SELECT o FROM Order o WHERE o.orderDate BETWEEN :startDate AND :endDate")
    List<Order> findByOrderDateBetween(@Param("startDate") java.time.LocalDateTime startDate,
                                      @Param("endDate") java.time.LocalDateTime endDate);
    
    @Query("SELECT COUNT(o) FROM Order o WHERE o.status = :status")
    long countByStatus(@Param("status") String status);
    
    @Query("SELECT SUM(o.totalAmount) FROM Order o WHERE o.status = :status")
    java.math.BigDecimal sumTotalAmountByStatus(@Param("status") String status);
}
```

---

## 練習 5：Controller 層與 API 設計 ⭐⭐⭐

### 5.1 完整的 Controller 層
**目標**：實作 RESTful API 設計

**實作**：

```java
package com.example.order.controller;

import com.example.order.dto.CreateOrderRequest;
import com.example.order.dto.OrderResponse;
import com.example.order.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import java.net.URI;

@RestController
@RequestMapping("/api/v1/orders")
@Tag(name = "訂單管理", description = "訂單的增刪改查操作")
public class OrderController {
    
    private final OrderService orderService;
    
    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }
    
    @PostMapping
    @Operation(summary = "建立新訂單", description = "建立一個新的訂單，包含客戶資訊與訂單項目")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "訂單建立成功"),
        @ApiResponse(responseCode = "400", description = "輸入驗證失敗"),
        @ApiResponse(responseCode = "404", description = "客戶或商品不存在"),
        @ApiResponse(responseCode = "500", description = "系統錯誤")
    })
    public ResponseEntity<OrderResponse> createOrder(
            @Valid @RequestBody CreateOrderRequest request) {
        
        OrderResponse response = orderService.createOrder(request);
        
        URI location = ServletUriComponentsBuilder
            .fromCurrentRequest()
            .path("/{id}")
            .buildAndExpand(response.getId())
            .toUri();
        
        return ResponseEntity.created(location).body(response);
    }
    
    @GetMapping("/{id}")
    @Operation(summary = "根據ID查詢訂單", description = "根據訂單ID查詢訂單詳細資訊")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "查詢成功"),
        @ApiResponse(responseCode = "404", description = "訂單不存在")
    })
    public ResponseEntity<OrderResponse> getOrderById(
            @Parameter(description = "訂單ID") @PathVariable Long id) {
        
        OrderResponse response = orderService.findById(id);
        return ResponseEntity.ok(response);
    }
    
    @GetMapping
    @Operation(summary = "查詢訂單列表", description = "分頁查詢訂單列表")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "查詢成功")
    })
    public ResponseEntity<Page<OrderResponse>> listOrders(
            @Parameter(description = "客戶ID") @RequestParam(required = false) Long customerId,
            @Parameter(description = "訂單狀態") @RequestParam(required = false) String status,
            @Parameter(description = "頁碼") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "每頁大小") @RequestParam(defaultValue = "20") int size,
            @Parameter(description = "排序欄位") @RequestParam(defaultValue = "createdAt") String sortBy,
            @Parameter(description = "排序方向") @RequestParam(defaultValue = "desc") String sortDir) {
        
        Sort sort = sortDir.equalsIgnoreCase("desc") ? 
            Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        
        Pageable pageable = PageRequest.of(page, size, sort);
        
        Page<OrderResponse> orders;
        if (customerId != null) {
            orders = orderService.findByCustomerId(customerId, pageable);
        } else if (status != null) {
            orders = orderService.findByStatus(status, pageable);
        } else {
            orders = orderService.findAll(pageable);
        }
        
        return ResponseEntity.ok(orders);
    }
    
    @PostMapping("/{id}/cancel")
    @Operation(summary = "取消訂單", description = "取消指定的訂單")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "取消成功"),
        @ApiResponse(responseCode = "400", description = "訂單狀態不允許取消"),
        @ApiResponse(responseCode = "404", description = "訂單不存在")
    })
    public ResponseEntity<Void> cancelOrder(
            @Parameter(description = "訂單ID") @PathVariable Long id) {
        
        orderService.cancelOrder(id);
        return ResponseEntity.ok().build();
    }
    
    @PatchMapping("/{id}/status")
    @Operation(summary = "更新訂單狀態", description = "更新指定訂單的狀態")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "更新成功"),
        @ApiResponse(responseCode = "400", description = "無效的狀態轉換"),
        @ApiResponse(responseCode = "404", description = "訂單不存在")
    })
    public ResponseEntity<OrderResponse> updateOrderStatus(
            @Parameter(description = "訂單ID") @PathVariable Long id,
            @Parameter(description = "新狀態") @RequestParam String status) {
        
        OrderResponse response = orderService.updateOrderStatus(id, status);
        return ResponseEntity.ok(response);
    }
}
```

### 5.2 測試用 Controller
**目標**：建立測試用的 API

**實作**：

```java
package com.example.order.controller;

import com.example.order.dto.ProductResponse;
import com.example.order.model.Product;
import com.example.order.repository.ProductRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/products")
@Tag(name = "商品管理", description = "商品查詢操作")
public class ProductController {
    
    private final ProductRepository productRepository;
    
    public ProductController(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }
    
    @GetMapping
    @Operation(summary = "查詢商品列表", description = "查詢所有可用商品")
    public ResponseEntity<List<ProductResponse>> listProducts() {
        List<Product> products = productRepository.findAll();
        List<ProductResponse> responses = products.stream()
            .map(this::toResponse)
            .collect(Collectors.toList());
        
        return ResponseEntity.ok(responses);
    }
    
    @GetMapping("/{id}")
    @Operation(summary = "根據ID查詢商品", description = "根據商品ID查詢商品詳細資訊")
    public ResponseEntity<ProductResponse> getProductById(@PathVariable Long id) {
        Product product = productRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("商品不存在"));
        
        return ResponseEntity.ok(toResponse(product));
    }
    
    private ProductResponse toResponse(Product product) {
        return ProductResponse.builder()
            .id(product.getId())
            .name(product.getName())
            .description(product.getDescription())
            .price(product.getPrice())
            .stock(product.getStock())
            .category(product.getCategory())
            .build();
    }
}
```

### 5.3 測試腳本
**目標**：建立 API 測試腳本

**實作**：

```bash
#!/bin/bash

# 設定基本 URL
BASE_URL="http://localhost:8080/api/v1"

echo "=== 1. 查詢商品列表 ==="
curl -s "$BASE_URL/products" | jq .

echo -e "\n=== 2. 建立訂單 ==="
curl -s -X POST "$BASE_URL/orders" \
  -H "Content-Type: application/json" \
  -d '{
    "customerId": 1,
    "items": [
      {"productId": 1, "quantity": 2},
      {"productId": 3, "quantity": 1}
    ],
    "notes": "測試訂單"
  }' | jq .

echo -e "\n=== 3. 查詢訂單 ==="
curl -s "$BASE_URL/orders/1" | jq .

echo -e "\n=== 4. 查詢訂單列表 ==="
curl -s "$BASE_URL/orders?page=0&size=10" | jq .

echo -e "\n=== 5. 取消訂單 ==="
curl -s -X POST "$BASE_URL/orders/1/cancel" | jq .

echo -e "\n=== 6. 測試庫存不足 ==="
curl -s -X POST "$BASE_URL/orders" \
  -H "Content-Type: application/json" \
  -d '{
    "customerId": 1,
    "items": [
      {"productId": 1, "quantity": 100}
    ]
  }' | jq .
```

---

## 自我評估

| 練習 | 完成狀態 | 重點掌握 | 需加強 |
|------|----------|----------|--------|
| 練習 1 | □ | □ | □ |
| 練習 2 | □ | □ | □ |
| 練習 3 | □ | □ | □ |
| 練習 4 | □ | □ | □ |
| 練習 5 | □ | □ | □ |

**評估標準**：
- **完成**：已成功實作並測試
- **重點掌握**：理解核心概念和原理
- **需加強**：需要複習或深入學習的部分

---

## 常見問題排除

### 1. 資料庫連線問題
**問題**：無法連線到 MySQL 資料庫
**解決方案**：
1. 確認 MySQL 服務已啟動
2. 檢查連線設定（URL、使用者名稱、密碼）
3. 確認資料庫已建立
4. 檢查防火牆設定

### 2. Flyway 遷移失敗
**問題**：遷移腳本執行失敗
**解決方案**：
1. 檢查 SQL 語法是否正確
2. 確認遷移腳本命名規則
3. 檢查資料庫權限
4. 查看詳細錯誤日誌

### 3. 交易管理問題
**問題**：交易不一致或無法回滾
**解決方案**：
1. 確認 @Transactional 註解配置
2. 檢查異常類型是否為 RuntimeException
3. 確認 TransactionManager 配置
4. 測試事務的提交和回滾

### 4. API 測試問題
**問題**：API 回應格式不正確
**解決方案**：
1. 檢查 DTO 設計是否完整
2. 確認 JSON 序列化設定
3. 檢查 Controller 的回應格式
4. 使用 Postman 或 curl 測試 API

---

## 參考資源

### 官方文件
- [Spring Boot 官方文件](https://spring.io/projects/spring-boot)
- [Spring Data JPA 官方文件](https://spring.io/projects/spring-data-jpa)
- [Hibernate 官方文件](https://hibernate.org/orm/documentation/)
- [Flyway 官方文件](https://flywaydb.org/documentation/)

### 進階學習
- [RESTful API 設計最佳實踐](https://restfulapi.net/)
- [Spring Boot 安全性實作](https://spring.io/guides/gs/securing-web/)
- [效能優化策略](https://www.baeldung.com/spring-performance)
- [微服務架構設計](https://microservices.io/)

### 工具推薦
- [Postman](https://www.postman.com/)：API 測試工具
- [DBeaver](https://dbeaver.io/)：資料庫管理工具
- [IntelliJ IDEA](https://www.jetbrains.com/idea/)：Java IDE
- [Swagger UI](https://swagger.io/tools/swagger-ui/)：API 文件工具