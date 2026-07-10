# Day 09 — Flyway 資料庫版本管理 + 多資料源 實作練習

## 學習目標
- 透過實作掌握 Flyway 資料庫版本管理
- 理解多資料源配置與切換
- 學習遷移腳本的設計與錯誤處理
- 掌握企業級應用中的資料庫管理策略

---

## 練習 1：基本 Flyway 遷移 ⭐

### 1.1 建立遷移腳本結構
**目標**：建立完整的 Flyway 遷移腳本結構

**步驟**：
1. 建立遷移腳本目錄結構
2. 建立 V1 版本遷移腳本
3. 建立 V2 版本遷移腳本
4. 驗證遷移腳本執行

**實作**：

```bash
# 建立目錄結構
mkdir -p src/main/resources/db/migration
```

```sql
-- V1__create_users_table.sql
CREATE TABLE users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL,
    email VARCHAR(100) NOT NULL,
    password VARCHAR(255) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_username (username),
    UNIQUE KEY uk_email (email)
);

-- V1.1__seed_users.sql
INSERT INTO users (username, email, password) VALUES
('admin', 'admin@example.com', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi'),
('user1', 'user1@example.com', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi');

-- V2__create_orders_table.sql
CREATE TABLE orders (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    total_amount DECIMAL(10,2) NOT NULL,
    status VARCHAR(20) DEFAULT 'PENDING',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id)
);
```

**application.properties 配置**：
```properties
# Flyway 設定
spring.flyway.enabled=true
spring.flyway.locations=classpath:db/migration
spring.flyway.baseline-on-migrate=true
spring.flyway.validate-on-migrate=true

# JPA 設定
spring.jpa.hibernate.ddl-auto=validate
spring.jpa.show-sql=true
```

**驗證方法**：
1. 啟動應用程式
2. 檢查資料庫是否建立 users 和 orders 表
3. 檢查 flyway_schema_history 表記錄

### 1.2 遷移腳本錯誤處理
**目標**：學習處理遷移腳本錯誤

**步驟**：
1. 建立一個有錯誤的遷移腳本
2. 觀察錯誤處理流程
3. 修正錯誤並重新執行

**實作**：

```sql
-- V3__create_products_table.sql (故意包含錯誤)
CREATE TABLE products (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    price DECIMAL(10,2) NOT NULL,
    stock INT DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 故意的語法錯誤
ALTER TABLE products ADD COLUMN invalid_column INT NOT NULL; -- 缺少預設值
```

**錯誤處理步驟**：
1. 啟動應用程式觀察錯誤
2. 檢查 flyway_schema_history 表狀態
3. 修正 SQL 語法
4. 重新執行遷移

**修正後的 SQL**：
```sql
-- V3__create_products_table.sql (修正後)
CREATE TABLE products (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    price DECIMAL(10,2) NOT NULL,
    stock INT DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

ALTER TABLE products ADD COLUMN category VARCHAR(50) DEFAULT 'GENERAL';
```

---

## 練習 2：Flyway 進階功能 ⭐⭐

### 2.1 Callbacks 實作
**目標**：學習使用 Flyway callbacks 監控遷移過程

**實作**：

```java
package com.example.flyway;

import org.flywaydb.core.api.callback.Callback;
import org.flywaydb.core.api.event.Event;
import org.springframework.stereotype.Component;

@Component
public class MigrationCallback implements Callback {

    @Override
    public String getEvent() {
        return Event.AFTER_MIGRATE.name();
    }

    @Override
    public void handle(Event event, Context context) {
        System.out.println("=== Migration Event: " + event.name() + " ===");
        System.out.println("Database: " + context.getConnection().getCatalog());
        
        // 記錄遷移資訊
        logMigrationInfo(context);
    }

    private void logMigrationInfo(Context context) {
        // 可以將遷移資訊記錄到資料庫或檔案
        System.out.println("Migration completed at: " + new java.util.Date());
    }
}
```

**配置 callbacks**：
```properties
spring.flyway.callbacks=com.example.flyway.MigrationCallback
```

### 2.2 Placeholders 實作
**目標**：學習使用 Flyway placeholders 動態替換

**實作**：

