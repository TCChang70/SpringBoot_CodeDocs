# 前後端整合講義 — online-exam 全端專案

> 簡報 / 講義版（每個區塊 = 一頁投影片，可直接投影或列印）
> 主題：**React 前端 × Spring Boot 後端 如何串成一整套系統**
> 對象：有基礎的學習者

---

## 目錄（投影片一覽）

| # | 主題 |
|---|------|
| 1 | 系統全貌：一張圖 |
| 2 | 啟動流程與 Proxy |
| 3 | 前後端「契約」 |
| 4 | 登入流程（全程） |
| 5 | Token 與身分 |
| 6 | 職責分層 |
| 7 | 功能追蹤 A：建測驗＋匯入 |
| 8 | 功能追蹤 B：作答＋計分 |
| 9 | 權限兩道關 |
| 10 | 錯誤處理合作 |
| 11 | 常見陷阱 |
| 12 | 整合路線圖 |
| 13 | 動手練習 |

---

## 投影片 1 — 系統全貌

```
┌── 前端 (React+Vite) ─────┐   HTTP+JSON    ┌─ 後端 (Spring Boot) ──────┐
│  examApi.js  所有 fetch  │ ────────────▶ │  Controller→Service→Repo  │
│  AuthContext 保管 token  │      /api      │     → SQLite 資料庫       │
│  頁面 Pages              │ ◀──────────── │  Security + JWT 驗身分     │
└──────────────────────────┘               └───────────────────────────┘
```

**三大關鍵：**
1. 前端**只靠 HTTP + JSON** 跟後端講話，絕不碰資料庫。
2. 溝通靠「**約定的網址 + 網定的欄位**」。
3. 身分靠 **JWT 通行證**。

---

## 投影片 2 — 啟動流程與 Proxy

| 端 | 指令 | 網址 |
|----|------|------|
| 後端 | `mvn spring-boot:run` (Java 21) | `localhost:8080` |
| 前端 | `npm install && npm run dev` | `localhost:5173` |

登入測試帳號：教師 `teacher/password123`、學生 `student1/password123`

**為什麼前端寫 `/api` 就好？**
```js
// vite.config.js
proxy: { '/api': { target: 'http://localhost:8080', changeOrigin: true } }
```
→ 前端打 `/api/exams`，Vite 自動轉成 `localhost:8080/api/exams`，**就不會撞 CORS**。

> 一句話：**兩端不同 origin，靠 proxy 橋接；前端永遠寫相對路徑。**

---

## 投影片 3 — 前後端「契約」

**網址 = 名詞 + 動詞 (HTTP)**

| 動作 | 網址 | 誰能用 |
|------|------|--------|
| 列出開放測驗 | `GET /api/exams` | 學生/教師 |
| 列出全部 | `GET /api/exams/all` | 教師 |
| 批次匯入題目 | `POST /api/exams/{id}/questions/batch` | 教師 |
| 開放/關閉 | `PATCH /api/exams/{id}/status` | 教師 |
| 提交作答 | `POST /api/exams/{id}/submit` | 學生 |

**DTO 就是契約書**（後端定義欄位，前端照讀）：

```java
record ExamSummaryResponse(Long id, String title, String description,
    Integer timeLimit, int questionCount, boolean active) {}
```

```json
{ "id": 1, "title": "Java 基礎", "questionCount": 5, "active": true }
```

> ⚠ 前端讀的欄位名，必須跟後端 DTO 完全一致，否則拿到 `undefined`。

---

## 投影片 4 — 登入流程（全程）

```
登入頁 → examApi.login → POST /api/auth/login
      → AuthService.login
         ① AuthenticationManager 驗帳密
         ② jwtUtil.generateToken → JWT
      → 回傳 { token, username, role, ... }
      → AuthContext.login() → 存 localStorage
      → navigate(依 role 導向 /teacher 或 /student)
```

**JWT 白話：** 一張「有簽名的通行證」，裡面藏 username。後端用密鑰簽發與驗證，前端每次帶上，後端就知道「你是誰、沒被竄改」。

> ❗ **密鑰要 ≥ 32 字元**，否則 `hmacShaKeyFor` 會炸。

---

## 投影片 5 — Token 與身分

**前端每次請求都帶：**
```js
function authHeader(token) {
  return { Authorization: `Bearer ${token}`, 'Content-Type': 'application/json' }
}
```

**後端每請求先過濾：** `JwtFilter`
```
拿到 Authorization: Bearer xxx
→ 驗證有效 → 取出 username → 放入 SecurityContext → 放行
```

**前端路由守門員：** `ProtectedRoute`
```
沒登入        → Navigate /login
角色不符      → Navigate 回自己該去的頁
有權限        → 顯示巢狀子頁 <Outlet />
```

> 🔑 **記住這句話：** 前端守「畫面」，後端守「資料」。

---

## 投影片 6 — 職責分層心法

```
前端                    後端
Page ← │ Controller  收請求/回傳
api ──▶│ Service     商業邏輯
Context│ Repository  存取資料
       └
```

| 層 | 只負責 | 範例 |
|----|--------|------|
| Controller | 收請求、轉交、回覆 | `ExamController` |
| Service | 算、檢查、交易 | `ExamService`、`ResultService` |
| Repository | 存取資料（方法名生 SQL） | `ExamRepository` |
| DTO | 傳輸的形狀 | `dto/` |

> 💡 **口訣：** 要存→Repo；要算/檢查→Service；要收發 HTTP→Controller。邏輯別放 Controller。

---

## 投影片 7 — 功能追蹤 A：建測驗＋匯入題目

