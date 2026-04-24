# FakeProductController 學習文件

## 文件涵蓋內容摘要

### 涉及的 4 個檔案
| 檔案 | 說明 |
|------|------|
| FakeProductController.java | 主控制器，5 個 HTTP 端點 |
| FakeProduct.java | 產品 Entity（含 BLOB 圖片欄位） |
| FakeProductRepository.java | JPA 資料庫操作介面 |
| Rating.java | 評分 Entity（與 FakeProduct 一對一關聯） |

### 5 個 API 端點詳細說明
| 端點 | 功能 |
|------|------|
| `POST /fakeproducts` | 儲存單一產品 |
| `GET /fakeproducts` | 從 `fakestoreapi.com` 抓取並儲存（含下載圖片存 BLOB） |
| `GET /fakeproducts/productimage/{code}` | 從 DB 讀取 BLOB 回傳圖片給瀏覽器 |
| `GET /fakeproducts/showall` | Thymeleaf HTML 頁面顯示所有產品 |

### 關鍵技術說明
- **`RestTemplate`** 呼叫外部 API 與下載圖片
- **`@Lob + byte[]`** 將圖片存入資料庫 BLOB
- **`ResponseEntity`** 控制 HTTP 狀態碼
- **`Optional<T>`** 防止 NullPointerException
- **`@OneToOne cascade`** JPA 一對一關聯與級聯操作

Made changes.
## 專案架構概覽

