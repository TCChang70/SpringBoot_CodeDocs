# Spring Boot JPA 初學到進階學習文件

> **適用版本：** Spring Boot 3.4.x｜Jakarta Persistence (JPA) 3.1｜Java 17+
>
> **適合對象：** 初學者（看得懂 Java 基礎語法即可）→ 有基礎者 → 進階工程師
>
> **學習方式：** 從第一章依序讀下去，每章末有「現在試試看」任務

---

## 目錄

### 🌱 初學階段
- [Unit 1 — 為什麼需要 JPA？](#unit-1--為什麼需要-jpa)
- [Unit 2 — 環境建置與第一個 Entity](#unit-2--環境建置與第一個-entity)
- [Unit 3 — 用 Repository 做 CRUD](#unit-3--用-repository-做-crud)

### 🌿 核心階段
- [Unit 4 — 查詢的三種方式](#unit-4--查詢的三種方式)
- [Unit 5 — 交易管理與 @Transactional](#unit-5--交易管理與-transactional)
- [Unit 6 — DTO 設計與資料保護](#unit-6--dto-設計與資料保護)

### 🌳 進階階段
- [Unit 7 — 關聯映射（一對多、多對多）](#unit-7--關聯映射)
- [Unit 8 — 分頁與排序查詢](#unit-8--分頁與排序查詢)
- [Unit 9 — 動態查詢（Specification）](#unit-9--動態查詢specification)

### 🚀 實戰階段
- [Unit 10 — EntityManager 與生命週期](#unit-10--entitymanager-與生命週期)
- [Unit 11 — 效能優化與常見陷阱](#unit-11--效能優化與常見陷阱)
- [Unit 12 — 整合測試寫法](#unit-12--整合測試寫法)
- [Unit 13 — 練習題庫（含解答）](#unit-13--練習題庫)

---

---

# 🌱 初學階段

---

## Unit 1 — 為什麼需要 JPA？

### 1-1 從 JDBC 的痛點說起

假設你有一張 `room` 資料表，想查出所有房間，用傳統 JDBC 要這樣寫：

```java
// ❌ JDBC 傳統寫法（又長又容易出錯）
String sql = "SELECT room_id, room_name, room_size FROM room";
Connection conn = DriverManager.getConnection(url, user, pass);
PreparedStatement ps = conn.prepareStatement(sql);
ResultSet rs = ps.executeQuery();
List<Room> rooms = new ArrayList<>();
while (rs.next()) {
    Room room = new Room();
    room.setRoomId(rs.getInt("room_id"));       // 手動一欄一欄對應
    room.setRoomName(rs.getString("room_name"));
    room.setRoomSize(rs.getInt("room_size"));
    rooms.add(room);
}
rs.close(); ps.close(); conn.close();           // 還要手動關連線！
```

用 JPA 只需要：

```java
// ✅ JPA 寫法（一行搞定）
List<Room> rooms = roomRepository.findAll();
```

### 1-2 JPA 是什麼？

**JPA（Jakarta Persistence API）** 是 Java 官方定義的 **ORM（Object-Relational Mapping，物件關聯對映）規範**。
它讓你用「Java 物件」操作資料庫，而不必親自撰寫 SQL。

```
你的 Java 程式
    ↓
[JPA 規範] → 你只需學習這層的 API
    ↓
[JPA 實作] → Hibernate（Spring Boot 預設） 或 EclipseLink
    ↓
[JDBC Driver]
    ↓
MySQL / PostgreSQL / Oracle...
```

> **重要觀念：** JPA 只是「規範」（介面），真正執行的是底層的 **Hibernate** 或 **EclipseLink**。
> 就像「USB 規格」只是規範，真正插上去的是 USB 隨身碟的實體。

### 1-3 關鍵名詞速查

| 名詞 | 英文 | 白話說明 |
|------|------|---------|
| **實體類** | Entity | 對應一張資料表的 Java 類別 |
| **持久化情境** | Persistence Context | JPA 管理 Entity 的工作區間 |
| **實體管理器** | EntityManager | 負責 CRUD 的核心物件 |
| **Repository** | — | Spring 提供的資料存取介面，封裝 EntityManager |
| **JPQL** | Jakarta Persistence Query Language | 操作 Entity 的查詢語言（像 SQL 但用物件名） |
| **DDL** | Data Definition Language | 建立/修改資料表的 SQL 語句 |

---

> **🔰 現在試試看：**
> 在紙上畫出「你的 Java 程式 → JPA → 資料庫」的三層圖，並標注每層的作用。

---

## Unit 2 — 環境建置與第一個 Entity

### 2-1 Maven 依賴設定

在 `pom.xml` 中加入以下依賴：

```xml
<!-- Spring Data JPA（包含 Hibernate） -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-jpa</artifactId>
</dependency>

<!-- MySQL 驅動程式 -->
<dependency>
    <groupId>com.mysql</groupId>
    <artifactId>mysql-connector-j</artifactId>
    <scope>runtime</scope>
</dependency>

<!-- Lombok（省略 getter/setter 樣板程式碼） -->
<dependency>
    <groupId>org.projectlombok</groupId>
    <artifactId>lombok</artifactId>
    <optional>true</optional>
</dependency>
```

### 2-2 application.properties 基本設定

```properties
# ── 資料庫連線 ─────────────────────────────────────────────
spring.datasource.url=jdbc:mysql://localhost:3306/web?useSSL=false&serverTimezone=Asia/Taipei&allowPublicKeyRetrieval=true
spring.datasource.username=root
spring.datasource.password=abc123
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver

# ── JPA 設定 ───────────────────────────────────────────────
# ddl-auto 說明（初學先用 update，正式環境用 validate 或 none）：
#   update      → 比對 Entity 與資料表，有新欄位就補上（開發常用）
#   create-drop → 每次啟動重建資料表（⚠️ 資料會清空！）
#   validate    → 只驗證，不修改（生產環境）
#   none        → 完全不動資料表
spring.jpa.hibernate.ddl-auto=update

# 在 console 顯示 JPA 產生的 SQL（學習時非常有幫助！）
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.format_sql=true

# 關閉 open-in-view（避免連線洩漏，正式專案必設）
spring.jpa.open-in-view=false
```

> **陷阱警告：** 第一次用 `create` 模式很方便，但每次重啟都會**清空所有資料**！
> 請確認改為 `update` 或 `validate` 再部署。

### 2-3 建立第一個 Entity

目標：對應資料庫中的 `room` 資料表。

**資料表結構：**
```sql
CREATE TABLE room (
    room_id   INTEGER      PRIMARY KEY,
    room_name VARCHAR(50)  NOT NULL UNIQUE,
    room_size INTEGER      DEFAULT 0
);
```

**對應的 Entity 類別：**

```java
// src/main/java/com/example/demo/model/entity/Room.java
package com.example.demo.model.entity;

import jakarta.persistence.*;   // JPA 標注都在這個套件
import lombok.*;

@Data               // ① Lombok：自動產生 getter / setter / toString / equals
@AllArgsConstructor // ② Lombok：產生全參數建構子
@NoArgsConstructor  // ③ Lombok：產生無參數建構子（JPA 強制要求！）
@Entity             // ④ 宣告這是一個 JPA 實體類（對應一張資料表）
@Table(name = "room") // ⑤ 指定資料表名稱（若類別名與表名相同可省略）
public class Room {

    @Id                       // ⑥ 宣告主鍵
    @Column(name = "room_id") // ⑦ 對應欄位名稱
    private Integer roomId;

    @Column(name = "room_name", nullable = false, unique = true)
    private String roomName;

    @Column(name = "room_size", columnDefinition = "integer default 0")
    private Integer roomSize;
}
```

**逐行解析：**

| 標注 | 必要性 | 說明 |
|------|--------|------|
| `@Entity` | **必須** | 沒有這個 JPA 不認識此類別 |
| `@Table` | 選用 | 省略時，JPA 用類別名（小寫）當表名 |
| `@Id` | **必須** | 每個 Entity 都必須有一個主鍵 |
| `@GeneratedValue` | 選用 | 讓 DB 自動產生 ID 值 |
| `@Column` | 選用 | 省略時，JPA 用欄位名（駝峰轉底線）對應 |
| `@NoArgsConstructor` | **必須** | JPA 用反射建立物件，需要無參數建構子 |

### 2-4 自動遞增主鍵

大多數情況下，你會想讓資料庫自動產生 ID：

```java
// 對應 MySQL 的 AUTO_INCREMENT
@Id
@GeneratedValue(strategy = GenerationType.IDENTITY)
@Column(name = "user_id")
private Integer userId;
```

**GenerationType 選項說明：**

| 策略 | 說明 | 建議使用情境 |
|------|------|------------|
| `IDENTITY` | 資料庫自動遞增 | MySQL（最常用） |
| `SEQUENCE` | 使用 DB 序列物件 | Oracle、PostgreSQL |
| `AUTO` | JPA 自動選擇 | 跨資料庫（不確定時） |
| `UUID`（JPA 3.1 新增） | 自動產生 UUID | 分散式系統 |

```java
// JPA 3.1 UUID 主鍵範例
@Id
@GeneratedValue(strategy = GenerationType.UUID)
private String id; // 自動填入 "a1b2c3d4-..."
```

---

> **🔰 現在試試看：**
> 建立 `User` Entity 對應以下資料表，並確認啟動後 console 有出現 `CREATE TABLE` 的 SQL：
> ```sql
> CREATE TABLE users (
>     user_id  INTEGER      AUTO_INCREMENT PRIMARY KEY,
>     username VARCHAR(50)  NOT NULL UNIQUE,
>     email    VARCHAR(100) NOT NULL,
>     active   BOOLEAN      DEFAULT TRUE
> );
> ```

---

## Unit 3 — 用 Repository 做 CRUD

### 3-1 什麼是 Repository？

**Repository（資料存取層）** 是 Spring Data JPA 提供的神奇介面：
只要繼承 `JpaRepository`，你**不需要寫任何實作程式碼**，Spring 會自動幫你產生所有 CRUD 方法！

```java
// src/main/java/com/example/demo/repository/RoomRepository.java
package com.example.demo.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.example.demo.model.entity.Room;

@Repository
public interface RoomRepository extends JpaRepository<Room, Integer> {
    //                                               ^^^   ^^^^^^^
    //                                            Entity   主鍵型別
    // 繼承後就自動擁有所有 CRUD 方法，不需要寫任何程式碼！
}
```

### 3-2 JpaRepository 內建方法一覽

```java
// 以 RoomRepository 為例，這些方法都不需要自己實作：

// ── 新增 / 更新 ─────────────────────────────────────────
Room saved = roomRepository.save(room);
// 規則：room.roomId == null → INSERT；有值 → UPDATE

List<Room> savedAll = roomRepository.saveAll(List.of(r1, r2, r3));

// ── 查詢 ────────────────────────────────────────────────
Optional<Room> opt  = roomRepository.findById(101);      // 找不到 → empty
List<Room>     all  = roomRepository.findAll();
long           cnt  = roomRepository.count();
boolean        exist = roomRepository.existsById(101);

// ── 刪除 ─────────────────────────────────────────────────
roomRepository.deleteById(101);
roomRepository.delete(room);
roomRepository.deleteAll();
```

### 3-3 使用 Optional 安全取值

`findById()` 回傳 `Optional<Room>`，而非直接回傳 `Room`（避免 NullPointerException）：

```java
// ❌ 危險寫法：找不到會拋 NullPointerException
Room room = roomRepository.findById(id).get();

// ✅ 安全寫法 1：找不到拋自訂例外
Room room = roomRepository.findById(id)
    .orElseThrow(() -> new RuntimeException("房間 " + id + " 不存在"));

// ✅ 安全寫法 2：找不到給預設值
Room room = roomRepository.findById(id)
    .orElse(new Room(0, "未知", 0));

// ✅ 安全寫法 3：用 isPresent() 判斷
Optional<Room> opt = roomRepository.findById(id);
if (opt.isPresent()) {
    Room room = opt.get();
    // 使用 room...
}
```

### 3-4 完整的 Service 層範例

```java
// src/main/java/com/example/demo/service/impl/RoomServiceImpl.java
@Service
public class RoomServiceImpl {

    @Autowired
    private RoomRepository roomRepository;

    // ── 新增 ────────────────────────────────────────────
    public Room addRoom(Integer roomId, String roomName, Integer roomSize) {
        Room room = new Room(roomId, roomName, roomSize);
        return roomRepository.save(room);  // 回傳儲存後的物件（含 DB 產生的值）
    }

    // ── 查詢全部 ─────────────────────────────────────────
    public List<Room> getAllRooms() {
        return roomRepository.findAll();
    }

    // ── 查詢單筆 ─────────────────────────────────────────
    public Room getRoom(Integer id) {
        return roomRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("找不到房間 " + id));
    }

    // ── 更新 ────────────────────────────────────────────
    public Room updateRoom(Integer id, String newName, Integer newSize) {
        Room room = getRoom(id);   // 先確認存在
        room.setRoomName(newName);
        room.setRoomSize(newSize);
        return roomRepository.save(room);  // 有 ID → 執行 UPDATE
    }

    // ── 刪除 ────────────────────────────────────────────
    public void deleteRoom(Integer id) {
        if (!roomRepository.existsById(id)) {
            throw new RuntimeException("找不到房間 " + id);
        }
        roomRepository.deleteById(id);
    }
}
```

### 3-5 在測試類中驗證 CRUD

```java
// src/test/java/com/example/demo/RoomCrudTest.java
@SpringBootTest
class RoomCrudTest {

    @Autowired
    private RoomRepository roomRepository;

    @Test
    void testSave() {
        Room room = new Room(101, "101(L)", 100);
        Room saved = roomRepository.save(room);
        System.out.println("已儲存：" + saved);

        // 驗證
        Optional<Room> found = roomRepository.findById(101);
        assertTrue(found.isPresent());
        assertEquals("101(L)", found.get().getRoomName());
    }

    @Test
    void testFindAll() {
        List<Room> rooms = roomRepository.findAll();
        System.out.println("房間數量：" + rooms.size());
        rooms.forEach(System.out::println);
    }
}
```

---

> **🔰 現在試試看：**
> 1. 建立 `UserRepository extends JpaRepository<User, Integer>`
> 2. 在測試類中新增三個使用者並呼叫 `findAll()` 印出
> 3. 用 `deleteById()` 刪除其中一個，再呼叫 `count()` 驗證數量

---

---

# 🌿 核心階段

---

## Unit 4 — 查詢的三種方式

### 4-1 方式一：方法名稱自動生成 SQL（最簡單）

Spring Data JPA 會根據**方法名稱**自動產生對應 SQL，不需要寫任何查詢！

**命名規則：** `findBy` + `欄位名（大駝峰）` + `條件關鍵字`

```java
@Repository
public interface RoomRepository extends JpaRepository<Room, Integer> {

    // 等於（Equals）
    Optional<Room> findByRoomName(String roomName);
    // 產生：SELECT * FROM room WHERE room_name = ?

    // 大於（GreaterThan）
    List<Room> findByRoomSizeGreaterThan(Integer size);
    // 產生：SELECT * FROM room WHERE room_size > ?

    // 介於（Between）
    List<Room> findByRoomSizeBetween(Integer min, Integer max);
    // 產生：SELECT * FROM room WHERE room_size BETWEEN ? AND ?

    // 模糊查詢（Containing）
    List<Room> findByRoomNameContaining(String keyword);
    // 產生：SELECT * FROM room WHERE room_name LIKE '%keyword%'

    // 複合條件（And / Or）
    List<Room> findByRoomSizeGreaterThanAndRoomNameContaining(Integer size, String name);
    // 產生：... WHERE room_size > ? AND room_name LIKE '%?%'

    // 排序（OrderBy）
    List<Room> findAllByOrderByRoomSizeDesc();
    // 產生：SELECT * FROM room ORDER BY room_size DESC
}
```

**常用條件關鍵字對照表：**

| 關鍵字 | SQL 對應 | 範例方法名 |
|--------|---------|-----------|
| `Is` / `Equals` | `=` | `findByStatus` |
| `Not` | `!=` | `findByStatusNot` |
| `GreaterThan` | `>` | `findByAgeGreaterThan` |
| `LessThanEqual` | `<=` | `findByAgeLessThanEqual` |
| `Between` | `BETWEEN` | `findByAgeBetween` |
| `Like` | `LIKE` | `findByNameLike` |
| `Containing` | `LIKE '%x%'` | `findByNameContaining` |
| `StartingWith` | `LIKE 'x%'` | `findByNameStartingWith` |
| `In` | `IN (...)` | `findByStatusIn` |
| `IsNull` | `IS NULL` | `findByDeletedAtIsNull` |
| `IsNotNull` | `IS NOT NULL` | `findByEmailIsNotNull` |

> **陷阱：** 條件太多時方法名稱會變得很長（如 `findByAgeGreaterThanAndNameContainingOrderByCreatedAtDesc`），
> 此時應改用 `@Query`。

---

### 4-2 方式二：@Query + JPQL（推薦，跨資料庫）

**JPQL（Jakarta Persistence Query Language）** 是操作 **Entity 物件**的查詢語言，
語法類似 SQL，但操作的是 Java 類別與欄位名，由 JPA 翻譯成 SQL。

```java
@Repository
public interface RoomRepository extends JpaRepository<Room, Integer> {

    // 基本 JPQL：FROM 後面是 Entity 類別名，不是資料表名！
    @Query("SELECT r FROM Room r WHERE r.roomSize > :roomSize")
    List<Room> readRooms(Integer roomSize);
    //                            ↑ 這裡是 Entity 的 Java 欄位名（roomSize），不是 room_size

    // 多條件 JPQL
    @Query("SELECT r FROM Room r WHERE r.roomSize BETWEEN :min AND :max ORDER BY r.roomSize DESC")
    List<Room> findRoomsBySizeRange(Integer min, Integer max);

    // 計算筆數
    @Query("SELECT COUNT(r) FROM Room r WHERE r.roomSize > :size")
    long countLargeRooms(Integer size);

    // JOIN（搭配關聯映射）
    @Query("SELECT u FROM User u JOIN FETCH u.orders WHERE u.userId = :id")
    Optional<User> findUserWithOrders(Integer id);
}
```

**JPQL vs SQL 對照：**

| JPQL | SQL | 說明 |
|------|-----|------|
| `FROM Room r` | `FROM room r` | Entity 名 vs 表名 |
| `r.roomSize` | `r.room_size` | Java 欄位名 vs DB 欄位名 |
| `r.user.userId` | `JOIN users...` | 直接跨關聯導航 |

---

### 4-3 方式三：@Query + Native SQL（最靈活）

直接寫原生 SQL，可使用資料庫特有函數，但會綁定特定資料庫。

```java
@Repository
public interface RoomRepository extends JpaRepository<Room, Integer> {

    // nativeQuery = true → 使用原生 SQL（欄位名要符合資料表中的設定）
    @Query(value = "SELECT room_id, room_name, room_size FROM room WHERE room_size > :roomSize",
           nativeQuery = true)
    List<Room> findRooms(Integer roomSize);

    // 使用資料庫函數（如 MySQL 的 DATE_FORMAT）
    @Query(value = "SELECT * FROM room WHERE room_name LIKE CONCAT('%', :keyword, '%')",
           nativeQuery = true)
    List<Room> searchByName(String keyword);
}
```

**搭配 @Modifying 做更新 / 刪除：**

```java
// ⚠️ @Query 預設是 SELECT，更新/刪除操作必須加 @Modifying
@Modifying
@Transactional
@Query(value = "UPDATE room SET room_size = :size WHERE room_id = :id", nativeQuery = true)
int updateRoomSize(Integer id, Integer size);
// 回傳 int：影響的資料列數

@Modifying
@Transactional
@Query("DELETE FROM Room r WHERE r.roomSize = 0")
int deleteEmptyRooms();
```

---

### 4-4 三種方式選用建議

```
需求                              建議方式
────────────────────────────────────────────────────────
簡單的等於、大於、模糊查詢          → ① 方法名稱自動生成
一般業務查詢（跨資料庫）            → ② @Query JPQL
需要用到 DB 特有函數                → ③ @Query Native SQL
複雜動態條件（條件數量不固定）       → Unit 9 的 Specification
```

---

> **🔰 現在試試看：**
> 在 `RoomRepository` 中用三種方式分別寫一個「查詢容納人數大於指定值的房間」的方法，
> 然後在測試類中呼叫，觀察 console 輸出的 SQL 有何不同。

---

## Unit 5 — 交易管理與 @Transactional

### 5-1 什麼是交易（Transaction）？

交易是「一組操作，要嘛全部成功，要嘛全部失敗」的機制。

**情境：** 訂房時需要「新增訂單 + 扣減房間數量」兩個動作：

```
如果沒有交易：
  Step 1: 新增訂單 ✅
  Step 2: 扣減房間 ❌ 發生錯誤！
  → 訂單存在但房間沒扣，資料不一致！

有交易的情況：
  Step 1: 新增訂單 ✅
  Step 2: 扣減房間 ❌ 發生錯誤！
  → 自動 ROLLBACK，Step 1 也回滾，資料保持一致
```

### 5-2 @Transactional 基本用法

```java
@Service
public class RoomServiceImpl {

    @Autowired
    private RoomRepository roomRepository;

    // ── 讀取操作：加 readOnly = true ──────────────────────────
    // 效益：避免寫入操作被誤觸、Hibernate 可做查詢優化
    @Transactional(readOnly = true)
    public List<Room> getAllRooms() {
        return roomRepository.findAll();
    }

    // ── 寫入操作：預設 readOnly = false ─────────────────────────
    @Transactional
    public Room addRoom(Integer roomId, String roomName, Integer roomSize) {
        Room room = new Room(roomId, roomName, roomSize);
        return roomRepository.save(room);
    }

    // ── 出現例外時自動回滾 ──────────────────────────────────────
    @Transactional
    public void transferBooking(Integer fromRoomId, Integer toRoomId) {
        Room from = roomRepository.findById(fromRoomId)
            .orElseThrow(() -> new RuntimeException("來源房間不存在"));
        Room to = roomRepository.findById(toRoomId)
            .orElseThrow(() -> new RuntimeException("目標房間不存在"));

        // 如果這裡拋出例外，整個方法的所有 DB 操作都會回滾
        from.setRoomSize(from.getRoomSize() - 1);
        to.setRoomSize(to.getRoomSize() + 1);

        roomRepository.save(from);
        roomRepository.save(to);
    }
}
```

### 5-3 回滾規則

```
預設行為：
  RuntimeException  → 自動回滾 ✅
  Error             → 自動回滾 ✅
  checked Exception → 不回滾 ❌（這是個陷阱！）

自訂回滾：
  @Transactional(rollbackFor = Exception.class)  // 對所有例外回滾
  @Transactional(noRollbackFor = MyException.class) // 排除特定例外
```

```java
// 想對 checked Exception 也回滾：
@Transactional(rollbackFor = Exception.class)
public void riskyOperation() throws IOException {
    // IOException 也會觸發回滾
}
```

### 5-4 @Transactional 放在哪裡？

```
✅ 推薦：放在 Service 層（業務邏輯層）
❌ 避免：放在 Repository 層（除非有特殊需求）
❌ 避免：放在 Controller 層
```

---

> **🔰 現在試試看：**
> 寫一個方法，同時儲存兩筆資料，然後在第二筆儲存前故意拋出 `RuntimeException`，
> 觀察是否兩筆資料都沒有被儲存（交易回滾生效）。

---

## Unit 6 — DTO 設計與資料保護

### 6-1 為什麼不能直接回傳 Entity？

```java
// ❌ 危險！直接回傳 User Entity 會暴露密碼雜湊、鹽值等敏感資料
@GetMapping("/users/{id}")
public User getUser(@PathVariable Integer id) {
    return userRepository.findById(id).get();
    // Response JSON：{"userId":1, "username":"john",
    //                 "passwordHash":"Qwd1234...", "salt":"$ED..."} ← 洩漏！
}
```

**DTO（Data Transfer Object，資料傳輸物件）** 是專門用來傳輸資料的物件，
只包含需要對外暴露的欄位：

```
Entity（資料庫層）        DTO（API 回應層）
┌─────────────────┐      ┌──────────────────┐
│ userId          │  →   │ userId           │
│ username        │  →   │ username         │
│ passwordHash    │  ✗   │ email            │
│ salt            │  ✗   │ active           │
│ email           │  →   │ role             │
│ active          │  →   └──────────────────┘
│ role            │  →
└─────────────────┘
```

### 6-2 建立 DTO 類別

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
    // ⚠️ 注意：沒有 passwordHash 和 salt！
}
```

### 6-3 手動轉換（簡單但重複）

```java
// Entity → DTO（手動）
public UserDto toDto(User user) {
    UserDto dto = new UserDto();
    dto.setUserId(user.getUserId());
    dto.setUsername(user.getUsername());
    dto.setEmail(user.getEmail());
    dto.setActive(user.getActive());
    dto.setRole(user.getRole());
    return dto;
}
```

### 6-4 使用 ModelMapper 自動轉換（推薦）

```xml
<!-- pom.xml 加入依賴 -->
<dependency>
    <groupId>org.modelmapper</groupId>
    <artifactId>modelmapper</artifactId>
    <version>3.2.1</version>
</dependency>
```

```java
// src/main/java/com/example/demo/config/AppConfig.java
@Configuration
public class AppConfig {
    @Bean
    public ModelMapper modelMapper() {
        return new ModelMapper();
    }
}
```

```java
// src/main/java/com/example/demo/mapper/UserMapper.java
@Component
public class UserMapper {

    @Autowired
    private ModelMapper modelMapper;

    // Entity → DTO（自動對應同名欄位）
    public UserDto toDto(User user) {
        return modelMapper.map(user, UserDto.class);
    }

    // DTO → Entity（如需反向轉換）
    public User toEntity(UserDto dto) {
        return modelMapper.map(dto, User.class);
    }
}
```

### 6-5 在 Service 中套用

```java
@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserMapper userMapper;

    @Transactional(readOnly = true)
    public UserDto getUser(String username) {
        User user = userRepository.findByUsername(username)
            .orElseThrow(() -> new RuntimeException("使用者不存在"));
        return userMapper.toDto(user); // 轉換後才回傳，不暴露敏感欄位
    }
}
```

---

> **🔰 現在試試看：**
> 建立 `RoomDto`，只包含 `roomId` 和 `roomName`（不含 `roomSize`），
> 並在 Service 中將 `findAll()` 的結果轉換成 `List<RoomDto>` 回傳。

---

---

# 🌳 進階階段

---

## Unit 7 — 關聯映射

> **觀念先行：** 先搞懂資料表之間的外鍵關係，再看 Entity 標注。

### 7-1 @ManyToOne / @OneToMany（一對多）

**情境：** 一個 `User` 可以有多筆 `Order`（訂單）

**資料表設計：**
```sql
users: user_id (PK)
orders: order_id (PK), product, user_id (FK → users.user_id)
```

```java
// Order.java — 多的一方（持有外鍵 user_id）
@Entity
@Table(name = "orders")
@Data @NoArgsConstructor @AllArgsConstructor
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer orderId;

    private String product;
    private Integer quantity;

    // 多個 Order → 一個 User
    // fetch = LAZY：不立即查詢 User，等第一次存取 order.getUser() 時才查
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id") // 指定 DB 中的外鍵欄位名
    private User user;
}
```

```java
// User.java — 一的一方
@Entity
@Table(name = "users")
@Data @NoArgsConstructor @AllArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer userId;
    private String username;

    // 一個 User → 多個 Order
    // mappedBy = "user"：告訴 JPA「關聯由 Order 的 user 欄位維護」
    // cascade = ALL：對 User 做任何操作，同步對 orders 執行
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Order> orders = new ArrayList<>();
}
```

**使用範例：**

```java
@Transactional
public void createUserWithOrders() {
    User user = new User(null, "alice");
    Order o1 = new Order(null, "筆電", 1, user);
    Order o2 = new Order(null, "滑鼠", 2, user);
    user.setOrders(List.of(o1, o2));

    userRepository.save(user); // cascade = ALL → 同時儲存 orders
}
```

### 7-2 FetchType 詳解

```
FetchType.LAZY（懶加載，預設值）
  userRepository.findById(1)
  → 只執行：SELECT * FROM users WHERE user_id = 1
  → 不查詢 orders
  → 當你呼叫 user.getOrders() 時，才執行：SELECT * FROM orders WHERE user_id = 1
  優點：效能好（不查不需要的資料）
  陷阱：若在 Transaction 外存取 orders，會拋 LazyInitializationException！

FetchType.EAGER（急加載）
  userRepository.findById(1)
  → 執行：SELECT u.*, o.* FROM users u LEFT JOIN orders o ON u.user_id = o.user_id
  → 一次查出所有資料
  缺點：即使你不需要 orders，也會一起查出（效能浪費）
```

**預設值整理：**

| 標注 | 預設 FetchType | 建議改為 |
|------|--------------|---------|
| `@OneToMany` | `LAZY` ✅ | 保持 LAZY |
| `@ManyToMany` | `LAZY` ✅ | 保持 LAZY |
| `@ManyToOne` | `EAGER` ⚠️ | 改為 LAZY |
| `@OneToOne` | `EAGER` ⚠️ | 改為 LAZY |

### 7-3 CascadeType 說明

| 選項 | 說明 | 情境 |
|------|------|------|
| `PERSIST` | 儲存主體時，同步儲存子集合 | 新增 User 同時新增 Orders |
| `MERGE` | 更新主體時，同步更新子集合 | 修改 User 同時修改 Orders |
| `REMOVE` | 刪除主體時，同步刪除子集合 | 刪除 User 時刪除所有 Orders |
| `ALL` | 以上全部 | 典型的父子關係 |

> **⚠️ 警告：** `CascadeType.REMOVE` 要謹慎使用！刪除 User 會同時刪除所有 Orders，
> 如果 Orders 還有其他業務意義（如帳務記錄），應改用 `orphanRemoval` 並謹慎設計。

### 7-4 @ManyToMany（多對多）

**情境：** 一個 `Student` 可以選多門 `Course`，一門 `Course` 有多個 `Student`

```java
// Student.java
@Entity
public class Student {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer studentId;
    private String name;

    @ManyToMany
    @JoinTable(
        name = "student_course",                          // 中間表名稱
        joinColumns = @JoinColumn(name = "student_id"),   // 本方外鍵
        inverseJoinColumns = @JoinColumn(name = "course_id") // 對方外鍵
    )
    private List<Course> courses = new ArrayList<>();
}

// Course.java
@Entity
public class Course {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer courseId;
    private String title;

    @ManyToMany(mappedBy = "courses") // 由 Student 端維護關聯
    private List<Student> students = new ArrayList<>();
}
```

---

> **🔰 現在試試看：**
> 建立 `Category`（分類）和 `Product`（商品）的一對多關係：
> - 一個 Category 有多個 Product
> - 設定 `cascade = CascadeType.PERSIST`
> - 新增一個 Category 並同時新增兩個 Product，觀察 SQL 輸出

---

## Unit 8 — 分頁與排序查詢

### 8-1 為什麼需要分頁？

一次查詢 100 萬筆資料是致命的效能問題。分頁讓每次只查一小塊：

```
全部資料：[1][2][3][4][5]...[1000000]

第 1 頁（每頁 10 筆）：[1][2][3][4][5][6][7][8][9][10]
第 2 頁：              [11][12]...[20]
...
```

### 8-2 Spring Data JPA 分頁用法

```java
// Service 層
@Transactional(readOnly = true)
public Page<Room> getRoomsPage(int pageNumber, int pageSize) {
    // PageRequest.of(頁碼從0開始, 每頁筆數)
    Pageable pageable = PageRequest.of(pageNumber, pageSize);
    return roomRepository.findAll(pageable);
}

// 使用回傳的 Page 物件
Page<Room> page = roomService.getRoomsPage(0, 10); // 第一頁，每頁10筆

System.out.println("總筆數：" + page.getTotalElements());
System.out.println("總頁數：" + page.getTotalPages());
System.out.println("當前頁碼：" + page.getNumber());
System.out.println("是否最後一頁：" + page.isLast());

List<Room> rooms = page.getContent(); // 取出本頁資料
```

### 8-3 分頁 + 排序

```java
// 依 roomSize 降冪，取第 0 頁，每頁 5 筆
Pageable pageable = PageRequest.of(
    0,                               // 頁碼（從 0 開始）
    5,                               // 每頁筆數
    Sort.by(Sort.Direction.DESC, "roomSize") // 排序欄位（Entity 欄位名）
);
Page<Room> page = roomRepository.findAll(pageable);
```

### 8-4 在 @Query 中使用分頁

```java
@Repository
public interface RoomRepository extends JpaRepository<Room, Integer> {

    // 分頁 + 自訂條件
    @Query("SELECT r FROM Room r WHERE r.roomSize > :minSize")
    Page<Room> findLargeRooms(Integer minSize, Pageable pageable);
}

// 呼叫方式
Page<Room> result = roomRepository.findLargeRooms(
    30,
    PageRequest.of(0, 5, Sort.by("roomSize"))
);
```

### 8-5 Controller 層整合

```java
@RestController
@RequestMapping("/api/rooms")
public class RoomController {

    @Autowired
    private RoomService roomService;

    // GET /api/rooms?page=0&size=10
    @GetMapping
    public Page<RoomDto> getRooms(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return roomService.getRoomsPage(page, size);
    }
}
```

---

> **🔰 現在試試看：**
> 實作一個 API `GET /api/rooms?page=0&size=5&sort=roomSize,desc`，
> 讓前端可以傳入分頁參數，回傳對應的資料。

---

## Unit 9 — 動態查詢（Specification）

### 9-1 為什麼需要動態查詢？

當搜尋條件數量不固定時（使用者可能填 0～N 個條件），用方法名稱或固定 JPQL 都不夠用：

```
搜尋條件（使用者選擇性填寫）：
  ☑ 容納人數 > 30
  ☐ 房間名稱包含「101」
  ☑ 房間編號 < 200

→ 有幾種組合就要寫幾個方法嗎？不！用 Specification！
```

### 9-2 設定 Repository

```java
// 繼承 JpaSpecificationExecutor 才能使用 Specification
@Repository
public interface RoomRepository
        extends JpaRepository<Room, Integer>,
                JpaSpecificationExecutor<Room> { // 加這個！
}
```

### 9-3 建立 Specification

```java
// src/main/java/com/example/demo/spec/RoomSpec.java
public class RoomSpec {

    // 條件 1：容納人數大於 minSize
    public static Specification<Room> sizeGreaterThan(Integer minSize) {
        return (root, query, cb) -> {
            if (minSize == null) return null; // 條件為空 → 不套用此條件
            return cb.greaterThan(root.get("roomSize"), minSize);
        };
    }

    // 條件 2：房間名稱包含關鍵字
    public static Specification<Room> nameContains(String keyword) {
        return (root, query, cb) -> {
            if (keyword == null || keyword.isBlank()) return null;
            return cb.like(root.get("roomName"), "%" + keyword + "%");
        };
    }

    // 條件 3：房間編號小於 maxId
    public static Specification<Room> idLessThan(Integer maxId) {
        return (root, query, cb) -> {
            if (maxId == null) return null;
            return cb.lessThan(root.get("roomId"), maxId);
        };
    }
}
```

### 9-4 組合並執行動態查詢

```java
@Service
public class RoomServiceImpl {

    @Autowired
    private RoomRepository roomRepository;

    @Transactional(readOnly = true)
    public List<Room> searchRooms(Integer minSize, String keyword, Integer maxId) {

        // 使用 Specification.where() 組合多個條件（null 條件自動忽略）
        Specification<Room> spec = Specification
            .where(RoomSpec.sizeGreaterThan(minSize))
            .and(RoomSpec.nameContains(keyword))
            .and(RoomSpec.idLessThan(maxId));

        return roomRepository.findAll(spec);
    }
}

// 呼叫範例：
// searchRooms(30, null, 200) → WHERE room_size > 30 AND room_id < 200
// searchRooms(null, "101", null) → WHERE room_name LIKE '%101%'
// searchRooms(null, null, null) → 查詢全部
```

---

> **🔰 現在試試看：**
> 為 `User` 實作動態搜尋：可根據 `username` 關鍵字、`role` 類型、`active` 狀態的任意組合搜尋。

---

---

# 🚀 實戰階段

---

## Unit 10 — EntityManager 與生命週期

### 10-1 Entity 的四種狀態

```
【New 瞬態】
  new Room(101, "101L", 100)
  → 剛建立的 Java 物件，JPA 完全不知道它的存在

       ↓ em.persist() 或 repository.save()（新物件）

【Managed 持久態】
  → JPA 正在追蹤此物件，任何修改都會自動同步到 DB（Dirty Checking）
  → 在同一個 Transaction 內，不需要呼叫 save() 也會自動更新！

       ↓ Transaction 結束 或 em.detach()

【Detached 分離態】
  → JPA 不再追蹤，修改不會自動同步
  → 需要呼叫 em.merge() 或 repository.save() 重新附著

       ↓ em.remove()

【Removed 刪除態】
  → 已標記刪除，Transaction commit 後從 DB 移除
```

### 10-2 Dirty Checking（自動髒檢查）

```java
@Transactional
public void updateRoom(Integer id, Integer newSize) {
    Room room = roomRepository.findById(id).orElseThrow();
    // room 現在是 Managed 狀態

    room.setRoomSize(newSize);
    // ⚡ 不需要呼叫 save()！
    // Transaction 結束時，JPA 會自動偵測到 roomSize 被修改，
    // 自動執行 UPDATE room SET room_size = ? WHERE room_id = ?
}
```

> **這就是「髒檢查（Dirty Checking）」：** JPA 在 Transaction 結束時，
> 比對 Entity 的當前值與快照值，若有差異則自動發出 UPDATE SQL。

### 10-3 直接使用 EntityManager（了解底層）

```java
@Service
@Transactional
public class AdvancedService {

    @PersistenceContext
    private EntityManager em;

    // persist：新增（Entity 必須是 New 狀態）
    public void persist(Room room) {
        em.persist(room);
    }

    // find：依主鍵查詢（回傳 Managed 狀態的 Entity）
    public Room find(Integer id) {
        return em.find(Room.class, id); // 找不到回傳 null
    }

    // merge：更新（接受 Detached 狀態的 Entity）
    public Room merge(Room detachedRoom) {
        return em.merge(detachedRoom); // 回傳新的 Managed Entity
    }

    // remove：刪除（Entity 必須是 Managed 狀態）
    public void remove(Integer id) {
        Room room = em.find(Room.class, id);
        if (room != null) em.remove(room);
    }

    // flush：立即將暫存的 SQL 發送到 DB（但不 commit）
    public void flush() {
        em.flush();
    }
}
```

---

## Unit 11 — 效能優化與常見陷阱

### 11-1 N+1 問題（最常見的效能殺手）

```
問題描述：
  查詢 100 個 User（1 次 SQL）
  → 對每個 User 再查一次 orders（100 次 SQL）
  → 總共 101 次 SQL！

症狀：
  SELECT * FROM users;                               ← 1 次
  SELECT * FROM orders WHERE user_id = 1;            ← 第 1 個 user 的 orders
  SELECT * FROM orders WHERE user_id = 2;            ← 第 2 個 user 的 orders
  ...                                                ← N 次
```

**解法一：JOIN FETCH（最常用）**

```java
// Repository 加入 JOIN FETCH 查詢
@Query("SELECT DISTINCT u FROM User u LEFT JOIN FETCH u.orders")
List<User> findAllWithOrders();
// 產生：SELECT u.*, o.* FROM users u LEFT JOIN orders o ON ...
// 只發出 1 次 SQL！
```

**解法二：@EntityGraph**

```java
@EntityGraph(attributePaths = {"orders"}) // 指定要一起抓取的關聯
List<User> findAll(); // 覆蓋原本的 findAll
```

**解法三：批量抓取（hibernate.default_batch_fetch_size）**

```properties
# application.properties
spring.jpa.properties.hibernate.default_batch_fetch_size=30
# 改為：每次用 IN 子句批量查詢 30 個，從 N 次變成 N/30 次
```

### 11-2 LazyInitializationException

```
錯誤：
  org.hibernate.LazyInitializationException:
    failed to lazily initialize a collection of role: ...
    could not initialize proxy - no Session
```

**原因：** Transaction 結束（Session 關閉）後，才嘗試存取 Lazy 關聯。

```java
// ❌ 問題程式碼
@Transactional(readOnly = true)
public User getUser(Integer id) {
    return userRepository.findById(id).orElseThrow();
    // Transaction 在方法結束後關閉
}

// Controller 中
User user = userService.getUser(1);
user.getOrders().size(); // ❌ 此時 Session 已關閉，拋出例外！
```

**解法：**

```java
// ✅ 解法 1：在 Service 層（Transaction 內）預先觸發加載
@Transactional(readOnly = true)
public UserDto getUserWithOrders(Integer id) {
    User user = userRepository.findById(id).orElseThrow();
    user.getOrders().size(); // 在 Transaction 內觸發懶加載
    return userMapper.toDto(user);
}

// ✅ 解法 2：用 JOIN FETCH 一次抓取
@Transactional(readOnly = true)
public User getUserWithOrders(Integer id) {
    return userRepository.findByIdWithOrders(id).orElseThrow();
}
```

### 11-3 批量操作優化

```java
// ❌ 低效：每次 save 都發一次 SQL
for (Room room : rooms) {
    roomRepository.save(room); // 1000 個 room → 1000 次 INSERT
}

// ✅ 高效：用 saveAll 一次批量處理
roomRepository.saveAll(rooms); // 搭配 batch_size 設定，效率大幅提升
```

```properties
# application.properties
spring.jpa.properties.hibernate.jdbc.batch_size=50
spring.jpa.properties.hibernate.order_inserts=true
spring.jpa.properties.hibernate.order_updates=true
```

### 11-4 常見錯誤速查表

| 錯誤訊息 | 原因 | 解法 |
|---------|------|------|
| `LazyInitializationException` | Transaction 外存取 Lazy 關聯 | 用 JOIN FETCH 或在 Service 內預先觸發 |
| `detached entity passed to persist` | 有 ID 的物件呼叫 persist | 改用 `save()` 或 `merge()` |
| `No identifier specified for entity` | Entity 缺少 `@Id` | 加上 `@Id` 標注 |
| `Column 'xxx' cannot be null` | 必填欄位傳入 null | 加 `@NotBlank` 或前置驗證 |
| `could not initialize proxy` | Lazy 代理物件未初始化 | 同 LazyInitializationException 解法 |
| `TransactionRequiredException` | 在無交易的環境執行寫入 | 在 Service 方法加 `@Transactional` |

---

## Unit 12 — 整合測試寫法

### 12-1 基本測試設定

```java
// @SpringBootTest：載入完整 Spring Context（包含資料庫連線）
@SpringBootTest
class RoomJPATest {

    @Autowired
    private RoomRepository roomRepository;

    // 每次測試前清空，確保測試隔離
    @BeforeEach
    void setUp() {
        roomRepository.deleteAll();
    }

    @Test
    void testSaveAndFind() {
        // Arrange（準備）
        Room room = new Room(101, "101(L)", 100);

        // Act（執行）
        roomRepository.save(room);
        Optional<Room> found = roomRepository.findById(101);

        // Assert（驗證）
        assertTrue(found.isPresent());
        assertEquals("101(L)", found.get().getRoomName());
        assertEquals(100, found.get().getRoomSize());
    }

    @Test
    void testFindByRoomSizeGreaterThan() {
        // 準備測試資料
        roomRepository.saveAll(List.of(
            new Room(101, "小房", 10),
            new Room(102, "中房", 50),
            new Room(103, "大房", 100)
        ));

        // 執行查詢
        List<Room> large = roomRepository.findByRoomSizeGreaterThan(30);

        // 驗證
        assertEquals(2, large.size());
        assertTrue(large.stream().allMatch(r -> r.getRoomSize() > 30));
    }
}
```

### 12-2 使用 @Transactional 讓測試自動回滾

```java
@SpringBootTest
@Transactional // 每個測試方法執行完後自動回滾，不污染測試資料庫
class UserServiceTest {

    @Autowired
    private UserService userService;

    @Test
    void testAddUser() {
        userService.addUser("testUser", "password", "test@test.com", true, "user");

        UserDto user = userService.getUser("testUser");
        assertNotNull(user);
        assertEquals("testUser", user.getUsername());
        // 測試結束後自動回滾，DB 中不會留下 testUser
    }
}
```

### 12-3 使用 @DataJpaTest（只測 JPA 層）

```java
// @DataJpaTest：只載入 JPA 相關元件，速度比 @SpringBootTest 快
// 預設使用 H2 記憶體資料庫（需加入 H2 依賴）
@DataJpaTest
class RoomRepositoryTest {

    @Autowired
    private RoomRepository roomRepository;

    @Test
    void testCustomQuery() {
        roomRepository.save(new Room(101, "大廳", 80));
        List<Room> rooms = roomRepository.findByRoomSizeGreaterThan(50);
        assertFalse(rooms.isEmpty());
    }
}
```

---

## Unit 13 — 練習題庫

### 🟢 Easy（初學）

**題目 1：建立 Product Entity**

建立一個 `Product` Entity 對應以下資料表，並使用 `ddl-auto=update` 讓 JPA 自動建立資料表：

```sql
product_id  INTEGER  AUTO_INCREMENT PRIMARY KEY
name        VARCHAR(100) NOT NULL
price       DECIMAL(10,2) NOT NULL
stock       INTEGER  DEFAULT 0
```

<details>
<summary>💡 提示</summary>

- `DECIMAL` 在 Java 對應 `BigDecimal`
- `@Column(nullable = false)` 對應 `NOT NULL`
- `columnDefinition = "integer default 0"` 可設定預設值

</details>

<details>
<summary>✅ 解答</summary>

```java
@Data @AllArgsConstructor @NoArgsConstructor
@Entity
@Table(name = "product")
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "product_id")
    private Integer productId;

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Column(name = "price", nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    @Column(name = "stock", columnDefinition = "integer default 0")
    private Integer stock;
}
```

</details>

---

**題目 2：Repository 基本 CRUD**

使用 `ProductRepository` 撰寫以下操作的測試：
1. 新增 3 個商品
2. 查詢所有商品，印出名稱與價格
3. 更新第一個商品的 stock 為 100
4. 刪除庫存為 0 的商品

<details>
<summary>✅ 解答</summary>

```java
@SpringBootTest
class ProductCrudTest {
    @Autowired ProductRepository productRepo;

    @Test
    @Transactional
    void testCrud() {
        // 新增
        Product p1 = productRepo.save(new Product(null, "筆電", new BigDecimal("30000"), 0));
        Product p2 = productRepo.save(new Product(null, "滑鼠", new BigDecimal("500"), 0));
        Product p3 = productRepo.save(new Product(null, "鍵盤", new BigDecimal("1200"), 10));

        // 查詢全部
        productRepo.findAll().forEach(p ->
            System.out.println(p.getName() + "：" + p.getPrice()));

        // 更新
        p1.setStock(100);
        productRepo.save(p1);

        // 刪除
        List<Product> zeroStock = productRepo.findByStock(0);
        productRepo.deleteAll(zeroStock);

        assertEquals(2, productRepo.count()); // 只剩 p1（stock=100）和 p3（stock=10）
    }
}
```

</details>

---

### 🟡 Medium（有基礎）

**題目 3：三種查詢方式實作**

為 `RoomRepository` 分別用方法名稱、JPQL、Native SQL 實作：
「查詢 roomSize 介於 minSize 和 maxSize 之間的房間，依 roomSize 升冪排序」

<details>
<summary>✅ 解答</summary>

```java
// 方法名稱
List<Room> findByRoomSizeBetweenOrderByRoomSizeAsc(Integer min, Integer max);

// JPQL
@Query("SELECT r FROM Room r WHERE r.roomSize BETWEEN :min AND :max ORDER BY r.roomSize ASC")
List<Room> findBySizeRangeJpql(Integer min, Integer max);

// Native SQL
@Query(value = "SELECT * FROM room WHERE room_size BETWEEN :min AND :max ORDER BY room_size ASC",
       nativeQuery = true)
List<Room> findBySizeRangeNative(Integer min, Integer max);
```

</details>

---

**題目 4：分頁 API 實作**

實作 `GET /api/products?page=0&size=5&sort=price,desc` API，
支援分頁、排序，回傳 `Page<ProductDto>`（DTO 只包含 name 和 price）。

<details>
<summary>✅ 解答</summary>

```java
// ProductDto.java
@Data @AllArgsConstructor @NoArgsConstructor
public class ProductDto {
    private String name;
    private BigDecimal price;
}

// ProductController.java
@GetMapping("/api/products")
public Page<ProductDto> getProducts(
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "5") int size,
        @RequestParam(defaultValue = "productId") String sortBy,
        @RequestParam(defaultValue = "asc") String direction) {

    Sort sort = direction.equalsIgnoreCase("desc")
        ? Sort.by(sortBy).descending()
        : Sort.by(sortBy).ascending();

    Pageable pageable = PageRequest.of(page, size, sort);
    return productRepository.findAll(pageable)
        .map(p -> new ProductDto(p.getName(), p.getPrice()));
}
```

</details>

---

### 🔴 Hard（進階）

**題目 5：動態搜尋 + 關聯查詢**

設計 `Order`（訂單）Entity，每筆 Order 屬於一個 `User`（ManyToOne）。
實作動態搜尋：可依 `username` 關鍵字、`product` 名稱、`minQuantity` 的任意組合搜尋訂單。

<details>
<summary>💡 提示</summary>

- 用 `Specification` 處理動態條件
- 跨關聯查詢時，用 `root.join("user")` 或 `root.get("user").get("username")`

</details>

<details>
<summary>✅ 解答</summary>

```java
// OrderSpec.java
public class OrderSpec {

    public static Specification<Order> usernameContains(String username) {
        return (root, query, cb) -> {
            if (username == null || username.isBlank()) return null;
            // 跨關聯查詢 user.username
            return cb.like(root.get("user").get("username"), "%" + username + "%");
        };
    }

    public static Specification<Order> productContains(String product) {
        return (root, query, cb) -> {
            if (product == null || product.isBlank()) return null;
            return cb.like(root.get("product"), "%" + product + "%");
        };
    }

    public static Specification<Order> quantityAtLeast(Integer minQty) {
        return (root, query, cb) -> {
            if (minQty == null) return null;
            return cb.greaterThanOrEqualTo(root.get("quantity"), minQty);
        };
    }
}

// OrderService.java
public List<Order> searchOrders(String username, String product, Integer minQty) {
    Specification<Order> spec = Specification
        .where(OrderSpec.usernameContains(username))
        .and(OrderSpec.productContains(product))
        .and(OrderSpec.quantityAtLeast(minQty));
    return orderRepository.findAll(spec);
}
```

</details>

---

## 總結：學習里程碑

```
✅ 初學者里程碑
  □ 能建立 Entity 並對應資料表
  □ 能用 JpaRepository 完成 CRUD
  □ 能用 Optional 安全處理查詢結果
  □ 知道 ddl-auto 各選項的差異

✅ 有基礎里程碑
  □ 能用三種方式寫查詢（方法名 / JPQL / Native）
  □ 能正確使用 @Transactional（含 readOnly）
  □ 能設計 DTO 並用 ModelMapper 轉換
  □ 能實作分頁排序 API

✅ 進階里程碑
  □ 能設計 @OneToMany / @ManyToOne 關聯
  □ 能識別並解決 N+1 問題
  □ 能用 Specification 實作動態查詢
  □ 能撰寫隔離性良好的整合測試

✅ 實戰里程碑
  □ 能解釋 Dirty Checking 機制
  □ 能診斷並修復 LazyInitializationException
  □ 知道批量操作的效能優化方法
  □ 能在生產環境設定正確的 JPA 配置
```

---

> **推薦學習資源**
>
> | 資源 | 說明 | 連結 |
> |------|------|------|
> | Spring Data JPA 官方文件 | Repository 查詢方法完整規格 | https://docs.spring.io/spring-data/jpa/docs/current/reference/html/ |
> | Jakarta Persistence 3.1 規格書 | JPA 規範原文 | https://jakarta.ee/specifications/persistence/ |
> | Baeldung JPA 教學 | 豐富的範例與說明 | https://www.baeldung.com/the-persistence-layer-with-spring-and-jpa |
> | H2 Console | 記憶體資料庫，適合快速測試 | 加入 H2 依賴後訪問 /h2-console |
