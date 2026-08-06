# Spring Boot 專案結構指南

## 標準目錄結構

```
my-api/
├── src/
│   ├── main/
│   │   ├── java/com/example/demo/
│   │   │   ├── DemoApplication.java          ← 主程式入口
│   │   │   ├── controller/
│   │   │   │   └── EmployeeController.java
│   │   │   ├── service/
│   │   │   │   ├── EmployeeService.java      ← 介面
│   │   │   │   └── EmployeeServiceImpl.java  ← 實作
│   │   │   ├── repository/
│   │   │   │   └── EmployeeRepository.java
│   │   │   ├── model/                        ← JPA Entity
│   │   │   │   └── Employee.java
│   │   │   ├── dto/
│   │   │   │   ├── request/
│   │   │   │   │   └── EmployeeRequest.java
│   │   │   │   └── response/
│   │   │   │       └── EmployeeResponse.java
│   │   │   ├── exception/
│   │   │   │   ├── ResourceNotFoundException.java
│   │   │   │   ├── DuplicateException.java
│   │   │   │   └── GlobalExceptionHandler.java
│   │   │   └── config/
│   │   │       ├── SecurityConfig.java       ← Spring Security
│   │   │       └── CorsConfig.java
│   │   └── resources/
│   │       ├── application.properties        ← 主設定
│   │       ├── application-dev.properties    ← 開發環境
│   │       └── application-prod.properties   ← 生產環境
│   └── test/
│       └── java/com/example/demo/
│           ├── controller/
│           │   └── EmployeeControllerTest.java
│           └── service/
│               └── EmployeeServiceTest.java
├── pom.xml
└── README.md
```

## application.properties 範例設定

```properties
# 應用設定
spring.application.name=my-api
server.port=8080

# MySQL 資料庫
spring.datasource.url=jdbc:mysql://localhost:3306/mydb?useSSL=false&serverTimezone=UTC
spring.datasource.username=root
spring.datasource.password=your_password
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver

# JPA / Hibernate
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.format_sql=true
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.MySQL8Dialect

# 時區
spring.jackson.time-zone=Asia/Taipei
spring.jackson.date-format=yyyy-MM-dd HH:mm:ss

# 錯誤回應設定
server.error.include-message=always
server.error.include-binding-errors=always
```

## 多環境設定（Profile）

```properties
# application.properties（公用）
spring.profiles.active=dev

# application-dev.properties（開發用 H2）
spring.datasource.url=jdbc:h2:mem:testdb
spring.datasource.driver-class-name=org.h2.Driver
spring.jpa.hibernate.ddl-auto=create-drop
spring.h2.console.enabled=true

# application-prod.properties（生產用 MySQL）
spring.datasource.url=jdbc:mysql://prod-host:3306/proddb
spring.jpa.hibernate.ddl-auto=validate
```

## 套件命名建議

```
com.{公司}.{專案}.{層級}
com.example.hrSystem.controller
com.example.hrSystem.service
com.example.hrSystem.repository
com.example.hrSystem.model
com.example.hrSystem.dto
com.example.hrSystem.exception
com.example.hrSystem.config
```

## 依賴注入最佳實踐

```java
// 建議：建構子注入（Constructor Injection）— 方便測試、不可變
@Service
public class EmployeeServiceImpl implements EmployeeService {
    private final EmployeeRepository repository;

    public EmployeeServiceImpl(EmployeeRepository repository) {
        this.repository = repository;
    }
}

// 可接受：@Autowired（簡單情境）
@Service
public class EmployeeServiceImpl implements EmployeeService {
    @Autowired
    private EmployeeRepository repository;
}
```

## Spring Data JPA 查詢方式

```java
// 1. 命名方法派生（Spring 自動產生 SQL）
List<Employee> findByDepartmentAndSalaryGreaterThan(String dept, BigDecimal salary);
Optional<Employee> findByEmail(String email);

// 2. @Query（JPQL）
@Query("SELECT e FROM Employee e WHERE e.name LIKE %:name%")
List<Employee> searchByName(@Param("name") String name);

// 3. @Query（Native SQL）
@Query(value = "SELECT * FROM employees WHERE salary > :salary", nativeQuery = true)
List<Employee> findBySalaryNative(@Param("salary") BigDecimal salary);
```
