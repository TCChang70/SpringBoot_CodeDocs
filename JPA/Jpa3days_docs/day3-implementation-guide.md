# Day 3 實作說明：交易管理、樂觀鎖定與 L2 快取

## 業務模型：票券預約系統

本日實作一個**票券預約**系統，展示 JPA 交易管理、樂觀鎖定處理並發衝突，以及 EclipseLink L2 快取。

### 實體關係圖

```
┌──────────────┐       ┌──────────────┐
│    Event     │1───N─→│    Ticket    │
│              │       │              │
│  totalTickets│       │  @Version    │ ← 樂觀鎖定
│  availableTk │       │  buyerName   │
└──────────────┘       │  quantity    │
                       │  status      │
                       │  bookingDate │
                       └──────────────┘

┌─────────────────┐
│    SysConfig    │ ← 純展示 L2 快取
│                 │
│  @Cacheable     │ ← 啟用 L2 快取
│  @Cache(expiry) │ ← 600 秒過期
│  configKey (UK) │
│  configValue    │
│  updatedAt      │
└─────────────────┘
```

---

## 1. 實體層

### 1.1 Ticket.java — 樂觀鎖定核心

```java
@Version
private Long version;
```

**`@Version` 機制：**

```
初始狀態：
  TICKETS 表格
  ┌────┬────────────┬──────────┬─────────┐
  │ ID │ BUYER_NAME │ VERSION  │ STATUS  │
  ├────┼────────────┼──────────┼─────────┤
  │  1 │ Alice      │       0  │ BOOKED  │
  └────┴────────────┴──────────┴─────────┘

更新流程：
  1. em.find(Ticket.class, 1) → version = 0
  2. 修改 buyerName
  3. em.merge(ticket)
  4. EclipseLink 執行:
     UPDATE TICKETS SET BUYER_NAME=?, VERSION=1
     WHERE ID=1 AND VERSION=0
  5. 若 WHERE VERSION=0 影響 0 列 → OptimisticLockException!
```

**`@Customizer(TicketDescriptorCustomizer.class)`：**

```java
public class TicketDescriptorCustomizer implements DescriptorCustomizer {
    @Override
    public void customize(ClassDescriptor descriptor) {
        // 擴充點：可在這裡設定 EclipseLink 專屬描述子設定
        // 例如：descriptor.setOptimisticLockingPolicy(...)
    }
}
```

目前為空實作，作為教學擴充點。學生可以在此加入自訂鎖定策略、歷史追蹤等。

### 1.2 Event.java — 庫存管理

```java
public void decrementAvailable(int count) {
    this.availableTickets -= count;
}
```

這個方法在 `bookTicket()` 中被呼叫，修改 `availableTickets` 後透過 `em.merge(event)` 持久化。**注意：** 因為 `availableTickets` 是非 `@Version` 欄位，並發修改可能導致超賣，須靠 `@Version` 在 Ticket 端攔截衝突。

### 1.3 SysConfig.java — L2 快取展示

```java
@Cacheable(true)
@Cache(expiry = 600000, isolation = CacheIsolationType.SHARED)
public class SysConfig {
```

| 註解 | 屬性 | 說明 |
|------|------|------|
| `@Cacheable(true)` | JPA 標準 | 允許 Entity 進入 L2 快取 |
| `@Cache(expiry=600000)` | EclipseLink 專屬 | 快取 10 分鐘後過期（單位：ms） |
| `@Cache(isolation=SHARED)` | EclipseLink 專屬 | 跨 EntityManager 共用快取 |

---

## 2. DAO 層

### 2.1 TicketDao.bookTicket() — 完整交易流程

這是最關鍵的方法，展示了**交易內多步驟操作**與**樂觀鎖定處理**：

