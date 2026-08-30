# 前後端整合學習文件 — online-exam 全端專案

> 以本專案 (`online-exam-api` + `online-exam-frontend`) 為教材，教你**前端 (React) 與後端 (Spring Boot) 如何串接**。
> 這份文件**不重新教單獨的前端或後端語法**，而是把兩者當成「同一套系統的兩半」，帶著你一筆一筆請求從瀏覽器追到 SQL、再從 SQL 回到畫面。
>
> 目標讀者：**已具備基礎**（看過前面的 `java-backend-learning.md` 或 `react-learning.md`），想學「一套完整的全端功能」怎麼拼起來。

---

## 目錄

1. [這套系統長什麼樣：一張圖看懂](#1-這套系統長什麼樣一張圖看懂)
2. [開發環境與啟動流程](#2-開發環境與啟動流程)
3. [API 設計：前後端如何「約定」](#3-api-設計前後端如何約定)
4. [第一次串接：登入流程（從表單到 JWT）](#4-第一次串接登入流程從表單到-jwt)
5. [Token 怎麼帶著走：前端身分與受保護路由](#5-token-怎麼帶著走前端身分與受保護路由)
6. [職責分層：每一層「只做一件事」](#6-職責分層每一層只做一件事)
7. [一次完整功能追蹤：教師建立測驗並批次匯入題目](#7-一次完整功能追蹤教師建立測驗並批次匯入題目)
8. [一次完整功能追蹤：學生作答並看成績](#8-一次完整功能追蹤學生作答並看成績)
9. [角色權限：教師/學生怎麼把關](#9-角色權限教師學生怎麼把關)
10. [錯誤處理的兩端合作](#10-錯誤處理的兩端合作)
11. [常見陷阱與除錯技巧](#11-常見陷阱與除錯技巧)
12. [整合學習路線圖](#12-整合學習路線圖)
13. [動手練習](#13-動手練習)
14. [總結](#14-總結)

---

## 1. 這套系統長什麼樣：一張圖看懂

```
┌───────────────────────────┐        HTTP + JSON        ┌──────────────────────────────┐
│  前端 (React + Vite)      │  ───────────────────────▶ │  後端 (Spring Boot)          │
│                           │  request （全部走 /api）   │                              │
│  src/api/examApi.js       │ ◀───────────────────────  │   Controller → Service →     │
│     所有 fetch 都在這      │        response           │   Repository → SQLite 資料庫  │
│  src/context/AuthContext  │                           │                              │
│     保管 token             │                          │   Security + JWT 驗身分       │
└───────────────────────────┘                           └──────────────────────────────┘
```

**三大關鍵事實：**

1. **前端永遠只透過 HTTP API 對話**，它不會直接碰資料庫。所有 `fetch` 收在 **`src/api/examApi.js`** 一支檔案裡。
2. **前後端用「約定好的網址 + JSON 格式」溝通**。網址長得像 `/api/exams`，資料格式是 JSON。
3. **身分靠 JWT**：登入拿到一張「通行證」（token），之後每次請求都帶著它，後端才能認得你是誰、你有沒有權限。

> ❗ 開發時前端跑在 `http://localhost:5173`，後端跑在 `http://localhost:8080`，這是**兩個不同的 origin**（跨域 CORS）。靠 Vite 的 `server.proxy` 把 `/api` 轉送到後端，前端寫的還是相對網址 `/api/...`，就不會撞到 CORS。

---

## 2. 開發環境與啟動流程

| 步驟 | 指令 | 說明 |
|------|------|------|
| 啟後端 | `cd online-exam-api && mvn spring-boot:run` | 用 Java 21，監聽 `8080` |
| 啟前端 | `cd online-exam-frontend && npm install && npm run dev` | 用 Vite，監聽 `5173` |
| 登入測試帳號 | 教師 `teacher/password123` 學生 `student1/password123` | 由 `DataInitializer.java` 產生 |

前端 `vite.config.js` 的 proxy：

```js
proxy: {
  '/api': {
    target: 'http://localhost:8080',
    changeOrigin: true,
  },
},
```

當你呼叫 `fetch('/api/exams')`，Vite 開發伺服器會把它轉成 `http://localhost:8080/api/exams`。你也隨時可以用瀏覽器或 Postman 直接打後端網址來測試 API。

> 🔹 前端在**生產環境與開發環境其實是同一支程式**，只是開發時多了 proxy 幫你轉址。理解這點，就懂「前端程式碼裡為什麼寫相對路徑」。

---

## 3. API 設計：前後端如何「約定」

### 3.1 網址是「名詞 + 動詞」

| 動詞 (HTTP) | 網址 | 用途 | 權限 |
|------------|------|------|------|
| GET | `/api/exams` | 列出**開放中**的測驗 | 學生/教師 |
| GET | `/api/exams/all` | 列出**全部**測驗 | 教師 |
| GET | `/api/exams/{id}/take` | 取得某測驗的作答題目（不含答案） | 學生 |
| POST | `/api/exams/{id}/submit` | 提交作答 | 學生 |
| POST | `/api/exams` | 建立測驗 | 教師 |
| PATCH | `/api/exams/{id}/status` | 開放 / 關閉測驗 | 教師 |
| POST | `/api/exams/{examId}/questions/batch` | 批次匯入題目 | 教師 |
| GET | `/api/students` | 列出學生 | 教師 |
| GET | `/api/teachers` | 列出教師 | 教師 |

### 3.2 DTO：前後端「契約書」

後端每個 API 的回傳都用 **record DTO** 定義好欄位，前端就照這個結構取值。例如：

```java
// ExamSummaryResponse.java
public record ExamSummaryResponse(
    Long id, String title, String description,
    Integer timeLimit, int questionCount, boolean active) {}
```

回傳的 JSON：

```json
{
  "id": 1,
  "title": "Java 基礎概念測驗",
  "questionCount": 5,
  "timeLimit": 30,
  "active": true
}
```

前端 `StudentDashboard.jsx` 就照這個結構渲染 `exam.title`、`exam.questionCount`、`exam.timeLimit`。

> 💡 **「契約」雙向都要守**：後端定義了什麼欄位，前端就讀什麼；前端要送什麼，後端 DTO 就收什麼。改一個就要改另一個，否則就會拿到 `undefined` 或出錯。

---

## 4. 第一次串接：登入流程（從表單到 JWT）

這是全端最重要的一條線，把「前端表單」→「後端認證」→「token 回傳」→「前端保存」串起來。

### 4.1 前端送出登入請求

`LoginPage.jsx` 的使用者按下「登入」→ 呼叫 `examApi.js`：

```js
// examApi.js
export const login = (body) =>
  fetch(`${BASE}/auth/login`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(body),
  }).then(handle)
```

其中 `BASE = '/api'`，所以實際打到 `/api/auth/login`。

### 4.2 後端接收與驗證

`AuthController.java` 收到 `/api/auth/login` → 交給 `AuthService.login()`：

```java
public LoginResponse login(LoginRequest req) {
    authenticationManager.authenticate(                       // 1. 驗證帳號密碼
            new UsernamePasswordAuthenticationToken(req.username(), req.password()));
    User user = userRepository.findByUsername(req.username()).orElseThrow();
    String token = jwtUtil.generateToken(user.getUsername());  // 2. 產出 JWT
    return new LoginResponse(token, user.getUsername(),         // 3. 回傳 token + 使用者資料
            user.getRole(), user.getDisplayName(), user.getClassName());
}
```

### 4.3 前端保存 token 並導向

```js
// LoginPage.jsx
const data = await login(form)          // 拿到 { token, username, role, ... }
authLogin(data)                         // 存進 localStorage
navigate(data.role === 'ROLE_TEACHER' ? '/teacher' : '/student')
```

`AuthContext.login()` 把資料寫進 `localStorage`：

```js
function login(data) {
  const payload = { token: data.token, username: data.username, role: data.role,
                    displayName: data.displayName, className: data.className }
  localStorage.setItem('exam_auth', JSON.stringify(payload))
  setAuth(payload)
}
```

### 4.4 這條流程的教學重點

| 階段 | 知識點 |
|------|--------|
| 送出 | `fetch` + `JSON.stringify` + `Content-Type` 三件套 |
| 驗證 | `AuthenticationManager` 內部呼叫 `UserDetailsServiceImpl`（見下） |
| 產 token | `JwtUtil.generateToken()` 把 username 簽進 JWT |
| 保存 | `localStorage` + `Context` 讓全 App 讀得到 |
| 導向 | 依 `role` 決定進哪位角色的頁面 |

**JWT 的內容（白話）：** 一張「帶著簽名的通行證」。後端用密鑰簽名，前端拿到的 token 裡藏著 username；之後每次請求帶給後端，後端用同一把鑰匙解開驗證，就知道「這是誰、沒被竄改」。

> ❌ **常見錯誤：密鑰太短。** JwtUtil 註解說密鑰必須 ≥ 32 個字元（256 bits），否則 `Keys.hmacShaKeyFor` 會拋例外。後端 `application.properties` 已用 `APP_JWT_SECRET` 環境變數覆寫，記得要夠長。

---

## 5. Token 怎麼帶著走：前端身分與受保護路由

### 5.1 每個請求都帶 `Authorization` Header

`examApi.js` 的 `authHeader`：

```js
function authHeader(token) {
  return { Authorization: `Bearer ${token}`, 'Content-Type': 'application/json' }
}
```

取測驗列表時把 token 帶上：

```js
export const getAllExams = (token) =>
  fetch(`${BASE}/exams/all`, { headers: authHeader(token) }).then(handle)
```

### 5.2 後端怎麼讀 token

`JwtFilter.java` 是 Spring Security 的「過濾器」：每個請求進到 Controller 前，先檢查 `Authorization: Bearer xxx`。若 token 有效，就把該使用者的身分放進 `SecurityContext`，之後 `@PreAuthorize` 才能判斷權限。

```java
String authHeader = request.getHeader("Authorization");
if (authHeader != null && authHeader.startsWith("Bearer ")) {
    String token = authHeader.substring(7);
    if (jwtUtil.validateToken(token)) {
        String username = jwtUtil.extractUsername(token);
        UserDetails userDetails = userDetailsService.loadUserByUsername(username);
        // ... 建立 UsernamePasswordAuthenticationToken 放進 SecurityContext
    }
}
filterChain.doFilter(request, response);   // 放行，交給 Controller
```

> 🔹 `AuthContext` 用 useState 存一份 + localStorage 存一份。**刷新頁面時**從 localStorage 讀回來，所以登入不會因為重整就消失；但 token 是存在瀏覽器的，**關掉無痕/清快取就會登出**，這很正常。

### 5.3 前端「路由守門員」：ProtectedRoute

`App.jsx` 把要保護的頁面包在 `ProtectedRoute` 裡：

```jsx
<Route element={<ProtectedRoute requiredRole="ROLE_TEACHER" />}>
  <Route element={<Layout role="teacher" />}>
    <Route path="/teacher" element={<TeacherDashboard />} />
    ...
```

`ProtectedRoute.jsx`：

```jsx
if (!auth) return <Navigate to="/login" replace />                    // 沒登入 → 回登入頁
if (requiredRole && auth.role !== requiredRole) {                     // 角色不符 → 回自己該去的頁
  return <Navigate to={auth.role === 'ROLE_TEACHER' ? '/teacher' : '/student'} replace />
}
return <Outlet />                                                      // 有權限 → 顯示巢狀子頁
```

**前端守「畫面」，後端守「資料」：** 前端路由擋住的是「看不看得見頁面」；真正能不能拿到/改動資料，還是靠後端的 `@PreAuthorize` 與權限檢查把關（第 9 節）。

---

## 6. 職責分層：每一層「只做一件事」

這是全端架構的**核心心法**。前端與後端各自再分層，但加起來的職責與資料流向都是單向的：

```
前端                       後端
─────────────────          ─────────────────────
頁面(Page)   ← ✅ ←        Controller（收請求/回傳）
  │                        │
api(examApi)→ ✅ →         Service（商業邏輯）
  │                        │
Context(共用狀態)           Repository（存取資料）
                             │
                           資料庫
```

| 層 | 職責 | 本專案範例（後端） |
|----|------|--------------------|
| **Controller** | 只負責「收請求、轉交、回覆」，不含邏輯 | `ExamController.java` |
| **Service** | 放商業邏輯（計分、權限檢查、交易） | `ExamService.java`、`ResultService.java` |
| **Repository** | 只負責「存取資料」，方法名自動生 SQL | `ExamRepository.java` |
| **Entity** | 對映資料表 | `Exam.java`、`Question.java`、`User.java` |
| **DTO** | 傳輸用的資料形狀（契約） | `dto/` 資料夾 |

> 💡 **判斷寫在哪一層的口訣：**「要不要存資料？」→ Repository；「要不要動手算/檢查？」→ Service；「是接收或回傳 HTTP？」→ Controller。商業邏輯**不要**放在 Controller 或 Entity 裡。

---

## 7. 一次完整功能追蹤：教師建立測驗並批次匯入題目

這是「有新功能的請求」的經典範例，一次跨越前端與後端所有層。

### 7.1 前端：建立測驗

`ExamFormPage.jsx` 提交表單 → `createExam`：

```js
export const createExam = (token, body) =>
  fetch(`${BASE}/exams`, { method: 'POST',
    headers: authHeader(token), body: JSON.stringify(body) }).then(handle)
```

### 7.2 後端：Controller → Service → Repository

`ExamController.java`：

```java
@PostMapping
@PreAuthorize("hasRole('TEACHER')")          // 只有教師能建
public ResponseEntity<ExamSummaryResponse> createExam(
        @Valid @RequestBody ExamRequest req, @AuthenticationPrincipal UserDetails user) {
    return ResponseEntity.status(HttpStatus.CREATED)
            .body(examService.createExam(req, user.getUsername()));
}
```

`ExamService.createExam()`：

```java
public ExamSummaryResponse createExam(ExamRequest req, String username) {
    User teacher = findUserByUsername(username);
    Exam exam = examRepository.save(Exam.builder()
            .title(req.title()).description(req.description())
            .timeLimit(req.timeLimit() != null ? req.timeLimit() : 60)
            .createdBy(teacher)
            .active(true)
            .build());
    return toSummary(exam);
}
```

### 7.3 前端：批次匯入題目（前端解析 CSV → 後端一次存多題）

`ExamDetailPage.jsx` 讀取 `.csv` 檔、前端解析成陣列，再呼叫 `batchImportQuestions`：

```js
export const batchImportQuestions = (token, examId, questions) =>
  fetch(`${BASE}/exams/${examId}/questions/batch`, {
    method: 'POST', headers: authHeader(token),
    body: JSON.stringify({ questions }),
  }).then(handle)
```

後端 `ExamController`：

```java
@PostMapping("/{examId}/questions/batch")
@PreAuthorize("hasRole('TEACHER')")
public ResponseEntity<List<QuestionDetailResponse>> batchImportQuestions(
        @PathVariable Long examId,
        @Valid @RequestBody BatchQuestionsRequest req,
        @AuthenticationPrincipal UserDetails user) {
    return ResponseEntity.status(HttpStatus.CREATED)
            .body(examService.batchImportQuestions(examId, req.questions(), user.getUsername()));
}
```

`ExamService.batchImportQuestions()` 用迴圈把每一題存進資料庫，並回傳所有新增的題目：

```java
public List<QuestionDetailResponse> batchImportQuestions(
        Long examId, List<QuestionRequest> requests, String username) {
    Exam exam = findExamById(examId);
    checkOwnership(exam, username);                 // 檢查「這份測驗是我的」才准
    List<QuestionDetailResponse> saved = new ArrayList<>();
    for (QuestionRequest req : requests) {
        Question q = questionRepository.save(Question.builder()
                .exam(exam).questionText(req.questionText())
                .optionA(req.optionA()).optionB(req.optionB())
                .optionC(req.optionC()).optionD(req.optionD())
                .correctAnswer(req.correctAnswer())
                .points(req.points() != null ? req.points() : 1)
                .build());
        saved.add(toQuestionDetail(q));
    }
    return saved;
}
```

### 7.4 教學重點：權限 + 所有權檢查

建立、編輯、刪除、匯入這類「改資料」的動作，**兩道關卡**缺一不可：

1. **角色關**（`@PreAuthorize`）→ 你是不是「教師」？
2. **所有權關**（`checkOwnership`）→ 這份測驗是不是「你」建的？

`ExamService.checkOwnership()`：

```java
private void checkOwnership(Exam exam, String username) {
    if (exam.getCreatedBy() == null
            || !exam.getCreatedBy().getUsername().equals(username)) {
        throw new ResponseStatusException(HttpStatus.FORBIDDEN, "您沒有權限修改此測驗");
    }
}
```

> 💡 這是全端安全最重要的一課：**前端能不能看到「編輯按鈕」不算數，後端必須在每次操作時都真正檢查權限。** 任何人都可以繞過前端直接送 API，所以把關必須在後端。

---

## 8. 一次完整功能追蹤：學生作答並看成績

### 8.1 前端：一題一頁 + 複習後提交

`TakeExamPage.jsx` 用 `useState` 管理：答案 `answers`、目前題號 `currentIdx`、階段 `phase`（作答 / 複習）。

- 每個選項被點選 → `setAnswers(prev => ({ ...prev, [String(q.id)]: opt }))`
- 「檢查並提交」→ `setPhase('review')` 進入複習頁
- 複習頁確認後呼叫 `submitExam(token, id, answers)`

```js
export const submitExam = (token, examId, answers) =>
  fetch(`${BASE}/exams/${examId}/submit`, {
    method: 'POST', headers: authHeader(token),
    body: JSON.stringify({ answers }),
  }).then(handle)
```

> 🔹 注意 `answers` 是「以題目 id 為 key」的物件：`{ "1": "A", "2": "C" }`。這是前後端約定好的格式（`SubmitRequest` 收 `Map<String,String> answers`）。

### 8.2 後端：自動計分

`ResultService.submitExam()`：

```java
List<Question> questions = questionRepository.findByExamOrderByIdAsc(exam);
Map<String, String> answers = req.answers();

int score = 0, totalPoints = 0;
for (Question q : questions) {
    totalPoints += q.getPoints();
    String submitted = answers.get(String.valueOf(q.getId()));
    if (q.getCorrectAnswer().equals(submitted)) score += q.getPoints();
}
```

重點：
- **正確答案只有後端有**（`Question.correctAnswer`），學生端拿到的 `/take` 回應刻意**不含**正確答案（`QuestionStudentResponse` 沒有該欄位），防止作弊。
- 計分邏輯放後端（Service），前端只是把答案送過來。
- 每個使用者每個測驗只能交一次：`existsByUserAndExam` 會擋掉重複作答。

### 8.3 前端：取得並顯示成績

`MyResultsPage.jsx` 呼叫 `getMyResults`，拿到 `result.percentage`、`result.grade` 等欄位渲染成表格與統計卡。

---

## 9. 角色權限：教師/學生怎麼把關

| 關卡 | 位置 | 作用 |
|------|------|------|
| 路由守門員 | 前端 `ProtectedRoute.jsx` | 擋「看不到頁面」 |
| 方法權限 | 後端 `@PreAuthorize("hasRole('TEACHER')")` | 擋「不能呼叫 API」 |
| 所有權 | 後端 `checkOwnership()` | 擋「不能動別人的資料」 |

`UserDetailsServiceImpl` 把 `User.role`（如 `ROLE_TEACHER`）轉成 Spring Security 認得的角色：

```java
.roles(user.getRole().replace("ROLE_", ""))   // "ROLE_TEACHER" → roles("TEACHER")
```

這樣 `@PreAuthorize("hasRole('TEACHER')")` 才對得上（`hasRole` 會自動補 `ROLE_` 前綴）。

> ❌ **常見錯誤：`hasRole('TEACHER')` vs `ROLE_`。** `hasRole('X')` 內部比的是 `ROLE_X`。如果資料庫存的角色是 `TEACHER` 而不是 `ROLE_TEACHER`，就會一直 403 拒權。本專案統一存 `ROLE_TEACHER`、`ROLE_STUDENT`。

---

## 10. 錯誤處理的兩端合作

後端統一透過 `GlobalExceptionHandler` 把錯誤包成 JSON：

```json
{ "timestamp": "...", "message": "該動作僅限教師", "fieldErrors": { "username": "帳號已存在" } }
```

前端 `examApi.js` 的 `handle` 統一讀取 `data.message` 並丟出 `Error`：

```js
async function handle(res) {
  if (res.status === 204) return null
  const data = await res.json().catch(() => ({}))
  if (!res.ok) throw new Error(data.message || `HTTP ${res.status}`)
  return data
}
```

頁面層再 `.catch(err => setError(err.message))` 顯示在畫面上。於是**錯誤訊息「只寫一次」在後端，前端所有頁面都共用同一套顯示邏輯**。

---

## 11. 常見陷阱與除錯技巧

| 症狀 | 原因 | 解法 |
|------|------|------|
| 前端一直 CORS 錯誤 | 直接打 `localhost:8080` 而非走 proxy | 前端統一用 `/api/...`，靠 Vite proxy 轉送 |
| 一直 401 未授權 | 沒帶 token，或 token 過期/被竄改 | 檢查 `Authorization: Bearer xxx`、密鑰長度 |
| 一直 403 拒絕 | 角色不符（`ROLE_` 前綴）或所有權不足 | 檢查 `@PreAuthorize` 與 `checkOwnership` |
| 拿到欄位 `undefined` | 前端讀的欄位名與後端 DTO 不一致 | 對照 record DTO 欄位名 |
| 修改後前端沒變 | 只是刷新頁面資料沒重載 | 檢查是否有呼叫 `load()` / `reload()` 重新 fetch |
| `400 輸入資料驗證失敗` | 送的資料不符 `@Valid` 規則 | 看回應的 `fieldErrors` 是哪個欄位 |

**除錯 SOP（我的建議）：**
1. 先用 **Postman / 瀏覽器** 直接打後端 API，確認「後端單獨」是否 OK（排除前端問題）。
2. 再看**瀏覽器 DevTools → Network**，確認請求網址、method、header、request body 都正確，並看 response。
3. 後端看 **console log**（`spring.jpa.show-sql=true` 會印出 SQL）。

---

## 12. 整合學習路線圖

> 循著「把一套完整功能做起來」的順序，而不是照語法順序。每跨過一階段，你就親手打通一層。

### 階段 A — 打通「前端 → 後端」最小請求
- 讀懂 `vite.config.js` proxy、`examApi.js` 的 `authHeader` + `handle`
- 練習：寫一支程式，從前端呼叫 `GET /api/exams` 並顯示
- **里程碑**：知道一個請求從點擊到畫面呈現經過哪些檔案

### 階段 B — 打通「登入 + 身分」
- 追完整條登入流程（第 4 節）
- 看懂 `AuthContext`、`ProtectedRoute`、`JwtFilter`
- **里程碑**：能自己加一個「只有登入才能看」的新頁面

### 階段 C — 打通「CRUD 與權限」
- 追蹤「建立測驗」與「批次匯入題目」（第 7 節）
- 看懂 `@PreAuthorize` + `checkOwnership`
- **里程碑**：能加一支「新增/查詢」API 並讓前端呼叫

### 階段 D — 打通「複雜流程與交易」
- 追蹤「作答 + 自動計分」（第 8 節）
- 理解 `@Transactional`、防止重複作答
- **里程碑**：能在加新流程時正確分層（Controller/Service/Repository）

---

## 13. 動手練習

做完以下練習，就等於親手把整套整合做過一遍。

### 練習 1：追蹤題（Easy）
用文字描述「學生點選測驗 → 進入作答頁 → 看到題目」所經過的前端檔案與後端 API 各是哪一支。提示：`StudentDashboard → examApi → /api/exams → getActiveExams`。

**參考答案**（先自己想再看）：
> 前端 `StudentDashboard.jsx` 呼叫 `getActiveExams`（`examApi.js`）→ GET `/api/exams` → 後端 `ExamController.getActiveExams()` → `ExamService.getActiveExams()` → 回傳開放中的測驗 → 前端 `setExams` 渲染卡片。

### 練習 2：加一個「題目數量上限」檢查（Medium）
在「建立題目」時，若測驗已達 50 題就拒絕新增。
- 該寫在後端哪一層？為什麼？（提示：跟商業規則有關 → Service）
- 試著在 `ExamService.addQuestion` 加上判斷：`if (questionRepository.countByExam(exam) >= 50) throw ...`

```java
if (questionRepository.countByExam(exam) >= 50) {
    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "此測驗最多 50 題");
}
```

### 練習 3：打通一個新功能（Hard）
自己新增「學生可以查看單一測驗已提交的成績明細（正確/錯誤）」。

1. **後端**：新增一個 DTO + Service 方法，回傳該使用者在某測驗的每題對錯。
2. **後端**：在 `ResultController` 加一支 `GET /api/results/exam/{id}/detail`，並檢查所有權（只能看自己的）。
3. **前端**：在 `examApi.js` 加一支 function，並在 `MyResultsPage` 增加「查看明細」按鈕與頁面。
4. 記得：正確答案不要提前洩漏；權限檢查放後端。

---

## 14. 總結

- **全端整合的精髓，是「分層 + 契約」**：每一層只做一件事，前後端用網址與 DTO 當契約溝通。
- **身分與權限，前端管「畫面」，後端管「資料」**，安全把關必須在後端。
- **所有請求統一收在 `examApi.js`、錯誤統一由 `handle` 處理**，是讓全端好維護的關鍵。
- **學整合最好的方法**，就是沿著「登入 → 建測驗 → 匯入題目 → 學生作答 → 看成績」這條線，把每個功能從點擊追到資料庫再追回來。

> 🔗 相關文件：後端語法詳見 `online-exam-api/docs/05-java-backend-learning.md`；前端語法詳見 `online-exam-frontend/docs/react-learning.md`。本文件補上的是兩者「如何合作」。