```sql
-- V4__create_audit_log_table.sql
CREATE TABLE audit_log_${environment} (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    table_name VARCHAR(50) NOT NULL,
    record_id BIGINT NOT NULL,
    action VARCHAR(20) NOT NULL,
    old_values JSON,
    new_values JSON,
    created_by VARCHAR(50),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 根據環境建立索引
CREATE INDEX idx_audit_log_${environment}_table_name ON audit_log_${environment}(table_name);
CREATE INDEX idx_audit_log_${environment}_created_at ON audit_log_${environment}(created_at);
```

**配置 placeholders**：
```properties
spring.flyway.placeholders.environment=${SPRING_PROFILES_ACTIVE:dev}
spring.flyway.placeholders.audit.enabled=true
```

### 2.3 多環境配置
**目標**：學習為不同環境配置不同的遷移策略

**實作**：

```yaml
# application-dev.yml
spring:
  flyway:
    locations: classpath:db/migration,classpath:db/migration/dev
    placeholders:
      environment: dev
      seed-data: true

# application-prod.yml
spring:
  flyway:
    locations: classpath:db/migration,classpath:db/migration/prod
    placeholders:
      environment: prod
      seed-data: false
    out-of-order: false
    validate-on-migrate: true
```

**目錄結構**：
```
src/main/resources/db/migration/
├── V1__create_base_tables.sql
├── V2__add_indexes.sql
├── dev/
│   ├── V1.1__seed_dev_data.sql
│   └── V1.2__create_test_tables.sql
└── prod/
    ├── V1.1__create_production_indexes.sql
    └── V1.2__add_audit_triggers.sql
```

---

## 練習 3：基本多資料源配置 ⭐⭐

### 3.1 雙資料源配置
**目標**：配置兩個獨立的資料源

**實作**：

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

```properties
# 主要資料源 (使用者資料)
spring.datasource.primary.url=jdbc:mysql://localhost:3306/user_db
spring.datasource.primary.username=root
spring.datasource.primary.password=1234
spring.datasource.primary.driver-class-name=com.mysql.cj.jdbc.Driver

# 次要資料源 (訂單資料)
spring.datasource.secondary.url=jdbc:mysql://localhost:3306/order_db
spring.datasource.secondary.username=root
spring.datasource.secondary.password=1234
spring.datasource.secondary.driver-class-name=com.mysql.cj.jdbc.Driver
```

### 3.2 JPA 配置
**目標**：為每個資料源配置獨立的 JPA

**實作**：

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
import java.util.HashMap;
import java.util.Map;

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
            @Qualifier("primaryDataSource") DataSource dataSource,
            EntityManagerFactoryBuilder builder) {
        
        LocalContainerEntityManagerFactoryBean em = new LocalContainerEntityManagerFactoryBean();
        em.setDataSource(dataSource);
        em.setPackagesToScan("com.example.model.primary");
        em.setPersistenceUnit("primary");
        
        JpaVendorAdapter vendorAdapter = new HibernateJpaVendorAdapter();
        em.setJpaVendorAdapter(vendorAdapter);
        
        Map<String, Object> properties = new HashMap<>();
        properties.put("hibernate.hbm2ddl.auto", "validate");
        properties.put("hibernate.dialect", "org.hibernate.dialect.MySQL8Dialect");
        em.setJpaPropertyMap(properties);
        
        return em;
    }

    @Bean(name = "primaryTransactionManager")
    public org.springframework.transaction.PlatformTransactionManager primaryTransactionManager(
            @Qualifier("primaryEntityManagerFactory") javax.persistence.EntityManagerFactory entityManagerFactory) {
        return new org.springframework.orm.jpa.JpaTransactionManager(entityManagerFactory);
    }
}
```

```java
package com.example.config;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaVendorAdapter;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

@Configuration
@EnableTransactionManagement
@EnableJpaRepositories(
    basePackages = "com.example.repository.secondary",
    entityManagerFactoryRef = "secondaryEntityManagerFactory",
    transactionManagerRef = "secondaryTransactionManager"
)
public class SecondaryJpaConfig {

    @Bean(name = "secondaryEntityManagerFactory")
    public LocalContainerEntityManagerFactoryBean entityManagerFactory(
            @Qualifier("secondaryDataSource") DataSource dataSource,
            EntityManagerFactoryBuilder builder) {
        
        LocalContainerEntityManagerFactoryBean em = new LocalContainerEntityManagerFactoryBean();
        em.setDataSource(dataSource);
        em.setPackagesToScan("com.example.model.secondary");
        em.setPersistenceUnit("secondary");
        
        JpaVendorAdapter vendorAdapter = new HibernateJpaVendorAdapter();
        em.setJpaVendorAdapter(vendorAdapter);
        
        Map<String, Object> properties = new HashMap<>();
        properties.put("hibernate.hbm2ddl.auto", "validate");
        properties.put("hibernate.dialect", "org.hibernate.dialect.MySQL8Dialect");
        em.setJpaPropertyMap(properties);
        
        return em;
    }