```java
public String bookTicket(Long eventId, String buyerName, int quantity) {
    EntityManager em = JpaUtil.getEntityManager();
    try {
        em.getTransaction().begin();                    // 1. 開始交易

        Event event = em.find(Event.class, eventId);     // 2. 讀取 Event
        if (event == null) {                             // 3. 驗證存在
            em.getTransaction().rollback();
            return "{\"error\":\"Event not found\"}";
        }
        if (event.getAvailableTickets() < quantity) {    // 4. 驗證庫存
            em.getTransaction().rollback();
            return "{\"error\":\"Insufficient tickets available\"}";
        }

        Ticket ticket = new Ticket();                    // 5. 建立 Ticket
        ticket.setBuyerName(buyerName);
        ticket.setQuantity(quantity);
        ticket.setStatus("BOOKED");
        ticket.setBookingDate(LocalDateTime.now());
        ticket.setEvent(event);

        event.decrementAvailable(quantity);              // 6. 扣庫存

        em.persist(ticket);                              // 7. 持久化 Ticket
        em.merge(event);                                 // 8. 更新 Event

        em.getTransaction().commit();                    // 9. 提交

        return "{\"success\":true,...}";
    } catch (OptimisticLockException e) {                // 10. 樂觀鎖定衝突
        if (em.getTransaction().isActive()) {
            em.getTransaction().rollback();
        }
        return "{\"error\":\"Concurrent booking conflict...\"}";
    } finally {
        em.close();
    }
}
```

**完整的 SQL 序列：**

```sql
-- 1. BEGIN (由 JDBC 自動管理)

-- 2. SELECT Event
SELECT ID, NAME, DATE, TOTAL_TICKETS, AVAILABLE_TICKETS
FROM EVENTS WHERE ID = ?

-- 5-8. INSERT Ticket + UPDATE Event
INSERT INTO TICKETS (BUYER_NAME, QUANTITY, BOOKING_DATE, STATUS, VERSION, EVENT_ID)
VALUES (?, ?, ?, ?, 0, ?)

UPDATE EVENTS SET AVAILABLE_TICKETS = ? WHERE ID = ?

-- 9. COMMIT
```

### 2.2 樂觀鎖定衝突情境

```
時間軸：
  Thread A                          Thread B
    │                                 │
    ├─ em.find(Ticket,1) v=0          ├─ em.find(Ticket,1) v=0
    │                                 │
    ├─ 修改欄位                        ├─ 修改欄位
    │                                 │
    ├─ em.merge()                     │
    │   UPDATE ... WHERE VERSION=0    │
    │   (成功, version→1)              │
    │                                 │
    │                                 ├─ em.merge()
    │                                 │   UPDATE ... WHERE VERSION=0
    │                                 │   (影響 0 行 → OptimisticLockException!)
    │                                 ▼
    └─ COMMIT                     ROLLBACK
```

### 2.3 SysConfigDao — L2 快取展示

```java
// 方法 A：一般查詢（不使用快取設定）
public SysConfig findByKey(String key) {
    // 標準 JPQL，每次查詢資料庫
}

// 方法 B：使用快取 Hint
public SysConfig findByKeyWithCache(String key) {
    TypedQuery<SysConfig> query = em.createQuery(...);
    query.setHint("jakarta.persistence.cache.retrieveMode", "USE");
    query.setHint("jakarta.persistence.cache.storeMode", "USE");
    // 優先從 L2 Cache 讀取，查到的結果也存入 L2 Cache
}
```

**Cache Hints 對照表：**

| Hint | 值 | 效果 |
|------|----|------|
| `retrieveMode` | `USE` | 先查 L2 Cache，找不到才查資料庫 |
| `retrieveMode` | `BYPASS` | 跳過 L2 Cache，直接查資料庫 |
| `storeMode` | `USE` | 查詢結果寫入 L2 Cache |
| `storeMode` | `BYPASS` | 查詢結果不寫入 L2 Cache |
| `storeMode` | `REFRESH` | 強制更新 L2 Cache |

---

## 3. JAX-RS Resource 層

Day 3 提供 3 個 JAX-RS Resource 涵蓋訂票、設定管理與活動 CRUD。

### 3.1 BookingResource — 訂票與併發測試

