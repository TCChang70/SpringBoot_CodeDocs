---
marp: true
theme: default
paginate: true
size: 16:9
header: '週 16：部署上線 + 求職準備｜全端就業班'
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
使用方式同前週：Marp 預覽；穿插「AI 動手做」。最後一週！
-->

# 週 16｜部署上線 + 求職準備

## 從「本機」到「看得見的網址」，從「作品」到「面試」

### 🎉 最後一週：收尾、包裝、出發

---

# 本週對象與目標

- 對象：已有成品、會 demo、會講（週 14-15）的你
- 時間：約 15 小時（3 天）：部署 1 天 + 求職 1 天 + 模擬面試與收尾 1 天
- **本週結束你將有**
  - 一個「別人點網址就能用」的線上系統
  - 履歷/作品集重點、面試答題練習、求職清單

> 「能線上展示」是求職最省力的武器——不用寄 zip，傳連結就好。

---

# 1. 部署：整體流程

```
後端（Spring Boot）
  原始碼 → 打包 jar → 伺服器(雲端) → 一個網域 URL
        （ex: https://emp-api.onrender.com）

前端（React）
  原始碼 → npm run build（靜態檔案）→ 靜態託管 → 一個網域 URL
        （ex: https://emp-system.vercel.app）

連線：前端 build 時 API 指向後端網域
```

> 本課用 **Render（後端，免費檔）** ＋ **Vercel（前端，免費）** 示範。
> 費用=0；重點是「跑得起來＋看得到」。

---

# 2. 後端打包（本機確認可跑）

```bash
# 進專案
mvn clean package
```

產生 `target/`.jar

```bash
java -jar target/<專案名>-0.0.1-SNAPSHOT.jar
```

> 先在本機用 `jar` 跑起來過一次，確認**獨立可直接執行**，再上雲。

---

# 2.1 準備 MySQL（雲端）或改用內建資料庫

- 免費資料庫（Render 有內建 PostgreSQL；也要給 MySQL 選項）
- 若不想外連 MySQL，也可暫時用 **H2**（教學 demo 可，正式不要）：

```
# application.properties（demo/快速演示用，不建議正式上線）
spring.datasource.url=jdbc:h2:mem:testdb
spring.h2.console.enabled=true
spring.jpa.hibernate.ddl-auto=create
```

> 正式專案用 MySQL/PostgreSQL。本週只要「能上線演示」，先求跑通。

---

# 3. 前端 build

```bash
npm run build
```

產生 `dist/`（靜態檔案：HTML/JS/CSS）。

設定 API base（環境變數，不要寫死 `localhost`）：

```bash
# .env 或部署平台設定
VITE_API_URL=https://emp-api.onrender.com
```

程式碼改用：

```js
const API_URL = import.meta.env.VITE_API_URL || 'http://localhost:8080'
```

> `.env`：本機 `VITE_API_URL=http://localhost:8080`；上線平台設線上網址。

---

# 4. Render 部署後端（步驟）

1. 把程式碼推到 GitHub（私有也可）
2. 到 render.com → New → Web Service → 連 GitHub repo
3. Build Command：`mvn clean package`
4. Start Command：`java -jar target/<專案名>-0.0.1-SNAPSHOT.jar`
5. Environment Variables 填：`JWT_SECRET`、資料庫 URL/帳密、`CORS` 前端網域
6. 部署完給一個網址：`https://emp-api.onrender.com`

> 注意 `application.properties` 用**環境變數**（`${JWT_SECRET}`）——**絕對不要 commit 密鑰**。

---

# 4.1 CORS 也要改掉

```java
registry.addMapping("/api/**")
        .allowedOrigins("https://emp-system.vercel.app")  // 正式網域
        // 不要再用 *（會允許所有人）
```

> 上線的 CORS 用**實際前端網域**，是資安基本。

---

# 5. Vercel 部署前端（步驟）

