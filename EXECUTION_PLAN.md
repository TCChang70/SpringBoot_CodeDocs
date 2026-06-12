# 作業五：期末專案執行方案

> 本文件根據 `HW5_Ch09E_Final_Project.md` 的需求，逐步說明每個實作與部署步驟。  
> 每個步驟均標明**狀態**、**對應的驗收項目**，以及完整的程式碼或指令。

---

## 目前狀態盤點

| 項目 | 狀態 | 備註 |
|------|------|------|
| `.gitignore` | ⚠️ 不完整 | 缺少 `*.db` 條目 |
| `Dockerfile` | ❌ 不存在 | 需新建 |
| Spring Profiles (dev/prod) | ❌ 未設定 | 需拆分設定檔 |
| PostgreSQL 依賴 | ❌ 未加入 | 需更新 `pom.xml` |
| 功能一：看診完成按鈕 | ✅ 已完成 | `dashboard.html` 已有實作 |
| 功能二：`GET /api/stats` | ❌ 未完成 | 僅有 Thymeleaf `/stats`，缺 REST 端點 |
| `AppointmentRepository` countByStatus | ❌ 缺少 | 需新增查詢方法 |
| `WebConfig` 豁免 `/api/stats` | ❌ 未豁免 | 需加入 `excludePathPatterns` |
| `README.md` | ❌ 不存在 | 需新建 |
| Git Repository | ❌ 未初始化 | 需初始化並推上 GitHub |
| Render 部署 | ❌ 未部署 | 最後執行 |

---

## 步驟一：修正 `.gitignore`

**對應驗收**：G06

`.gitignore` 已存在但缺少 `*.db`，在檔案末尾補上以下內容：

```
# SQLite 資料庫檔案（絕對不能推上去）
*.db

# macOS
.DS_Store
```

**確認重點**：`target/`、`.idea/`、`*.db` 三項都必須存在。

---

## 步驟二：設定 Spring Profiles（dev / prod）

**對應驗收**：作業 Part A 部署前提條件

### 2-1. 修改 `application.properties`（主設定檔）

將現有的 `application.properties` 清空並改為只定義共用設定：

```properties
# ===============================
# 預設啟用 dev Profile
# ===============================
spring.profiles.active=dev

# ===============================
# 共用設定（不分環境）
# ===============================
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.format_sql=true
spring.sql.init.encoding=UTF-8
spring.jpa.defer-datasource-initialization=true
spring.sql.init.mode=always
```

### 2-2. 新建 `application-dev.properties`

在 `src/main/resources/` 建立此檔，對應本機開發環境（SQLite）：

```properties
# ===============================
# dev Profile：本機 SQLite
# ===============================
spring.datasource.driver-class-name=org.sqlite.JDBC
spring.datasource.url=jdbc:sqlite:miniclinic.db
spring.jpa.database-platform=org.hibernate.community.dialect.SQLiteDialect
spring.jpa.hibernate.ddl-auto=update
```

### 2-3. 新建 `application-prod.properties`

在 `src/main/resources/` 建立此檔，對應 Render 雲端環境（PostgreSQL）：

```properties
# ===============================
# prod Profile：Render PostgreSQL
# 實際值由 Render 環境變數注入
# ===============================
spring.datasource.url=${DATABASE_URL}
spring.datasource.username=${SPRING_DATASOURCE_USERNAME}
spring.datasource.password=${SPRING_DATASOURCE_PASSWORD}
spring.jpa.database-platform=org.hibernate.dialect.PostgreSQLDialect
spring.jpa.hibernate.ddl-auto=update
spring.sql.init.mode=never
```

> `spring.sql.init.mode=never`：Render 上不再執行 `data.sql`，避免每次重啟時重複插入資料。

---

## 步驟三：在 `pom.xml` 加入 PostgreSQL 驅動

**對應驗收**：prod Profile 能正常連線 PostgreSQL

