---
marp: true
theme: default
paginate: true
size: 16:9
header: '週 15：期末專題（補強+測試+文件）｜全端就業班'
style: |
  section { font-size: 25px; padding: 50px 70px; }
  h1 { font-size: 40px; }
  h2 { font-size: 30px; }
  h3 { font-size: 25px; }
  pre { font-size: 15px; line-height: 1.4; padding: 12px 16px; }
  table { font-size: 18px; }
  blockquote { font-size: 20px; }
  li { margin: 5px 0; }
---

<!--
使用方式同前週：Marp 預覽；穿插「AI 動手做」。
-->

# 週 15｜期末專題：補強 + 測試 + 文件

## 讓你的作品「能講、能測、能展示」

### 📢 作品好，也要會「說明」

---

# 本週對象與目標

- 對象：已有完整 CRUD + 登入的第一版成品（週 14）
- 時間：約 15 小時（3 天）
- **本週結束你將有**
  - 全流程測試檢查表（每項都測過）
  - 專案 README（安裝/啟動/功能/API 列表）
  - 一支「5 分鐘 demo 簡報」腳本
  - （加分）後端測試 + 部署說明

> 面試/徵選時「能不能講清楚」佔至少一半印象分。

---

# 1. 第一個任務：全流程總測試

寫一張「測試檢查表」，一格一格打勾：

```
□ 註冊帳號 → 能登入
□ 登錯密碼 → 顯示錯誤
□ 未登入點頁面 → 跳轉登入頁
□ token 過期（可改 expire-hours 測）→ 自動踢出
□ 新增部門 → 列表出現
□ 新增員工（指定部門）→ 列表有 + 部門正確
□ 編輯員工 → 資料更新
□ 刪除員工 → 列表移除
□ 輸入空值/錯誤 → 有錯誤提示（不崩潰）
□ 分頁/查詢（若有）→ 正常
```

> 找一個「沒碰過你程式的同學」來點——**換個看的人，很多 bug 冒出來**。

---

# 2. 補強：剛剛測出的洞

最常見的四種「差一點點」：

| 問題 | 解法 |
|---|---|
| 空值/重複沒擋 | 後端 Bean Validation（週 9）補上 |
| 錯誤訊息的亂碼/不統一 | 統一 `ApiError` DTO + 例外處理 |
| 前端表單沒驗證 | submit 前檢查；空欄位提示 |
| 撞到重複帳號/信箱 | 資料庫唯一鍵 + 友善錯誤訊息 |

> 不用追求「完美」，**把「扣分項」補掉**就好——重複資料、空值、錯誤頁面。

---

# 3. 後端測試（加分，但仍值得學）

`spring-boot-starter-test` 已內建。

```java
@SpringBootTest
@AutoConfigureMockMvc
class EmployeeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void 查員工清單_回傳200() throws Exception {
        mockMvc.perform(get("/api/employees"))
                .andExpect(status().isOk());
    }

    @Test
    void 沒認證_回傳401() throws Exception {
        mockMvc.perform(get("/api/employees"))
                .andExpect(status().isUnauthorized());
    }
}
```

> 有測試 = 面試加分項（「我要的測試我有寫」）。先把「登入保護 + CRUD」寫最基本的。

---

# 4. README：別人能不能照著跑？

`README.md` 放專案根目錄（後端與前端各一份或合併均可）：

```markdown
# 員工管理系統

## 功能
- 會員註冊/登入（JWT）
- 員工 CRUD（指定部門）
- 部門管理

## 技術
- 後端：Spring Boot 3 / JPA / MySQL / Spring Security
- 前端：React 18 + Vite + axios + react-router-dom

## 啟動步驟
1. MySQL 建資料庫，改 application.properties 連線
2. 後端：mvn spring-boot:run （localhost:8080）
3. 前端：npm install && npm run dev （localhost:5173）

## API
| 方法 | 路徑 | 說明 | 需 token |
| POST | /api/auth/register | 註冊 | 否 |
| POST | /api/auth/login | 登入 | 否 |
| GET  | /api/employees | 員工清單 | 是 |
| ...
```

> **「別人照 README 能不能跑起來？」**——這是最好的測試。

---

# 5. 簡報腳本（5 分鐘 demo）

`DEMO.md` 或簡報先寫「要講的話」，不是先做投影片：

```
1.（0:00）開場：「我做的是員工管理系統…
2.（0:30）架構：後端 Spring Boot + 前端 React，JWT 登入
3.（1:00）Demo：登入 → 新增部門 → 新增員工(選部門) → 編輯 → 刪除
4.（3:30）技術亮點：@ManyToOne 關聯、JWT、例外處理統一回應、axios 攔截器
5.（4:30）心得與未來：想加…（角色權限、報表、部署）
```

> 重點原則：**Demo 慢、字少、結論清楚**。「講不出來＝你還沒抓到重點」。

---

# 5.1 三分鐘「一句話就能講」