1. 前端 repo 推到 GitHub
2. vercel.com → Add New Project → 連 repo
3. Framework：Vite；Build：`npm run build`；Output：`dist`
4. Environment Variables 填：`VITE_API_URL=https://emp-api.onrender.com`
5. 部署完成 → 網址：`https://emp-system.vercel.app`

**測試**：開那個網址 → 能註冊/登入/CRUD → 完成！🎉

> 一定要「用手機或另一位同學」開一次：CORS、路徑、400/500 都會現形。

---

# 6. 本機驗收（最後的檢查表）

```
□ 後端 jar 檔可以單獨 java -jar 啟動
□ 前端 build 成功、dist 能開
□ 雲端後端網址能打 API（瀏覽器直接 GET /api/employees + token）
□ 雲端前端網址能操作完整流程
□ README 更新成「線上網址」版本
□ 備份：GitHub repo（README + 程式 + DEMO）
```

> 結訓前兩件事：**一個線上網址** + **一個 GitHub repo**。履歷直接放兩行。

---

# 7. 求職：你的履歷這樣寫

```markdown
## 專案：員工管理系統（全端）
- 後端：Spring Boot 3、Spring Data JPA、MySQL、Spring Security + JWT
- 前端：React 18、Vite、axios、react-router-dom
- 功能：會員登入/註冊、員工與部門 CRUD、JWT 權限控管
- 亮點：
  - JWT + BCrypt 密碼加密
  - @ManyToOne/OneToMany 關聯設計
  - 統一回應例外處理 + Bean Validation 驗證
  - 部署：Render（後端）+ Vercel（前端）
- 網址：https://emp-system.vercel.app
```

> 履歷就是「我做了什麼 + 用了什麼 + 做得怎樣 + 證明在哪」。**數據與網址最有力**。

---

# 7.1 求職答題重點（熱門題）

| 題目 | 你答的核心 |
|---|---|
| 你為什麼做 / 這個專案 | 「把課堂學的串成完整全端」＋解決什麼 |
| 你怎麼處理錯誤 | 統一回應（ApiError）、例外處理（週 9） |
| 密碼怎麼存 | BCrypt（雜湊+鹽），不存明文 |
| JWT 放哪 | Authorization header；filter 驗證 |
| 前端怎麼拿資料 | axios 攔截器自動帶 token、useEffect 載入 |
| 資料庫關聯 | @ManyToOne / @OneToMany 部門-員工 |
| 怎麼測試 | MockMvc + Postman + 手動全流程檢查表 |

> **被問到「講一下你做過什麼專案」**＝最常見開場。把上面七題練熟。

---

# 8. 去哪裡找工作（求職管道）

- 人力銀行：104、Yourator、Cake（資訊/科技類）
- 接案/外包：Toptal、Upwork（較吃經驗）
- 上班族轉職：先在 SaaS/軟體公司開 lower 職位練功
- **有系統展示 + GitHub + 這份履歷**，直接寄作品集連結

> 新手的武器：**成品完整 + 講得清楚 + 態度誠懇**（很多公司看重你能「自己搞定、問對問題」）。

---

# 9. 面試心法（最後一週、最重要）

1. **把 demo 練到 3 分半內**：只帶重點流
2. **誠實**：「我不會、但我會用 AI/文件查」比裝會好
3. 準備「我最自豪的程式碼」：
   - （範例）JWT filter、@Query、統一例外處理
4. **問問題**（反問面試官）：團隊用什麼技術？這缺主要做什麼？
5. 面試後感謝函（隔天）

> 「最自豪的一段程式碼 + 能講 30 秒」——這幾乎是每場面試都會被問到。

---

# AI 動手做｜部署排障

```
我把 Spring Boot 部署到 Render，前端到 Vercel，但網頁呼叫 API 失敗。
請幫我列「/api 打不到的檢查清單」，逐一給驗證方法：
1. 後端有沒有啟動成功（看 logs）
2. CORS 是不是還用 * 或沒設前端網域
3. 前端 VITE_API_URL 有沒有 Build 時吃到（重 build）
4. 瀏覽器 Network 看是不是 500/404
5. 資料庫連線（環境變數有沒有被吃）
請列成「可勾檢查表」並寫上錯誤訊息看哪裡。
```