在 `pom.xml` 的 `<dependencies>` 區塊內，`sqlite-jdbc` 之後加入：

```xml
<!-- PostgreSQL JDBC 驅動（prod 環境使用） -->
<dependency>
    <groupId>org.postgresql</groupId>
    <artifactId>postgresql</artifactId>
    <scope>runtime</scope>
</dependency>
```

---

## 步驟四：確認功能一（看診完成按鈕）✅ 已完成

**對應驗收**：T07（`byStatus.COMPLETED ≥ 1`）的前提操作

`dashboard.html` 已包含以下關鍵程式碼，**不需要修改**：

```html
<!-- 只在 BOOKED 狀態顯示「看診完成」按鈕 -->
<button th:if="${appt.status == 'BOOKED'}"
        th:data-id="${appt.apptId}"
        class="btn-complete"
        onclick="completeAppointment(this)">看診完成</button>
```

確認 `dashboard.html` 的 `<script>` 區塊中有 `completeAppointment()` 函式，若尚未加入，新增至 `cancelAppointment()` 函式之後：

```javascript
// 處理「看診完成」的邏輯
function completeAppointment(button) {
    const apptId = button.getAttribute('data-id');
    if (confirm('確定要將此掛號標記為看診完成嗎？')) {
        fetch(`/api/appointments/${apptId}/status`, {
            method: 'PUT',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ status: 'COMPLETED' })
        })
        .then(response => {
            if (response.ok) location.reload();
            else alert('操作失敗，請稍後再試。');
        });
    }
}
```

---

## 步驟五：實作功能二（`GET /api/stats`）

**對應驗收**：T05、T06、T07

### 5-1. 在 `AppointmentRepository` 新增計數方法

在 `AppointmentRepository.java` 介面中，加入依狀態計數的方法：

```java
// 依掛號狀態計算筆數（供 /api/stats 使用）
long countByStatus(String status);
```

### 5-2. 在 `StatsController` 新增 REST 端點

`StatsController.java` 現有的 `/stats`（Thymeleaf 頁面）**保留不動**，僅新增以下方法（在類別最上方補 `@ResponseBody` 相關 import）：

```java
import org.springframework.web.bind.annotation.ResponseBody;
import java.util.HashMap;
```

在 `showStats()` 方法之後新增：

```java
@GetMapping("/api/stats")
@ResponseBody
public Map<String, Object> getStats() {
    Map<String, Object> result = new LinkedHashMap<>();
    result.put("totalDoctors",       doctorRepo.count());
    result.put("totalPatients",      patientRepo.count());
    result.put("totalAppointments",  appointmentRepo.count());

    Map<String, Long> byStatus = new LinkedHashMap<>();
    byStatus.put("BOOKED",    appointmentRepo.countByStatus("BOOKED"));
    byStatus.put("COMPLETED", appointmentRepo.countByStatus("COMPLETED"));
    byStatus.put("CANCELLED", appointmentRepo.countByStatus("CANCELLED"));
    result.put("byStatus", byStatus);

    return result;
}
```

> **注意**：`@Controller` 類別中要讓某個方法回傳 JSON，必須加上 `@ResponseBody`。  
> 或者，也可以將 `StatsController` 的類別層級註解從 `@Controller` 改為 `@RestController`，  
> 但此時 `showStats()` 的回傳值 `"stats"` 將被視為字串而非 view name，需另行處理。  
> **建議方案**：保留 `@Controller`，只在新方法上加 `@ResponseBody`。

### 5-3. 在 `WebConfig` 豁免 `/api/stats`

`/api/stats` 不需要登入即可呼叫，需將它排除在攔截器之外。  
編輯 `WebConfig.java` 的 `addInterceptors()` 方法：

```java
.excludePathPatterns(
    "/login",
    "/logout",
    "/api/stats",          // ← 新增此行：統計端點不需要認證
    "/api/health",         // ← 新增此行：健康檢查也不需要認證
    "/api/doctors",        // ← 新增此行：醫師清單不需要認證（T02/T03）
    "/api/doctors/**"      // ← 新增此行：醫師詳情不需要認證
);
```