本範例展示一個 **Spring Boot + JPA + RestTemplate** 的整合應用，功能是從外部 API（[fakestoreapi.com](https://fakestoreapi.com)）抓取產品資料，將圖片以二進位方式存入資料庫，並提供 REST 端點讀取與顯示。

```
com.example.demo/
├── controller/
│   └── FakeProductController.java   ← 本文件主角：HTTP 端點處理
├── model/
│   ├── FakeProduct.java             ← JPA Entity：產品資料表
│   ├── FakeProductRepository.java   ← JPA Repository：資料庫操作介面
│   └── Rating.java                  ← JPA Entity：評分資料表
```

---

## 一、資料模型 (Model Layer)

### 1. `Rating.java` — 評分 Entity

```java
@Entity
@Table(name="rating")
@Data
public class Rating {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Integer id;
    double rate;   // 評分（如 4.5）
    int count;     // 評分人數
}
```

| 重點 | 說明 |
|------|------|
| `@Entity` | 告訴 JPA 這個類別對應一張資料表 |
| `@Table(name="rating")` | 指定資料表名稱為 `rating` |
| `@Id` | 主鍵欄位 |
| `@GeneratedValue(strategy = GenerationType.IDENTITY)` | 主鍵由資料庫自動遞增（AUTO_INCREMENT） |
| `@Data` (Lombok) | 自動產生所有欄位的 getter、setter、toString、equals、hashCode |

---

### 2. `FakeProduct.java` — 產品 Entity

```java
@Entity
@Table(name="fakeproducts")
@Data
public class FakeProduct {
    @Id
    Integer id;                            // 主鍵（使用 API 原始 id，不自動產生）

    String title;                          // 產品名稱
    
    @Column(name = "description", length = 1024)
    String description;                    // 產品描述（限 1024 字元）
    
    String category;                       // 產品分類

    @OneToOne(cascade = CascadeType.ALL, targetEntity = Rating.class)
    @JoinColumn(name = "rating_id", referencedColumnName = "id")
    Rating rating;                         // 一對一關聯到評分資料表

    @Lob
    @Column(name = "picture", length = Integer.MAX_VALUE, nullable = true)
    private byte[] picture;               // 圖片二進位資料（儲存至 DB）

    double price;                          // 價格
    String image;                          // 圖片的原始 URL（來自 API）
}
```

#### 重點說明

| 注解 | 說明 |
|------|------|
| `@Column(length = 1024)` | description 欄位長度設為 1024，避免超過預設的 255 導致截斷或錯誤 |
| `@OneToOne(cascade = CascadeType.ALL)` | FakeProduct 與 Rating 是一對一關係；`cascade = ALL` 表示儲存/刪除 FakeProduct 時，Rating 也會一起被儲存/刪除 |
| `@JoinColumn(name = "rating_id")` | 在 fakeproducts 資料表中建立外鍵欄位 `rating_id` 指向 `rating.id` |
| `@Lob` | 告訴 JPA 這是大型物件（Large Object），對應資料庫 BLOB/LONGBLOB 型別 |
| `byte[] picture` | 將圖片以二進位方式直接存入資料庫（無需檔案系統） |

#### 資料表關聯圖

```
fakeproducts
┌──────────────┬───────────────┐
│ id (PK)      │ INT           │
│ title        │ VARCHAR       │
│ description  │ VARCHAR(1024) │
│ category     │ VARCHAR       │
│ rating_id    │ INT (FK) ─────┼──► rating.id
│ picture      │ LONGBLOB      │
│ price        │ DOUBLE        │
│ image        │ VARCHAR       │
└──────────────┴───────────────┘

rating
┌──────┬────────┐
│ id   │ INT PK │
│ rate │ DOUBLE │
│ count│ INT    │
└──────┴────────┘
```

---

### 3. `FakeProductRepository.java` — 資料庫操作介面

```java
public interface FakeProductRepository extends JpaRepository<FakeProduct, Integer> {
}
```

| 說明 | 細節 |
|------|------|
| 繼承 `JpaRepository<T, ID>` | `T` = Entity 類型、`ID` = 主鍵型別 |
| 無需撰寫任何 SQL | Spring Data JPA 自動提供 `save()`, `findAll()`, `findById()`, `delete()` 等方法 |
| 空介面即可使用 | 所有基礎 CRUD 方法已由父介面提供 |

---

## 二、控制器 (Controller Layer)

### `FakeProductController.java` 完整說明

```java
@RestController
@RequestMapping("/fakeproducts")
public class FakeProductController {
```

| 注解 | 說明 |
|------|------|
| `@RestController` | 等同於 `@Controller + @ResponseBody`，所有方法的回傳值自動序列化為 JSON |
| `@RequestMapping("/fakeproducts")` | 此 Controller 所有路由的基礎前綴為 `/fakeproducts` |

---

### 方法一：`saveProduct()` — 儲存單一產品

```java
@PostMapping()
public FakeProduct saveProduct(@RequestBody FakeProduct product) {
    dao.save(product);
    return product;
}
```

**路由：** `POST /fakeproducts`

| 元素 | 說明 |
|------|------|
| `@PostMapping()` | 對應 HTTP POST 請求 |
| `@RequestBody FakeProduct product` | 從請求的 Body（JSON 格式）自動反序列化成 FakeProduct 物件 |
| `dao.save(product)` | 呼叫 JPA 的 save 方法寫入資料庫（若 id 已存在則更新，不存在則新增） |
| 回傳值 | 回傳儲存後的產品物件（JSON 格式） |

**範例請求：**
```json
POST /fakeproducts
Content-Type: application/json

{
  "id": 1,
  "title": "Test Product",
  "price": 29.99,
  "category": "electronics"
}
```

---

### 方法二：`getAllFakeProducts()` — 從外部 API 抓取所有產品

```java
@GetMapping()
public ResponseEntity<Object> getAllFakeProducts() {
    RestTemplate rt = new RestTemplate();
    FakeProduct[] pts = rt.getForObject(
        URI.create("https://fakestoreapi.com/products"),
        FakeProduct[].class
    );
    if (pts.length > 0) {
        saveToTable(pts);
        return new ResponseEntity(pts, HttpStatus.OK);
    } else {
        return ResponseEntity.noContent().build();
    }
}
```

**路由：** `GET /fakeproducts`

#### 執行流程

```
客戶端
  │
  ▼
GET /fakeproducts
  │
  ▼
RestTemplate 呼叫 https://fakestoreapi.com/products
  │
  ▼
取得 FakeProduct[] JSON 陣列
  │
  ├─ 有資料 → saveToTable(pts) → 回傳 200 OK + JSON
  └─ 無資料 → 回傳 204 No Content
```

#### 關鍵技術說明

| 元素 | 說明 |
|------|------|
| `RestTemplate` | Spring 提供的 HTTP 客戶端，用於呼叫外部 REST API |
| `rt.getForObject(URI, Class)` | 發出 GET 請求，並自動將 JSON 回應反序列化成指定型別 |
| `FakeProduct[].class` | 指定反序列化成 FakeProduct 陣列 |
| `ResponseEntity<Object>` | 可以控制 HTTP 狀態碼的回傳包裝類 |
| `new ResponseEntity(pts, HttpStatus.OK)` | 明確設定狀態碼為 200 OK |
| `ResponseEntity.noContent().build()` | 回傳 204 No Content（有成功但沒有資料） |

---

### 方法三：`saveToTable()` — 抓取圖片並批次儲存（私有輔助方法）

```java
void saveToTable(FakeProduct[] pts) {
    for (FakeProduct f : pts) {
        if (f.getImage() != null) {
            RestTemplate rt = new RestTemplate();
            byte[] pic = rt.getForObject(URI.create(f.getImage()), byte[].class);
            if (pic != null && pic.length > 0)
                f.setPicture(pic);
            dao.save(f);
        }
    }
}
```

#### 執行流程

```
for 每個 FakeProduct
  │
  ├─ image URL 不為 null？
  │    │
  │    ▼
  │  RestTemplate 下載圖片 (byte[])
  │    │
  │    ├─ 下載成功 → f.setPicture(pic) 設定圖片二進位資料
  │    └─ 下載失敗（null 或空） → 跳過設圖片
  │
  └─ dao.save(f) 寫入資料庫（含圖片）
```

| 元素 | 說明 |
|------|------|
| `f.getImage()` | 取得產品的圖片 URL 字串（來自 fakestoreapi.com 的 JSON） |
| `rt.getForObject(URI, byte[].class)` | 下載圖片並轉成 byte 陣列 |
| `f.setPicture(pic)` | 將圖片的 byte[] 設入 FakeProduct 的 `picture` 欄位 |
| `dao.save(f)` | 將含圖片的產品存入資料庫 BLOB 欄位 |

> **注意：** 此方法沒有 `@GetMapping` 等注解，是 Controller 內部的私有輔助方法，不對外暴露 HTTP 端點。

---

### 方法四：`productImage()` — 回傳產品圖片

```java
@GetMapping(value = { "/productimage/{code}" })
public void productImage(
        HttpServletRequest request,
        HttpServletResponse response,
        Model model,
        @PathVariable("code") int code) throws IOException {

    Optional<FakeProduct> product = dao.findById(code);

    if (product.isPresent()) {
        response.setContentType("image/jpeg, image/jpg, image/png, image/gif");
        response.getOutputStream().write(product.get().getPicture());
    }
    response.getOutputStream().close();
}
```

**路由：** `GET /fakeproducts/productimage/{code}`

**範例：** `GET /fakeproducts/productimage/3` → 回傳 id=3 的產品圖片

| 元素 | 說明 |
|------|------|
| `@PathVariable("code") int code` | 從 URL 路徑擷取 `{code}` 並轉成整數 |
| `dao.findById(code)` | JPA 依主鍵查詢，回傳 `Optional<FakeProduct>`（避免 null） |
| `Optional.isPresent()` | 確認資料是否存在後再操作，防止 NullPointerException |
| `response.setContentType(...)` | 設定回應的 Content-Type 讓瀏覽器知道是圖片 |
| `response.getOutputStream().write(byte[])` | 直接將 byte[] 寫入 HTTP 回應串流 |
| 回傳 `void` | 因為直接操作 response 輸出流，不需要 return 值 |

> **用途：** 可在 HTML `<img>` 標籤中使用此端點顯示圖片：
> ```html
> <img src="/fakeproducts/productimage/3" />
> ```

---

### 方法五：`getAllImage()` — 顯示所有產品（Thymeleaf 模板）

```java
@GetMapping(value = "/showall")
public ModelAndView getAllImage() {
    ModelAndView mv = new ModelAndView("showfakeall");
    mv.addObject("products", dao.findAll());
    return mv;
}
```

**路由：** `GET /fakeproducts/showall`

| 元素 | 說明 |
|------|------|
| `ModelAndView` | Spring MVC 的模型+視圖容器，可同時指定模板名稱與傳遞資料 |
| `new ModelAndView("showfakeall")` | 指定要渲染的 Thymeleaf 模板為 `showfakeall.html` |
| `mv.addObject("products", ...)` | 將資料傳入模板，模板中可用 `${products}` 取得 |
| `dao.findAll()` | 查詢資料庫中所有 FakeProduct 記錄 |

> **對應模板：** `src/main/resources/templates/showfakeall.html`（Thymeleaf）

---

## 三、完整 API 端點整理

| 方法 | 路由 | 說明 | 回傳 |
|------|------|------|------|
| `POST` | `/fakeproducts` | 儲存單一產品（JSON Body） | FakeProduct JSON |
| `GET` | `/fakeproducts` | 從外部 API 抓取並儲存全部產品 | FakeProduct[] JSON |
| `GET` | `/fakeproducts/productimage/{code}` | 回傳指定 id 的產品圖片（二進位） | image/jpeg 等 |
| `GET` | `/fakeproducts/showall` | 顯示所有產品（HTML 頁面） | HTML（Thymeleaf） |

---

## 四、資料流程圖（完整）

```
瀏覽器/客戶端
      │
      ├─ GET /fakeproducts ──────────────────────────────────────────────────►
      │                                                                     外部 API
      │   FakeProductController                                    fakestoreapi.com
      │         │                                                         │
      │         ◄──── JSON (FakeProduct[]) ────────────────────────────────┘
      │         │
      │         ├── RestTemplate 下載每張圖片 byte[]
      │         │        ◄──── byte[] ──── 圖片 URL
      │         │
      │         └── dao.save() ──────────────────────────► MySQL fakeproducts 資料表
      │                                                     （含 BLOB 圖片欄位）
      │
      ├─ GET /fakeproducts/productimage/3 ──────────────────────────────────►
      │         │                                                        dao.findById(3)
      │         ◄──── byte[] picture ──── MySQL ───────────────────────────────┘
      │         │
      │         └── 寫入 response OutputStream → 瀏覽器顯示圖片
      │
      └─ GET /fakeproducts/showall ─────────────────────────────────────────►
                │                                                        dao.findAll()
                ◄──── List<FakeProduct> ────────────────────────────────────────┘
                │
                └── ModelAndView("showfakeall") → Thymeleaf 渲染 HTML
```

---

## 五、關鍵技術觀念整理

### `RestTemplate` 使用方式比較

| 方法 | 說明 | 使用場景 |
|------|------|----------|
| `getForObject(url, Class)` | GET 請求，直接回傳物件 | 取得 JSON / byte[] |
| `postForObject(url, body, Class)` | POST 請求，回傳物件 | 發送資料並取得回應 |
| `exchange(url, method, entity, Class)` | 完整控制，支援所有 HTTP 動詞與 Header | 需要自訂 Header 時 |

### `ResponseEntity` vs 直接回傳物件

```java
// 方式一：直接回傳（狀態碼預設 200）
public FakeProduct saveProduct(...) {
    return product;   // 自動 200 OK
}

// 方式二：ResponseEntity（可控制狀態碼）
public ResponseEntity<Object> getAllFakeProducts() {
    return new ResponseEntity(pts, HttpStatus.OK);      // 200
    return ResponseEntity.noContent().build();           // 204
    return ResponseEntity.notFound().build();            // 404
}
```

### `Optional<T>` 防止 NullPointerException

```java
Optional<FakeProduct> product = dao.findById(code);

// 不安全的寫法 ❌
FakeProduct p = dao.findById(code);  // 若不存在直接拋出例外

// 安全的寫法 ✅
if (product.isPresent()) {
    // 安全使用 product.get()
}
```

---

## 六、相依的 Maven 依賴（pom.xml 需要）

```xml
<!-- Spring Boot Web (含 RestTemplate) -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-web</artifactId>
</dependency>

<!-- Spring Data JPA + Hibernate -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-jpa</artifactId>
</dependency>

<!-- Thymeleaf 模板引擎 -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-thymeleaf</artifactId>
</dependency>

<!-- MySQL 驅動 -->
<dependency>
    <groupId>com.mysql</groupId>
    <artifactId>mysql-connector-j</artifactId>
    <scope>runtime</scope>
</dependency>

<!-- Lombok（@Data 注解） -->
<dependency>
    <groupId>org.projectlombok</groupId>
    <artifactId>lombok</artifactId>
    <optional>true</optional>
</dependency>
```

---

## 七、學習重點總結

1. **`@RestController`** — 讓 Controller 的方法回傳值自動序列化為 JSON，不需要 `@ResponseBody`。
2. **`RestTemplate`** — Spring 內建的 HTTP 客戶端，可呼叫外部 API，支援 JSON 自動反序列化與 byte[] 二進位下載。
3. **`@OneToOne + @Lob`** — JPA 一對一關聯與大型物件存儲，可將圖片直接存入資料庫。
4. **`ResponseEntity`** — 可精確控制 HTTP 回應狀態碼（200/204/404 等）。
5. **`Optional<T>`** — 包裝可能為 null 的查詢結果，強制使用 `isPresent()` 進行 null 安全檢查。
6. **`ModelAndView`** — 在同一個 `@RestController` 中仍可回傳 Thymeleaf HTML 視圖（混用 REST 與 MVC）。
