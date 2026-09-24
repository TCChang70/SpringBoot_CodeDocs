# Spring Boot + MySQL Docker 製作與學習指南

> 本文件教你**從零建立**一個「Spring Boot 應用程式 + MySQL 資料庫」的完整專案，並用 **Docker / Docker Compose** 把 MySQL 與 Spring Boot 都容器化、一次啟動。
> 全程繁體中文、逐步驟實作，每個章節都依「**先講概念 → 再給程式碼 → 最後講解程式碼**」編排，並附「執行結果 / 預期輸出」供你自行驗證。

---

## 目錄

1. [專案簡介與技術棧](#1-專案簡介與技術棧)
2. [環境準備](#2-環境準備)
3. [建立專案骨架](#3-建立專案骨架)
4. [Docker 與 MySQL 設定](#4-docker-與-mysql-設定)
5. [設定檔 application.properties](#5-設定檔-applicationproperties)
6. [實體設計（Entity）](#6-實體設計entity)
7. [Repository 層](#7-repository-層)
8. [Service 層（@Transactional 交易示範）](#8-service-層transactional-交易示範)
9. [Controller 層（RESTful API）](#9-controller-層restful-api)
10. [種子資料 data.sql](#10-種子資料-datasql)
11. [將 Spring Boot 容器化（Dockerfile + docker-compose）](#11-將-spring-boot-容器化dockerfile--docker-compose)
12. [執行與驗證](#12-執行與驗證)
13. [常見問題排解](#13-常見問題排解)
14. [練習題 / 學習檢查點](#14-練習題--學習檢查點)

---

## 1. 專案簡介與技術棧

### 1.1 主題與目標

我們要建立一個「**電商商品清單系統**」：

- **MySQL** 用 **Docker 容器**執行（不需要在本機安裝 MySQL）
- **Spring Boot** 透過 JDBC 連到 Docker 裡的 MySQL
- 最後把 Spring Boot 也打包成 Docker 映像（Image），用 **Docker Compose 一條指令同時啟動「DB + 應用程式」**
- 提供完整的 RESTful API，並用 Swagger 自動生成 API 文件

學完你會得到：

| 能力 | 說明 |
| --- | --- |
| Docker 基本操作 | `docker run`、`docker ps`、`docker exec`、`docker compose up` |
| 映像 vs 容器 | 知道兩者差別，知道怎麼打包自己的應用映像 |
| MySQL 容器化 | 用容器跑 DB，資料透過 Volume 持久化 |
| Spring Data JPA | Entity、Repository 各種查詢技巧 |
| 多層建置 Dockerfile | Multi-stage build 縮小映像 |

### 1.2 技術清單

| 技術 | 版本 | 用途 |
| --- | --- | --- |
| Java | 17 | 開發語言 |
| Maven | 3.9+ | 專案建置與依賴管理 |
| Spring Boot | 3.2.5 | 應用程式框架 |
| Spring Boot Starter Web | 3.2.5 | RESTful API |
| Spring Boot Starter Data JPA | 3.2.5 | ORM + Repository |
| MySQL Connector/J | 隨 Boot 管理 | JDBC 驅動 |
| MySQL Server | 8.0 | 資料庫（跑在 Docker） |
| Lombok | 隨 Boot 管理 | 減少樣板程式碼 |
| springdoc-openapi | 2.5.0 | Swagger UI / OpenAPI 3 |
| Docker Desktop + Docker Compose | 最新穩定版 | 容器化編排 |

> 🧭 本指南對應的分析架構：**Controller → Service → Repository → Entity**，這是 Spring Boot 專案最標準的分層方式。

---

## 2. 環境準備

### 2.1 需要安裝的軟體

| 軟體 | 確認方式 | 最低要求 |
| --- | --- | --- |
| JDK 17 | `java -version` | 17 以上（推薦 17 或 21） |
| Maven | `mvn -version` | 3.6 以上 |
| Docker Desktop（Windows） | `docker version` | 支援 Docker Compose v2 |
| IDE | Spring Tool Suite / IntelliJ | 任選 |

> ⚠️ **Windows 注意事項**
> - Docker Desktop 需要安裝 **WSL 2** 才能穩定執行，安裝流程會在第一次啟動時引導你。
> - 本機 **不需要** 安裝 MySQL！這正是本篇重點：資料庫跑在容器內。

### 2.2 驗證環境

在終端機（CMD / PowerShell）依序執行，確認輸出正常：

```powershell
java -version
mvn -version
docker version
docker compose version
```

預期輸出（節錄）：

```
openjdk version "17.0.13" ...
Apache Maven 3.9.x ...
Client: Docker Engine ...
Docker Compose version v2.xx
```

> ⚠️ 若 `docker compose` 顯示 `command not found`，很可能是 Docker Desktop 尚未啟動，請先開啟 Docker Desktop 再試。

### 2.3 認識三個關鍵名詞

| 名詞 | 說明 | 類比 |
| --- | --- | --- |
| **Image（映像）** | 唯讀的「模板」，裡面含程式碼、執行環境、設定 | 安裝光碟 |
| **Container（容器）** | 由映像啟動的「實例」，可讀寫、可關閉 | 安裝光碟安裝出來的程式 |
| **Volume（磁碟區）** | 掛載到容器的資料夾，容器刪掉資料仍保留 | 外接硬碟 |

**核心規則**：容器隨時可以刪掉重建，所以**資料絕對不能放容器裡**，要放 Volume。

---

## 3. 建立專案骨架

### 3.1 建立 Maven 專案結構

先建立資料夾，之後所有檔案都放在這裡：

```
spring-boot-mysql-docker/
├── pom.xml
├── src/main/java/com/example/demo/
│   ├── DemoApplication.java
│   ├── controller/
│   ├── config/
│   ├── model/
│   ├── repository/
│   └── service/
├── src/main/resources/
│   ├── application.properties
│   └── data.sql
└── docker-compose.yml      # (第 4 章建立)
└── Dockerfile              # (第 11 章建立)
```

### 3.2 pom.xml — 每個依賴的用途

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 https://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <parent>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-parent</artifactId>
        <version>3.2.5</version>
        <relativePath/>
    </parent>

    <groupId>com.example</groupId>
    <artifactId>product-demo</artifactId>
    <version>1.0.0</version>
    <name>product-demo</name>
    <description>Spring Boot + MySQL + Docker Demo</description>

    <properties>
        <java.version>17</java.version>
    </properties>

    <dependencies>
        <!-- RESTful API 所需：內含 Spring MVC + Tomcat + Jackson JSON -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
        </dependency>

        <!-- JPA ORM 所需：內含 Hibernate，讓我們用 Entity 操控資料庫 -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-data-jpa</artifactId>
        </dependency>

        <!-- MySQL JDBC 驅動：runtime 才會用到 -->
        <dependency>
            <groupId>com.mysql</groupId>
            <artifactId>mysql-connector-j</artifactId>
            <scope>runtime</scope>
        </dependency>

        <!-- 自動產生 getter/setter/建構子，減少樣板程式碼 -->
        <dependency>
            <groupId>org.projectlombok</groupId>
            <artifactId>lombok</artifactId>
            <optional>true</optional>
        </dependency>

        <!-- Swagger UI + OpenAPI 3 文件，localhost:8080/swagger-ui.html 就能看 API -->
        <dependency>
            <groupId>org.springdoc</groupId>
            <artifactId>springdoc-openapi-starter-webmvc-ui</artifactId>
            <version>2.5.0</version>
        </dependency>

        <!-- 開發時熱重載（可不加，非必要） -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-devtools</artifactId>
            <scope>runtime</scope>
            <optional>true</optional>
        </dependency>
    </dependencies>

    <build>
        <plugins>
            <plugin>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-maven-plugin</artifactId>
                <configuration>
                    <excludes>
                        <exclude>
                            <groupId>org.projectlombok</groupId>
                            <artifactId>lombok</artifactId>
                        </exclude>
                    </excludes>
                </configuration>
            </plugin>
        </plugins>
    </build>
</project>
```

**重點說明**：
- `spring-boot-starter-parent` 幫你管理所有 Spring 依賴的版本，所以下面的依賴**不用寫版本號**。
- `mysql-connector-j` 若少了，JPA 會報「Unable to determine a suitable driver class」。
- `springdoc-openapi-starter-webmvc-ui` 的 version **必填**（因為它不在 Boot 的 BOM 管理內）。

### 3.3 主啟動類

```java
package com.example.demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class DemoApplication {

    public static void main(String[] args) {
        SpringApplication.run(DemoApplication.class, args);
    }
}
```

**重點說明**：
- `@SpringBootApplication` = `@Configuration` + `@EnableAutoConfiguration` + `@ComponentScan`，讓 Spring 自動掃描 `com.example.demo` 套件底下所有的 Bean。
- 未來所有類別都放在 `com.example.demo` **之下**的套件，才會被掃描到。

### 3.4 建立套件目錄

在 `src/main/java/com/example/demo/` 下手動建立四個資料夾：`controller`、`service`、`repository`、`model`，另有 `config`（第 6 章 Swagger 設定用）。

> 💡 也可以先繼續往下寫，等 IDE 匯入專案後再建立套件。

---

## 4. Docker 與 MySQL 設定

### 4.1 什麼是 Docker Compose？

當系統需要「多個容器」（例如：MySQL + 你的 App），逐一 `docker run` 太麻煩。**Docker Compose** 用一個 `docker-compose.yml` 檔描述所有服務，一條指令全部啟動。

```
┌─────────────────────────────┐
│  Host (你的電腦)              │
│  ┌───────────┐  ┌─────────┐ │
│  │ app       │  │ mysql   │ │
│  │ :8080     │  │ :3306   │ │
│  └───────────┘  └─────────┘ │
└─────────────────────────────┘
```

> ⚠️ 本階段我們**只先用 Docker 跑 MySQL**，Spring Boot 暫時還在本機跑。這樣做的好處：一次只學一件事，先確認 DB 連得上，再進行容器化的下一步（第 11 章）。

### 4.2 建立 docker-compose.yml（先只有 MySQL）

在專案根目錄建立 `docker-compose.yml`：

```yaml
services:
  mysql:
    image: mysql:8.0
    container_name: product-mysql
    environment:
      MYSQL_ROOT_PASSWORD: 1234
      MYSQL_DATABASE: product_db
      TZ: Asia/Taipei
    ports:
      - "3306:3306"
    volumes:
      - mysql-data:/var/lib/mysql
    healthcheck:
      test: ["CMD", "mysqladmin", "ping", "-h", "localhost", "-p1234"]
      interval: 10s
      timeout: 5s
      retries: 5

volumes:
  mysql-data:
```

**逐項說明**：

| 設定 | 意義 |
| --- | --- |
| `image: mysql:8.0` | 使用 MySQL 官方 8.0 映像（首次執行會自動下載） |
| `container_name` | 容器名稱，方便 `docker exec` 操作 |
| `MYSQL_ROOT_PASSWORD` | root 密碼（**本範例用 1234，正式環境請換掉**） |
| `MYSQL_DATABASE` | 容器啟動時自動建立這個資料庫 |
| `TZ: Asia/Taipei` | 設定容器時區，避免時間差 8 小時 |
| `ports: "3306:3306"` | 把容器內 3306 對外映射到本機 3306，本機程式才連得到 |
| `volumes: mysql-data:/var/lib/mysql` | **最重要**：DB 資料實體存在 Volume，容器刪掉資料不消失 |
| `healthcheck` | 定期 ping MySQL，確認「真的可以用了」，第 11 章會用到 |

> ⚠️ **常見陷阱**：忘了寫 `volumes`，`docker rm` 後資料全部不見，等於資料庫被重置。Volume 是容器化 DB 的**保命符**。

### 4.3 啟動 MySQL 容器並驗證

在 `docker-compose.yml` 所在的目錄執行：

```powershell
docker compose up -d mysql
```

預期輸出：

```
[+] Running 2/2
 ✔ Network ...created
 ✔ Container product-mysql  Started
```

接著驗證容器狀態與 DB：

```powershell
docker ps              # 看 product-mysql 是否 Up
docker exec -it product-mysql mysql -uroot -p1234 -e "SHOW DATABASES;"
```

預期輸出（`-e` 直接執行 SQL，不需要進互動介面）：

```
+--------------------+
| Database           |
+--------------------+
| information_schema |
| mysql              |
| performance_schema |
| product_db         |   ← 已由 MYSQL_DATABASE 自動建立
| sys                |
+--------------------+
```

**重點說明**：
- `docker exec -it` = 進入**執行中的容器**執行指令；`mysql -uroot -p1234 -e "..."` 連到本容器內的 MySQL。
- 你也可以先確認 Volume 存在：`docker volume ls` 會看到 `spring-boot-mysql-docker_mysql-data`。

> 🧪 **自己動手驗證**：執行 `docker compose down` 再把 MySQL 重新 `up -d`，重新登入後 `SHOW DATABASES;` 的 `product_db` 依然在 → 這就證明了 Volume 持久化有效。

---

## 5. 設定檔 application.properties

### 5.1 建立設定檔

在 `src/main/resources/application.properties` 寫入：

```properties
# ---- 伺服器 ----
server.port=8080

# ---- MySQL 連線（第 11 章容器化後，url 會改用環境變數覆寫） ----
spring.datasource.url=jdbc:mysql://localhost:3306/product_db?useUnicode=true&characterEncoding=utf8&serverTimezone=UTC&allowPublicKeyRetrieval=true&useSSL=false
spring.datasource.username=root
spring.datasource.password=1234
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver

# ---- JPA ----
# create-drop：啟動建立表格、停止時刪除。初學最方便
spring.jpa.hibernate.ddl-auto=create-drop
# 讓 Hibernate 建表「先於」data.sql 執行，否則種子資料會建表失敗
spring.jpa.defer-datasource-initialization=true
spring.jpa.open-in-view=false
spring.jpa.properties.hibernate.jdbc.time_zone=UTC

# ---- 種子資料 ----
spring.sql.init.data-locations=classpath:data.sql

# ---- Swagger ----
springdoc.swagger-ui.path=/swagger-ui.html
springdoc.api-docs.path=/v3/api-docs
```

**逐項說明**：

| 屬性 | 意義 |
| --- | --- |
| `spring.datasource.url` | JDBC 連線字串。`localhost:3306` 指向 Docker 對外映射的 MySQL |
| `?useUnicode&characterEncoding=utf8` | 讓中文能正確存讀 |
| `allowPublicKeyRetrieval=true` | **必須**：MySQL 8 預設驗證方式 `caching_sha2_password`，首次連線需要它，否則報 Public Key Retrieval 錯誤 |
| `useSSL=false` | 本機開發關閉 SSL 加密連線 |
| `ddl-auto=create-drop` | 啟動時用 Entity 自動建表、刪表。**初學用**；正式環境用 `none` + Flyway 等遷移工具 |
| `defer-datasource-initialization=true` | 沒加它時，`data.sql` 會在 Hibernate 建表**之前**執行 → 錯誤「Table 'product' doesn't exist」 |

> ⚠️ 只有 **3 個字元之間差異**就很折磨人：`url` 寫成 `ur`、密碼打錯、或漏了 `allowPublicKeyRetrieval`，啟動時都會報 `Cannot create PoolableConnectionFactory`。遇到連線錯誤先檢查這三項。

### 5.2 Spring Boot 怎麼「讀」這種設定？

Spring Boot 採用**約定優於設定**：啟動時自動讀取 `classpath:application.properties`，把 `spring.datasource.*` 對應到內建的 `DataSourceAutoConfiguration`，自動建立 `DataSource` Bean，讓 JPA 可以直接用，你**完全不用寫連線程式**。

---

## 6. 實體設計（Entity）

### 6.1 領域模型

我們有兩個實體，是一個**一對多**關聯：

```
Category (類別)  1 ──── *  Product (商品)
```

| 表格 | 欄位 |
| --- | --- |
| `tb_category` | id, name |
| `tb_product` | id, name, price, stock, category_id(FK) |

### 6.2 Category 實體

`model/Category.java`：

```java
package com.example.demo.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "tb_category")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Category {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", length = 50, nullable = false, unique = true)
    private String name;

    // 一對多：一個類別有多個商品
    @OneToMany(mappedBy = "category", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnoreProperties("category")   // 序列化時忽略商品的 category 欄位，防止無窮迴圈
    private List<Product> products = new ArrayList<>();

    public void addProduct(Product product) {
        products.add(product);
        product.setCategory(this);
    }
}
```

**重點說明**：
- `@Entity` 告訴 Hibernate 這是資料表對應類別；`@Table(name="tb_category")` 明確指定表名（**不建議**用預設的類別名 `category`，因為 `CATEGORY` 在部分資料庫是保留字）。
- `@Id` + `@GeneratedValue(strategy = IDENTITY)`：主鍵自動遞增，MySQL 限定用 `IDENTITY`。
- `@OneToMany(mappedBy = "category")`：**mappedBy 的一方是「被擁有一方」**，外鍵在 `Product` 身上，所以這裡只要 `mappedBy`，Hibernate **不會**在 category 表多加欄位。
- `@JsonIgnoreProperties("category")`：沒有它，回傳 API 時 → Product 有 category → category 又有 products → products 又有 category… **無窮遞迴**，Jackson 直接報錯。

> ⚠️ 實體**一定要有無參數建構子**（`@NoArgsConstructor`），Hibernate 內部用反射建立物件。`@Builder` + `@AllArgsConstructor` 會把無參數建構子蓋掉，所以三個 Lombok 註解要一起用。

### 6.3 Product 實體

`model/Product.java`：

```java
package com.example.demo.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "tb_product")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", length = 100, nullable = false)
    private String name;

    @Column(name = "price", precision = 10, scale = 2, nullable = false)
    private BigDecimal price;

    @Column(name = "stock", nullable = false)
    private Integer stock;

    // 多對一：多個商品屬於同一個類別（外鍵在這邊）
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    @JsonIgnoreProperties("products")   // 反向：忽略類別裡的 products，兩邊互相忽略就安全了
    private Category category;
}
```

**重點說明**：
- `BigDecimal` 存金額，**不要**用 `double`（浮點數會有 0.1 + 0.2 ≠ 0.3 的問題）。
- `@ManyToOne(fetch = LAZY)`：查商品**不會**馬上查類別，等真的用到再查（見第 7 章的 N+1 問題）。
- `@JoinColumn(name = "category_id", nullable = false)`：`tb_product` 表實際會多出 `category_id` 這支外鍵欄位。

---

## 7. Repository 層

### 7.1 從最簡單的 CRUD 開始

`repository/CategoryRepository.java`：

```java
package com.example.demo.repository;

import com.example.demo.model.Category;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoryRepository extends JpaRepository<Category, Long> {
}
```

`repository/ProductRepository.java`（先只寫一個基礎介面）：

```java
package com.example.demo.repository;

import com.example.demo.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductRepository extends JpaRepository<Product, Long> {
}
```

**重點說明**：`JpaRepository<T, ID>` 已經內建 `save`、`findById`、`findAll`、`deleteById`、`count` 等 20+ 個方法，**一行程式都不用寫**。你先後會依難度學習下面 6 種查詢技巧：

1. Derived Query（依方法名推導 SQL）
2. `@Query` JPQL
3. `@Modifying` 批次更新
4. Native Query
5. `JOIN FETCH`（解決 N+1）
6. 分頁與排序

---

### 7.2 技巧一：Derived Query（方法名自動推導 SQL）

新增到 `ProductRepository`：

```java
    // WHERE c.name = ?  （透過關聯欄位查詢）
    List<Product> findByCategoryName(String categoryName);

    // WHERE name LIKE %?%
    List<Product> findByNameContaining(String keyword);

    // WHERE price < ?
    List<Product> findByPriceLessThan(BigDecimal price);

    // WHERE c.name = ? AND price > ?
    List<Product> findByCategoryNameAndPriceGreaterThan(String categoryName, BigDecimal price);

    // SELECT COUNT(*) WHERE c.name = ?
    long countByCategoryName(String categoryName);

    // EXISTS (SELECT 1 WHERE name = ?)
    boolean existsByName(String name);
```

**重點說明**：Spring Data 依「方法名」自動生成查詢，關鍵字是 `findBy` / `countBy` / `existsBy` + 屬性名 + `And` / `Or` / `Containing` / `LessThan` / `GreaterThan`。**屬性名拼錯**會在啟動時直接報錯（`Property ... could not be found`）——這是優點，因為執行前就抓到了。

---

### 7.3 技巧二：@Query（JPQL）

JPQL 是**針對 Entity** 的查詢語言，寫的是「類別名稱 / 屬性名稱」，不是資料表欄位。

新增到 `ProductRepository`：

```java
    @Query("""
            SELECT p FROM Product p
            WHERE (:category IS NULL OR p.category.name = :category)
              AND (:minPrice IS NULL OR p.price >= :minPrice)
            ORDER BY p.price DESC
            """)
    List<Product> searchProducts(@Param("category") String category,
                                 @Param("minPrice") BigDecimal minPrice);

    // 聚合：查某類別商品的平均價格
    @Query("SELECT AVG(p.price) FROM Product p WHERE p.category.name = :categoryName")
    BigDecimal findAveragePriceByCategory(@Param("categoryName") String categoryName);
```

**重點說明**：
- 參數用 `:名稱` + `@Param` 對應。
- `(:category IS NULL OR ...)` 是「**可選條件**」寫法：前端不給 category 就英過濾。很常用！
- JPQL 的 `Product`、`p.price` 對應的是 Java 類別大小寫，**不是** `tb_product`。

---

### 7.4 技巧三：@Modifying（批次更新）

`save()` 是逐筆 UPDATE；當你要「一次把所有商品漲價」時，SQL 層級一次搞定最有效率。

新增到 `ProductRepository`：

```java
    @Modifying
    @Query("UPDATE Product p SET p.price = p.price * (1 + :percent / 100) WHERE p.category.name = :categoryName")
    int increasePriceByPercent(@Param("categoryName") String categoryName,
                               @Param("percent") BigDecimal percent);
```

**重點說明**：
- `@Modifying` 讓查詢變成 UPDATE，**且**會把持久化內容（Entity cache）清掉，避免快取與資料庫不一致。
- 使用 `@Modifying` 的方法**必須在交易內執行**（見第 8 章，否則報 `TransactionRequiredException`）。

---

### 7.5 技巧四：Native Query（原生 SQL）

能使用 MySQL 專屬語法的逃生門（例如 `LIMIT`、`GROUP BY` 或其他資料庫函式）。

```java
    // 原生 SQL：TOP 3 便宜商品
    @Query(value = "SELECT * FROM tb_product ORDER BY price ASC LIMIT 3", nativeQuery = true)
    List<Product> findTop3Cheapest();

    // 原生 SQL 對映到 DTO（只挑需要的欄位）
    @Query(value = "SELECT c.name AS categoryName, COUNT(p.id) AS productCount " +
                   "FROM tb_category c LEFT JOIN tb_product p ON c.id = p.category_id " +
                   "GROUP BY c.id",
           nativeQuery = true)
    List<CategoryCount> countProductsByCategoryNative();
```

`repository/CategoryCount.java`（內嵌投影介面）：

```java
package com.example.demo.repository;

public interface CategoryCount {
    String getCategoryName();
    Long getProductCount();
}
```

**重點說明**：
- JPQL 查不到、但 MySQL 辦得到的事（`LIMIT`、`GROUP BY` 某些情況），就用 `nativeQuery = true`。
- Projection（投影介面）：Spring Data 會自動用欄位別名（如 `categoryName`、`productCount`）餵進 getter，**不用寫建構子或 DTO 類別**。

---

### 7.6 技巧五：JOIN FETCH（解決 N+1 問題）

```java
    // 關聯表一筆 JOIN 撈完，避免逐筆商品各查一次類別
    @Query("SELECT DISTINCT c FROM Category c LEFT JOIN FETCH c.products")
    List<Category> findAllWithProducts();
```

**為什麼需要它？**

N+1 問題示意：

```
查 100 個商品 → 1 次 SELECT tb_product
        ↓ (LAZY，每筆用到 category 時)
每個商品各查 1 次類別 → 100 次 SELECT tb_category
= 總共 101 次 SQL 查詢（N+1）
```

`JOIN FETCH` 改為**一條 SQL 用 LEFT JOIN 一次把 class與產品撈回**：

```
SELECT ... FROM tb_category c LEFT JOIN tb_product p ON ... → 1 次搞定
```

**重點說明**：`LEFT JOIN FETCH` 會因為一對多擴張資料列數，要加 `DISTINCT` 去重；反過來（從 Product 側 `JOIN FETCH p.category`）則不會膨脹。

---

### 7.7 技巧六：分頁與排序

```java
    Page<Product> findAll(Pageable pageable);   // 沿用 JpaRepository 既有方法
```

新版 `PageRequest.of(page, size, Sort)` 已取代舊的 `new PageRequest(...)`（舊版已 deprecated）。

**重點說明**：回傳 `Page` 會自動附帶 `totalElements`（總筆數）與 `totalPages`（總頁數），方便前端做分頁器，回傳資訊比 `List` 完整。

---

## 8. Service 層（@Transactional 交易示範）

### 8.1 為什麼需要交易？

假設「**下單結帳**」要做兩件事：**扣庫存** + **新增訂單**。如果第 1 步成功、第 2 步才失敗，庫存少了、訂單卻沒成立 → 資料錯亂。

**交易（Transaction）** 保證這串動作**全部成功 or 全部失敗**（同時 rollback）。

> 💡 想想 ATM 轉帳：帳戶 A 扣錢 + 帳戶 B 加錢，兩者必須同時成功，否則就是系統 bug。

### 8.2 示範 Case：下單扣庫存

`service/OrderService.java`：

```java
package com.example.demo.service;

import com.example.demo.model.Product;
import com.example.demo.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final ProductRepository productRepository;

    /**
     * 下單：扣庫存。
     * 同一交易內若發生例外（例如庫存不足），會自動 rollback，庫存不會被扣掉。
     */
    @Transactional
    public Product placeOrder(Long productId, int quantity) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("Product not found: " + productId));

        if (product.getStock() < quantity) {
            throw new IllegalStateException("庫存不足，目前庫存: " + product.getStock());
        }

        product.setStock(product.getStock() - quantity);   // 更新實體

        // 模擬第二步失敗的情境：庫存已是負數 → 強制 rollback 驗證
        if (product.getStock() < 0) {
            throw new RuntimeException("Simulated failure: stock went negative!");
        }

        return productRepository.save(product);   // 交易成功才 commit
    }
}
```

`service/PriceService.java`（第二個交易示範）：

```java
package com.example.demo.service;

import com.example.demo.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class PriceService {

    private final ProductRepository productRepository;

    /** 促銷活動：某類別全部漲價 percent% */
    @Transactional
    public int increasePrice(String categoryName, BigDecimal percent) {
        return productRepository.increasePriceByPercent(categoryName, percent);
    }
}
```

**重點說明**：
- `@Transactional` 由 Spring AOP 產生**代理**，方法進入時開交易、正常結束 commit、拋例外 rollback。
- **rollback 的關鍵**：同一個 `Product` 實體被抓出來改 `stock`，Hibernate 在 commit 前自動產生 `UPDATE`。若途中拋例外，交易 rollback，那筆 UPDATE 一起取消 → 庫存還原。

> ⚠️ **陷阱**：`@Transactional` 放在「同類別內呼叫的私有方法」不會生效（代理繞不過 this 呼叫）。要跨 Bean 或 public 方法才有交易邊界。

---

## 9. Controller 層（RESTful API）

### 9.1 RESTful 設計與狀態碼

| HTTP 動詞 | 路徑 | 用途 | 成功狀態碼 |
| --- | --- | --- | --- |
| GET | `/api/categories` | 列表 | 200 |
| POST | `/api/categories` | 新增 | 201 |
| GET | `/api/products` | 列表 | 200 |
| GET | `/api/products/{id}` | 單筆 | 200 |
| POST | `/api/products` | 新增 | 201 |
| PUT | `/api/products/{id}` | 更新 | 200 |
| DELETE | `/api/products/{id}` | 刪除 | 204 |
| POST | `/api/orders/{productId}?quantity=2` | 下單（交易示範） | 200 |

> ⚠️ 新手最常犯：新增也回 200、刪除也回 200。正確慣例是 **POST 成功 → 201 Created**、**刪除成功 → 204 No Content**、**找不到資源 → 404**、**參數錯 → 400**。

### 9.2 CategoryController

`controller/CategoryController.java`：

```java
package com.example.demo.controller;

import com.example.demo.model.Category;
import com.example.demo.repository.CategoryRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
@Tag(name = "Category", description = "商品類別管理")
public class CategoryController {

    private final CategoryRepository categoryRepository;

    @GetMapping
    @Operation(summary = "列出所有類別", description = "回傳全部類別（含商品）")
    public List<Category> list() {
        return categoryRepository.findAll();
    }

    @PostMapping
    @Operation(summary = "新增類別")
    public ResponseEntity<Category> create(@RequestBody Category category) {
        return ResponseEntity.status(HttpStatus.CREATED).body(categoryRepository.save(category));
    }
}
```

### 9.3 ProductController

`controller/ProductController.java`（完整版）：

```java
package com.example.demo.controller;

import com.example.demo.model.Category;
import com.example.demo.model.Product;
import com.example.demo.repository.CategoryRepository;
import com.example.demo.repository.CategoryCount;
import com.example.demo.repository.ProductRepository;
import com.example.demo.service.OrderService;
import com.example.demo.service.PriceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
@Tag(name = "Product", description = "商品管理")
public class ProductController {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final OrderService orderService;
    private final PriceService priceService;

    // ---------- CRUD ----------

    @GetMapping
    @Operation(summary = "全部商品")
    public List<Product> list() {
        return productRepository.findAllWithProducts().stream()
                .flatMap(Category::getProductsStream)
                .toList();
    }

    @GetMapping("/{id}")
    @Operation(summary = "查單筆商品")
    public ResponseEntity<Product> get(@PathVariable Long id) {
        return productRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping
    @Operation(summary = "新增商品")
    public ResponseEntity<Product> create(@RequestBody Product product) {
        return ResponseEntity.status(HttpStatus.CREATED).body(productRepository.save(product));
    }

    @PutMapping("/{id}")
    @Operation(summary = "更新商品")
    public ResponseEntity<Product> update(@PathVariable Long id, @RequestBody Product body) {
        return productRepository.findById(id).map(existing -> {
            body.setId(existing.getId());
            return ResponseEntity.ok(productRepository.save(body));
        }).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "刪除商品", responses = {
            @ApiResponse(responseCode = "204", description = "刪除成功"),
            @ApiResponse(responseCode = "404", description = "找不到商品")
    })
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        if (!productRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        productRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    // ---------- 技巧二：Derived Query ----------

    @GetMapping("/by-category")
    @Operation(summary = "依類別名稱查商品")
    public List<Product> byCategory(@RequestParam String categoryName) {
        return productRepository.findByCategoryName(categoryName);
    }

    @GetMapping("/search")
    @Operation(summary = "名稱模糊搜尋")
    public List<Product> search(@RequestParam String keyword) {
        return productRepository.findByNameContaining(keyword);
    }

    @GetMapping("/cheap")
    @Operation(summary = "低於某價格")
    public List<Product> cheap(@RequestParam BigDecimal price) {
        return productRepository.findByPriceLessThan(price);
    }

    @GetMapping("/category-price")
    @Operation(summary = "類別 + 最低價格組合查詢")
    public List<Product> categoryAndPrice(@RequestParam String category,
                                          @RequestParam BigDecimal price) {
        return productRepository.findByCategoryNameAndPriceGreaterThan(category, price);
    }

    @GetMapping("/count-by-category")
    @Operation(summary = "計算某類別商品數量")
    public long countByCategory(@RequestParam String categoryName) {
        return productRepository.countByCategoryName(categoryName);
    }

    @GetMapping("/exists")
    @Operation(summary = "商品名稱是否存在")
    public boolean exists(@RequestParam String name) {
        return productRepository.existsByName(name);
    }

    // ---------- 技巧三：JPQL @Query ----------

    @GetMapping("/advanced-search")
    @Operation(summary = "進階搜尋（類別 + 最低價格，可選）")
    public List<Product> advancedSearch(@RequestParam(required = false) String category,
                                        @RequestParam(required = false) BigDecimal minPrice) {
        return productRepository.searchProducts(category, minPrice);
    }

    @GetMapping("/avg-price")
    @Operation(summary = "某類別的商品平均價格")
    public BigDecimal avgPrice(@RequestParam String categoryName) {
        return productRepository.findAveragePriceByCategory(categoryName);
    }

    // ---------- 技巧四：Native Query ----------

    @GetMapping("/cheapest-3")
    @Operation(summary = "最便宜的前 3 個商品（Native）")
    public List<Product> cheapest3() {
        return productRepository.findTop3Cheapest();
    }

    @GetMapping("/category-count")
    @Operation(summary = "每個類別的商品數量（Native + Projection）")
    public List<CategoryCount> categoryCount() {
        return productRepository.countProductsByCategoryNative();
    }

    // ---------- 技巧五：資料層 JOIN FETCH（列表已用） ----------

    @GetMapping("/categories-with-products")
    @Operation(summary = "類別＋商品的 JOIN FETCH 查詢")
    public List<Category> categoriesWithProducts() {
        return productRepository.findAllWithProducts();
    }

    // ---------- 技巧六：分頁 ----------

    @GetMapping("/paged")
    @Operation(summary = "分頁查詢",
               parameters = {
                       @Parameter(name = "page", description = "頁碼，從 0 開始"),
                       @Parameter(name = "size", description = "每頁筆數"),
                       @Parameter(name = "sort", description = "排序，例：price,desc")
               })
    public Page<Product> paged(@RequestParam(defaultValue = "0") int page,
                               @RequestParam(defaultValue = "5") int size,
                               @RequestParam(defaultValue = "price,desc") String sort) {
        String[] sortParams = sort.split(",");
        Sort order = Sort.by(Sort.Direction.fromString(sortParams[1]), sortParams[0]);
        return productRepository.findAll(PageRequest.of(page, size, order));
    }

    // ---------- 技巧三：@Modifying ----------

    @PostMapping("/increase-price")
    @Operation(summary = "某類別全部商品漲價 percent%（交易內批次更新）")
    public ResponseEntity<?> increasePrice(@RequestParam String category,
                                           @RequestParam BigDecimal percent) {
        int updated = priceService.increasePrice(category, percent);
        if (updated == 0) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("找不到類別: " + category);
        }
        return ResponseEntity.ok("更新了 " + updated + " 筆商品");
    }

    // ---------- 交易示範：下單扣庫存 ----------

    @PostMapping("/orders/{productId}")
    @Operation(summary = "下單扣庫存（示範 @Transactional rollback）")
    public ResponseEntity<?> placeOrder(@PathVariable Long productId,
                                        @RequestParam int quantity) {
        try {
            return ResponseEntity.ok(orderService.placeOrder(productId, quantity));
        } catch (IllegalStateException | IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }
}
```

**重點說明**：
- 列表 API 刻意用第 7.6 章的 `findAllWithProducts()` 展示 N+1 解法（但 `Category` 回傳會包含 products，因此用 `getProductsStream()` 攤平）。
- Controller 只做：**收參數 → 呼叫 Service/Repository → 包成 ResponseEntity**，業務邏輯留在 Service，這是分層的精神。

### 9.4 補上 Category 的串流方法

為讓 9.3 的 `flatMap(Category::getProductsStream)` 編譯通過，在 `Category.java` 加一個工具方法：

```java
    public java.util.stream.Stream<Product> getProductsStream() {
        return products.stream();
    }
```

> 💡 更清楚的替代方案：列表 API 直接改用 `productRepository.findAll(Sort.by("id"))`，就不需要攤平。你可以在練習題中思考兩者差異。

---

## 10. 種子資料 data.sql

### 10.1 建立種子資料

`src/main/resources/data.sql`：

```sql
-- 注意順序：先刪子表（tb_product），再刪父表（tb_category）
DELETE FROM tb_product;
DELETE FROM tb_category;

-- 先插類別（外鍵的「一」端）
INSERT INTO tb_category (id, name) VALUES
(1, '飲料'),
(2, '零食'),
(3, '3C周邊');

-- 再插商品（外鍵的「多」端，category_id 指向 tb_category.id）
INSERT INTO tb_product (id, name, price, stock, category_id) VALUES
(1, '無糖綠茶', 25.00, 100, 1),
(2, '珍珠奶茶', 60.00, 80, 1),
(3, '洋芋片', 40.00, 200, 2),
(4, '巧克力', 90.00, 150, 2),
(5, '滑鼠', 490.00, 30, 3),
(6, '鍵盤', 990.00, 20, 3);
```

**重點說明**：
- `id` 寫死，是為了讓 `category_id` 外鍵關係清楚、可預期。因為 `ddl-auto=create-drop`，每次重啟表格是新的，寫死 id 不會衝突。
- 執行順序 **先父表後子表**：先插 `tb_category`，`tb_product` 的 `category_id` 才找得到父資料 → 兩個順序顛倒會報外鍵錯誤。
- `data.sql` 只對應 `ddl-auto=create-drop` / `none`；若用 `update`，重啟可能重複插入（可加 `@Sql` 或在 SQL 前 `DELETE`，上面已示範）。

> ⚠️ 沒有 `spring.jpa.defer-datasource-initialization=true` 時，Hibernate 還沒建表，`data.sql` 執行就報 `Table 'tb_product' doesn't exist`。記得確認第 5 章那行有寫。

---

## 11. 將 Spring Boot 容器化（Dockerfile + docker-compose）

前段章節完成後，我們已能在本機 `mvn spring-boot:run` 跑起來連 Docker MySQL（驗證步驟見第 12-1 節）。接著把 Spring Boot **也打包成映像**，讓整組可以只靠 Docker 啟動。

### 11.1 為什麼要用 Multi-stage Build？

```
原始做法（缺點）：                        多階段建置（推薦）：
FROM maven (含 JDK+打包工具)               階段1 build:
COPY src  → RUN mvn package                 FROM maven → 編譯出 app.jar
CMD java -jar app.jar                      階段2 runtime:
→ 映像同時裝了「建置工具 + 執行環境」        FROM eclipse-temurin:17-jre
  ~700MB，又肥又危險                       COPY --from=build app.jar
                                           → 映像只有「執行環境 + jar」
                                             ~250MB，小而安全
```

### 11.2 Dockerfile

在專案根目錄建立 `Dockerfile`：

```dockerfile
# ---------- 階段 1：編譯 ----------
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app
# 先單獨複製 pom.xml，善用 Docker 快取層：只有 pom 變動時才重新下載依賴
COPY pom.xml .
RUN mvn dependency:go-offline
# 再複製原始碼並打包
COPY src ./src
RUN mvn clean package -DskipTests

# ---------- 階段 2：執行 ----------
FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=build /app/target/product-demo-1.0.0.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
```

**逐行說明**：

| 指令 | 意義 |
| --- | --- |
| `FROM ... AS build` | 第一階段命名 `build`，之後可以 `--from=build` 取用它 |
| `COPY pom.xml .` + `mvn dependency:go-offline` | 先把依賴下載成快取層；改程式碼不會重新下載所有依賴，**大幅加速二次建置** |
| `FROM eclipse-temurin:17-jre` | 只留執行環境（JRE），沒有編譯器，體積小 |
| `COPY --from=build /app/target/...jar app.jar` | 只把打好的 jar 搬進執行階段 |
| `EXPOSE 8080` | 只是文件性質「此容器會聽 8080」，實際映射仍需 `ports` |
| `ENTRYPOINT ["java","-jar","app.jar"]` | 容器啟動時執行的指令 |

> ⚠️ jar 檔名要與 `pom.xml` 的 `artifactId + version` 一致：`artifactId=product-demo`、`version=1.0.0` → `target/product-demo-1.0.0.jar`。若改過版本，Dockerfile 這行要同步改。

### 11.3 .dockerignore

避免把本機的 `target/`、`uploads/`、`.git` 一起複製進建置環境（會影響建置速度，甚至把本機敏感性檔案帶進映像）：

```
target/
.gradle/
.git/
*.iml
.idea/
.vscode/
```

### 11.4 更新 docker-compose.yml（加入 app 服務）

現在把 Spring Boot 加進 Compose。**關鍵差異**：容器內 app 連 MySQL 要用**服務名稱** `mysql`，不能用 `localhost`（容器之間是隔離的網路）。

```yaml
services:
  mysql:
    image: mysql:8.0
    container_name: product-mysql
    environment:
      MYSQL_ROOT_PASSWORD: 1234
      MYSQL_DATABASE: product_db
      TZ: Asia/Taipei
    ports:
      - "3306:3306"
    volumes:
      - mysql-data:/var/lib/mysql
    healthcheck:
      test: ["CMD", "mysqladmin", "ping", "-h", "localhost", "-p1234"]
      interval: 10s
      timeout: 5s
      retries: 5

  app:
    build: .
    container_name: product-app
    depends_on:
      mysql:
        condition: service_healthy     # 等 MySQL healthcheck 通過才啟動 app
    environment:
      SPRING_DATASOURCE_URL: jdbc:mysql://mysql:3306/product_db?useUnicode=true&characterEncoding=utf8&serverTimezone=UTC&allowPublicKeyRetrieval=true&useSSL=false
      SPRING_DATASOURCE_USERNAME: root
      SPRING_DATASOURCE_PASSWORD: 1234
    ports:
      - "8080:8080"
    restart: unless-stopped

volumes:
  mysql-data:
```

**重點說明**：

| 差異 | 說明 |
| --- | --- |
| `build: .` | 指定用本目錄的 `Dockerfile` 建立映像（而非 `image:` 拉現成的） |
| `SPRING_DATASOURCE_URL=jdbc:mysql://mysql:3306/...` | **環境變數覆寫**設定檔：Spring Boot 的環境變數優先順序高於 `application.properties`，且用服務名稱 `mysql` 取代 `localhost` |
| `depends_on: condition: service_healthy` | 只靠 `depends_on` 無法保證 DB 已就緒（MySQL 起容器到可連線要數秒），靠 healthcheck 才精準 |
| `restart: unless-stopped` | 例外掛掉時自動重啟，開發期很方便 |

> ⚠️ Spring Boot 連線參數為何**不用**在 environment 重寫 `ddl-auto`？因為它們在 `application.properties` 已有值，環境變數**只覆寫需要改的**（DB 位址與帳密）。這是「同一份設定可用於本機與容器」的核心技巧。

### 11.5 網路的秘密：容器間怎麼互連？

Compose 會自動建立一個**私有網段**，把同一個 `docker-compose.yml` 裡所有服務放在一起。`mysql` 服務在自己的網段內有一個別名（DNS 名稱）就叫 `mysql`，所以 app 容器內可以用 `jdbc:mysql://mysql:3306` 連過去——**這不是魔法，是 Docker 內建的 DNS 解析**。

---

## 12. 執行與驗證

### 12-1 執行模式 A：MySQL 跑 Docker + App 跑本機（開發期）

**目的**：驗證程式與 DB 連線是否正常，Hot reload（devtools）也方便。

```powershell
docker compose up -d mysql                # 1. 啟動 MySQL（若尚未啟動）
mvn spring-boot:run                        # 2. 在本機啟動 Spring Boot
```

預期輸出（最後幾行）：

```
Tomcat started on port 8080
Completed initialization in ... ms
```

接著看 Swagger 文件：瀏覽器開啟 → **http://localhost:8080/swagger-ui.html**

### 12-2 執行模式 B：全部用 Docker（部署/展示期）

```powershell
docker compose up --build                  # --build 強制重新打包映像
```

預期輸出：

```
[+] Building ...
✔ Container product-mysql  Started
✔ Container product-app   Started
```

看兩個容器的狀態與 app 的 log：

```powershell
docker ps
docker logs -f product-app
```

`docker logs` 出現 `Started DemoApplication` 即成功。開啟 **http://localhost:8080/swagger-ui.html**。

### 12-3 用 Swagger 驗證每個 API

1. 開啟 `http://localhost:8080/swagger-ui.html`。
2. 展開一個 API，點右上角「Try it out」→ 填參數 →「Execute」。
3. 觀察「Responses」區的 **Code**（應為 200/201/204）與 Response body。

建議驗證順序（由簡入深）：

| 順序 | API | 預期 |
| --- | --- | --- |
| 1 | `GET /api/categories` | 200，回傳 3 個類別 |
| 2 | `GET /api/products` | 200，回傳 6 個商品 |
| 3 | `GET /api/products/search?keyword=奶茶` | 1 筆：珍珠奶茶 |
| 4 | `GET /api/products/by-category?categoryName=零食` | 2 筆 |
| 5 | `GET /api/products/paged?page=0&size=2&sort=price,asc` | Page 結構，totalElements=6 |
| 6 | `POST /api/products`（body 放商品 JSON） | 201 Created |
| 7 | `DELETE /api/products/{新增的id}` | 204 No Content |
| 8 | `POST /api/orders/1?quantity=5` | 200，stock 從 100 變 95 |
| 9 | `POST /api/orders/1?quantity=999` | 400，顯示錯誤訊息（交易 rollback，stock 仍 95） |

> 🧪 **第 8/9 步就是交易驗證**：第二次故意下超出庫存的單，回 400 且**庫存維持 95** → 證明 rollback 生效。

### 12-4 用 curl 驗證（不想開瀏覽器時）

```powershell
curl http://localhost:8080/api/products
curl http://localhost:8080/api/products/by-category?categoryName=飲料
curl -X POST "http://localhost:8080/api/products/orders/1?quantity=3"
```

### 12-5 開發完成後關閉

```powershell
docker compose down          # 停止並移除容器與自建網路
docker compose down -v       # 連 Volume 一起刪（= 刪除所有資料，小心使用）
docker compose down          # 之後想重新開始：docker compose up --build
```

---

## 13. 常見問題排解

| 症狀 | 原因 | 解法 |
| --- | --- | --- |
| `Cannot create PoolableConnectionFactory` / `Access denied` | 帳密錯 / host 錯 | 檢查 `application.properties` 的 url、username、password 三項 |
| 本機 3306 被占用（`port is already allocated`） | 本機也裝了 MySQL | 把 compose 的 ports 改 `"3307:3306"`，url 相應改 `localhost:3307` |
| `Public Key Retrieval is not allowed` | MySQL 8 驗證方式 | url 加 `allowPublicKeyRetrieval=true&useSSL=false` |
| `Table 'product' doesn't exist`（data.sql 報錯） | Hibernate 還沒建表 | 補 `spring.jpa.defer-datasource-initialization=true` |
| app `Connection refused`，明明 DB 有起來 | app 比 DB 早啟動、或用了 `localhost` | 用 `depends_on: condition: service_healthy`；容器內改用服務名 `mysql` |
| 重啟後種子資料重複 | `ddl-auto=update` 不會清資料 | 用 `create-drop`（開發期）或 data.sql 先 `DELETE FROM` 再插入 |
| `The Tomcat connector configured to listen on port 8080` | 8080 已被占用 | `server.port=8081` 或關掉占用程式 |
| 映像 build 很慢 / 每次都重抓依賴 | 沒利用 Docker 快取 | 確認 Dockerfile 先 `COPY pom.xml` 再 `mvn dependency:go-offline` |
| 刪除 app 容器後資料還在 | —— | 這是 **Volume** 的功勞；要刪資料才用 `docker compose down -v` |

---

## 14. 練習題 / 學習檢查點

> 每一題都有明確「完成標準」。做完代表你已掌握該階段。

### 練習 1：加一個「上架日期」欄位
**難度：★**
- 在 `Product` 加 `private LocalDate releaseDate;`（`@Column(name="release_date")`）。
- 用 Derived Query 新增：`findByReleaseDateAfter(LocalDate date)`。
- 開新 API：`GET /api/products/new-arrivals?after=2026-01-01`。
- 完成標準：Swagger 呼叫該 API，回傳 releaseDate 大於指定日期的商品；`tb_product` 表有 `release_date` 欄。

### 練習 2：把列表改成不 N+1
**難度：★★**
- 現有 `GET /api/products` 用 `flatMap` 攤平很醜。改用 **JOIN FETCH 從 Product 側撈**：在 Repository 寫 `@Query("SELECT p FROM Product p JOIN FETCH p.category")`。
- 完成標準：API 回傳商品時**內含 category 的 name**，且觀察 log 只有 **1 條** SQL（可開 `spring.jpa.show-sql=true` 驗證）。

### 練習 3：防呆參數驗證
**難度：★★**
- 目前 `POST /api/orders/{id}?quantity=5` 若 quantity 是負數也會執行。
- 在 Controller 加一層檢查：`quantity <= 0` 時回 **400** 與訊息。
- 完成標準：`quantity=0` 或負數 → 400；正數 → 200 且庫存正確減少。

### 練習 4：改造為「環境變數優先」的正式設定
**難度：★★★**
- 目前 `application.properties` 寫死 `localhost` 與 root 密碼。
- 改成**環境變數可覆寫**：把三行為
  `spring.datasource.url=${DB_URL:jdbc:mysql://localhost:3306/product_db?...}`，
  username 用 `${DB_USER:root}`，password 用 `${DB_PASSWORD:1234}`。
- 本機測試 `mvn spring-boot:run` 應正常（用預設值）；再以
  `$env:DB_PASSWORD="0000"` 後重跑，應連線失敗 → 證明變數已生效。
- 完成標準：三段預設值語法 `:預設值` 正確，本機與容器兩種模式都正常。

### 練習 5：動手建立一個「第二個服務」練習 Compose 技巧
**難度：★★★**
- 在 `docker-compose.yml` 加一個 **phpMyAdmin** 服務：
  `image: phpmyadmin/phpmyadmin`，設 `PMA_HOST: mysql`（連同 Compose 網段內的 MySQL）、`PMA_PORT: 3306`、`ports: "8081:80"`，並 `depends_on` MySQL。
- 完成標準：`docker compose up -d` 後，瀏覽器開 `http://localhost:8081`，用 root / 1234 登入，能看到 `product_db` 與 `tb_product`、`tb_category` 表資料。

---

## 附錄：本指南所有檔案清單

```
spring-boot-mysql-docker/
├── docker-compose.yml
├── Dockerfile
├── .dockerignore
├── pom.xml
└── src/main/
    ├── java/com/example/demo/
    │   ├── DemoApplication.java
    │   ├── controller/CategoryController.java
    │   ├── controller/ProductController.java
    │   ├── model/Category.java
    │   ├── model/Product.java
    │   ├── repository/CategoryRepository.java
    │   ├── repository/ProductRepository.java
    │   ├── repository/CategoryCount.java
    │   └── service/OrderService.java
    │   └── service/PriceService.java
    └── resources/
        ├── application.properties
        └── data.sql
```

完成以上全部章節與練習，你就同時掌握了 **Spring Boot 分層架構、JPA 六大查詢技巧、以及 Docker 容器化 MySQL + App** 三套技能。