---

## 步驟六：建立 `Dockerfile`

**對應驗收**：G05

在專案**根目錄**（與 `pom.xml` 同層）建立 `Dockerfile`，內容如下：

```dockerfile
# =============================================
# Stage 1：使用 Maven 編譯專案，產出 JAR 檔
# =============================================
FROM maven:3.9-eclipse-temurin-17-alpine AS builder

WORKDIR /app

# 先複製 pom.xml 讓 Docker 快取 Maven 依賴（加速後續建構）
COPY pom.xml .
RUN mvn dependency:go-offline -B

# 再複製完整原始碼並編譯（跳過測試以縮短建構時間）
COPY src ./src
RUN mvn package -DskipTests -B

# =============================================
# Stage 2：只帶 JAR 到精簡的 JRE 執行環境
# =============================================
FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

# 從 Stage 1 複製編譯好的 JAR（版本號用萬用字元處理）
COPY --from=builder /app/target/*.jar app.jar

# 容器啟動時執行 JAR，並明確指定使用 prod Profile
ENTRYPOINT ["java", "-Dspring.profiles.active=prod", "-jar", "app.jar"]

# 宣告服務使用的 port（Render 預設讀取此值）
EXPOSE 8080
```

---

## 步驟七：建立 `README.md`

**對應驗收**：Part A 要求

在專案根目錄建立 `README.md`，內容範本如下（請填入你自己的資訊）：

```markdown
# MiniClinic 迷你診所系統

一個以 Spring Boot 實作的輕量診所管理系統，支援醫師登入、病患管理、掛號與看診流程。

## 線上 Demo

> https://miniclinic-你的帳號.onrender.com

## 技術棧

- **後端**：Spring Boot 4.x、Spring Data JPA、Thymeleaf
- **本機資料庫**：SQLite
- **雲端資料庫**：PostgreSQL（Render）
- **容器化**：Docker（multi-stage build）
- **部署平台**：Render

## 本機執行步驟

1. 確保已安裝 Java 17+ 與 Maven 3.9+
2. Clone 專案：
   ```bash
   git clone https://github.com/你的帳號/miniclinic.git
   cd miniclinic
   ```
3. 啟動（預設使用 dev Profile，自動建立 SQLite 資料庫）：
   ```bash
   ./mvnw spring-boot:run
   ```
4. 開啟瀏覽器訪問 `http://localhost:8080`

## 預設帳密

| 帳號（doctorId） | 密碼 |
|-----------------|------|
| D001            | pass1234 |
| D002            | pass1234 |

## API 端點

| 方法 | 路徑 | 說明 | 需登入 |
|------|------|------|--------|
| GET  | `/api/health` | 服務健康檢查 | 否 |
| GET  | `/api/doctors` | 醫師清單 | 否 |
| GET  | `/api/stats` | 系統統計摘要 | 否 |
| GET  | `/api/appointments` | 掛號列表 | 是 |
| PUT  | `/api/appointments/{id}/status` | 更新掛號狀態 | 是 |
```

---

## 步驟八：Git 初始化與推上 GitHub

**對應驗收**：G01、G02、G03、G04

### 8-1. 在 GitHub 建立 Repository

1. 前往 https://github.com/new
2. Repository name：`miniclinic`
3. 設定為 **Public**
4. **不要**勾選 "Add a README file"（本機已有）
5. 點擊 "Create repository"

### 8-2. 本機初始化 Git 並推送

在 VS Code 終端機（PowerShell）依序執行：

```powershell
# 移至專案根目錄
cd "d:\Source\Clinic_Exercise\exercise"

# 初始化 Git
git init

# 設定你的身份（若尚未設定）
git config user.name "你的姓名"
git config user.email "你的Email"

# 第一筆 commit：初始化 + gitignore
git add .gitignore
git commit -m "[NO-AI] 初始化 Git repository