**練習用一句話講你的系統**

```
「我做的是一個員工管理系統：
後端用 Spring Boot 加 JWT 登入與密碼加密，
前端用 React 呼叫 REST API，
只要登入就能對員工與部門做新增、編輯、刪除。」
```

> 自己錄下來重聽。**那種「聽得懂你在做什麼」的簡短說明**，就是面試的起手式。

---

# 6. 純 JWT vs session：講給面試官聽

**「你為什麼用 JWT？」**（練習版答案）

```
「我用 JWT 是因為它是無狀態的。
伺服器不需要記住每個使用者的 session，
前端每次請求把 token 放在 Authorization header，
伺服器只要驗簽章就知道是誰。
身分驗證分散在各台伺服器也不用同步，
比較適合現在的前後端分離架構。」
```

> 講「無狀態 + header」→ 高分。講「大家都用」→ 普通分。

---

# 7. (加分) 部署到免費空間

後端：Render/Railway/Fly.io 免費 tier
前端：Vercel/Netlify

```
後端（Render 用 Docker 或 buildpacks）
    ├─ Java 17 + Maven
    ├─ 環境變數：DB 連線、jwt.secret
    └─ CORS allowedOrigins 改成前端網域

前端（Vercel）
    ├─ 環境變數：VITE_API_URL=https://xxx.onrender.com
    └─ API 用環境變數，不要寫死 localhost
```

> 能上線＝作品瞬間提升一個等級。**週 16 會細講**，本週若時間允許可以先摸。

---

# AI 動手做｜後端測試生成

```
我的員工系統後端有：JWT 登入、員工 CRUD（@ManyToOne 部門）。
請幫我寫測試（JUnit + MockMvc）：
1. 登入成功拿 token
2. 沒 token 打 GET /api/employees → 401
3. 帶 token 打 GET /api/employees → 200
4. 新增員工驗證（空 name → 400）
請附中文註解，並教我怎麼跑（mvn test）。
```

---

# AI 動手做｜README 生成

```
我剛完成一個全端專案：
後端 Spring Boot 3（JWT 登入、員工/部門 CRUD），前端 React 18 + Vite + axios。
請幫我寫一份專業的 README.md：
- 專案簡介（一句話）
- 功能列表
- 技術棧（後端/前端分開列）
- 環境需求（JDK、MySQL、Node）
- 啟動步驟（可複製貼上）
- API 表格
- 目錄結構
請用繁體中文，簡潔清楚。
```

---

# AI 動手做｜面試練習題

```
我準備面試「Java 全端工程師」，我的期末專題是員工管理系統（JWT + 員工/部門 CRUD）。
請用「模擬面試官」方式問我 10 題（先問、不給答案），涵蓋：
- Spring 的 DI、三層架構
- JPA 的 @ManyToOne、@Query
- JWT 與 BCrypt
- REST API 設計
- React 的 state/props/useEffect
答完後幫我批改並給建議答案。
```

---

# 8. 本週驗收作品

**題目**：專案「能講、能測、文件齊」。

**必須**

1. 填滿的測試檢查表（至少 8 項打勾）
2. `README.md`（依它可跑起專案）
3. 5 分鐘 demo 腳本 `DEMO.md`
4. 能「一句話」講出專案

**加分**

- 後端 MockMvc 測試通過
- 部署上免費平台（可給網址）

---

# 9. 自我測驗

1. 為什麼要「找沒碰過程式的人來測試」？
2. README 最重要的一頁是什麼？
3. 5 分鐘 demo 前 30 秒該講什麼？
4. 「你為什麼用 JWT？」三個關鍵字？
5. 沒 token 打 `/api/employees` 應該回什麼？
6. 前端 401 該做什麼？
7. 部署時 `jwt.secret` 該怎麼處理？（不能寫死）

---

# 測驗解答

**1.** 換個使用者、換雙眼睛，很容易發現「說明不清、少了哪步、流程卡住」等自己習慣忽視的洞。

**2.** **啟動步驟**（能不能照著跑起來）。其次功能與 API 表。

**3.** 先講「這是什麼系統＋一句話架構＋為什麼」，再開始 Demo。

**4.** 無狀態（stateless）、Authorization header、不需 session 同步。

**5.** 401 Unauthorized。

**6.** 清掉 token，跳轉回登入頁（axios 攔截器統一處理）。

**7.** 用環境變數（Render 的 env / application.properties 用 `${JWT_SECRET}`），不進 git。

---

# 本週小結

你今天完成了：

- 全流程測試檢查表
- 補強驗證與錯誤處理
- 後端 MockMvc 測試（加分）
- README（別人可照做）
- 5 分鐘 demo 腳本 + 一句話簡潔講法
- JWT 面試題練習

**下週（週 16）**：**部署與求職**——上線演示 + 履歷/面試/作品集收尾。

> 你現在有「成品 + 會講 + 有文件」。最後一週把門面打理好，準備投履歷。