```java
@Path("/day3/bookings")
public class BookingResource {

    private TicketDao ticketDao = new TicketDao();

    @POST @Path("/book")
    public Response book(Map<String, Object> body) {
        long eventId = ((Number) body.get("eventId")).longValue();
        String buyerName = (String) body.get("buyerName");
        int quantity = ((Number) body.get("quantity")).intValue();
        String result = ticketDao.bookTicket(eventId, buyerName, quantity);
        if (result.contains("\"error\"")) {
            int status = result.contains("not found") ? 404 : 409;
            return Response.status(status).entity(result).build();
        }
        return Response.ok(result).build();
    }

    @GET @Path("/concurrency-test")
    public String concurrencyTest() {
        return runConcurrencyTest();   // CountDownLatch 5 threads
    }

    @GET @JsonView(Views.List.class)
    public List<Ticket> getAll() { return ticketDao.findAll(); }

    @GET @Path("/{id}") @JsonView(Views.Detail.class)
    public Response getById(@PathParam("id") Long id) { ... }

    @GET @Path("/event/{eventId}") @JsonView(Views.List.class)
    public List<Ticket> getByEvent(@PathParam("eventId") Long eventId) { ... }
}
```

### 3.2 ConfigResource — L2 Cache 展示

```java
@Path("/day3/configs")
public class ConfigResource {

    @GET @Path("/cache-demo")
    public String cacheDemo() {
        // 第一次呼叫 (查資料庫)
        long start1 = System.nanoTime();
        configDao.findByKeyWithCache("cache.demo.key");
        long end1 = System.nanoTime();

        // 第二次呼叫 (從 L2 Cache)
        long start2 = System.nanoTime();
        configDao.findByKeyWithCache("cache.demo.key");
        long end2 = System.nanoTime();

        return "{\"firstCallMicros\":" + ((end1 - start1) / 1000)
             + ",\"secondCallMicros\":" + ((end2 - start2) / 1000)
             + ",\"cachedResult\":\"" + (判斷是否快取) + "\"}";
    }

    @GET @JsonView(Views.List.class)
    public List<SysConfig> getAll() { ... }

    @GET @Path("/key/{key}") @JsonView(Views.Detail.class)
    public Response getByKey(@PathParam("key") String key) { ... }

    @POST @JsonView(Views.Detail.class)
    public Response create(SysConfig config) { ... }

    @PUT @Path("/{id}")
    public Response update(@PathParam("id") Long id, Map<String, Object> body) { ... }

    @DELETE @Path("/{id}")
    public Response delete(@PathParam("id") Long id) { ... }
}
```

### 3.3 EventResource — 活動管理

```java
@Path("/day3/events")
public class EventResource {

    private EventDao eventDao = new EventDao();

    @POST
    public Response create(Event event) { ... }

    @GET
    public List<Event> getAll() { return eventDao.findAll(); }

    @GET @Path("/{id}")
    public Response getById(@PathParam("id") Long id) { ... }

    @DELETE @Path("/{id}")
    public Response delete(@PathParam("id") Long id) { ... }
}
```

### 3.4 JAX-RS API 端點總表

| 方法 | JAX-RS Endpoint | 說明 |
|------|----------------|------|
| POST | `/rs/day3/bookings/book` | 訂票（樂觀鎖定） |
| GET | `/rs/day3/bookings` | 所有訂單 |
| GET | `/rs/day3/bookings/{id}` | 單筆訂單 |
| GET | `/rs/day3/bookings/event/{eventId}` | 依活動查訂單 |
| GET | `/rs/day3/bookings/concurrency-test` | 併發搶票測試 |
| GET | `/rs/day3/configs` | 系統設定列表 |
| GET | `/rs/day3/configs/{id}` | 依 ID 查設定 |
| GET | `/rs/day3/configs/key/{key}` | 依 Key 查設定 |
| GET | `/rs/day3/configs/cache-demo` | L2 Cache 效能比較 |
| POST | `/rs/day3/configs` | 新增設定 |
| PUT | `/rs/day3/configs/{id}` | 更新設定 |
| DELETE | `/rs/day3/configs/{id}` | 刪除設定 |
| POST | `/rs/day3/events` | 新增活動 |
| GET | `/rs/day3/events` | 活動列表 |
| GET | `/rs/day3/events/{id}` | 依 ID 查活動 |
| DELETE | `/rs/day3/events/{id}` | 刪除活動 |

