# Day 9 — 練習題：Flyway 資料庫版本管理 + 多資料源

> **對應教材**：`springboot-day09-flyway-multidatasource.md`
> **難度**：⭐⭐⭐⭐ 中高階
> **主題**：Flyway 遷移腳本、版本管理、修復策略、多資料源配置

---

## 練習題 1 — Flyway 腳本設計（概念 + 動手）

### 題目

你的專案需要分 **4 個版本**逐步建立資料庫結構，請根據以下需求撰寫對應的 Flyway SQL 腳本：

**版本規劃**：
```
V1   — 建立 products 資料表
V1.1 — 插入初始商品資料（5 筆）
V2   — 建立 categories 資料表，並為 products 加入 category_id 外鍵
V3   — 新增 products.discount_price 欄位，加入非負數約束
```

**products 表格規格**：
```
id, name(100), description(500), price(10,2), stock(INT), status(20) DEFAULT 'ACTIVE', created_at
```

**categories 表格規格**：
```
id, name(50) UNIQUE, created_at
```

### 提示（Hint）

- 遷移腳本放在 `src/main/resources/db/migration/`
- 命名規則：`V{版本}_{_{說明}.sql`（兩個底線）
- 已執行的腳本絕不能修改（Flyway 會用 checksum 驗證）
- 新增欄位用 `ALTER TABLE`

<details>
<summary>✅ 解答</summary>

**V1__create_products.sql**
```sql
CREATE TABLE products (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    name        VARCHAR(100) NOT NULL,
    description VARCHAR(500),
    price       DECIMAL(10,2) NOT NULL,
    stock       INT NOT NULL DEFAULT 0,
    status      VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at  DATETIME DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_price_positive CHECK (price >= 0),
    CONSTRAINT chk_stock_non_negative CHECK (stock >= 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
```

**V1.1__seed_products.sql**
```sql
INSERT INTO products (name, description, price, stock) VALUES
('Spring Boot 實戰',    'Spring Boot 入門到實戰完整教學', 580.00, 50),
('Java 設計模式',       '23 種 GoF 設計模式詳解',          650.00, 30),
('資料庫系統原理',      'MySQL + PostgreSQL 資料庫設計',   420.00, 40),
('演算法與資料結構',    '面試必備演算法精講',               780.00, 25),
('Docker 容器實戰',     'Docker + Kubernetes 微服務部署',  520.00, 20);
```

**V2__add_categories.sql**
```sql
CREATE TABLE categories (
    id         BIGINT AUTO_INCREMENT PRIMARY KEY,
    name       VARCHAR(50) NOT NULL UNIQUE,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

INSERT INTO categories (name) VALUES ('程式設計'), ('資料庫'), ('DevOps');

ALTER TABLE products
    ADD COLUMN category_id BIGINT,
    ADD CONSTRAINT fk_product_category
        FOREIGN KEY (category_id) REFERENCES categories(id);

-- 初始分類分配
UPDATE products SET category_id = 1 WHERE name IN ('Spring Boot 實戰', 'Java 設計模式', '演算法與資料結構');
UPDATE products SET category_id = 2 WHERE name = '資料庫系統原理';
UPDATE products SET category_id = 3 WHERE name = 'Docker 容器實戰';
```

**V3__add_discount_price.sql**
```sql
ALTER TABLE products
    ADD COLUMN discount_price DECIMAL(10,2) AFTER price;

ALTER TABLE products
    ADD CONSTRAINT chk_discount_price_non_negative
        CHECK (discount_price IS NULL OR discount_price >= 0);

-- 設定預設折扣（9 折）
UPDATE products SET discount_price = ROUND(price * 0.9, 2) WHERE discount_price IS NULL;
```

**application.yml（Flyway 設定）**
```yaml
spring:
  flyway:
    enabled: true
    locations: classpath:db/migration
    baseline-on-migrate: false   # 新建資料庫不需要 baseline
    validate-on-migrate: true    # 啟動時驗證 checksum
```
</details>

---

