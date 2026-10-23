# 經典模型車 E-Commerce 與 ERP 後台管理系統
## 客戶與信用管理模組 (Customer & Credit Management) 系統分析、設計與實作文件

---

## 目錄 (Table of Contents)
1. [軟體需求規格書 (SRS Summary)](#1-軟體需求規格書-srs-summary)
2. [系統分析 (System Analysis - SA)](#2-系統分析-system-analysis---sa)
   - 2.1 [業務規則 (Business Rules)](#21-業務規則-business-rules)
   - 2.2 [使用案例說明 (Use Case Specifications)](#22-使用案例說明-use-case-specifications)
   - 2.3 [系統流程 (Sequence Diagram)](#23-系統流程-sequence-diagram)
3. [系統設計 (System Design - SD)](#3-系統設計-system-design---sd)
   - 3.1 [資料庫設計與索引優化 (Database Schema & Indexing)](#31-資料庫設計與索引優化-database-schema--indexing)
   - 3.2 [RESTful API 介面規格 (API Specification)](#32-restful-api-介面規格-api-specification)
   - 3.3 [前端 UI/UX 設計與狀態處理](#33-前端-uiux-設計與狀態處理)
4. [後端 Java Spring Boot + SQLite 系統實作](#4-後端-java-spring-boot--sqlite-系統實作)
   - 4.1 [Maven 依賴檔 (`pom.xml`)](#41-maven-依賴檔-pomxml)
   - 4.2 [應用程式設定檔 (`application.properties`)](#42-應用程式設定檔-applicationproperties)
   - 4.3 [完整 Java 核心程式碼 (`CustomerCreditModuleApplication.java`)](#43-完整-java-核心程式碼-customercreditmoduleapplicationjava)

---

## 1. 軟體需求規格書 (SRS Summary)

### 1.1 系統目的與範圍
本系統旨在為企業提供一套完整的模型商品 E-Commerce 與 ERP 後台管理平台。模組涵蓋：
* **前端與業務管理：** 客戶資料維護、專屬業務代表指派。
* **銷售與營運管理：** 訂單建立、訂單明細維護、出貨狀態變更。
* **財務與信用管理：** 客戶信用額度控管、付款紀錄與帳款核對。
* **組織與內部管理：** 跨國辦公室與員工職務管理。

### 1.2 資料庫實體關係 (Entity Relationships)
```text
[Offices] ──(1:N)──► [Employees] ──(1:N)──► [Customers] ──(1:N)──► [Payments]
                                                 │
                                              (1:N)
                                                 ▼
[ProductLines] ──(1:N)──► [Products] ──(1:N)──► [OrderDetails] ◄──(N:1)── [Orders]
```

---

## 2. 系統分析 (System Analysis - SA)

### 2.1 業務規則 (Business Rules)

* **[BR-CUST-01] 客戶基本資料完整性：**
  * 客戶名稱 (`customerName`)、聯絡人姓名 (`contactFirstName`, `contactLastName`) 與電話 (`phone`) 為必填欄位。
  * `creditLimit` 預設值為 `0.00`。
* **[BR-CUST-02] 業務代表指派規範：**
  * 客戶可指派一名專屬業務代表 (`salesRepEmployeeNumber`)，該員工職務必須包含 `Sales Rep`。
* **[BR-CUST-03] 信用額度與下單控管邏輯 (Credit Limit Control)：**
  * **當前未結餘額 (Outstanding Amount)：**
    $$\text{Outstanding Amount} = \sum (\text{所有非 Cancelled 訂單總額}) - \sum (\text{已成功付款總金額})$$
  * **可用信用額度 (Available Credit)：**
    $$\text{Available Credit} = \text{creditLimit} - \text{Outstanding Amount}$$
  * **信用檢查機制：** 
    * 若新訂單金額 $\le \text{Available Credit}$：允許下單，訂單進入 `In Process`。
    * 若新訂單金額 $> \text{Available Credit}$：拒絕自動核單，系統自動將訂單狀態鎖定為 **`On Hold`**，並發送通知給業務代表及其主管。

---

### 2.2 使用案例說明 (Use Case Specifications)

#### UC-CUST-01: 建立/維護客戶檔案
* **主要參與者：** Sales Rep（業務代表）、Admin（系統管理員）
* **基本流程：**
  1. 使用者進入「客戶管理」頁面，點選「新增客戶」。
  2. 輸入客戶基本聯絡資訊與地址。
  3. 選擇指定之 Sales Rep（預設可帶入登入者 ID）。
  4. 儲存並寫入資料庫 `customers` 資料表。

#### UC-CUST-02: 客戶信用額度評估與調整
* **主要參與者：** Finance / Admin（財務主管 / 系統管理員）
* **基本流程：**
  1. 財務人員搜尋並開啟特定客戶頁面。
  2. 系統自動即時計算並顯示「歷史訂單總額」、「已付款總額」與「當前未結餘額」。
  3. 財務人員輸入新的信用額度 (`creditLimit`) 並儲存。
  4. 系統更新 `customers.creditLimit` 並紀錄變更日誌。

#### UC-CUST-03: 信用額度即時自動核可檢查
* **主要參與者：** 訂單系統（系統自動執行）
* **基本流程：**
  1. 取得客戶之 `creditLimit`，計算當前未結餘額與新訂單金額。
  2. 判斷未超出信用額度時，標記訂單為 `In Process`。
  3. 若已超出，觸發警告並標記訂單為 `On Hold`，於備註寫入「Credit Limit Exceeded」。

---

### 2.3 系統流程 (Sequence Diagram)

```text
[Client / Order System]      [Customer Service]          [Database]
          │                          │                        │
          │ 1. Place Order (Amount)  │                        │
          ├─────────────────────────►│                        │
          │                          │ 2. Get creditLimit &   │
          │                          │    Outstanding Amount  │
          │                          ├───────────────────────►│
          │                          │ 3. Return Credit Data  │
          │                          │◄───────────────────────┤
          │                          │                        │
          │-- [Calculates: Outstanding + New Order > Limit?] -│
          │                          │                        │
  [IF EXCEEDED]                      │                        │
          │                          │ 4a. Save Order Status  │
          │                          │     = 'On Hold'        │
          │                          ├───────────────────────►│
          │ 5a. Return 'On Hold'     │                        │
          │     + Credit Warning     │                        │
          │◄─────────────────────────┤                        │
          │                          │                        │
  [IF WITHIN LIMIT]                  │                        │
          │                          │ 4b. Save Order Status  │
          │                          │     = 'In Process'     │
          │                          ├───────────────────────►│
          │ 5b. Order Confirmed      │                        │
          │◄─────────────────────────┤                        │
```

---

## 3. 系統設計 (System Design - SD)

### 3.1 資料庫設計與索引優化 (Database Schema & Indexing)

#### 資料表綱要 (`customers`)
```sql
CREATE TABLE `customers` (
  `customerNumber` INT(11) NOT NULL AUTO_INCREMENT,
  `customerName` VARCHAR(50) NOT NULL,
  `contactLastName` VARCHAR(50) NOT NULL,
  `contactFirstName` VARCHAR(50) NOT NULL,
  `phone` VARCHAR(50) NOT NULL,
  `addressLine1` VARCHAR(50) NOT NULL,
  `addressLine2` VARCHAR(50) DEFAULT NULL,
  `city` VARCHAR(50) NOT NULL,
  `state` VARCHAR(50) DEFAULT NULL,
  `postalCode` VARCHAR(15) DEFAULT NULL,
  `country` VARCHAR(50) NOT NULL,
  `salesRepEmployeeNumber` INT(11) DEFAULT NULL,
  `creditLimit` DECIMAL(10,2) DEFAULT '0.00',
  PRIMARY KEY (`customerNumber`),
  KEY `salesRepEmployeeNumber` (`salesRepEmployeeNumber`),
  CONSTRAINT `customers_ibfk_1` FOREIGN KEY (`salesRepEmployeeNumber`) 
    REFERENCES `employees` (`employeeNumber`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
```

#### 效能優化索引 (Indexing)
```sql
-- 提升查詢客戶未結算訂單之效能
CREATE INDEX idx_orders_customer_status ON orders(customerNumber, status);

-- 提升查詢客戶累計付款之效能
CREATE INDEX idx_payments_customer_amount ON payments(customerNumber, amount);
```

---

### 3.2 RESTful API 介面規格 (API Specification)

#### 1. 取得客戶詳細資訊與信用狀況
* **HTTP Method:** `GET`
* **Endpoint:** `/api/v1/customers/{customerNumber}/credit-summary`
* **Response (200 OK):**
```json
{
  "customerNumber": 103,
  "customerName": "Atelier graphique",
  "creditLimit": 21000.00,
  "totalOrderAmount": 22314.36,
  "totalPaidAmount": 14571.44,
  "outstandingAmount": 7742.92,
  "availableCredit": 13257.08,
  "creditStatus": "HEALTHY" // HEALTHY | WARNING | EXCEEDED
}
```

#### 2. 新增客戶資料
* **HTTP Method:** `POST`
* **Endpoint:** `/api/v1/customers`
* **Request Body:**
```json
{
  "customerName": "Signal Gift Stores",
  "contactLastName": "King",
  "contactFirstName": "Jean",
  "phone": "7025551838",
  "addressLine1": "8489 Strong St.",
  "city": "Las Vegas",
  "state": "NV",
  "postalCode": "89104",
  "country": "USA",
  "salesRepEmployeeNumber": 1165,
  "creditLimit": 71800.00
}
```

#### 3. 調整客戶信用額度
* **HTTP Method:** `PATCH`
* **Endpoint:** `/api/v1/customers/{customerNumber}/credit-limit`
* **Request Body:**
```json
{
  "creditLimit": 85000.00,
  "reason": "Annual financial review approved limit increase."
}
```

---

### 3.3 前端 UI/UX 設計與狀態處理

1. **信用狀況卡片 (Credit Status Card)：**
   * **Credit Limit:** \$71,800.00
   * **Outstanding Amount:** \$12,450.00
   * **Available Credit:** \$59,350.00
   * **Status Badge:**
     * 綠色 `[Healthy]`：可用額度 $> 10\%$
     * 黃色 `[Warning]`：可用額度 $\le 10\%$
     * 紅色 `[Limit Exceeded]`：可用額度 $< 0$

2. **下單攔截機制：**
   * 當系統回應 `Credit Exceeded` 時，跳出 Modal 視窗提示「該客戶已超出授信額度。訂單將以 `On Hold` 狀態送出，需經財務部審核。」

---

## 4. 後端 Java Spring Boot + SQLite 系統實作

### 4.1 Maven 依賴檔 (`pom.xml`)

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <groupId>com.classicmodels</groupId>
    <artifactId>customer-credit-service</artifactId>
    <version>1.0.0</version>

    <parent>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-parent</artifactId>
        <version>3.2.3</version>
        <relativePath/>
    </parent>

    <properties>
        <java.version>17</java.version>
    </properties>

    <dependencies>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-data-jpa</artifactId>
        </dependency>

        <!-- SQLite Driver & Community Hibernate Dialects -->
        <dependency>
            <groupId>org.sqlite</groupId>
            <artifactId>sqlite-jdbc</artifactId>
            <version>3.45.1.0</version>
        </dependency>
        <dependency>
            <groupId>org.hibernate.orm</groupId>
            <artifactId>hibernate-community-dialects</artifactId>
        </dependency>

        <!-- Lombok -->
        <dependency>
            <groupId>org.projectlombok</groupId>
            <artifactId>lombok</artifactId>
            <optional>true</optional>
        </dependency>
    </dependencies>

    <build>
        <plugins>
            <plugin>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-maven-plugin</artifactId>
            </plugin>
        </plugins>
    </build>
</project>
```

---

### 4.2 應用程式設定檔 (`application.properties`)

```properties
spring.application.name=customer-credit-service
server.port=8080

# SQLite JDBC Driver & Database File Path
spring.datasource.driver-class-name=org.sqlite.JDBC
spring.datasource.url=jdbc:sqlite:classicmodels.db

# Hibernate Dialect for SQLite
spring.jpa.database-platform=org.hibernate.community.dialect.SQLiteDialect
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.format_sql=true
```

---

### 4.3 完整 Java 核心程式碼 (`CustomerCreditModuleApplication.java`)

```java
package com.classicmodels.customer;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;

@SpringBootApplication
public class CustomerCreditModuleApplication {

    public static void main(String[] args) {
        SpringApplication.run(CustomerCreditModuleApplication.class, args);
    }
}

// ==========================================
// 1. DOMAIN ENTITIES (JPA Entities)
// ==========================================

@Entity
@Table(name = "customers")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
class Customer {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "customerNumber")
    private Integer customerNumber;

    @Column(name = "customerName", nullable = false, length = 50)
    private String customerName;

    @Column(name = "contactLastName", nullable = false, length = 50)
    private String contactLastName;

    @Column(name = "contactFirstName", nullable = false, length = 50)
    private String contactFirstName;

    @Column(name = "phone", nullable = false, length = 50)
    private String phone;

    @Column(name = "addressLine1", nullable = false, length = 50)
    private String addressLine1;

    @Column(name = "addressLine2", length = 50)
    private String addressLine2;

    @Column(name = "city", nullable = false, length = 50)
    private String city;

    @Column(name = "state", length = 50)
    private String state;

    @Column(name = "postalCode", length = 15)
    private String postalCode;

    @Column(name = "country", nullable = false, length = 50)
    private String country;

    @Column(name = "salesRepEmployeeNumber")
    private Integer salesRepEmployeeNumber;

    @Column(name = "creditLimit", precision = 10, scale = 2)
    private BigDecimal creditLimit;
}

@Entity
@Table(name = "orders")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
class Order {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "orderNumber")
    private Integer orderNumber;

    @Column(name = "orderDate", nullable = false)
    private LocalDate orderDate;

    @Column(name = "requiredDate", nullable = false)
    private LocalDate requiredDate;

    @Column(name = "shippedDate")
    private LocalDate shippedDate;

    @Column(name = "status", nullable = false, length = 15)
    private String status; // In Process, Shipped, Cancelled, On Hold

    @Column(name = "comments")
    private String comments;

    @Column(name = "customerNumber", nullable = false)
    private Integer customerNumber;
}

@Entity
@Table(name = "orderdetails")
@IdClass(OrderDetailId.class)
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
class OrderDetail {
    @Id
    @Column(name = "orderNumber")
    private Integer orderNumber;

    @Id
    @Column(name = "productCode")
    private String productCode;

    @Column(name = "quantityOrdered", nullable = false)
    private Integer quantityOrdered;

    @Column(name = "priceEach", nullable = false, precision = 10, scale = 2)
    private BigDecimal priceEach;

    @Column(name = "orderLineNumber", nullable = false)
    private Short orderLineNumber;
}

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @EqualsAndHashCode
class OrderDetailId implements java.io.Serializable {
    private Integer orderNumber;
    private String productCode;
}

@Entity
@Table(name = "payments")
@IdClass(PaymentId.class)
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
class Payment {
    @Id
    @Column(name = "customerNumber")
    private Integer customerNumber;

    @Id
    @Column(name = "checkNumber")
    private String checkNumber;

    @Column(name = "paymentDate", nullable = false)
    private LocalDate paymentDate;

    @Column(name = "amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal amount;
}

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @EqualsAndHashCode
class PaymentId implements java.io.Serializable {
    private Integer customerNumber;
    private String checkNumber;
}

// ==========================================
// 2. DATA ACCESS LAYER (JPA Repositories)
// ==========================================

@Repository
interface CustomerRepository extends JpaRepository<Customer, Integer> {}

@Repository
interface OrderRepository extends JpaRepository<Order, Integer> {
    
    // 計算客戶非 Cancelled 訂單的總累計金額
    @Query(value = "SELECT COALESCE(SUM(od.quantityOrdered * od.priceEach), 0) " +
                   "FROM orders o " +
                   "JOIN orderdetails od ON o.orderNumber = od.orderNumber " +
                   "WHERE o.customerNumber = :custNo AND o.status != 'Cancelled'", 
           nativeQuery = true)
    BigDecimal calculateTotalOrderAmountByCustomer(@Param("custNo") Integer customerNumber);
}

@Repository
interface PaymentRepository extends JpaRepository<Payment, PaymentId> {
    
    // 計算客戶已支付總金額
    @Query(value = "SELECT COALESCE(SUM(p.amount), 0) FROM payments p WHERE p.customerNumber = :custNo", 
           nativeQuery = true)
    BigDecimal calculateTotalPaymentByCustomer(@Param("custNo") Integer customerNumber);
}

// ==========================================
// 3. DTOs & SERVICE LAYER
// ==========================================

@Getter @Setter @AllArgsConstructor @NoArgsConstructor @Builder
class CreditSummaryDTO {
    private Integer customerNumber;
    private String customerName;
    private BigDecimal creditLimit;
    private BigDecimal totalOrderAmount;
    private BigDecimal totalPaidAmount;
    private BigDecimal outstandingAmount;
    private BigDecimal availableCredit;
    private String creditStatus; // HEALTHY, WARNING, EXCEEDED
}

@Getter @Setter
class UpdateCreditLimitRequest {
    private BigDecimal creditLimit;
    private String reason;
}

@Service
@Transactional
class CustomerCreditService {

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private PaymentRepository paymentRepository;

    public List<Customer> getAllCustomers() {
        return customerRepository.findAll();
    }

    public Customer createCustomer(Customer customer) {
        if (customer.getCreditLimit() == null) {
            customer.setCreditLimit(BigDecimal.ZERO);
        }
        return customerRepository.save(customer);
    }

    @Transactional(readOnly = true)
    public CreditSummaryDTO getCustomerCreditSummary(Integer customerNumber) {
        Customer customer = customerRepository.findById(customerNumber)
                .orElseThrow(() -> new RuntimeException("Customer not found: " + customerNumber));

        BigDecimal totalOrderAmount = orderRepository.calculateTotalOrderAmountByCustomer(customerNumber);
        BigDecimal totalPaidAmount = paymentRepository.calculateTotalPaymentByCustomer(customerNumber);

        // 未結餘額 = 總訂單金額 - 已付款總額
        BigDecimal outstandingAmount = totalOrderAmount.subtract(totalPaidAmount);
        // 可用信用額度 = 信用額度 - 未結餘額
        BigDecimal availableCredit = customer.getCreditLimit().subtract(outstandingAmount);

        String status = "HEALTHY";
        if (availableCredit.compareTo(BigDecimal.ZERO) < 0) {
            status = "EXCEEDED";
        } else if (availableCredit.compareTo(customer.getCreditLimit().multiply(new BigDecimal("0.1"))) <= 0) {
            status = "WARNING";
        }

        return CreditSummaryDTO.builder()
                .customerNumber(customer.getCustomerNumber())
                .customerName(customer.getCustomerName())
                .creditLimit(customer.getCreditLimit())
                .totalOrderAmount(totalOrderAmount)
                .totalPaidAmount(totalPaidAmount)
                .outstandingAmount(outstandingAmount)
                .availableCredit(availableCredit)
                .creditStatus(status)
                .build();
    }

    public Customer updateCreditLimit(Integer customerNumber, BigDecimal newLimit) {
        Customer customer = customerRepository.findById(customerNumber)
                .orElseThrow(() -> new RuntimeException("Customer not found: " + customerNumber));
        customer.setCreditLimit(newLimit);
        return customerRepository.save(customer);
    }
}

// ==========================================
// 4. REST CONTROLLER (API Endpoints)
// ==========================================

@RestController
@RequestMapping("/api/v1/customers")
class CustomerCreditController {

    @Autowired
    private CustomerCreditService customerCreditService;

    @GetMapping
    public ResponseEntity<List<Customer>> getAllCustomers() {
        return ResponseEntity.ok(customerCreditService.getAllCustomers());
    }

    @PostMapping
    public ResponseEntity<Customer> createCustomer(@RequestBody Customer customer) {
        return new ResponseEntity<>(customerCreditService.createCustomer(customer), HttpStatus.CREATED);
    }

    @GetMapping("/{customerNumber}/credit-summary")
    public ResponseEntity<CreditSummaryDTO> getCreditSummary(@PathVariable Integer customerNumber) {
        return ResponseEntity.ok(customerCreditService.getCustomerCreditSummary(customerNumber));
    }

    @PatchMapping("/{customerNumber}/credit-limit")
    public ResponseEntity<Customer> updateCreditLimit(
            @PathVariable Integer customerNumber,
            @RequestBody UpdateCreditLimitRequest request) {
        Customer updatedCustomer = customerCreditService.updateCreditLimit(customerNumber, request.getCreditLimit());
        return ResponseEntity.ok(updatedCustomer);
    }
}

// ==========================================
// 5. DATABASE INITIALIZER (Mock Data)
// ==========================================

@org.springframework.stereotype.Component
class DataInitializer implements CommandLineRunner {

    @Autowired
    private CustomerRepository customerRepository;
    @Autowired
    private OrderRepository orderRepository;
    @Autowired
    private PaymentRepository paymentRepository;

    @Override
    public void run(String... args) throws Exception {
        // 初始化測試客戶
        Customer cust = Customer.builder()
                .customerName("Atelier graphique")
                .contactLastName("Schmitt")
                .contactFirstName("Carine")
                .phone("40.30.1455")
                .addressLine1("54, rue Royale")
                .city("Nantes")
                .country("France")
                .creditLimit(new BigDecimal("21000.00"))
                .build();
        cust = customerRepository.save(cust);

        // 初始化測試訂單
        Order order = Order.builder()
                .customerNumber(cust.getCustomerNumber())
                .orderDate(LocalDate.now().minusDays(10))
                .requiredDate(LocalDate.now().plusDays(5))
                .status("In Process")
                .build();
        orderRepository.save(order);

        // 初始化測試付款
        Payment payment = Payment.builder()
                .customerNumber(cust.getCustomerNumber())
                .checkNumber("HQ336336")
                .paymentDate(LocalDate.now().minusDays(2))
                .amount(new BigDecimal("5000.00"))
                .build();
        paymentRepository.save(payment);
    }
}
```