---

## 4. EclipseLink 快取架構

### 5.1 三層快取

```
                        Application
                            │
                    ┌───────┴───────┐
                    │   L2 Cache    │ ← @Cacheable, 跨 EntityManager 共用
                    │ (Shared Cache)│   存在於 EntityManagerFactory
                    └───────┬───────┘
                            │
              ┌─────────────┼─────────────┐
              │             │             │
        ┌─────┴────┐  ┌────┴────┐  ┌────┴────┐
        │  EM 1    │  │  EM 2   │  │  EM 3   │
        │ L1 Cache │  │ L1 Cache│  │ L1 Cache│
        │ (UnitOfWork)│         │  │         │
        └──────────┘  └─────────┘  └─────────┘
              │             │             │
        ┌─────┴─────────────┴─────────────┴────┐
        │           Database (MySQL)           │
        └──────────────────────────────────────┘
```

| 快取層 | 範圍 | 存活期 | 配置方式 |
|--------|------|--------|---------|
| L1 (UnitOfWork) | 單一 EntityManager | `create()` ~ `close()` | 自動啟用，不可關閉 |
| L2 (Shared Cache) | 整個應用 | 應用啟動 ~ 關閉 | `@Cacheable(true)` 啟用 |

### 5.2 @Cache 配置選項

```java
@Cache(
    expiry = 600000,           // 毫秒，過期後自動重新查詢
    isolation = SHARED,        // SHARED: 跨 EM 共用; PROTECTED: 不共用;
                               // ISOLATED: 獨立; UNDEFINED: 預設
    size = 1000,               // 最大快取數
    alwaysRefresh = false,     // 是否每次都從 DB 重新整理
    disableHits = false        // 是否停用快取命中
)
```

---

## 5. DescriptorCustomizer — EclipseLink 擴充點

```java
@Customizer(TicketDescriptorCustomizer.class)
public class Ticket { ... }

public class TicketDescriptorCustomizer implements DescriptorCustomizer {
    @Override
    public void customize(ClassDescriptor descriptor) {
        // 擴充範例（學生練習用）：
        // descriptor.setHistoryPolicy(historyPolicy);
        // descriptor.setOptimisticLockingPolicy(
        //     new TimestampLockingPolicy());
        // descriptor.getProperties().put("myProp", "value");
    }
}
```

**DescriptorCustomizer 的呼叫時機：** Entity 載入時（Session 初始化階段），在 `persistence.xml` 解析完成後、Entity 被使用前。

---

## 6. 教學練習

### 練習 1：觀察樂觀鎖定

1. 建立一個 Event with totalTickets=3
2. 呼叫 `GET /rs/day3/bookings/concurrency-test`
3. 重複執行 3-5 次，觀察 success/failed 分布
4. 每次結果應不同（取決於執行緒排程）
5. 查詢 TICKETS 表確認只有 3 筆成功

**Console 預期輸出（衝突時）：**
```
FINE: UPDATE TICKETS SET STATUS=?, VERSION=1 WHERE ID=? AND VERSION=0
      bind => ["BOOKED", 1, 0]  -- 成功
FINE: UPDATE TICKETS SET STATUS=?, VERSION=1 WHERE ID=? AND VERSION=0
      bind => ["BOOKED", 2, 0]  -- 成功
FINE: UPDATE TICKETS SET STATUS=?, VERSION=1 WHERE ID=? AND VERSION=0
      bind => ["BOOKED", 3, 0]  -- 成功

-- 第 4、5 個 thread:
WARNING: OptimisticLockException occurred in UnitOfWork
-- 交易回滾，傳回 409 Conflict
```

### 練習 2：觀察 L2 快取

