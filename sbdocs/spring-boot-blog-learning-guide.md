# Spring Boot 部落格系統學習文件

> 本文件依照 `pratice-day2`（Spring Boot + JPA 練習專案）的分層架構與章節模式產出。
> 目標是教讀者**從零建立一個部落格（Blog）系統**，涵蓋相同等級的技術深度：分層架構、JPA 實體關聯、各種查詢技巧、交易示範、Swagger 文件與種子資料。

---

## 目錄

1. [專案簡介與技術棧](#1-專案簡介與技術棧)
2. [環境準備](#2-環境準備)
3. [建立專案骨架](#3-建立專案骨架)
4. [設定檔](#4-設定檔)
5. [實體設計（Entity）](#5-實體設計entity)
6. [Repository 層](#6-repository-層)
7. [Service 層](#7-service-層)
8. [Controller 層](#8-controller-層)
9. [種子資料](#9-種子資料)
10. [執行與驗證](#10-執行與驗證)
11. [練習題](#11-練習題)

---

## 1. 專案簡介與技術棧

### 專案目標

建立一個簡單的**部落格系統**，具有以下核心功能：

- **文章（Post）** 與 **分類（Category）** 兩個實體
- 一篇文章屬於一個分類，一個分類下有多篇文章（一對多關聯）
- 文章的 CRUD、依分類查詢、標題模糊搜尋、熱門文章篩選
- 依分類統計文章數量、查詢平均觀看數
- 分頁與排序
- 「發佈文章 / 增加觀看數」的交易示範（含 rollback 練習）
- Swagger 自動產生 API 文件

### 技術棧

| 項目 | 版本 / 內容 |
|---|---|
| Java | 17 |
| Spring Boot | 4.1.0 |
| Build Tool | Maven |
| ORM | Spring Data JPA（Hibernate） |
| 資料庫 | MySQL 8.x（資料庫名稱 `blog_db`） |
| API 文件 | springdoc-openapi 3.1.0（Swagger UI / OpenAPI 3） |
| 其他 | Lombok（選用）、spring-boot-devtools |

### 與 pratice-day2 的對照

| pratice-day2 | 本專案（部落格） |
|---|---|
| `Product`（商品） | `Post`（文章） |
| `Category`（類別） | `Category`（分類） |
| 商品下單扣庫存 | 文章發佈 / 增加觀看數 |
| `/api/products` | `/api/posts` |

---

## 2. 環境準備

### 2.1 安裝清單

1. **JDK 17**：確認 `java -version` 輸出為 17 以上
2. **Maven**：確認 `mvn -version`
3. **MySQL 8.x**：本機安裝並啟動服務
4. **IDE**：Spring Tool Suite 4 或 IntelliJ IDEA

### 2.2 建立資料庫

開啟 MySQL，執行下列 SQL：

```sql
CREATE DATABASE blog_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

> ⚠️ 使用 `utf8mb4`，確保中文與 emoji 都能正確儲存。

---

## 3. 建立專案骨架

### 3.1 建立 Maven 專案

使用 [Spring Initializr](https://start.spring.io) 或 IDE 內建精靈建立：

- Group：`blog.example`
- Artifact：`blog-system`
- Packaging：`jar`
- Java：`17`

### 3.2 pom.xml 完整內容

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
	xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 https://maven.apache.org/xsd/maven-4.0.0.xsd">
	<modelVersion>4.0.0</modelVersion>
	<parent>
		<groupId>org.springframework.boot</groupId>
		<artifactId>spring-boot-starter-parent</artifactId>
		<version>4.1.0</version>
		<relativePath/>
	</parent>
	<groupId>blog.example</groupId>
	<artifactId>blog-system</artifactId>
	<version>0.0.1-SNAPSHOT</version>
	<name>blog-system</name>
	<properties>
		<java.version>17</java.version>
	</properties>
	<dependencies>
		<dependency>
			<groupId>org.springframework.boot</groupId>
			<artifactId>spring-boot-starter-data-jpa</artifactId>
		</dependency>
		<dependency>
			<groupId>org.springframework.boot</groupId>
			<artifactId>spring-boot-starter-webmvc</artifactId>
		</dependency>

		<!-- Swagger / OpenAPI 3 文件（Spring Boot 4 相容） -->
		<dependency>
			<groupId>org.springdoc</groupId>
			<artifactId>springdoc-openapi-starter-webmvc-ui</artifactId>
			<version>3.1.0</version>
		</dependency>

		<dependency>
			<groupId>org.springframework.boot</groupId>
			<artifactId>spring-boot-devtools</artifactId>
			<scope>runtime</scope>
			<optional>true</optional>
		</dependency>
		<dependency>
			<groupId>com.mysql</groupId>
			<artifactId>mysql-connector-j</artifactId>
			<scope>runtime</scope>
		</dependency>
		<dependency>
			<groupId>org.projectlombok</groupId>
			<artifactId>lombok</artifactId>
			<optional>true</optional>
		</dependency>
		<dependency>
			<groupId>org.springframework.boot</groupId>
			<artifactId>spring-boot-starter-data-jpa-test</artifactId>
			<scope>test</scope>
		</dependency>
		<dependency>
			<groupId>org.springframework.boot</groupId>
			<artifactId>spring-boot-starter-webmvc-test</artifactId>
			<scope>test</scope>
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
			<plugin>
				<groupId>org.apache.maven.plugins</groupId>
				<artifactId>maven-compiler-plugin</artifactId>
				<executions>
					<execution>
						<id>default-compile</id>
						<phase>compile</phase>
						<goals>
							<goal>compile</goal>
						</goals>
						<configuration>
							<annotationProcessorPaths>
								<path>
									<groupId>org.projectlombok</groupId>
									<artifactId>lombok</artifactId>
								</path>
							</annotationProcessorPaths>
						</configuration>
					</execution>
					<execution>
						<id>default-testCompile</id>
						<phase>test-compile</phase>
						<goals>
							<goal>testCompile</goal>
						</goals>
						<configuration>
							<annotationProcessorPaths>
								<path>
									<groupId>org.projectlombok</groupId>
									<artifactId>lombok</artifactId>
								</path>
							</annotationProcessorPaths>
						</configuration>
					</execution>
				</executions>
			</plugin>
		</plugins>
	</build>
</project>
```

**依賴逐一說明：**

| 依賴 | 用途 |
|---|---|
| `spring-boot-starter-data-jpa` | JPA / Hibernate，負責資料庫對映與 Repository 自動實作 |
| `spring-boot-starter-webmvc` | REST API（@RestController 等） |
| `springdoc-openapi-starter-webmvc-ui` | Swagger UI 與 OpenAPI 3 文件 |
| `spring-boot-devtools` | 開發時自動重啟、LiveReload |
| `mysql-connector-j` | MySQL 驅動程式 |
| `lombok` | 減少 getter/setter 樣板碼（需在 compiler plugin 中設定 annotation processor） |

> ⚠️ Lombok 若搭配新版 Java 編譯，務必照上方設定 `maven-compiler-plugin` 的 `annotationProcessorPaths`，否則 IDE 與 `mvn` 可能無法產生 getter/setter。

### 3.3 主啟動類

`src/main/java/blog/example/BlogSystemApplication.java`：

```java
package blog.example;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class BlogSystemApplication {

	public static void main(String[] args) {
		SpringApplication.run(BlogSystemApplication.class, args);
	}

}
```

### 3.4 套件目錄結構

```
src/main/java/blog/example/
├── BlogSystemApplication.java
├── config/SwaggerConfig.java
├── controller/PostController.java
├── controller/CategoryController.java
├── model/Post.java
├── model/Category.java
├── repository/PostRepository.java
├── repository/CategoryRepository.java
└── service/PostService.java
```

**分層職責（此架構是全專案的核心）：**

```
Controller（接收 HTTP 請求、回應狀態碼）
    ↓
Service（商業邏輯、交易）
    ↓
Repository（資料庫存取）
    ↓
Entity（資料表對映）
```

---

## 4. 設定檔

### 4.1 application.properties

`src/main/resources/application.properties`：

```properties
spring.application.name=blog-system

server.port=8080

spring.jpa.database-platform=org.hibernate.dialect.MySQLDialect
spring.datasource.username=root
spring.datasource.password=1234
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver
spring.datasource.url=jdbc:mysql://localhost:3306/blog_db?useSSL=false&characterEncoding=utf8

spring.jpa.hibernate.ddl-auto=create-drop
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.format_sql=true

spring.jpa.defer-datasource-initialization=true
spring.sql.init.mode=always
spring.sql.init.data-locations=classpath:data.sql

springdoc.swagger-ui.path=/swagger-ui.html
springdoc.api-docs.path=/v3/api-docs
springdoc.api-docs.enabled=true
springdoc.swagger-ui.tags-sorter=alpha
```

**屬性逐一說明：**

| 屬性 | 意義 |
|---|---|
| `spring.datasource.url` | 連線到 `blog_db`，`characterEncoding=utf8` 支援中文 |
| `spring.jpa.hibernate.ddl-auto=create-drop` | 每次啟動重建資料表（練習用）。正式環境改為 `validate` 或 `none` |
| `spring.jpa.defer-datasource-initialization=true` | 讓 Hibernate 先建好資料表，再執行 `data.sql`，否則會因「表不存在」失敗 |
| `spring.sql.init.data-locations` | 指定種子資料檔位置 |
| `springdoc.*` | Swagger UI 路徑 `/swagger-ui.html`，API JSON 在 `/v3/api-docs` |

> ⚠️ 密碼 `1234` 只是練習用。正式專案請用環境變數注入，切勿寫死並 commit 到版控。

### 4.2 SwaggerConfig

`src/main/java/blog/example/config/SwaggerConfig.java`：

```java
package blog.example.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("blog-system API")
                        .description("Spring Boot JPA 練習專案 API 文檔（Post / Category）")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("開發者")
                                .email("developer@example.com"))
                        .license(new License()
                                .name("Apache 2.0")
                                .url("https://www.apache.org/licenses/LICENSE-2.0")));
    }
}
```

---

## 5. 實體設計（Entity）

### 5.1 Category（分類）

`src/main/java/blog/example/model/Category.java`：

```java
package blog.example.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "categories")
@Schema(description = "文章分類資料模型")
public class Category {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(description = "分類 ID（自動產生）", example = "1", accessMode = Schema.AccessMode.READ_ONLY)
    private Long id;

    @Column(nullable = false, unique = true)
    @Schema(description = "分類名稱（不可重複）", example = "Java", requiredMode = Schema.RequiredMode.REQUIRED)
    private String name;

    @OneToMany(mappedBy = "category", fetch = FetchType.LAZY)
    @JsonIgnoreProperties("category")
    @Schema(description = "此分類下的文章清單（LAZY，預設不載入）", accessMode = Schema.AccessMode.READ_ONLY)
    private List<Post> posts = new ArrayList<>();

    public Category() {}
    public Category(String name) { this.name = name; }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public List<Post> getPosts() { return posts; }
    public void setPosts(List<Post> posts) { this.posts = posts; }

    // ⚠️ 若有 toString()，切勿直接印出 posts（會觸發 LAZY 載入並可能遞迴）
    @Override
    public String toString() {
        return "Category{id=" + id + ", name='" + name + "'}";
    }
}
```

### 5.2 Post（文章）

`src/main/java/blog/example/model/Post.java`：

```java
package blog.example.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "posts")
@Schema(description = "文章資料模型")
public class Post {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(description = "文章 ID（自動產生）", example = "1", accessMode = Schema.AccessMode.READ_ONLY)
    private Long id;

    @Column(nullable = false)
    @Schema(description = "文章標題（必填）", example = "Spring Boot 入門教學", requiredMode = Schema.RequiredMode.REQUIRED)
    private String title;

    @Column(nullable = false, length = 5000)
    @Schema(description = "文章內容（必填）", example = "本文介紹 Spring Boot...", requiredMode = Schema.RequiredMode.REQUIRED)
    private String content;

    @Column(name = "view_count")
    @Schema(description = "觀看數", example = "120")
    private Integer viewCount;

    @Column(name = "published_at")
    @Schema(description = "發佈時間（可為 null）", example = "2026-08-06T10:00:00")
    private LocalDateTime publishedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    @JsonIgnoreProperties("posts")
    @Schema(description = "所屬分類（多對一關聯）")
    private Category category;

    public Category getCategory() { return category; }
    public void setCategory(Category category) { this.category = category; }

    // ★ JPA 必須有無參數建構子（JPA 反射建立物件時使用）
    public Post() {}

    // 帶參數建構子，方便在測試或 Service 中快速建立物件
    public Post(String title, String content, Integer viewCount, LocalDateTime publishedAt, Category category) {
        this.title = title;
        this.content = content;
        this.viewCount = viewCount;
        this.publishedAt = publishedAt;
        this.category = category;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public Integer getViewCount() { return viewCount; }
    public void setViewCount(Integer viewCount) { this.viewCount = viewCount; }
    public LocalDateTime getPublishedAt() { return publishedAt; }
    public void setPublishedAt(LocalDateTime publishedAt) { this.publishedAt = publishedAt; }
}
```

### 5.3 設計重點

1. **@ManyToOne / @OneToMany**：`Post` 端用 `@JoinColumn(name = "category_id")` 持有外鍵；`Category` 端用 `mappedBy = "category"` 表示被動方。
2. **LAZY 載入**：`posts` 與 `category` 都是 `FetchType.LAZY`，避免一次載入太多資料，但序列化時要注意（見第 6 節 JOIN FETCH）。
3. **避免 JSON 遞迴**：雙向關聯序列化會無限遞迴，兩端互相用 `@JsonIgnoreProperties` 指定要忽略的欄位。
4. **無參數建構子**：JPA 透過反射建立物件，必須提供。
5. **@Column(length = 5000)**：內容欄位較長，明確指定資料表欄位長度。
6. **LocalDateTime**：JPA 3 / Hibernate 6 已原生支援 `java.time`，不需 `@Temporal`。

---

## 6. Repository 層

### 6.1 CategoryRepository

`src/main/java/blog/example/repository/CategoryRepository.java`：

```java
package blog.example.repository;

import blog.example.model.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {

    // 依名稱查詢分類（Derived Query）
    Optional<Category> findByName(String name);

    // JOIN FETCH：一次查詢所有分類 + 其文章，解決 N+1 查詢問題
    // 不用 JOIN FETCH 的話，每個 Category 都會再發一次 SQL 查文章 → N+1 問題
    @Query("SELECT c FROM Category c LEFT JOIN FETCH c.posts")
    List<Category> findAllWithPosts();
}
```

### 6.2 PostRepository

`src/main/java/blog/example/repository/PostRepository.java`：

```java
package blog.example.repository;

import blog.example.model.Post;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface PostRepository extends JpaRepository<Post, Long> {

    // ===== 練習 1：Derived Query 方法 =====

    List<Post> findByCategoryName(String name);                                            // (1)
    List<Post> findByTitleContaining(String keyword);                                      // (2)
    List<Post> findByViewCountGreaterThan(Integer minView);                                 // (3)
    List<Post> findByCategoryNameAndViewCountGreaterThan(String name, Integer minView);    // (4)
    List<Post> findByCategoryNameOrderByViewCountDesc(String name);                         // (5)
    long countByCategoryName(String name);                                                 // (6)
    boolean existsByTitle(String title);                                                   // (7)

    // ===== 練習 2：@Query JPQL =====

    @Query("SELECT p FROM Post p WHERE p.category.name = :cat ORDER BY p.viewCount DESC")
    List<Post> findHotPostsByCategory(@Param("cat") String categoryName);

    @Query("SELECT AVG(p.viewCount) FROM Post p WHERE p.category.name = :cat")
    Double averageViewCountByCategory(@Param("cat") String categoryName);

    @Modifying
    @Query("UPDATE Post p SET p.viewCount = 0 WHERE p.category.name = :cat")
    int resetViewCountByCategory(@Param("cat") String categoryName);

    // Native Query（表名為 posts，因 @Table(name = "posts")）
    @Query(value = "SELECT * FROM posts WHERE title LIKE '%:keyword%'", nativeQuery = true)
    List<Post> searchByTitleNative(@Param("keyword") String keyword);
}
```

### 6.3 查詢技巧逐步解說

#### 練習 1：Derived Query（依方法名稱自動產生查詢）

Spring Data JPA 會解析方法名稱產生 SQL：

| 方法 | 產生的 SQL 邏輯 |
|---|---|
| `findByCategoryName(name)` | `WHERE category.name = ?`（透過關聯導航到 category 的 name 欄位） |
| `findByTitleContaining(keyword)` | `WHERE title LIKE '%' ? '%'` |
| `findByViewCountGreaterThan(minView)` | `WHERE view_count > ?` |
| `findByCategoryNameAndViewCountGreaterThan(...)` | `WHERE category.name = ? AND view_count > ?` |
| `findByCategoryNameOrderByViewCountDesc(name)` | `WHERE category.name = ? ORDER BY view_count DESC` |
| `countByCategoryName(name)` | `SELECT COUNT(*) WHERE category.name = ?` |
| `existsByTitle(title)` | 判斷是否存在（回傳 boolean） |

> ⚠️ 方法名稱裡對「分類名稱」要寫 `CategoryName`（導航屬性 + 欄位名），而不是 `Category`。這是常見錯誤。

#### 練習 2：@Query JPQL

- **JPQL** 查的是實體與屬性，不是資料表與欄位：`p.category.name`（關聯導航）、`p.viewCount`。
- **@Modifying** 用於 UPDATE / DELETE，且**必須搭配 @Transactional**（見第 7 節 Service）。
- **Native Query** 寫的是原生 SQL：表名 `posts`、欄位 `title`。SQL 字串中的 `%:keyword%` 不會被解析成 LIKE 包裹，故呼叫端需自行加上 `%`（見 Service 的 `searchByTitleNative`）。

---

## 7. Service 層

`src/main/java/blog/example/service/PostService.java`：

```java
package blog.example.service;

import blog.example.model.Post;
import blog.example.repository.PostRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class PostService {

    private final PostRepository postRepository;

    public PostService(PostRepository postRepository) {
        this.postRepository = postRepository;
    }

    // ===== 交易示範：發佈文章（模擬失敗測試 rollback）=====

    @Transactional
    public String publish(Long postId, LocalDateTime publishTime) {
        Post p = postRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("文章不存在，id: " + postId));
        p.setPublishedAt(publishTime);
        postRepository.save(p);
        if (p.getPublishedAt().isAfter(LocalDateTime.now())) {
            throw new RuntimeException("模擬交易失敗，測試 rollback"); // 模擬交易失敗，測試 rollback
        }
        return p.getPublishedAt().toString(); // 若後續拋例外，此 save 也會 rollback
    }

    // ===== 交易示範：增加觀看數（模擬失敗測試 rollback）=====

    @Transactional
    public int addView(Long postId, int amount) {
        if (amount <= 0) {
            throw new IllegalArgumentException("觀看數增量必須大於 0");
        }
        Post p = postRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("文章不存在，id: " + postId));
        int newCount = (p.getViewCount() == null ? 0 : p.getViewCount()) + amount;
        p.setViewCount(newCount);
        postRepository.save(p);
        if (newCount > 10000) {
            throw new RuntimeException("模擬交易失敗，測試 rollback"); // 模擬交易失敗，測試 rollback
        }
        return newCount;
    }

    // ===== 基本 CRUD =====

    public List<Post> findAll() {
        return postRepository.findAll();
    }

    public Optional<Post> findById(Long id) {
        return postRepository.findById(id);
    }

    public Post create(Post post) {
        return postRepository.save(post);
    }

    public Optional<Post> update(Long id, Post updated) {
        return postRepository.findById(id).map(existing -> {
            existing.setTitle(updated.getTitle());
            existing.setContent(updated.getContent());
            existing.setViewCount(updated.getViewCount());
            existing.setCategory(updated.getCategory());
            return postRepository.save(existing);
        });
    }

    public boolean delete(Long id) {
        if (postRepository.existsById(id)) {
            postRepository.deleteById(id);
            return true;
        }
        return false;
    }

    // ===== 練習 1：Derived Query =====

    public List<Post> findByCategory(String categoryName) {
        return postRepository.findByCategoryName(categoryName);
    }

    public List<Post> findByTitleContaining(String keyword) {
        return postRepository.findByTitleContaining(keyword);
    }

    public List<Post> findByViewCountGreaterThan(Integer minView) {
        return postRepository.findByViewCountGreaterThan(minView);
    }

    public List<Post> findByCategoryAndViewCountGreaterThan(String categoryName, Integer minView) {
        return postRepository.findByCategoryNameAndViewCountGreaterThan(categoryName, minView);
    }

    public long countByCategory(String categoryName) {
        return postRepository.countByCategoryName(categoryName);
    }

    public boolean existsByTitle(String title) {
        return postRepository.existsByTitle(title);
    }

    // ===== 練習 2：@Query JPQL / Native =====

    public List<Post> findHotPostsByCategory(String category) {
        return postRepository.findHotPostsByCategory(category);
    }

    public Double averageViewCountByCategory(String category) {
        return postRepository.averageViewCountByCategory(category);
    }

    @Transactional  // ← @Modifying 必須搭配 @Transactional
    public int resetViewCountByCategory(String category) {
        return postRepository.resetViewCountByCategory(category);
    }

    public List<Post> searchByTitleNative(String keyword) {
        return postRepository.searchByTitleNative("%" + keyword + "%");
    }

    // ===== 練習 3：分頁與排序 =====

    public Page<Post> findPaged(int page, int size, String sortBy) {
        return postRepository.findAll(
            PageRequest.of(page, size, Sort.by(sortBy).ascending())
        );
    }
}
```

### 交易（@Transactional）重點

1. **@Transactional** 標記在 Service 方法上，整個方法包成一個資料庫交易。
2. 方法中任何例外（RuntimeException）拋出，交易**自動 rollback**，先前對 `save()` 的修改全部還原。
3. 與 pratice-day2 相同，本文件刻意保留「模擬交易失敗」的程式碼，讀者可驗證 rollback 是否生效：呼叫 API 後，檢查資料庫觀看數 / 發佈時間是否真的沒變。
4. 交易最好開在 **Service 層**，不要開在 Controller。

---

## 8. Controller 層

### 8.1 CategoryController

`src/main/java/blog/example/controller/CategoryController.java`：

```java
package blog.example.controller;

import blog.example.model.Category;
import blog.example.repository.CategoryRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/categories")
@Tag(name = "分類 API", description = "Category 分類的查詢與新增操作")
public class CategoryController {

    private final CategoryRepository categoryRepository;

    public CategoryController(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    // GET /api/categories → 全部分類（不含文章）
    @GetMapping
    @Operation(summary = "查詢分全部分類", description = "回傳所有分類（posts 為 LAZY，此端點不會載入文章）")
    @ApiResponse(responseCode = "200", description = "查詢成功")
    public List<Category> getAll() {
        return categoryRepository.findAll();
    }

    // GET /api/categories/with-posts → 全部分類 + 其文章（JOIN FETCH）
    @GetMapping("/with-posts")
    @Operation(summary = "查詢全部分類及文章", description = "回傳所有分類與各自的文章（JOIN FETCH 一次載入）")
    @ApiResponse(responseCode = "200", description = "查詢成功")
    public List<Category> getAllWithPosts() {
        return categoryRepository.findAllWithPosts();
    }

    // GET /api/categories/{id} → 單筆分類
    @GetMapping("/{id}")
    @Operation(summary = "查詢單筆分類", description = "依 ID 查詢分類")
    @Parameter(name = "id", description = "分類 ID", required = true)
    @ApiResponse(responseCode = "200", description = "查詢成功")
    @ApiResponse(responseCode = "404", description = "分類不存在")
    public ResponseEntity<Category> getById(@PathVariable Long id) {
        return categoryRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // POST /api/categories → 新增分類
    @PostMapping
    @Operation(summary = "新增分類", description = "建立一筆新的分類")
    @ApiResponse(responseCode = "201", description = "分類建立成功")
    public ResponseEntity<Category> create(@RequestBody Category category) {
        Category saved = categoryRepository.save(category);
        URI location = URI.create("/api/categories/" + saved.getId());
        return ResponseEntity.created(location).body(saved);
    }
}
```

### 8.2 PostController

`src/main/java/blog/example/controller/PostController.java`：

```java
package blog.example.controller;

import blog.example.model.Post;
import blog.example.service.PostService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.net.URI;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/posts")
@Tag(name = "文章 API", description = "Post 文章 CRUD、查詢、分頁與交易示範操作")
public class PostController {

    private final PostService postService;

    public PostController(PostService postService) {
        this.postService = postService;
    }

    // ===== 基本 CRUD =====

    // GET /api/posts → 全部文章
    @GetMapping
    @Operation(summary = "查詢全部文章", description = "回傳所有文章的清單")
    @ApiResponse(responseCode = "200", description = "查詢成功")
    public List<Post> getAll() {
        return postService.findAll();
    }

    // GET /api/posts/{id} → 單筆文章
    @GetMapping("/{id}")
    @Operation(summary = "查詢單筆文章", description = "依 ID 查詢單筆文章")
    @Parameter(name = "id", description = "文章 ID", required = true)
    @ApiResponse(responseCode = "200", description = "查詢成功")
    @ApiResponse(responseCode = "404", description = "文章不存在")
    public ResponseEntity<Post> getById(@PathVariable Long id) {
        return postService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // POST /api/posts → 新增文章（201 Created）
    @PostMapping
    @Operation(summary = "新增文章", description = "建立一筆新的文章")
    @ApiResponse(responseCode = "201", description = "文章建立成功")
    public ResponseEntity<Post> create(@RequestBody Post post) {
        Post saved = postService.create(post);
        URI location = URI.create("/api/posts/" + saved.getId());
        return ResponseEntity.created(location).body(saved);
    }

    // PUT /api/posts/{id} → 修改文章
    @PutMapping("/{id}")
    @Operation(summary = "修改文章", description = "依 ID 更新文章資料")
    @Parameter(name = "id", description = "文章 ID", required = true)
    @ApiResponse(responseCode = "200", description = "更新成功")
    @ApiResponse(responseCode = "404", description = "文章不存在")
    public ResponseEntity<Post> update(@PathVariable Long id,
                                       @RequestBody Post updated) {
        return postService.update(id, updated)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // DELETE /api/posts/{id} → 刪除文章（204 No Content）
    @DeleteMapping("/{id}")
    @Operation(summary = "刪除文章", description = "依 ID 刪除文章")
    @Parameter(name = "id", description = "文章 ID", required = true)
    @ApiResponse(responseCode = "204", description = "刪除成功")
    @ApiResponse(responseCode = "404", description = "文章不存在")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        if (postService.delete(id)) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }

    // ===== 交易示範 =====

    // GET /api/posts/{id}/publish?time=2026-08-07T10:00:00 → 發佈文章
    @GetMapping("/{id}/publish")
    @Operation(summary = "發佈文章", description = "設定發佈時間（@Transactional 交易示範，發佈時間在未來會模擬失敗回滾）")
    @Parameter(name = "id", description = "文章 ID", required = true)
    @Parameter(name = "time", description = "發佈時間，格式 yyyy-MM-ddTHH:mm:ss", required = true)
    @ApiResponse(responseCode = "200", description = "發佈成功")
    @ApiResponse(responseCode = "400", description = "文章不存在")
    @ApiResponse(responseCode = "500", description = "交易失敗，已回滾")
    public ResponseEntity<String> publish(@PathVariable Long id,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime time) {
        try {
            String publishedAt = postService.publish(id, time);
            return ResponseEntity.ok("發佈成功，發佈時間: " + publishedAt);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (RuntimeException e) {
            return ResponseEntity.status(500).body("發佈失敗，交易已回滾: " + e.getMessage());
        }
    }

    // GET /api/posts/{id}/add-view?amount=5 → 增加觀看數
    @GetMapping("/{id}/add-view")
    @Operation(summary = "增加觀看數", description = "累加觀看數（@Transactional 交易示範，累加後超過 10000 會模擬失敗回滾）")
    @Parameter(name = "id", description = "文章 ID", required = true)
    @Parameter(name = "amount", description = "增加量（必須大於 0）", required = true)
    @ApiResponse(responseCode = "200", description = "增加成功")
    @ApiResponse(responseCode = "400", description = "文章不存在或增量不合法")
    @ApiResponse(responseCode = "500", description = "交易失敗，已回滾")
    public ResponseEntity<String> addView(@PathVariable Long id, @RequestParam int amount) {
        try {
            int viewCount = postService.addView(id, amount);
            return ResponseEntity.ok("增加成功，目前觀看數: " + viewCount);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (RuntimeException e) {
            return ResponseEntity.status(500).body("增加失敗，交易已回滾: " + e.getMessage());
        }
    }

    // ===== 練習 1：Derived Query =====

    // GET /api/posts/category/{category} → 依分類查詢
    @GetMapping("/category/{category}")
    @Operation(summary = "依分類查詢文章", description = "依分類名稱查詢文章（Derived Query）")
    @Parameter(name = "category", description = "分類名稱", required = true)
    @ApiResponse(responseCode = "200", description = "查詢成功")
    public List<Post> getByCategory(@PathVariable String category) {
        return postService.findByCategory(category);
    }

    // GET /api/posts/search?keyword=Spring → 標題搜尋
    @GetMapping("/search")
    @Operation(summary = "標題模糊搜尋", description = "依標題關鍵字搜尋文章（LIKE）")
    @Parameter(name = "keyword", description = "搜尋關鍵字", required = true)
    @ApiResponse(responseCode = "200", description = "查詢成功")
    public List<Post> search(@RequestParam String keyword) {
        return postService.findByTitleContaining(keyword);
    }

    // GET /api/posts/hot?minView=100 → 熱門文章（觀看數以上）
    @GetMapping("/hot")
    @Operation(summary = "查詢熱門文章", description = "查詢觀看數高於指定值的文章")
    @Parameter(name = "minView", description = "觀看數下限", required = true)
    @ApiResponse(responseCode = "200", description = "查詢成功")
    public List<Post> getHot(@RequestParam Integer minView) {
        return postService.findByViewCountGreaterThan(minView);
    }

    // GET /api/posts/category/{cat}/hot?minView=100 → 分類 + 觀看數篩選
    @GetMapping("/category/{cat}/hot")
    @Operation(summary = "分類 + 觀看數篩選", description = "查詢指定分類且觀看數高於指定值的文章")
    @Parameter(name = "cat", description = "分類名稱", required = true)
    @Parameter(name = "minView", description = "觀看數下限", required = true)
    @ApiResponse(responseCode = "200", description = "查詢成功")
    public List<Post> getCategoryHot(
            @PathVariable String cat, @RequestParam Integer minView) {
        return postService.findByCategoryAndViewCountGreaterThan(cat, minView);
    }

    // GET /api/posts/category/{cat}/count → 分類文章數量
    @GetMapping("/category/{cat}/count")
    @Operation(summary = "統計分類文章數量", description = "計算指定分類下的文章總數")
    @Parameter(name = "cat", description = "分類名稱", required = true)
    @ApiResponse(responseCode = "200", description = "查詢成功")
    public long countByCategory(@PathVariable String cat) {
        return postService.countByCategory(cat);
    }

    // GET /api/posts/exists?title=Spring → 判斷標題是否存在
    @GetMapping("/exists")
    @Operation(summary = "判斷文章標題是否存在", description = "回傳 true/false")
    @Parameter(name = "title", description = "文章標題", required = true)
    @ApiResponse(responseCode = "200", description = "查詢成功")
    public boolean existsByTitle(@RequestParam String title) {
        return postService.existsByTitle(title);
    }

    // ===== 練習 2：@Query JPQL =====

    // GET /api/posts/category/{cat}/hot-posts → 分類熱門排行（依觀看數降序）
    @GetMapping("/category/{cat}/hot-posts")
    @Operation(summary = "查詢分類熱門排行", description = "查詢指定分類下的文章，依觀看數降序（@Query JPQL）")
    @Parameter(name = "cat", description = "分類名稱", required = true)
    @ApiResponse(responseCode = "200", description = "查詢成功")
    public List<Post> getHotPostsByCategory(@PathVariable String cat) {
        return postService.findHotPostsByCategory(cat);
    }

    // GET /api/posts/category/{cat}/avg-view → 平均觀看數
    @GetMapping("/category/{cat}/avg-view")
    @Operation(summary = "查詢分類平均觀看數", description = "計算指定分類下的文章平均觀看數（@Query JPQL）")
    @Parameter(name = "cat", description = "分類名稱", required = true)
    @ApiResponse(responseCode = "200", description = "查詢成功")
    public Double getAvgView(@PathVariable String cat) {
        return postService.averageViewCountByCategory(cat);
    }

    // POST /api/posts/category/{cat}/reset-views → 批次觀看數歸零
    @PostMapping("/category/{cat}/reset-views")
    @Operation(summary = "批次觀看數歸零", description = "將指定分類下所有文章觀看數設為 0（@Modifying 批次更新）")
    @Parameter(name = "cat", description = "分類名稱", required = true)
    @ApiResponse(responseCode = "200", description = "更新成功")
    public ResponseEntity<String> resetViews(@PathVariable String cat) {
        int updated = postService.resetViewCountByCategory(cat);
        return ResponseEntity.ok("已更新 " + updated + " 筆文章觀看數為 0");
    }

    // GET /api/posts/native-search?keyword=Spring → 原生 SQL 搜尋
    @GetMapping("/native-search")
    @Operation(summary = "原生 SQL 搜尋", description = "使用 Native Query 依標題關鍵字搜尋文章")
    @Parameter(name = "keyword", description = "搜尋關鍵字", required = true)
    @ApiResponse(responseCode = "200", description = "查詢成功")
    public List<Post> nativeSearch(@RequestParam String keyword) {
        return postService.searchByTitleNative(keyword);
    }

    // ===== 練習 3：分頁與排序 =====

    // GET /api/posts/page?page=0&size=5&sortBy=viewCount → 分頁查詢
    @GetMapping("/page")
    @Operation(summary = "分頁查詢文章", description = "分頁 + 排序查詢（Pageable）")
    @Parameter(name = "page", description = "頁碼（從 0 開始），預設 0")
    @Parameter(name = "size", description = "每頁筆數，預設 10")
    @Parameter(name = "sortBy", description = "排序欄位，預設 id")
    @ApiResponse(responseCode = "200", description = "查詢成功")
    public Page<Post> getPage(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy) {
        return postService.findPaged(page, size, sortBy);
    }
}
```

### 8.3 Controller 設計重點

1. **RESTful 狀態碼**：查詢 `200`、建立 `201 Created`（帶 Location header）、刪除 `204 No Content`、找不到 `404`、參數錯誤 `400`、交易失敗 `500`。
2. **Controller 只做「接收請求 → 呼叫 Service → 包裝回應」**，不寫商業邏輯。
3. **Swagger 註解**：`@Tag` 分類、`@Operation` 說明、`@Parameter` 參數、`@ApiResponse` 狀態碼，讓 Swagger UI 文件完整可讀。
4. 路徑命名慣例：資源複數 `/api/posts`，子資源 `/api/posts/category/{cat}/count`。

---

## 9. 種子資料

`src/main/resources/data.sql`：

```sql
-- src/main/resources/data.sql
-- categories 表：對應 Category entity 的 @Table(name = "categories")
INSERT INTO blog_db.categories (id, name) VALUES (1, 'Java');
INSERT INTO blog_db.categories (id, name) VALUES (2, '前端');
INSERT INTO blog_db.categories (id, name) VALUES (3, '工具');

-- posts 表：對應 Post entity 的 @Table(name = "posts")
-- category_id 為外鍵，對應上方 categories.id
INSERT INTO blog_db.posts (title, content, view_count, published_at, category_id) VALUES
  ('Spring Boot 入門教學', '從環境安裝開始一步步建立第一個 REST API。', 320, '2026-07-01 10:00:00', 1),
  ('JPA 進階查詢技巧',   'Derived Query、JPQL、Native Query 一次搞懂。',    150, '2026-07-10 09:30:00', 1),
  ('Vue 3 快速上手',     'Composition API 與響應式系統介紹。',             200, '2026-07-15 14:00:00', 2),
  ('Git 日常使用指令',   'commit、branch、merge 常用情境整理。',             88, '2026-07-20 18:00:00', 3),
  ('Docker 基礎概念',    '容器與映像檔的概念與第一個 Container。',         45,  NULL,                    3);
```

### 設計重點

1. **外鍵順序**：先插 `categories` 再插 `posts`，因為 `posts.category_id` 參考 `categories.id`。
2. **與 `ddl-auto` 搭配**：`create-drop` 啟動時重建資料表，再由 `data.sql` 塞入種子資料；必須搭配 `defer-datasource-initialization=true`。
3. **`published_at` 允許 NULL**：有一篇文章尚未發佈（NULL），方便練習「發佈文章」的交易示範。
4. 若要符合 pratice-day2 中「庫存低於 10 會失敗」的設計，可自行調整 `view_count` 使 `add-view` 累加後超過 10000 觸發 rollback。

---

## 10. 執行與驗證

### 10.1 啟動

```bash
mvn spring-boot:run
```

或由 IDE 直接執行 `BlogSystemApplication`。

啟動成功後會看到：

```
Started BlogSystemApplication in x.xx seconds
```

### 10.2 開啟 Swagger UI

瀏覽器開啟：<http://localhost:8080/swagger-ui.html>

Swagger UI 會列出兩個分組：

- **分類 API**（`/api/categories`）
- **文章 API**（`/api/posts`）

每個端點都可以直接在 Swagger UI 上「Try it out」測試。

### 10.3 驗證清單（建議逐項測試）

| 編號 | 操作 | 預期結果 |
|---|---|---|
| 1 | `GET /api/categories` | 回傳 3 筆分類 |
| 2 | `GET /api/categories/with-posts` | 回傳 3 筆分類，每筆含其文章（JOIN FETCH） |
| 3 | `POST /api/posts` 新增一篇文章 | 201 Created，Location 指向新文章 |
| 4 | `GET /api/posts` | 回傳全部文章 |
| 5 | `GET /api/posts/1` | 200，回傳單筆文章 |
| 6 | `PUT /api/posts/1` 修改標題 | 200，更新成功 |
| 7 | `DELETE /api/posts/5` | 204 No Content |
| 8 | `GET /api/posts/category/Java` | 回傳 Java 分類下的文章 |
| 9 | `GET /api/posts/search?keyword=Spring` | 回傳標題含 Spring 的文章 |
| 10 | `GET /api/posts/category/Java/count` | 回傳 2 |
| 11 | `GET /api/posts/category/工具/avg-view` | 回傳平均觀看數 |
| 12 | `POST /api/posts/category/工具/reset-views` | 回傳「已更新 n 筆文章觀看數為 0」 |
| 13 | `GET /api/posts/native-search?keyword=Docker` | 回傳標題含 Docker 的文章 |
| 14 | `GET /api/posts/page?page=0&size=2&sortBy=viewCount` | 回傳 Page 結構，每頁 2 筆、依觀看數升序 |
| 15 | `GET /api/posts/1/publish?time=2026-08-07T10:00:00` | **500**，交易已回滾；再查 `GET /api/posts/1`，`publishedAt` 應仍為 NULL |
| 16 | `GET /api/posts/1/publish?time=2026-08-05T10:00:00` | 200，發佈成功 |
| 17 | `GET /api/posts/2/add-view?amount=5` | 200，觀看數增加 |
| 18 | `GET /api/posts/1/add-view?amount=9999999` | **500**，交易已回滾；再查觀看數應未變 |

> ⚠️ 第 15 步是驗證 rollback 的關鍵：即使 Service 內已 `save()`，拋出例外後交易回滾，資料庫內容不變。

---

## 11. 練習題

以下練習題循序漸進，答案留白供讀者自行實作驗證。

### 練習 1：實作「最新文章」查詢（★）

在 `PostRepository` 加一個 Derived Query，回傳指定分類下、依 `publishedAt` 由新到舊排序的文章，並在 Service、Controller 加上對應 API（`GET /api/posts/category/{cat}/latest`）。

**完成標準**：呼叫 API 能回傳依發佈時間降序的文章清單。

### 練習 2：實作「未發佈文章」查詢（★）

在 `PostRepository` 用 `@Query JPQL` 查詢 `publishedAt IS NULL` 的文章，並在 Controller 加上 `GET /api/posts/drafts`。

**完成標準**：回傳所有尚未發佈的文章。

### 練習 3：批次刪除舊文章（★★）

用 `@Modifying` + `@Query` 刪除指定分類下、發佈時間早於某個日期之前的文章，並在 Service 加上 `@Transactional`。設計 `DELETE /api/posts/category/{cat}/cleanup?before=yyyy-MM-dd`。

**完成標準**：呼叫後指定條件下的舊文章被刪除，且回傳刪除筆數。

### 練習 4：交易不共用（★★）

參考 pratice-day2 的修正重點，試著在 `PostService` 新增一個方法同時呼叫「發佈文章」與「增加觀看數」，觀察 `@Transactional` 對自我呼叫（this 呼叫）的影響。

> 💡 提示：Spring 的 `@Transactional` 透過 Proxy 生效，同類別內部方法呼叫不會觸發新交易。

**完成標準**：能解釋「為何同類別內部呼叫 @Transactional 方法無效」，並提出改用另一 Service 或注入自身 Proxy 的方案。

### 練習 5：整合挑戰（★★★）

將本文件的部落格系統擴充為「支援作者」：新增 `Author` 實體（一對多關聯到 `Post`），並實作：

- `POST /api/authors` 新增作者
- `GET /api/authors/{id}` 查詢作者（含其文章）
- `GET /api/posts/author/{authorId}` 依作者查文章

**完成標準**：三個端點皆可正常運作，且 JSON 序列化無遞迴問題。

---

## 附錄：完整 API 一覽

| Method | Path | 說明 |
|---|---|---|
| GET | /api/categories | 全部分類 |
| GET | /api/categories/with-posts | 全部分類 + 文章（JOIN FETCH） |
| GET | /api/categories/{id} | 單筆分類 |
| POST | /api/categories | 新增分類 |
| GET | /api/posts | 全部文章 |
| GET | /api/posts/{id} | 單筆文章 |
| POST | /api/posts | 新增文章 |
| PUT | /api/posts/{id} | 修改文章 |
| DELETE | /api/posts/{id} | 刪除文章 |
| GET | /api/posts/{id}/publish | 發佈文章（交易示範） |
| GET | /api/posts/{id}/add-view | 增加觀看數（交易示範） |
| GET | /api/posts/category/{category} | 依分類查詢 |
| GET | /api/posts/search?keyword= | 標題模糊搜尋 |
| GET | /api/posts/hot?minView= | 熱門文章 |
| GET | /api/posts/category/{cat}/hot | 分類 + 觀看數篩選 |
| GET | /api/posts/category/{cat}/count | 分類文章數量 |
| GET | /api/posts/exists?title= | 標題是否存在 |
| GET | /api/posts/category/{cat}/hot-posts | 分類熱門排行（JPQL） |
| GET | /api/posts/category/{cat}/avg-view | 平均觀看數 |
| POST | /api/posts/category/{cat}/reset-views | 批次觀看數歸零 |
| GET | /api/posts/native-search?keyword= | 原生 SQL 搜尋 |
| GET | /api/posts/page?page=&size=&sortBy= | 分頁 + 排序 |

---

本文件完成。