---

# AI 動手做｜從履歷到面試的模擬

```
我要找「Java 全端工程師」職位（社會新鮮人）。
請當面試官，依我的專案（員工管理系統：JWT 登入 + 員工/部門 CRUD）：
1. 先考我 5 題技術題（答完再檢討）
2. 再考我 2 個情境題（例如：「部門刪不掉、有員工引用，怎麼辦？」）
3. 最後給我履歷加強建議（以我上面的專案內容為例）
權限：綜合、不要一次給答案。
```

---

# AI 動手做｜求職文件助產

```
我是職訓結業，剛完成「員工管理系統」全端專案。
請幫我寫：
1. 一段 150 字的「自我介紹」（給面試開場用）
2. 履歷「專案經歷」段落（含：技術、功能、亮點、部署、網址欄位留白）
3. 一封寄履歷給公司的信件（附作品連結）
請繁體中文、專業、不浮誇。
```

---

# 10. 結訓檢查表（畢業前完成）

```
□ 一個線上網址可用（全端）
□ GitHub repo（程式 + README + DEMO）
□ 履歷：專案段 + 網址
□ 可講一句話自我介紹 + 3 分半 demo
□ 面試 7 大題練習過
□ 拍一支「2 分鐘 demo 錄影」（存雲端備份）
```

> 全部打勾，你就可以投履歷了！

---

# 11. 最後一堂小結：你這 16 週學到了…

**後端**
- Java 基礎/面向物件/集合/IO（週 1-4）
- MySQL 與 SQL（週 5）
- Spring Boot：三層架構、DI、REST（週 6-7）
- JPA CRUD、關聯、進階查詢（週 8-10）
- 安全：BCrypt、JWT（週 10、14）

**前端**
- HTML/CSS/JS、fetch（週 11）
- React：元件、state、props、router、axios（週 12-13）

**整合與展示**
- 期末專題（週 14-15）
- 部署上線（Render + Vercel）（週 16）

> 你現在是**能獨立完成全端小專案的人**。剩下的就是「多練習、多講、多修」——加油！

---

# 12. 測驗（本週／結訓）

1. `mvn clean package` 產生什麼？怎麼啟動？
2. `npm run build` 產生什麼？
3. `VITE_API_URL` 環境變數幹嘛？
4. 上線 CORS 為什麼不要用 `"*"`？
5. 一個「別人能開啟的線上網址」為什麼對面試很有用？
6. 面試被問「不會的技術」怎麼答？
7. 自我介紹用一句話講專案，怎麼組織？

---

# 解答

**1.** 產生可分發的 jar；`java -jar target/xxx.jar` 啟動。

**2.** 產生 `dist/` 靜態網站檔案（HTML/JS/CSS），交給靜態託管。

**3.** 讓前端 build 時知道「API 主機」在哪，方便切本機/上線，不把網址寫死在程式碼。

**4.** `"*"` 允許任何網站呼叫你的 API；上線只開放自己的前端網域，避免被濫用。

**5.** 面試官/公司直接點開就能「看得到」你的作品，比 zip、截圖都更有說服力。

**6.** 誠實說「目前不熟」＋「但我會用文件/AI 快速查」、「我很願意學」，不要硬掰。

**7.** 「我做的是＿＿（一句話），後端用＿＿，前端用＿＿，它能＿＿（核心功能）」四段式。

---

# 最後的話

🎉 結訓了！你完成了：

- 1 個完整全端專案（看得見、可上線）
- 1 份履歷亮點 + 面試答題庫
- 16 週的知識與自信

**接下來**

- 把自己的專案多玩幾次、多改功能（加報表、權限、搜尋…）
- 把「能講出口」持續練
- 開始投履歷、大量面試、從每次面試中學

> 你是全端工程師了。去找工作吧！