## 練習題 2 — Flyway 問題修復演練（動手實作）

### 題目

模擬以下兩種 Flyway 故障情境，並學習如何修復：

**情境 A — 腳本執行到一半失敗**：
```sql
-- V4__add_tags.sql（故意寫錯 SQL）
CREATE TABLE tags (
    id   BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(50) NOT NULL
);

-- 刻意寫一個語法錯誤的語句
ALTER TABLE products ADD tag_id BIGINT REFERENCING tags(id);  -- 語法錯誤
```

啟動應用後會出現什麼錯誤？如何修復並繼續？

**情境 B — 誤改了已執行的腳本**：
不小心修改了 `V1__create_products.sql`（加了一個空格），應用啟動時會發生什麼？如何解決？

### 提示（Hint）

- 情境 A：Flyway 會將 V4 標記為 `FAILED`，需要修正腳本後執行 `flyway repair`
- 情境 B：Flyway 啟動時驗證 checksum，不符就拋出 `FlywayValidateException`
  - 解法：執行 `flyway repair` 重新計算 checksum（若改動無害）
  - 或：回復腳本內容到原始版本

<details>
<summary>✅ 解答</summary>

**情境 A 修復步驟**：

1. 應用啟動失敗，console 顯示：
   ```
   FlywayException: Validate failed: Migration V4 failed!
   ```

2. 修正 `V4__add_tags.sql`（修正 SQL 語法）：
   ```sql
   CREATE TABLE tags (
       id   BIGINT AUTO_INCREMENT PRIMARY KEY,
       name VARCHAR(50) NOT NULL
   );
   ALTER TABLE products ADD COLUMN tag_id BIGINT;
   ALTER TABLE products ADD CONSTRAINT fk_product_tag FOREIGN KEY (tag_id) REFERENCES tags(id);
   ```

3. 執行 Flyway Repair（命令列或 Spring Boot 啟動時）：
   ```bash
   # Maven
   mvn flyway:repair -Dflyway.url=jdbc:mysql://localhost:3306/yourdb -Dflyway.user=root -Dflyway.password=1234

   # 或在 application.yml 設定
   spring.flyway.repair-on-migrate: true
   ```

4. 重新啟動應用，V4 從 `FAILED` 變為 `SUCCESS`

**情境 B 修復步驟**：

```
錯誤訊息：FlywayValidateException: Validate failed:
Migration checksum mismatch for migration version 1
-> Applied to database : 1234567890
-> Resolved locally    : 9876543210
```

**解法 1（推薦）**：還原 V1 腳本到原始內容
**解法 2**：若修改無害（如只是空格），執行 `flyway repair` 更新 checksum

**重要原則**：Flyway 腳本是**不可變的歷史記錄**，已執行的腳本絕不應修改！

**情境 B 正確做法**：
- 若需要修改資料庫結構 → 新建 V5 腳本
- 已執行的腳本視為「已發布的提交」，不可更改
</details>

---

## 練習題 3 — 多資料源配置（動手實作）

### 題目

設計一個需要雙資料庫的應用：
- **主庫（primary）**：存放業務資料（employees）
- **唯讀庫（readonly）**：存放審計/報表資料（audit_logs），模擬讀寫分離

**需求**：
1. 配置兩個獨立的 `DataSource`
2. `EmployeeRepository` 使用主庫
3. `AuditLogRepository` 使用唯讀庫
4. 兩個庫各自有獨立的 Flyway 遷移腳本（不同路徑）

**application.yml**：
```yaml
datasource:
  primary:
    url: jdbc:mysql://localhost:3306/employee_db
    username: root
    password: "1234"
  readonly:
    url: jdbc:mysql://localhost:3306/audit_db
    username: root
    password: "1234"
```

### 提示（Hint）

- 每個資料源需要獨立的 `EntityManagerFactory` 和 `TransactionManager`
- 用 `@Primary` 標記主要資料源
- 用 `@EnableJpaRepositories(basePackages = "...", entityManagerFactoryRef = "...")` 指定 Repository 使用哪個 EntityManager
- Flyway 需要針對每個資料源各自設定