    @Bean(name = "secondaryTransactionManager")
    public org.springframework.transaction.PlatformTransactionManager secondaryTransactionManager(
            @Qualifier("secondaryEntityManagerFactory") javax.persistence.EntityManagerFactory entityManagerFactory) {
        return new org.springframework.orm.jpa.JpaTransactionManager(entityManagerFactory);
    }
}
```

### 3.3 Repository 實作
**目標**：建立不同資料源的 Repository

**實作**：

```java
// Primary Repository (使用者資料)
package com.example.repository.primary;

import com.example.model.primary.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    User findByUsername(String username);
    User findByEmail(String email);
}

// Secondary Repository (訂單資料)
package com.example.repository.secondary;

import com.example.model.secondary.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    List<Order> findByUserId(Long userId);
    List<Order> findByStatus(String status);
}
```

```java
// Entity 類別
package com.example.model.primary;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, unique = true)
    private String username;
    
    @Column(nullable = false, unique = true)
    private String email;
    
    @Column(nullable = false)
    private String password;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    // getters and setters
}

package com.example.model.secondary;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "orders")
public class Order {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "user_id", nullable = false)
    private Long userId;
    
    @Column(name = "total_amount", nullable = false)
    private BigDecimal totalAmount;
    
    @Column(nullable = false)
    private String status;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    // getters and setters
}
```

---

## 練習 4：動態資料源切換 ⭐⭐⭐

### 4.1 動態資料源配置
**目標**：實現運行時動態切換資料源

**實作**：

```java
package com.example.config;

import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;

public class DynamicDataSource extends AbstractRoutingDataSource {

    @Override
    protected Object determineCurrentLookupKey() {
        return DataSourceContextHolder.getDataSourceType();
    }
}
```

```java
package com.example.config;

public class DataSourceContextHolder {

    private static final ThreadLocal<String> contextHolder = new ThreadLocal<>();

    public static void setDataSourceType(String dataSourceType) {
        contextHolder.set(dataSourceType);
    }

    public static String getDataSourceType() {
        return contextHolder.get();
    }

    public static void clearDataSourceType() {
        contextHolder.remove();
    }
}
```

```java
package com.example.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

@Configuration
public class DynamicDataSourceConfig {

    @Bean
    @Primary
    public DataSource dynamicDataSource() {
        DynamicDataSource dataSource = new DynamicDataSource();
        
        Map<Object, Object> targetDataSources = new HashMap<>();
        targetDataSources.put("primary", primaryDataSource());
        targetDataSources.put("secondary", secondaryDataSource());
        
        dataSource.setTargetDataSources(targetDataSources);
        dataSource.setDefaultTargetDataSource(primaryDataSource());
        
        return dataSource;
    }

    private DataSource primaryDataSource() {
        // 返回主要資料源配置
        return DataSourceBuilder.create()
            .url("jdbc:mysql://localhost:3306/user_db")
            .username("root")
            .password("1234")
            .build();
    }

    private DataSource secondaryDataSource() {
        // 返回次要資料源配置
        return DataSourceBuilder.create()
            .url("jdbc:mysql://localhost:3306/order_db")
            .username("root")
            .password("1234")
            .build();
    }
}
```

### 4.2 AOP 切面實現
**目標**：使用 AOP 自動切換資料源

**實作**：

```java
package com.example.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface DataSource {
    String value() default "primary";
}
```

```java
package com.example.aspect;

import com.example.annotation.DataSource;
import com.example.config.DataSourceContextHolder;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class DataSourceAspect {

    @Pointcut("@annotation(com.example.annotation.DataSource)")
    public void dataSourcePointcut() {
    }

    @Around("dataSourcePointcut()")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
        DataSource dataSource = joinPoint.getTarget().getClass()
            .getMethod(joinPoint.getSignature().getName())
            .getAnnotation(DataSource.class);
        
        if (dataSource != null) {
            DataSourceContextHolder.setDataSourceType(dataSource.value());
        }
        
        try {
            return joinPoint.proceed();
        } finally {
            DataSourceContextHolder.clearDataSourceType();
        }
    }
}
```

### 4.3 使用註解切換
**目標**：在 Service 層使用註解切換資料源

**實作**：

```java
package com.example.service;

