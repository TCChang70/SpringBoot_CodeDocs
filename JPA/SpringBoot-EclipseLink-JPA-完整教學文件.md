# Spring Boot × EclipseLink 3.2 JPA 完整教學文件

> **適用版本：** Spring Boot 3.4.x｜Jakarta Persistence (JPA) 3.1｜EclipseLink 3.2｜Java 17+
>
> **學習目標：** 從 Entity 對應到進階查詢，系統性掌握 JPA 在 Spring Boot 中的完整用法

---

## 目錄

1. [JPA 概念總覽](#1-jpa-概念總覽)
2. [Maven 依賴設定](#2-maven-依賴設定)
3. [application.properties 設定](#3-applicationproperties-設定)
4. [Entity 實體類對應](#4-entity-實體類對應)
5. [Repository 資料存取層](#5-repository-資料存取層)
6. [三種查詢方式比較](#6-三種查詢方式比較)
7. [關聯映射 (Relationship Mapping)](#7-關聯映射-relationship-mapping)
8. [EntityManager 生命週期](#8-entitymanager-生命週期)
9. [交易管理 (@Transactional)](#9-交易管理-transactional)
10. [DTO 與 Entity 轉換 (ModelMapper)](#10-dto-與-entity-轉換-modelmapper)
11. [EclipseLink 進階特性](#11-eclipselink-進階特性)
12. [常見錯誤與解法](#12-常見錯誤與解法)
13. [學習路線圖](#13-學習路線圖)

---

## 1. JPA 概念總覽

### 什麼是 JPA？

**JPA（Java Persistence API）** 是 Java 官方定義的 ORM（Object-Relational Mapping，物件關聯對映）**規範**，
讓你用 Java 物件操作資料庫，而不必直接寫 SQL。

```
Java 物件 (Entity)  ←→  JPA 規範  ←→  實作層 (EclipseLink / Hibernate)  ←→  資料庫
```

| 名詞 | 說明 |
|------|------|
| **JPA** | 規範介面（Jakarta Persistence API） |
| **EclipseLink** | JPA 的**參考實作**，由 Eclipse 基金會維護 |
| **Hibernate** | 另一個常見的 JPA 實作（Spring Boot 預設） |
| **Entity** | 對應資料表的 Java 類別 |
| **EntityManager** | 管理 Entity 生命週期的核心物件 |
| **JPQL** | JPA Query Language，類似 SQL 但操作物件 |
| **Repository** | Spring Data JPA 提供的資料存取介面 |

### JPA vs JDBC 比較

```
JDBC（低階）：
  String sql = "SELECT * FROM users WHERE username = ?";
  PreparedStatement ps = conn.prepareStatement(sql);
  ps.setString(1, username);
  ResultSet rs = ps.executeQuery();
  // 手動逐欄對應...

JPA（高階）：
  User user = userRepository.findByUsername(username); // 一行搞定！
```

---

## 2. Maven 依賴設定

### 2-1 使用預設 Hibernate（最常見）

```xml
<!-- pom.xml -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-jpa</artifactId>
</dependency>

<dependency>
    <groupId>com.mysql</groupId>
    <artifactId>mysql-connector-j</artifactId>
    <scope>runtime</scope>
</dependency>
```

### 2-2 切換為 EclipseLink 3.2（排除 Hibernate）

```xml
<!-- pom.xml -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-jpa</artifactId>
    <!-- 排除 Hibernate -->
    <exclusions>
        <exclusion>
            <groupId>org.hibernate.orm</groupId>
            <artifactId>hibernate-core</artifactId>
        </exclusion>
    </exclusions>
</dependency>

<!-- 引入 EclipseLink 3.2 -->
<dependency>
    <groupId>org.eclipse.persistence</groupId>
    <artifactId>eclipselink</artifactId>
    <version>3.2.0</version>
</dependency>

<!-- MySQL Driver -->
<dependency>
    <groupId>com.mysql</groupId>
    <artifactId>mysql-connector-j</artifactId>
    <scope>runtime</scope>
</dependency>

<!-- 其他常用 -->
<dependency>
    <groupId>org.projectlombok</groupId>
    <artifactId>lombok</artifactId>
    <optional>true</optional>
</dependency>

<dependency>
    <groupId>org.modelmapper</groupId>
    <artifactId>modelmapper</artifactId>
    <version>3.2.1</version>
</dependency>
```

### 2-3 EclipseLink JPA Provider 設定 Bean

切換 EclipseLink 後，需要在設定類中指定 Provider：

```java
// src/main/java/com/example/demo/config/JpaConfig.java
@Configuration
public class JpaConfig {

    @Bean
    public JpaVendorAdapter jpaVendorAdapter() {
        // 告訴 Spring Boot 使用 EclipseLink
        EclipseLinkJpaVendorAdapter adapter = new EclipseLinkJpaVendorAdapter();
        adapter.setShowSql(true);
        adapter.setGenerateDdl(true); // 對應 ddl-auto=update
        return adapter;
    }
}
```

---

## 3. application.properties 設定

```properties
# ==============================
# 資料庫連線
# ==============================
spring.datasource.url=jdbc:mysql://localhost:3306/web?useSSL=false&serverTimezone=Asia/Taipei&useLegacyDatetimeCode=false&allowPublicKeyRetrieval=true
spring.datasource.username=root
spring.datasource.password=abc123
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver

# ==============================
# JPA 共用設定
# ==============================
# ddl-auto 選項說明：
#   none     → 不做任何事（生產環境建議）
#   validate → 只驗證 Entity 與資料表是否相符，不修改
#   update   → 有新欄位/表時自動新增（常用於開發）
#   create   → 每次啟動重建資料表（會清除資料！）
#   create-drop → 啟動建表、關閉刪表（測試用）
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.format_sql=true
spring.jpa.open-in-view=false

# ==============================
# 使用 EclipseLink 時的額外設定
# ==============================
spring.jpa.properties.eclipselink.weaving=false
spring.jpa.properties.eclipselink.logging.level=FINE
```

> **重要提醒：** `spring.jpa.open-in-view=false` 可避免 N+1 問題與 Session 洩漏，
> 正式專案一律設為 `false`。

---

## 4. Entity 實體類對應

### 4-1 基礎 Entity 範例（users 資料表）

```java
// src/main/java/com/example/demo/model/entity/User.java
package com.example.demo.model.entity;

import jakarta.persistence.*;
import lombok.*;

/**
 * Entity 對應資料表 users
 *
 * 資料表結構：
 * +---------+-----------+---------------+------+-------+--------+------+
 * | user_id | username  | password_hash | salt | email | active | role |
 * +---------+-----------+---------------+------+-------+--------+------+
 */
@Data               // Lombok：自動生成 getter/setter/toString/equals
@AllArgsConstructor // Lombok：全參數建構子
@NoArgsConstructor  // Lombok：無參數建構子（JPA 必須有！）
@Entity             // 宣告此類別為 JPA Entity（對應一張資料表）
@Table(name = "users") // 指定資料表名稱（預設為類別名小寫）
public class User {

    @Id                                                    // 主鍵
    @GeneratedValue(strategy = GenerationType.IDENTITY)    // 自動遞增（MySQL AUTO_INCREMENT）
    @Column(name = "user_id")                              // 對應資料表欄位名
    private Integer userId;

    @Column(name = "username", unique = true, nullable = false, length = 50)
    private String username;

    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    @Column(name = "salt", nullable = false)
    private String salt;

    @Column(name = "email", nullable = false)
    private String email;

    @Column(name = "active")
    private Boolean active;

    @Column(name = "role")
    private String role;
}
```

### 4-2 不用自動遞增的主鍵（手動指定 ID）

```java
// src/main/java/com/example/demo/model/entity/Room.java
@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "room")
public class Room {

    @Id                         // 主鍵，但不自動生成 → 要手動給值
    @Column(name = "room_id")
    private Integer roomId;

    @Column(name = "room_name", nullable = false, unique = true)
    private String roomName;

    // columnDefinition：直接嵌入 DDL 定義（設定預設值）
    @Column(name = "room_size", columnDefinition = "integer default 0")
    private Integer roomSize;
}
```

### 4-3 常用 @Column 屬性一覽

| 屬性 | 說明 | 範例 |
|------|------|------|
| `name` | 欄位名稱 | `name = "user_id"` |
| `nullable` | 是否允許 NULL | `nullable = false` |
| `unique` | 是否唯一 | `unique = true` |
| `length` | 字串最大長度 | `length = 50` |
| `columnDefinition` | 自訂 DDL | `columnDefinition = "integer default 0"` |
| `insertable` | 是否參與 INSERT | `insertable = false` |
| `updatable` | 是否參與 UPDATE | `updatable = false` |

### 4-4 @GeneratedValue 策略比較

| 策略 | 說明 | 適用資料庫 |
|------|------|-----------|
| `IDENTITY` | 資料庫自動遞增（最常用） | MySQL、PostgreSQL |
| `SEQUENCE` | 使用資料庫序列 | Oracle、PostgreSQL |
| `TABLE` | 用輔助表模擬序列（跨資料庫） | 所有資料庫 |
| `AUTO` | JPA 自動選擇（預設） | 視資料庫而定 |
| `UUID` (JPA 3.1+) | 自動生成 UUID | 所有資料庫 |

```java
// JPA 3.1 新增 UUID 支援（EclipseLink 3.2 支援）
@Id
@GeneratedValue(strategy = GenerationType.UUID)
private String id; // 或 java.util.UUID
```

---

## 5. Repository 資料存取層

### 5-1 JpaRepository 繼承架構

```
JpaRepository<T, ID>          ← 你繼承這個
    └─ PagingAndSortingRepository  （分頁/排序）
        └─ CrudRepository          （基本 CRUD）
            └─ Repository          （頂層介面）
```

### 5-2 內建方法總覽

```java
@Repository
public interface RoomRepository extends JpaRepository<Room, Integer> {
    // 繼承的內建方法（不用自己寫）：

    // 新增 / 更新（有 ID 就更新，沒有就新增）
    Room save(Room room);
    List<Room> saveAll(List<Room> rooms);

    // 查詢
    Optional<Room> findById(Integer id);  // 找不到回傳 empty Optional
    List<Room> findAll();
    long count();
    boolean existsById(Integer id);

    // 刪除
    void deleteById(Integer id);
    void delete(Room room);
    void deleteAll();

    // 分頁 + 排序
    Page<Room> findAll(Pageable pageable);
    List<Room> findAll(Sort sort);
}
```

### 5-3 save() 的新增 vs 更新邏輯

```java
Room newRoom = new Room(null, "101(L)", 100);  // userId = null → 執行 INSERT
Room saved = roomRepository.save(newRoom);
System.out.println(saved.getRoomId()); // 資料庫自動產生的 ID

Room existing = new Room(101, "101(L)", 150);  // userId = 101 → 執行 UPDATE
roomRepository.save(existing);
```

---

## 6. 三種查詢方式比較

Spring Data JPA 提供三種查詢方式，難度遞增：

### 方式 1：方法名稱自動生成 SQL（最簡單）

Spring 依照方法名稱**自動產生** SQL，無需寫任何查詢。

```java
@Repository
public interface RoomRepository extends JpaRepository<Room, Integer> {

    // findBy + 欄位名（大駝峰）+ 條件關鍵字
    List<Room> findByRoomSizeGreaterThan(Integer size);
    // 自動生成：SELECT * FROM room WHERE room_size > ?

    List<Room> findByRoomNameContaining(String keyword);
    // 自動生成：SELECT * FROM room WHERE room_name LIKE '%keyword%'

    Optional<Room> findByRoomName(String roomName);
    // 自動生成：SELECT * FROM room WHERE room_name = ?

    List<Room> findByRoomSizeBetween(Integer min, Integer max);
    // 自動生成：SELECT * FROM room WHERE room_size BETWEEN ? AND ?
}
```

**常用關鍵字對照表：**

| 方法名稱關鍵字 | 對應 SQL | 範例 |
|---------------|---------|------|
| `findBy` | `SELECT ... WHERE` | `findByUsername` |
| `GreaterThan` | `>` | `findByAgeGreaterThan` |
| `LessThan` | `<` | `findByAgeLessThan` |
| `Between` | `BETWEEN` | `findByAgeBetween` |
| `Like` | `LIKE` | `findByNameLike` |
| `Containing` | `LIKE '%...%'` | `findByNameContaining` |
| `StartingWith` | `LIKE '...%'` | `findByNameStartingWith` |
| `In` | `IN (...)` | `findByStatusIn` |
| `IsNull` | `IS NULL` | `findByDeletedAtIsNull` |
| `OrderBy` | `ORDER BY` | `findAllOrderByRoomIdAsc` |

### 方式 2：@Query + Native SQL（nativeQuery=true）

直接寫原生 SQL，欄位名要符合**資料表**中的設定。

```java
@Query(value = "SELECT room_id, room_name, room_size " +
               "FROM room WHERE room_size > :roomSize",
       nativeQuery = true)
List<Room> findRooms(Integer roomSize);
// :roomSize 對應方法參數名稱（也可用 ?1 代表第一個參數）
```

```java
// 也可用 @Query 做更新/刪除（必須搭配 @Modifying 和 @Transactional）
@Modifying
@Transactional
@Query(value = "UPDATE room SET room_size = :size WHERE room_id = :id",
       nativeQuery = true)
int updateRoomSize(Integer id, Integer size);
```

### 方式 3：@Query + JPQL（操作 Entity，推薦）

JPQL（Jakarta Persistence Query Language）操作 **Entity 物件**，而非資料表，
由 JPA 實作層（EclipseLink / Hibernate）翻譯成對應的 SQL。

```java
// 注意：FROM Room 是 Entity 類別名，r.roomSize 是 Entity 的欄位名
@Query(value = "SELECT r FROM Room r WHERE r.roomSize > :roomSize")
List<Room> readRooms(Integer roomSize);

// 帶排序
@Query("SELECT r FROM Room r WHERE r.roomSize > :size ORDER BY r.roomSize DESC")
List<Room> findLargeRooms(Integer size);

// 查詢部分欄位 → 回傳投影（Projection）
@Query("SELECT r.roomName, r.roomSize FROM Room r WHERE r.roomSize > :size")
List<Object[]> findRoomSummary(Integer size);
```

### 三種方式優缺點比較

| | 方式 1 方法名稱 | 方式 2 Native SQL | 方式 3 JPQL |
|--|---|---|---|
| **優點** | 最簡單、不用寫 SQL | 最靈活、可用 DB 函數 | 跨資料庫、語意清晰 |
| **缺點** | 複雜查詢命名過長 | 綁定特定資料庫 | 不能用 DB 特有函數 |
| **適合場景** | 簡單查詢 | 複雜原生 SQL | 一般業務查詢 |

---

## 7. 關聯映射 (Relationship Mapping)

> 關聯映射是 JPA 最重要也最容易混淆的概念！

### 7-1 @ManyToOne / @OneToMany（一對多）

情境：一個 `User` 可以有多個 `Order`（訂單）

```java
// Order.java — 多的一方（擁有外鍵）
@Entity
@Table(name = "orders")
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer orderId;

    private String product;

    // 多個 Order 對應一個 User（Order 表持有 user_id 外鍵）
    @ManyToOne(fetch = FetchType.LAZY)    // 懶加載（延遲載入）
    @JoinColumn(name = "user_id")         // 外鍵欄位名稱
    private User user;
}
```

```java
// User.java — 一的一方
@Entity
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer userId;

    private String username;

    // mappedBy 指向 Order 類別中的 user 欄位（非資料表欄位名）
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Order> orders = new ArrayList<>();
}
```

### 7-2 @ManyToMany（多對多）

情境：一個 `Student` 可以選多門 `Course`，一門 `Course` 有多個 `Student`

```java
// Student.java
@Entity
public class Student {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer studentId;

    private String name;

    @ManyToMany
    @JoinTable(
        name = "student_course",           // 中間表名稱
        joinColumns = @JoinColumn(name = "student_id"),
        inverseJoinColumns = @JoinColumn(name = "course_id")
    )
    private List<Course> courses = new ArrayList<>();
}

// Course.java
@Entity
public class Course {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer courseId;

    private String title;

    @ManyToMany(mappedBy = "courses") // 由 Student 端維護
    private List<Student> students = new ArrayList<>();
}
```

### 7-3 @OneToOne（一對一）

情境：一個 `User` 對應一個 `UserProfile`

```java
// UserProfile.java
@Entity
public class UserProfile {
    @Id
    private Integer userId;  // 主鍵同時也是外鍵

    private String bio;
    private String avatarUrl;

    @OneToOne
    @MapsId                         // 用 User 的主鍵作為自己的主鍵
    @JoinColumn(name = "user_id")
    private User user;
}
```

### 7-4 FetchType — 懶加載 vs 急加載

```
FetchType.LAZY  （懶加載）：
  → 查詢 User 時，不立即查詢 orders
  → 當你第一次存取 user.getOrders() 時，才執行 SQL
  → 效能較好，但要注意 Session 生命週期

FetchType.EAGER （急加載）：
  → 查詢 User 時，立即同時查詢 orders（JOIN 或額外 SELECT）
  → 資料量大時效能差，通常不建議用於 @OneToMany
```

**預設值：**
- `@OneToMany`、`@ManyToMany` → 預設 `LAZY`（較好）
- `@ManyToOne`、`@OneToOne` → 預設 `EAGER`（建議改為 `LAZY`）

### 7-5 CascadeType — 級聯操作

```java
@OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
private List<Order> orders;
```

| 選項 | 說明 |
|------|------|
| `PERSIST` | 儲存主實體時，一併儲存關聯實體 |
| `MERGE` | 更新主實體時，一併更新關聯實體 |
| `REMOVE` | 刪除主實體時，一併刪除關聯實體 |
| `REFRESH` | 刷新主實體時，一併刷新關聯實體 |
| `DETACH` | 脫離持久化 Context 時，關聯實體也脫離 |
| `ALL` | 以上全部（最常用於父子關係） |

---

## 8. EntityManager 生命週期

### 8-1 Entity 的四種狀態

```
New（瞬態）
  ↓ entityManager.persist()
Managed（持久態）  ← 由 EntityManager 管理，有追蹤
  ↓ entityManager.detach() 或 context 結束
Detached（分離態） ← 脫離管理，修改不會自動同步到 DB
  ↓ entityManager.merge()
Managed（重新持久化）
  ↓ entityManager.remove()
Removed（刪除態） ← commit 後從 DB 刪除
```

### 8-2 使用 EntityManager（低階，較少用）

```java
@Service
@Transactional
public class RoomServiceImpl {

    @PersistenceContext
    private EntityManager em;

    public Room saveRoom(Room room) {
        em.persist(room);    // INSERT
        return room;
    }

    public Room findRoom(Integer id) {
        return em.find(Room.class, id);  // SELECT by primary key
    }

    public Room updateRoom(Room room) {
        return em.merge(room);  // UPDATE（room 可以是 detached 狀態）
    }

    public void removeRoom(Integer id) {
        Room room = em.find(Room.class, id);
        if (room != null) {
            em.remove(room);  // DELETE
        }
    }
}
```

> **提醒：** 在 Spring Data JPA 中，`JpaRepository.save()` 底層就是呼叫
> `em.persist()` 或 `em.merge()`，大多數情況直接用 Repository 即可。

---

## 9. 交易管理 (@Transactional)

### 9-1 基本用法

```java
@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserRepository userRepository;

    // 讀取操作：加上 readOnly = true 提升效能
    @Transactional(readOnly = true)
    public UserDto getUser(String username) {
        User user = userRepository.getUser(username);
        return userMapper.toDto(user);
    }

    // 寫入操作：不加 readOnly（預設 false）
    @Transactional
    public void addUser(String username, String password, String email,
                        Boolean active, String role) {
        String salt = Hash.getSalt();
        String passwordHash = Hash.getHash(password, salt);
        User user = new User(null, username, passwordHash, salt, email, active, role);
        userRepository.save(user);
    }
}
```

### 9-2 交易傳播行為 (Propagation)

```java
// 最常用的兩種
@Transactional(propagation = Propagation.REQUIRED)
// → 有現有交易就加入，沒有就新建（預設）

@Transactional(propagation = Propagation.REQUIRES_NEW)
// → 無論如何都新建一個交易（舊交易暫停）
```

### 9-3 回滾條件

```java
// 預設只對 RuntimeException 和 Error 回滾
// 若要對 checked exception 也回滾：
@Transactional(rollbackFor = Exception.class)
public void someOperation() throws IOException {
    // ...
}
```

---

## 10. DTO 與 Entity 轉換 (ModelMapper)

### 10-1 為什麼需要 DTO？

```
Entity（資料層）         DTO（傳輸層）
  User                     UserDto
  - userId         →       - userId
  - username       →       - username
  - passwordHash   ✗ 不傳  （密碼不對外暴露！）
  - salt           ✗ 不傳
  - email          →       - email
  - active         →       - active
  - role           →       - role
```

### 10-2 定義 DTO

```java
// src/main/java/com/example/demo/model/dto/UserDto.java
@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserDto {
    private Integer userId;
    private String username;
    private String email;
    private Boolean active;
    private String role;
    // 注意：沒有 passwordHash 和 salt！
}
```

### 10-3 ModelMapper Bean 設定

```java
// src/main/java/com/example/demo/config/AppConfig.java
@Configuration
public class AppConfig {

    @Bean
    public ModelMapper modelMapper() {
        ModelMapper mapper = new ModelMapper();
        // 設定為嚴格匹配（推薦）
        mapper.getConfiguration()
              .setMatchingStrategy(MatchingStrategies.STRICT);
        return mapper;
    }
}
```

### 10-4 Mapper 介面

```java
// src/main/java/com/example/demo/mapper/UserMapper.java
@Component
public class UserMapper {

    @Autowired
    private ModelMapper modelMapper;

    // Entity → DTO
    public UserDto toDto(User user) {
        return modelMapper.map(user, UserDto.class);
    }

    // DTO → Entity
    public User toEntity(UserDto dto) {
        return modelMapper.map(dto, User.class);
    }
}
```

---

## 11. EclipseLink 進階特性

### 11-1 EclipseLink vs Hibernate 特性比較

| 功能 | EclipseLink 3.2 | Hibernate 6.x |
|------|----------------|---------------|
| JPA 規範版本 | JPA 3.1（完整） | JPA 3.1（完整） |
| 二級快取 (L2) | 內建 Cache（`@Cache`） | 需整合 Ehcache/Redis |
| 物件圖 (Object Graph) | 優秀 | 良好 |
| 靜態編織 (Weaving) | 支援靜態/動態 | 位元組碼增強 |
| NoSQL 支援 | 支援（擴充套件） | 不原生支援 |
| Spring Boot 整合 | 需額外設定 | 開箱即用（預設） |

### 11-2 EclipseLink 二級快取（L2 Cache）

**第一級快取（L1）**：EntityManager 範圍，自動管理。
**第二級快取（L2）**：跨 EntityManager，需要手動設定。

```java
// Entity 加上 @Cache 啟用 EclipseLink L2 快取
import org.eclipse.persistence.annotations.Cache;
import org.eclipse.persistence.annotations.CacheType;

@Entity
@Table(name = "room")
@Cache(
    type = CacheType.SOFT_WEAK,   // 快取類型
    expiry = 300000,              // 過期時間（毫秒），300 秒
    size = 1000                   // 快取最大數量
)
public class Room {
    // ...
}
```

**application.properties 設定 EclipseLink 共用快取：**

```properties
spring.jpa.properties.eclipselink.cache.shared.default=true
spring.jpa.properties.eclipselink.cache.size.default=1000
spring.jpa.properties.eclipselink.cache.type.default=SoftWeak
```

### 11-3 EclipseLink @ReadOnly（唯讀 Entity）

```java
import org.eclipse.persistence.annotations.ReadOnly;

@Entity
@Table(name = "product_view")  // 資料庫 VIEW
@ReadOnly  // EclipseLink 獨有：此 Entity 永遠不會發出 INSERT/UPDATE/DELETE
public class ProductView {
    @Id
    private Integer productId;
    private String productName;
    private BigDecimal price;
}
```

### 11-4 EclipseLink @Customizer（自訂描述子）

```java
import org.eclipse.persistence.annotations.Customizer;
import org.eclipse.persistence.config.DescriptorCustomizer;
import org.eclipse.persistence.descriptors.ClassDescriptor;

@Entity
@Customizer(UserDescriptorCustomizer.class)
public class User { /* ... */ }

public class UserDescriptorCustomizer implements DescriptorCustomizer {
    @Override
    public void customize(ClassDescriptor descriptor) {
        // 設定 EclipseLink 特有的描述子屬性
        descriptor.setAlias("UserAlias");
    }
}
```

### 11-5 EclipseLink Batch Writing（批量寫入優化）

```properties
# 開啟批次寫入，大幅提升批量 INSERT/UPDATE 效能
spring.jpa.properties.eclipselink.jdbc.batch-writing=JDBC
spring.jpa.properties.eclipselink.jdbc.batch-writing.size=100
```

---

## 12. 常見錯誤與解法

### ❌ 錯誤 1：LazyInitializationException

```
org.hibernate.LazyInitializationException:
  failed to lazily initialize a collection of role: ...
  could not initialize proxy - no Session
```

**原因：** 在交易結束後（Session 關閉），才存取懶加載的關聯。

**解法 A（推薦）：在 Service 層預先取用資料**

```java
@Transactional(readOnly = true)
public UserDto getUserWithOrders(Integer userId) {
    User user = userRepository.findById(userId).orElseThrow();
    user.getOrders().size(); // 在交易內先觸發懶加載
    return userMapper.toDto(user);
}
```

**解法 B：使用 JOIN FETCH**

```java
@Query("SELECT u FROM User u JOIN FETCH u.orders WHERE u.userId = :id")
Optional<User> findByIdWithOrders(Integer id);
```

**解法 C：設定 application.properties（不推薦）**

```properties
spring.jpa.open-in-view=true  # 不建議！會造成效能問題
```

---

### ❌ 錯誤 2：detached entity passed to persist

```
org.hibernate.PersistentObjectException:
  detached entity passed to persist: com.example.demo.model.entity.Room
```

**原因：** Entity 有設定 ID，JPA 認為是 detached 狀態，但用 `persist()` 只能接受 new 狀態。

**解法：** 使用 `merge()` 或直接用 `save()`（Spring Data JPA 自動判斷）

```java
// 有 ID → 用 save() 就會自動呼叫 merge()
roomRepository.save(existingRoom);
```

---

### ❌ 錯誤 3：No identifier specified for entity

```
org.hibernate.AnnotationException:
  No identifier specified for entity: com.example.demo.model.entity.Room
```

**原因：** Entity 沒有標記 `@Id`。

**解法：** 確保每個 Entity 都有一個欄位標記 `@Id`。

---

### ❌ 錯誤 4：Column 'xxx' cannot be null

```
java.sql.SQLIntegrityConstraintViolationException:
  Column 'username' cannot be null
```

**原因：** 欄位設定 `nullable = false`，但傳入了 `null` 值。

**解法：** 加入 Bean Validation，在 Controller 層提前驗證

```java
@Column(name = "username", nullable = false, length = 50)
@NotBlank(message = "使用者名稱不能為空")  // Jakarta Validation
private String username;
```

---

### ❌ 錯誤 5：N+1 問題

**症狀：** 查詢 100 筆 User，每筆都多發一次 SQL 查詢 orders → 共 101 次 SQL

**解法：使用 JOIN FETCH 一次抓取**

```java
@Query("SELECT DISTINCT u FROM User u LEFT JOIN FETCH u.orders")
List<User> findAllWithOrders();
```

或使用 `@EntityGraph`：

```java
@EntityGraph(attributePaths = {"orders"})
List<User> findAll();
```

---

## 13. 學習路線圖

```
🌱 基礎階段（建議 1-2 週）
  ├─ JPA 是什麼、Entity 概念
  ├─ @Entity、@Table、@Id、@Column 基本標注
  ├─ JpaRepository 內建 CRUD 方法
  └─ application.properties JPA 設定

🌿 核心階段（建議 2-3 週）
  ├─ @Query 寫 JPQL 和 Native SQL
  ├─ 方法名稱自動生成查詢
  ├─ @Transactional 交易管理
  ├─ DTO 設計與 ModelMapper 轉換
  └─ Optional 處理 null

🌳 進階階段（建議 3-4 週）
  ├─ 關聯映射：@OneToMany、@ManyToOne、@ManyToMany
  ├─ FetchType（LAZY vs EAGER）與 N+1 問題
  ├─ CascadeType 級聯操作
  ├─ 分頁查詢（Pageable）
  └─ 動態查詢（Specification / Criteria API）

🚀 實戰階段（建議 2 週）
  ├─ EclipseLink 特性：L2 Cache、@ReadOnly
  ├─ 效能優化：batch-writing、JOIN FETCH
  ├─ 與 Spring Boot Security 整合
  ├─ 撰寫 @SpringBootTest 測試
  └─ 生產環境配置（ddl-auto=validate）
```

---

## 附錄：完整範例 — Room CRUD

以下是根據本教學整合的完整範例，可直接在專案中執行：

```java
// RoomServiceImpl.java
@Service
public class RoomServiceImpl implements RoomService {

    @Autowired
    private RoomRepository roomRepository;

    @Transactional(readOnly = true)
    public List<Room> getAllRooms() {
        return roomRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Room getRoom(Integer id) {
        return roomRepository.findById(id)
            .orElseThrow(() -> new RoomNotFoundException("房間 " + id + " 不存在"));
    }

    @Transactional
    public Room addRoom(Integer roomId, String roomName, Integer roomSize) {
        if (roomRepository.existsById(roomId)) {
            throw new RoomAlreadyExistsException("房間 " + roomId + " 已存在");
        }
        Room room = new Room(roomId, roomName, roomSize);
        return roomRepository.save(room);
    }

    @Transactional
    public Room updateRoom(Integer id, String roomName, Integer roomSize) {
        Room room = getRoom(id); // 會拋出 exception 若不存在
        room.setRoomName(roomName);
        room.setRoomSize(roomSize);
        return roomRepository.save(room);
    }

    @Transactional
    public void deleteRoom(Integer id) {
        if (!roomRepository.existsById(id)) {
            throw new RoomNotFoundException("房間 " + id + " 不存在");
        }
        roomRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public List<Room> getLargeRooms(Integer minSize) {
        // 三種寫法皆等效：
        // return roomRepository.findByRoomSizeGreaterThan(minSize);
        // return roomRepository.findRooms(minSize);
        return roomRepository.readRooms(minSize);
    }
}
```

---

> **現在試試看：** 依照本文件在本機啟動 `springboot-jpa` 專案，
> 並執行 `RoomJPATest` 和 `RoomJPARead` 測試類，觀察 console 輸出的 SQL 語句，
> 驗證三種查詢方式產生的 SQL 有何不同。