```
ExamFormPage ─POST→ /api/exams ─→ ExamController.createExam
                                   ↓ @PreAuthorize hasRole('TEACHER')
                                   ExamService.createExam → save
```

**批次匯入（前端解析 CSV → 後端一次存多題）：**
```js
fetch('/api/exams/' + examId + '/questions/batch', {
  method: 'POST', headers: authHeader(token),
  body: JSON.stringify({ questions })
})
```
後端 `ExamService.batchImportQuestions`：迴圈 `save` 每一題，回傳新增清單。

**改資料必守兩道關：**
1. **角色關** `@PreAuthorize` — 你是不是教師
2. **所有權關** `checkOwnership` — 是不是你建的測驗

> ⚠ 前端有「編輯按鈕」不代表能改；**把關必須在後端**（人人都可繞過前端直接打 API）。

---

## 投影片 8 — 功能追蹤 B：作答＋計分

**前端一題一頁（TakeExamPage）：**
```
選項 → answers[qId] = opt（state）
「檢查並提交」→ phase = review（複習頁）
確認 → submitExam(id, { answers })
```
`answers = { "1":"A", "2":"C", ... }`（以題目 id 為 key）

**後端自動計分（ResultService.submitExam）：**
```
總分 = Σ 每題 points
答對（correctAnswer == submitted）→ score += points
```
- **正確答案只有後端有**：學生端 `/take` 回應刻意不含正確答案，防作弊。
- **不可重複作答**：`existsByUserAndExam` 擋掉。

> 🔢 計分是「商業規則」→ 一定放 **Service**，不放前端。

---

## 投影片 9 — 權限兩道關

| 關卡 | 位置 | 擋什麼 |
|------|------|--------|
| 路由守門員 | 前端 `ProtectedRoute` | 看不到頁面 |
| 方法權限 | 後端 `@PreAuthorize` | 不能呼叫 API |
| 所有權 | 後端 `checkOwnership` | 不能動別人的資料 |

**角色如何對上：** `UserDetailsServiceImpl`
```java
.roles(user.getRole().replace("ROLE_", ""))   // ROLE_TEACHER → TEACHER
```
`hasRole('TEACHER')` 內部比 `ROLE_TEACHER`。

> ❗ 存成 `TEACHER`（少了 ROLE_）→ 一直 403。

---

## 投影片 10 — 錯誤處理兩端合作

**後端統一包錯誤**（`GlobalExceptionHandler`）：
```json
{ "message": "該動作僅限教師", "fieldErrors": { "username": "帳號已存在" } }
```

**前端統一讀錯誤**（`examApi.js` 的 `handle`）：
```js
if (!res.ok) throw new Error(data.message || `HTTP ${res.status}`)
```

**頁面統一顯示**：`.catch(err => setError(err.message))`

> ✅ 錯誤訊息「後端寫一次」，前端所有頁面共用同一套顯示邏輯。

---

## 投影片 11 — 常見陷阱

| 症狀 | 原因 | 解法 |
|------|------|------|
| CORS 錯誤 | 直打 8080，沒走 proxy | 統一用 `/api` |
| 一直 401 | 沒帶 token / 過期 / 密鑰短 | 檢查 Bearer + 密鑰長度 |
| 一直 403 | 角色不符或所有權不足 | 查 `@PreAuthorize`、`ROLE_`、所有權 |
| 欄位 `undefined` | 欄位名對不上 DTO | 對照 record DTO |
| 改了沒反應 | 沒重新 fetch | 檢查 `load()`/`reload()` |

**除錯 SOP：**
1. Postman 直打後端，確認「後端單獨」OK
2. 瀏覽器 DevTools → Network：檢查 網址/method/header/body
3. 後端 console（`show-sql=true`）看 SQL

---

## 投影片 12 — 整合路線圖

| 階段 | 打通什麼 | 里程碑 |
|------|---------|--------|
| A | 前端→後端最小請求 | 一個請求從點擊到畫面的完整路徑 |
| B | 登入＋身分 | 自己能加「登入才可看」的新頁面 |
| C | CRUD＋權限 | 能加一支 API 並讓前端呼叫 |
| D | 複雜流程＋交易 | 加新流程時正確分層 |

> 路徑：**登入 → 建測驗 → 匯入題目 → 作答 → 看成績**，每段都追到底。

---

## 投影片 13 — 動手練習

**① 追蹤題（Easy）**：「學生點測驗→看到題目」經過哪些檔案/API？
> 答：`StudentDashboard → getActiveExams → GET /api/exams → ExamController.getActiveExams → ExamService → 回傳 → render`

**② 加題數上限（Medium）** 在 `ExamService.addQuestion`：
```java
if (questionRepository.countByExam(exam) >= 50)
    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "此測驗最多 50 題");
```

**③ 打通新功能（Hard）**「查看單測驗答題明細」：
- 後端：加 DTO + Service（回傳每題對錯）
- 後端：加 `GET /api/results/exam/{id}/detail` + 只准看自己的
- 前端：`examApi.js` 加 function + `MyResultsPage` 加「查看明細」

---

## 收尾語

> **全端整合 = 分層 × 契約 × 權限。**
> 前端管畫面、後端管資料、安全在後端、錯誤兩端合作。
> 沿著「登入→建測驗→匯入→作答→看成績」一條線追到底，你就會「整合」了。

🔗 延伸閱讀：`online-exam-api/docs/05-java-backend-learning.md`、`online-exam-frontend/docs/react-learning.md`、`docs/fullstack-integration-learning.md`
