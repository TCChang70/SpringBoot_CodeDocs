# Day 9 — Flyway 資料庫版本管理 + 多資料源

## 學習目標
- 理解資料庫版本管理的重要性
- 使用 Flyway 管理 Schema 版本
- 理解多資料源配置與切換

---

## 1. 為什麼需要 Flyway？

```text
問題：開發團隊多人修改資料庫，Schema 不同步
開發 A 新增了 phone 欄位，開發 B 不知道
上線時忘記手動執行 DDL → 程式崩潰
```

Flyway 將 SQL 腳本納入版本控制，自動執行尚未套用的遷移。

---

## 2. Flyway 依賴

```xml
<dependency>
    <groupId>org.flywaydb</groupId>
    <artifactId>flyway-core</artifactId>
</dependency>
<dependency>
    <groupId>org.flywaydb</groupId>
    <artifactId>flyway-mysql</artifactId>
</dependency>
```

---

## 3. 遷移腳本命名規則

```
V{版本號}__{描述}.sql
```

```
src/main/resources/db/migration/
├── V1__create_employees_table.sql
├── V1.1__seed_employees.sql
├── V2__add_department_table.sql
├── V2.1__add_dept_id_to_employees.sql
└── V3__add_email_unique_constraint.sql
```

### V1__create_employees_table.sql

```sql
CREATE TABLE employees (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    name        VARCHAR(100) NOT NULL,
    email       VARCHAR(150) NOT NULL,
    department  VARCHAR(50),
    salary      DECIMAL(10,2),
    hire_date   DATE,
    created_at  DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at  DATETIME DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_email (email)
);
```

### V1.1__seed_employees.sql

```sql
INSERT INTO employees (name, email, department, salary) VALUES
('Alice Chen', 'alice@test.com', 'Engineering', 85000),
('Bob Wang',   'bob@test.com',   'Marketing',  72000);
```

### V2__add_department_table.sql

```sql
CREATE TABLE departments (
    id   BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE
);

INSERT INTO departments (name) VALUES ('Engineering'), ('Marketing'), ('HR');
```

### V2.1__add_dept_id_to_employees.sql

```sql
ALTER TABLE employees ADD COLUMN dept_id BIGINT;
ALTER TABLE employees ADD CONSTRAINT fk_dept FOREIGN KEY (dept_id) REFERENCES departments(id);
```

---

## 4. application.properties 設定

```properties
# Flyway 設定
spring.flyway.enabled=true
spring.flyway.locations=classpath:db/migration
spring.flyway.baseline-on-migrate=true

# JPA — 改由 Flyway 管理 Schema，ddl-auto 設為 validate
spring.jpa.hibernate.ddl-auto=validate
```

> **重要**：使用 Flyway 後，`ddl-auto` 應設為 `validate` 或 `none`，避免 Hibernate 與 Flyway 衝突。

---

## 5. 多資料源配置

### 5.1 application.properties

```properties
# 主要資料源
spring.datasource.primary.url=jdbc:mysql://localhost:3306/primary_db
spring.datasource.primary.username=root
spring.datasource.primary.password=1234
spring.datasource.primary.driver-class-name=com.mysql.cj.jdbc.Driver

# 次要資料源
spring.datasource.secondary.url=jdbc:mysql://localhost:3306/secondary_db
spring.datasource.secondary.username=root
spring.datasource.secondary.password=1234
spring.datasource.secondary.driver-class-name=com.mysql.cj.jdbc.Driver
```

### 5.2 資料源配置類別

```java
package com.example.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import javax.sql.DataSource;

@Configuration
public class DataSourceConfig {

    @Primary
    @Bean(name = "primaryDataSource")
    @ConfigurationProperties(prefix = "spring.datasource.primary")
    public DataSource primaryDataSource() {
        return DataSourceBuilder.create().build();
    }

    @Bean(name = "secondaryDataSource")
    @ConfigurationProperties(prefix = "spring.datasource.secondary")
    public DataSource secondaryDataSource() {
        return DataSourceBuilder.create().build();
    }
}
```

### 5.3 JPA 設定

```java
package com.example.config;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaVendorAdapter;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import javax.sql.DataSource;

@Configuration
@EnableTransactionManagement
@EnableJpaRepositories(
    basePackages = "com.example.repository.primary",
    entityManagerFactoryRef = "primaryEntityManagerFactory",
    transactionManagerRef = "primaryTransactionManager"
)
public class PrimaryJpaConfig {

    @Primary
    @Bean(name = "primaryEntityManagerFactory")
    public LocalContainerEntityManagerFactoryBean entityManagerFactory(
            @Qualifier("primaryDataSource") DataSource ds) {
        LocalContainerEntityManagerFactoryBean em = new LocalContainerEntityManagerFactoryBean();
        em.setDataSource(ds);
        em.setPackagesToScan("com.example.model.primary");
        em.setJpaVendorAdapter(new HibernateJpaVendorAdapter());
        return em;
    }
}
```

---

## 6. 動手練習

1. 加入 Flyway 依賴，建立 V1、V2 SQL 遷移腳本
2. 啟動專案，觀察 Flyway 自動建立 flyway_schema_history 表
3. 修改 `ddl-auto` 為 `validate`，確認 Hibernate 與 Flyway 的互動
4. 練習回滾：修改已套用的 V1 SQL，觀察錯誤
5. （選做）配置第二資料源，練習寫入不同資料庫

---

## 7. 優化與進階學習

### 優化建議
關於本文件的優化修改意見，請參考：[優化修改意見](springboot-day09-optimization-suggestions.md)

### 實作練習
為了加深對 Flyway 和多資料源的理解，我們提供了完整的實作練習文件，包含 5 個梯度式練習：
- **練習 1**：基本 Flyway 遷移實作 ⭐
- **練習 2**：Flyway 進階功能實作 ⭐⭐
- **練習 3**：基本多資料源配置實作 ⭐⭐
- **練習 4**：動態資料源切換實作 ⭐⭐⭐
- **練習 5**：整合測試與最佳實踐實作 ⭐⭐⭐

**實作練習文件**：[Spring Boot Day 09 實作練習](springboot-day09-practice.md)

### 學習建議
1. **循序漸進**：按照練習順序完成，先掌握基礎再挑戰進階
2. **動手實作**：不要只看程式碼，務必親自輸入並執行
3. **觀察遷移過程**：啟動應用時觀察 Flyway 的遷移日誌
4. **測試切換**：使用 Postman 或 curl 測試動態資料源切換功能
5. **擴展功能**：在完成基礎練習後，嘗試整合 Spring Batch 或微服務架構

### 常見問題
在學習過程中遇到問題時，可以參考：
- [實作練習 - 常見問題排除](springboot-day09-practice.md#常見問題排除)
- [Flyway 官方文件](https://flywaydb.org/documentation/)
- [Spring Boot 多資料源配置](https://spring.io/guides/gs/multi-datasource/)