建立 .gitignore，排除 target/、miniclinic.db、.idea/"

# 第二筆 commit：專案基礎架構
git add pom.xml src/main/resources/application*.properties Dockerfile
git commit -m "[NO-AI] 新增 Dockerfile 與 Spring Profiles 設定

- 建立 multi-stage Dockerfile（maven:3.9-eclipse-temurin-17-alpine 編譯）
- 拆分 application.properties 為 dev（SQLite）與 prod（PostgreSQL）兩個 Profile"

# 第三筆 commit：後端功能（Repository + Controller）
git add src/main/java/
git commit -m "[AI-USED] 實作 GET /api/stats 統計端點

問AI：Spring Data JPA 如何依照 enum 欄位分組計算數量？
AI建議：使用 countByStatus(String status) 方法，Spring Data 會自動產生 WHERE status = ? 的查詢
我的修改：採用 countByStatus 方式，並在 Controller 中組裝 byStatus Map，
          回傳格式符合驗收規格（totalDoctors/totalPatients/totalAppointments/byStatus）"

# 第四筆 commit：前端功能
git add src/main/resources/templates/dashboard.html
git commit -m "[AI-USED] 新增看診完成按鈕（dashboard.html）

問AI：Thymeleaf 如何在表格依狀態顯示不同按鈕？fetch 呼叫 PUT API 後如何重新整理頁面？
AI建議：使用 th:if=\"\${appt.status == 'BOOKED'}\" 控制按鈕顯示，fetch 成功後呼叫 location.reload()
我的修改：依照 AI 建議實作，並加入 confirm() 確認提示避免誤點，
          完成/取消按鈕共用相同的資料流但傳送不同的 status 值"

# 第五筆 commit：文件
git add README.md
git commit -m "[NO-AI] 新增 README.md

包含專案介紹、線上 Demo 網址、技術棧、本機執行步驟、預設帳密"

# 連結 GitHub 遠端（替換為你的 GitHub 帳號）
git remote add origin https://github.com/你的帳號/miniclinic.git

# 推送到 GitHub
git branch -M main
git push -u origin main
```

### 8-3. 確認 commit 數量與格式

```powershell
# 確認至少 5 筆 commit
git log --oneline

# 確認所有 commit 都以 [NO-AI] 或 [AI-USED] 開頭
git log --format="%s"
```

---

## 步驟九：Render 部署

**對應驗收**：T01~T07 全部

### 9-1. 建立 PostgreSQL 資料庫

1. 登入 https://render.com
2. 點擊 **New → PostgreSQL**
3. 設定：
   - Name：`miniclinic-db`
   - Region：選擇離台灣最近（如 `Singapore`）
   - Plan：**Free**
4. 建立後，複製以下資訊備用：
   - **Internal Database URL**（供 Web Service 使用，格式：`postgresql://user:password@host/dbname`）
   - **Username**
   - **Password**

### 9-2. 建立 Web Service

1. 點擊 **New → Web Service**
2. 選擇 **Connect a GitHub repository**，授權並選取 `miniclinic`
3. 設定：
   - Name：`miniclinic-你的帳號`
   - Region：與資料庫相同
   - Branch：`main`
   - **Runtime：Docker**（Render 會自動偵測 Dockerfile）
   - Plan：**Free**
4. 展開 **Environment Variables**，新增以下四個變數：

   | Key | Value |
   |-----|-------|
   | `SPRING_PROFILES_ACTIVE` | `prod` |
   | `DATABASE_URL` | jdbc:postgresql://host_address/miniclinic |
   | `SPRING_DATASOURCE_USERNAME` | sa |
   | `SPRING_DATASOURCE_PASSWORD` | sa |

5. 點擊 **Create Web Service**
6. 等待部署完成（約 5～10 分鐘，可在 Logs 頁面監看）

### 9-3. 確認部署成功

部署完成後，在瀏覽器測試：

