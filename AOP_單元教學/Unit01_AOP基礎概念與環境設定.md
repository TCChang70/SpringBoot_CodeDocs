# Unit 01：AOP 基礎概念與環境設定

> **學習目標**：理解 AOP 是什麼、為什麼需要它，並完成可執行的最小專案骨架  
> **預計時間**：20 分鐘  
> **前置需求**：Spring Boot 基本架構（Controller / Service / Repository）

---

## 1. 白話理解 AOP

### 問題情境：重複的橫切邏輯

沒有 AOP 時，你可能在每個 Service 方法都寫同樣的程式碼：

```java
public class OrderService {
    public void createOrder() {
        log.info("createOrder 開始"); // 重複
        // ... 業務邏輯
        log.info("createOrder 結束"); // 重複
    }
}

public class UserService {
    public void updateUser() {
        log.info("updateUser 開始"); // 重複
        // ... 業務邏輯
        log.info("updateUser 結束"); // 重複
    }
}
```

**AOP 解法**：把「Log 記錄」這個橫切邏輯抽出來，自動套用到所有方法。

```
橫切關注點（Cross-Cutting Concerns）：
  Log 記錄 ──────────────────────────────────┐
  安全驗證 ────────────────────────────────┐  │
  效能計時 ──────────────────────────────┐ │  │
                                        ↓ ↓  ↓
  OrderService.createOrder() ══════════════════
  UserService.updateUser()   ══════════════════
  ProductService.addStock()  ══════════════════
```

---

## 2. 核心術語對照

| 術語 | 中文 | 一句話說明 |
|------|------|-----------|
| **Aspect** | 切面 | 封裝橫切邏輯的類別（加上 `@Aspect`）|
| **Join Point** | 連接點 | 可以被攔截的時機點（方法呼叫的當下）|
| **Pointcut** | 切入點 | 指定「攔截哪些方法」的規則 |
| **Advice** | 通知 | 攔截後「要做什麼」的邏輯 |
| **Proxy** | 代理 | Spring 自動建立的包裝物件，負責攔截 |

### Advice 執行時機速覽

```
目標方法執行流程：

  @Before ──► 目標方法 ──► @AfterReturning（成功）
                       └──► @AfterThrowing（例外）
                            ↓
                         @After（無論成功失敗）

  @Around 完整包住以上所有流程
```

---

## 3. 環境設定（可直接複製執行）

### 3.1 pom.xml

```xml
<dependencies>
    <!-- Spring Boot Web -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-web</artifactId>
    </dependency>

    <!-- ✅ AOP 必要依賴 -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-aop</artifactId>
    </dependency>

    <!-- JPA + 交易管理 -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-data-jpa</artifactId>
    </dependency>

    <!-- H2 記憶體資料庫（測試用）-->
    <dependency>
        <groupId>com.h2database</groupId>
        <artifactId>h2</artifactId>
        <scope>runtime</scope>
    </dependency>

    <!-- Lombok -->
    <dependency>
        <groupId>org.projectlombok</groupId>
        <artifactId>lombok</artifactId>
        <optional>true</optional>
    </dependency>
</dependencies>
```

### 3.2 application.properties

```properties
spring.datasource.url=jdbc:h2:mem:aopdb
spring.datasource.driver-class-name=org.h2.Driver
spring.datasource.username=sa
spring.datasource.password=

spring.jpa.database-platform=org.hibernate.dialect.H2Dialect
spring.jpa.hibernate.ddl-auto=create-drop
spring.jpa.show-sql=true

spring.h2.console.enabled=true
spring.h2.console.path=/h2-console
```

---

## 4. 最小可執行骨架

以下是後續所有 Unit 都會用到的公共基礎類別。

### 4.1 專案結構

```
src/main/java/com/example/aop/
├── AopDemoApplication.java       ← 啟動類別
├── entity/
│   └── Product.java
├── repository/
│   └── ProductRepository.java
├── service/
│   └── ProductService.java       ← 被攔截的目標
├── controller/
│   └── ProductController.java
└── aspect/
    └── （各 Unit 的 Aspect 放這裡）
```

### 4.2 AopDemoApplication.java

```java
package com.example.aop;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class AopDemoApplication {
    public static void main(String[] args) {
        SpringApplication.run(AopDemoApplication.class, args);
    }
}
```

### 4.3 Product.java（Entity）

```java
package com.example.aop.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Entity
@Table(name = "products")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private Integer stock;
    private Double price;
}
```

### 4.4 ProductRepository.java

```java
package com.example.aop.repository;

import com.example.aop.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductRepository extends JpaRepository<Product, Long> {
}
```

### 4.5 ProductService.java（被攔截的目標）

```java
package com.example.aop.service;

import com.example.aop.entity.Product;
import com.example.aop.repository.ProductRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProductService {

    private final ProductRepository productRepository;

    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    public Product create(Product product) {
        return productRepository.save(product);
    }

    public Product findById(Long id) {
        return productRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("商品不存在: " + id));
    }

    public List<Product> findAll() {
        return productRepository.findAll();
    }

    public void deleteById(Long id) {
        productRepository.deleteById(id);
    }
}
```

### 4.6 ProductController.java

```java
package com.example.aop.controller;

import com.example.aop.entity.Product;
import com.example.aop.service.ProductService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/products")
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @PostMapping
    public ResponseEntity<Product> create(@RequestBody Product product) {
        return ResponseEntity.ok(productService.create(product));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Product> findById(@PathVariable Long id) {
        return ResponseEntity.ok(productService.findById(id));
    }

    @GetMapping
    public ResponseEntity<List<Product>> findAll() {
        return ResponseEntity.ok(productService.findAll());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        productService.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
```

---

## 5. 驗證環境正常

### 啟動後執行以下測試：

```bash
# 建立商品
curl -X POST http://localhost:8080/api/products \
  -H "Content-Type: application/json" \
  -d '{"name":"MacBook","stock":10,"price":45000.0}'

# 查詢商品
curl http://localhost:8080/api/products/1

# 查詢 H2 Console（瀏覽器開啟）
# http://localhost:8080/h2-console
# JDBC URL: jdbc:h2:mem:aopdb
```

環境正常後，進入 **Unit 02** 學習 Pointcut。

---

## ✅ 本單元重點

- AOP 解決「橫切關注點」重複程式碼的問題
- 需要加入 `spring-boot-starter-aop` 依賴
- Aspect 類別需加上 `@Aspect` + `@Component`
- Spring AOP 透過 **動態代理（Proxy）** 實現攔截

---

*下一單元：[Unit 02 - Pointcut 切入點定義](Unit02_Pointcut切入點定義.md)*