import com.example.annotation.DataSource;
import com.example.model.primary.User;
import com.example.model.secondary.Order;
import com.example.repository.primary.UserRepository;
import com.example.repository.secondary.OrderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private OrderRepository orderRepository;

    @DataSource("primary")
    @Transactional("primaryTransactionManager")
    public User createUser(User user) {
        return userRepository.save(user);
    }

    @DataSource("secondary")
    @Transactional("secondaryTransactionManager")
    public Order createOrder(Order order) {
        return orderRepository.save(order);
    }

    @DataSource("primary")
    @Transactional("primaryTransactionManager")
    public User findUserById(Long id) {
        return userRepository.findById(id).orElse(null);
    }

    @DataSource("secondary")
    @Transactional("secondaryTransactionManager")
    public List<Order> findOrdersByUserId(Long userId) {
        return orderRepository.findByUserId(userId);
    }
}
```

### 4.4 動態資料源切換 Service
**目標**：建立可動態切換的 Service

**實作**：

```java
package com.example.service;

import com.example.config.DataSourceContextHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class DynamicDataService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private OrderRepository orderRepository;

    public <T> T executeWithDataSource(String dataSourceType, DataSourceCallback<T> callback) {
        try {
            DataSourceContextHolder.setDataSourceType(dataSourceType);
            return callback.execute();
        } finally {
            DataSourceContextHolder.clearDataSourceType();
        }
    }

    public interface DataSourceCallback<T> {
        T execute();
    }

    // 使用範例
    public User getUserFromPrimary(Long id) {
        return executeWithDataSource("primary", () -> userRepository.findById(id).orElse(null));
    }

    public List<Order> getOrdersFromSecondary(Long userId) {
        return executeWithDataSource("secondary", () -> orderRepository.findByUserId(userId));
    }
}
```

---

## 練習 5：整合測試與最佳實踐 ⭐⭐⭐

### 5.1 Flyway 整合測試
**目標**：編寫 Flyway 整合測試

**實作**：

```java
package com.example.flyway;

import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import javax.sql.DataSource;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@TestPropertySource(locations = "classpath:application-test.properties")
public class FlywayIntegrationTest {

    @Autowired
    private DataSource dataSource;

    @Autowired
    private Flyway flyway;

    @Test
    public void testFlywayMigration() {
        // 執行遷移
        flyway.migrate();

        // 驗證遷移結果
        assertNotNull(dataSource);
        
        // 檢查 flyway_schema_history 表
        // 檢查目標表是否建立
    }

    @Test
    public void testMigrationVersion() {
        flyway.migrate();
        
        // 檢查當前版本
        String currentVersion = flyway.info().getCurrent().getVersion().getVersion();
        assertNotNull(currentVersion);
    }
}
```

**application-test.properties**：
```properties
# 測試環境配置
spring.datasource.url=jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1
spring.datasource.driver-class-name=org.h2.Driver
spring.datasource.username=sa
spring.datasource.password=

spring.flyway.enabled=true
spring.flyway.locations=classpath:db/migration
spring.jpa.hibernate.ddl-auto=validate
```

### 5.2 多資料源測試
**目標**：測試多資料源配置

**實作**：

```java
package com.example.config;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import javax.sql.DataSource;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@TestPropertySource(locations = "classpath:application-test.properties")
public class MultiDataSourceTest {

    @Autowired
    private DataSource primaryDataSource;

    @Autowired
    private DataSource secondaryDataSource;

    @Test
    public void testPrimaryDataSource() {
        assertNotNull(primaryDataSource);
        assertNotEquals(primaryDataSource, secondaryDataSource);
    }

    @Test
    public void testSecondaryDataSource() {
        assertNotNull(secondaryDataSource);
    }
}
```

### 5.3 遷移腳本最佳實踐
**目標**：建立符合最佳實踐的遷移腳本

**實作**：

```sql
-- V5__create_user_profiles_table.sql
-- 最佳實踐：使用事務、錯誤處理、日誌記錄

-- 開始事務
START TRANSACTION;

-- 主要變更
CREATE TABLE user_profiles (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    bio TEXT,
    avatar_url VARCHAR(255),
    location VARCHAR(100),
    website VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    UNIQUE KEY uk_user_id (user_id)
);