```
GET https://miniclinic-你的帳號.onrender.com/api/health
→ 期望回應：{"status":"ok"}

GET https://miniclinic-你的帳號.onrender.com/api/stats
→ 期望回應：{"totalDoctors":5,"totalPatients":3,...}

GET https://miniclinic-你的帳號.onrender.com/api/doctors
→ 期望回應：JSON 陣列，長度 ≥ 5
```

> **免費方案冬眠說明**：Render 免費 Web Service 閒置 15 分鐘後會自動進入休眠，  
> 下次請求需等待約 30 秒喚醒。繳交前先手動訪問一次確保已喚醒。

---

## 步驟十：完成「看診完成」功能並更新資料（T07 聯動）

**對應驗收**：T07（`byStatus.COMPLETED ≥ 1`）

部署完成後，**必須實際操作**才能通過 T07：

1. 開啟 Render 上的服務 URL
2. 以醫師帳號登入（`D001` / `pass1234`）
3. 前往 Dashboard，點擊其中一筆掛號的「**看診完成**」按鈕
4. 確認狀態變更為 `COMPLETED`（文字變綠色）
5. 訪問 `/api/stats` 確認 `byStatus.COMPLETED` ≥ 1

---

## 步驟十一：繳交

**繳交兩個網址**至教學平台：

1. **GitHub Repository URL**：
   ```
   https://github.com/你的帳號/miniclinic
   ```

2. **Render 部署 URL**：
   ```
   https://miniclinic-你的帳號.onrender.com
   ```

---

## 驗收清單（自我檢查）

### Part A 基本部署

- [ ] `.gitignore` 包含 `target/`、`*.db`、`.idea/`
- [ ] `Dockerfile` 存在於專案根目錄（multi-stage build）
- [ ] GitHub 有公開的 `miniclinic` repository
- [ ] `git log --oneline` 輸出 ≥ 5 筆 commit
- [ ] 所有 commit message 以 `[NO-AI]` 或 `[AI-USED]` 開頭
- [ ] `[AI-USED]` commit 至少 2 筆
- [ ] Spring dev Profile 使用 SQLite，prod Profile 使用 PostgreSQL
- [ ] Render 服務公開可訪問
- [ ] `README.md` 包含完整內容（介紹、Demo 網址、技術棧、執行步驟、預設帳密）

### Part B 新功能

- [ ] Dashboard 顯示「看診完成」按鈕（僅 BOOKED 狀態）
- [ ] 點擊後狀態變更為 COMPLETED，頁面自動重新整理
- [ ] `GET /api/stats` 回傳正確 JSON 格式
- [ ] `byStatus.COMPLETED ≥ 1`（已實際點過按鈕）

### HTTP 驗收（T 序列）

- [ ] T01：`GET /api/health` → HTTP 200，含 `"status":"ok"`
- [ ] T02：`GET /api/doctors` → HTTP 200，陣列長度 ≥ 5
- [ ] T03：每筆醫師含 `doctorId`、`name`、`department`、`specialty`
- [ ] T04：`GET /dashboard` 未登入 → 302 重導到 `/login`
- [ ] T05：`GET /api/stats` → HTTP 200
- [ ] T06：`GET /api/stats` → 含所有必要欄位
- [ ] T07：`totalDoctors≥5`、`totalPatients≥3`、`totalAppointments≥3`、`BOOKED≥1`、`COMPLETED≥1`、`CANCELLED≥1`

### GitHub 驗收（G 序列）

- [ ] G01：GitHub URL 公開可存取
- [ ] G02：commit 數量 ≥ 5
- [ ] G03：所有 commit 以 `[NO-AI]` 或 `[AI-USED]` 開頭
- [ ] G04：`[AI-USED]` commit ≥ 2 筆（含三欄詳述）
- [ ] G05：`Dockerfile` 存在於 repo 根目錄
- [ ] G06：`.gitignore` 存在且包含 `target/`