1. 確保 Event 有 `availableTickets >= 1` 以上
2. 呼叫 `POST /rs/day3/configs` 建立一筆設定
3. 呼叫 `GET /rs/day3/configs/cache-demo`
4. 觀察回傳的 `firstCallMicros` vs `secondCallMicros`
5. 修改該設定的值 (`PUT /rs/day3/configs/{id}`)
6. 再次呼叫 cache-demo，觀察第二筆是否仍為舊值？（快取尚未過期，需 `REFRESH` 模式）

### 練習 3：自訂 DescriptorCustomizer

在 `TicketDescriptorCustomizer.customize()` 中加入：

```java
descriptor.setOptimisticLockingPolicy(
    new org.eclipse.persistence.descriptors.
        VersionLockingPolicy("version"));
```

重新編譯部署，觀察行為是否改變。

### 練習 4：交易回滾觀察

在 `bookTicket()` 的 `decrementAvailable` 之後、`commit()` 之前手動加入：

```java
if (true) throw new RuntimeException("rollback test");
```

觀察 Console：
```
FINE: INSERT INTO TICKETS (...) VALUES (...)
FINE: UPDATE EVENTS SET AVAILABLE_TICKETS = ... WHERE ID = ?
WARNING: Transaction rolled back due to exception
```

資料庫中不應有任何新增的 ticket 或扣減的庫存（**交易原子性**）。

### 練習 5：完整訂票流程

1. 用 Postman 建立 Event：`POST /rs/day3/events`
2. 查 Event 列表：`GET /rs/day3/events`
3. 訂票：`POST /rs/day3/bookings/book`
   ```json
   {"eventId": 1, "buyerName": "Alice", "quantity": 1}
   ```
4. 測試併發：`GET /rs/day3/bookings/concurrency-test`
5. 觀察成功的訂單與被拒絕的請求數量

---

## 7. 常見問題

### Q1: `OptimisticLockException` 不是我想的那樣

**原因：** `@Version` 是樂觀鎖定，不是悲觀鎖定。它不阻止並發讀取，只在**提交時**驗證版本。

**解法：**
- 樂觀鎖定適合低衝突場景
- 高衝突場景需使用 `Pessimistic Locking`：
  ```java
  em.find(Entity.class, id, LockModeType.PESSIMISTIC_WRITE);
  ```

### Q2: L2 Cache 更新不及時

**原因：** `SysConfigDao.updateValue()` 使用 `merge()`，會更新 L2 Cache。但如果透過其他機制（如 SQL `UPDATE`直接修改資料庫），L2 Cache 會過時。

**解法：**
- `@Cache(alwaysRefresh = true)` → 每次讀取都檢查資料庫
- `em.refresh(entity)` → 手動刷新
- Cache hint `REFRESH` → 查詢時強制更新

### Q3: L2 Cache 導致測試結果不準確

**建議：** 快取示範時，先用 `DELETE` 或 `BYPASS` hint 確保 clean state。

---

## 8. 程式碼統計

| 層級 | 檔案 | 行數 | 重點 |
|------|------|------|------|
| Entity | `Event.java` | 86 | 庫存管理業務邏輯 |
| Entity | `Ticket.java` | 107 | `@Version` + `@Customizer` |
| Entity | `SysConfig.java` | 77 | `@Cacheable` + `@Cache` |
| DAO | `EventDao.java` | 67 | 基本 CRUD |
| DAO | `TicketDao.java` | 106 | **樂觀鎖定交易** |
| DAO | `SysConfigDao.java` | 96 | **快取 Hint 查詢** |
| JAX-RS | `BookingResource.java` | 113 | 訂票 + 併發測試 (`/rs/day3/bookings`) |
| JAX-RS | `ConfigResource.java` | 99 | 系統設定 + 快取展示 (`/rs/day3/configs`) |
| JAX-RS | `EventResource.java` | 45 | 活動 CRUD (`/rs/day3/events`) |
| Customizer | `TicketDescriptorCustomizer.java` | 11 | EclipseLink 擴充點 |
| **合計** | **7 檔案** | **~595 行** | **最深入 JPA 進階主題的一天** |