-- 建立索引
CREATE INDEX idx_user_profiles_location ON user_profiles(location);

-- 插入預設資料
INSERT INTO user_profiles (user_id, bio, location)
SELECT id, 'Hello, I am new here!', 'Unknown'
FROM users
WHERE id NOT IN (SELECT user_id FROM user_profiles);

-- 驗證變更
-- 檢查表格是否建立成功
SELECT COUNT(*) INTO @table_exists
FROM information_schema.tables
WHERE table_schema = DATABASE()
AND table_name = 'user_profiles';

-- 如果表格不存在則回滾
IF @table_exists = 0 THEN
    ROLLBACK SIGNAL SQLSTATE '45000'
    SET MESSAGE_TEXT = 'Failed to create user_profiles table';
END IF;

COMMIT;
```

### 5.4 監控與日誌
**目標**：實作遷移過程的監控

**實作**：

```java
package com.example.monitoring;

import org.flywaydb.core.api.callback.Callback;
import org.flywaydb.core.api.event.Event;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import java.time.LocalDateTime;
import java.util.logging.Logger;

@Component
public class MigrationMonitor implements Callback {

    private static final Logger logger = Logger.getLogger(MigrationMonitor.class.getName());

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Override
    public String getEvent() {
        return Event.AFTER_MIGRATE.name();
    }

    @Override
    public void handle(Event event, Context context) {
        logger.info("=== Flyway Migration Event: " + event.name() + " ===");
        
        // 記錄遷移資訊
        recordMigrationInfo(context, event);
        
        // 發送通知（可選）
        if (event == Event.AFTER_MIGRATE) {
            sendMigrationNotification(context);
        }
    }

    private void recordMigrationInfo(Context context, Event event) {
        String sql = "INSERT INTO migration_logs (event_type, database, executed_at) VALUES (?, ?, ?)";
        jdbcTemplate.update(sql, 
            event.name(), 
            context.getConnection().getCatalog(),
            LocalDateTime.now()
        );
    }

    private void sendMigrationNotification(Context context) {
        // 可以整合 Email、Slack 等通知
        logger.info("Migration completed successfully for database: " + 
            context.getConnection().getCatalog());
    }
}
```

**建立日誌表**：
```sql
-- V6__create_migration_logs_table.sql
CREATE TABLE migration_logs (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    event_type VARCHAR(50) NOT NULL,
    database_name VARCHAR(100) NOT NULL,
    executed_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_executed_at (executed_at)
);
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

### 1. Flyway 遷移失敗
**問題**：遷移腳本執行失敗
**解決方案**：
1. 檢查 SQL 語法是否正確
2. 確認資料庫連線設定
3. 檢查遷移腳本命名規則
4. 查看詳細錯誤日誌

### 2. 多資料源配置錯誤
**問題**：無法正確切換資料源
**解決方案**：
1. 確認每個資料源的 Bean 名稱
2. 檢查 @Primary 註解配置
3. 確認 TransactionManager 名稱
4. 測試資料源連線

### 3. 事務管理問題
**問題**：跨資料源的事務不一致
**解決方案**：
1. 確認每個資料源的事務管理器
2. 使用 @Transactional 指定 TransactionManager
3. 考慮使用分布式事務（如需要）
4. 測試事務的提交和回滾

### 4. 測試環境配置
**問題**：測試環境無法正常運作
**解決方案**：
1. 使用 H2 記憶體資料庫進行測試
2. 確認測試配置檔案正確
3. 檢查測試資料的初始化
4. 驗證測試後的清理工作

---

## 參考資源

### 官方文件
- [Flyway 官方文件](https://flywaydb.org/documentation/)
- [Spring Boot 多資料源配置](https://spring.io/guides/gs/multi-datasource/)
- [Spring Data JPA 官方文件](https://spring.io/projects/spring-data-jpa)

### 進階學習
- [Flyway 進階功能](https://flywaydb.org/documentation/concepts/callbacks)
- [動態資料源切換](https://www.baeldung.com/spring-dynamic-datasource)
- [資料庫版本管理最佳實踐](https://www.thoughtworks.com/insights/blog/database-version-control)

### 工具推薦
- [DBeaver](https://dbeaver.io/)：資料庫管理工具
- [Flyway Desktop](https://flywaydb.org/documentation/usage/flyway-desktop)：Flyway 桌面工具
- [IntelliJ IDEA](https://www.jetbrains.com/idea/)：Java IDE