<details>
<summary>✅ 解答與解析</summary>

**PrimaryDataSourceConfig.java（主庫）**
```java
@Configuration
@EnableJpaRepositories(
    basePackages = "com.example.repository.primary",
    entityManagerFactoryRef = "primaryEntityManagerFactory",
    transactionManagerRef = "primaryTransactionManager"
)
public class PrimaryDataSourceConfig {

    @Primary
    @Bean
    @ConfigurationProperties("datasource.primary")
    public DataSourceProperties primaryDataSourceProperties() {
        return new DataSourceProperties();
    }

    @Primary
    @Bean
    public DataSource primaryDataSource() {
        return primaryDataSourceProperties()
            .initializeDataSourceBuilder().build();
    }

    @Primary
    @Bean
    public LocalContainerEntityManagerFactoryBean primaryEntityManagerFactory(
            EntityManagerFactoryBuilder builder) {
        return builder
            .dataSource(primaryDataSource())
            .packages("com.example.model.primary")
            .persistenceUnit("primary")
            .build();
    }

    @Primary
    @Bean
    public PlatformTransactionManager primaryTransactionManager(
            @Qualifier("primaryEntityManagerFactory") EntityManagerFactory emf) {
        return new JpaTransactionManager(emf);
    }
}
```

**ReadonlyDataSourceConfig.java（唯讀庫）**
```java
@Configuration
@EnableJpaRepositories(
    basePackages = "com.example.repository.readonly",
    entityManagerFactoryRef = "readonlyEntityManagerFactory",
    transactionManagerRef = "readonlyTransactionManager"
)
public class ReadonlyDataSourceConfig {

    @Bean
    @ConfigurationProperties("datasource.readonly")
    public DataSourceProperties readonlyDataSourceProperties() {
        return new DataSourceProperties();
    }

    @Bean
    public DataSource readonlyDataSource() {
        return readonlyDataSourceProperties()
            .initializeDataSourceBuilder().build();
    }

    @Bean
    public LocalContainerEntityManagerFactoryBean readonlyEntityManagerFactory(
            EntityManagerFactoryBuilder builder) {
        return builder
            .dataSource(readonlyDataSource())
            .packages("com.example.model.readonly")
            .persistenceUnit("readonly")
            .build();
    }

    @Bean
    public PlatformTransactionManager readonlyTransactionManager(
            @Qualifier("readonlyEntityManagerFactory") EntityManagerFactory emf) {
        return new JpaTransactionManager(emf);
    }
}
```

**專案結構**：
```
src/main/java/com/example/
├── model/
│   ├── primary/
│   │   └── Employee.java
│   └── readonly/
│       └── AuditLog.java
└── repository/
    ├── primary/
    │   └── EmployeeRepository.java
    └── readonly/
        └── AuditLogRepository.java

src/main/resources/
├── db/
│   ├── migration/          ← 主庫 Flyway 腳本
│   │   ├── V1__create_employees.sql
│   └── audit-migration/    ← 唯讀庫 Flyway 腳本
│       └── V1__create_audit_logs.sql
└── application.yml
```

**Flyway 多資料源設定（application.yml）**：
```yaml
# 停用 Spring Boot 自動配置的 Flyway（改為手動設定）
spring:
  flyway:
    enabled: false

# 主庫 Flyway 手動 Bean（在 PrimaryDataSourceConfig 加入）
# @Bean
# public Flyway primaryFlyway() {
#     return Flyway.configure()
#         .dataSource(primaryDataSource())
#         .locations("classpath:db/migration")
#         .load();
# }
```
</details>

---

## 練習題 4 — 讀寫分離路由（進階）

### 題目

利用 Spring 的 `AbstractRoutingDataSource`，實作動態資料源路由：
- `@Transactional(readOnly = true)` → 自動路由到唯讀副本
- `@Transactional` → 路由到主庫

這個模式常見於主從架構（Master-Slave / Primary-Replica）。

### 提示（Hint）

- 繼承 `AbstractRoutingDataSource`，覆寫 `determineCurrentLookupKey()`
- 使用 `TransactionSynchronizationManager.isCurrentTransactionReadOnly()` 判斷
- 搭配 AOP 在方法執行前設定資料源 key

<details>
<summary>✅ 解答（核心架構）</summary>

```java
public class RoutingDataSource extends AbstractRoutingDataSource {

    @Override
    protected Object determineCurrentLookupKey() {
        return TransactionSynchronizationManager.isCurrentTransactionReadOnly()
            ? "readonly"
            : "primary";
    }
}

@Configuration
public class DataSourceRoutingConfig {

    @Bean
    public DataSource routingDataSource(
            @Qualifier("primaryDataSource") DataSource primary,
            @Qualifier("readonlyDataSource") DataSource readonly) {

        Map<Object, Object> sources = new HashMap<>();
        sources.put("primary", primary);
        sources.put("readonly", readonly);

        RoutingDataSource routing = new RoutingDataSource();
        routing.setDefaultTargetDataSource(primary);
        routing.setTargetDataSources(sources);
        routing.afterPropertiesSet();
        return routing;
    }
}
```

**使用方式**：
```java
@Service
public class ProductService {

    @Transactional(readOnly = true)  // ← 自動路由到唯讀庫
    public List<Product> findAll() { ... }

    @Transactional  // ← 自動路由到主庫
    public Product save(Product p) { ... }
}
```
</details>

---

## 🏆 挑戰題 — Flyway 腳本策略設計

### 題目

設計一個完整的 Flyway 版本管理策略，回答以下問題：

1. 如何在不停機的情況下執行破壞性遷移（如重命名欄位）？
2. 如何設計支援多環境（dev/prod）但部分腳本只在 dev 執行的策略？
3. 你的團隊有 3 位開發者同時在不同 feature branch 上開發，各自新增了 `V5__xxx.sql`，合併時會發生什麼？如何預防？

<details>
<summary>✅ 解答</summary>

**1. 不停機破壞性遷移（Blue-Green 策略）**：
```sql
-- 步驟 1：新增新欄位（V5__add_full_name.sql）
ALTER TABLE employees ADD COLUMN full_name VARCHAR(200);
UPDATE employees SET full_name = CONCAT(first_name, ' ', last_name);

-- 步驟 2：應用程式同時寫入新舊欄位（部署新版本）

-- 步驟 3：確認無問題後，移除舊欄位（V6__remove_old_name_cols.sql，下一個版本）
ALTER TABLE employees DROP COLUMN first_name;
ALTER TABLE employees DROP COLUMN last_name;
```

**2. 環境差異腳本策略**：
```
db/migration/          ← 所有環境共用
db/migration-dev/      ← 只在 dev 執行（測試資料等）
```

```yaml
# application-dev.yml
spring:
  flyway:
    locations:
      - classpath:db/migration
      - classpath:db/migration-dev

# application-prod.yml
spring:
  flyway:
    locations:
      - classpath:db/migration
```

**3. 版本衝突預防**：
- 約定：不同開發者使用時間戳記 prefix 而非純數字
  ```
  V20240101_001__alice_add_phone.sql
  V20240101_002__bob_add_address.sql
  ```
- 使用 Git 分支保護，合併前必須 rebase 並確認版本號不衝突
- CI pipeline 加入 Flyway 驗證步驟（`flyway validate`）
</details>

---

## 本日學習重點回顧

| 概念 | 重點 |
|------|------|
| Flyway 命名規則 | `V{版本}__{說明}.sql`（兩個底線） |
| checksum 驗證 | 已執行的腳本不可修改；flyway repair 可修復 |
| 不可逆原則 | 不能 undo 已執行的遷移；新需求加新版本 |
| 多資料源 | 每個 DataSource 需要獨立的 EntityManagerFactory |
| AbstractRoutingDataSource | readOnly 交易路由到副本，寫交易路由到主庫 |
