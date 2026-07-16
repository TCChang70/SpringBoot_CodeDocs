## 步驟實例：Temperature 轉換程式專案（圖解說明）

本章節以建立「Temperature 溫度轉換程式」為實例，提供每個操作的詳細步驟、界面說明與截圖位置提示。

---

### 實例情境說明
**專案名稱**：`temperature-converter`  
**功能需求**：建立一個 Java 程式，提供攝氏與華氏溫度相互轉換功能  
**開發流程**：本機建立 → GitHub 上傳 → 功能分支開發 → Pull Request → 合併 → 版本標記

---

### 階段 A：環境設定與帳號連結（首次使用必做）

#### A-1：安裝與啟動 SourceTree
1. **下載安裝**：
   - 前往 https://www.sourcetreeapp.com/
   - 下載 Windows 版本並執行安裝程式
   - 安裝過程中會提示安裝 Git，選擇「使用內建 Git」或「使用系統 Git」
   
2. **首次啟動設定**：
   - 啟動 SourceTree
   - 若出現註冊畫面，可選擇使用 Atlassian 帳號或略過
   - 進入主畫面後，會看到空白的 Repository 列表

#### A-2：設定 GitHub 個人存取權杖（PAT）
1. **在 GitHub 建立 PAT**：
   - 登入 GitHub → 右上角個人圖示 → Settings
   - 左側選單：Developer settings → Personal access tokens → Tokens (classic)
   - 點擊「Generate new token」→「Generate new token (classic)」
   - Note：`SourceTree Access`
   - Expiration：選擇有效期限（建議 90 days）
   - 勾選 Scopes：`repo`（完整存取倉庫）、`workflow`（選用）
   - 點擊「Generate token」→ **立即複製 Token**（離開頁面後無法再查看）

2. **在 SourceTree 加入 GitHub 帳號**：
   - SourceTree 上方選單：Tools → Options
   - 切換到「Authentication」分頁
   - 點擊「Add」按鈕
   - 設定如下：
     - **Hosting Service**：GitHub
     - **Preferred Protocol**：HTTPS
     - **Authentication**：Personal Access Token
     - **Username**：你的 GitHub 使用者名稱
     - **Token**：貼上剛才複製的 PAT
   - 點擊「OK」儲存

#### A-3：設定全域 Git 使用者資訊
1. **在 SourceTree 設定**：
   - Tools → Options → Git 分頁
   - 設定：
     - **Default user information**：
       - Full Name：`你的姓名`（會出現在 Commit 歷史）
       - Email Address：`your-email@example.com`（建議與 GitHub 相同）
   - 點擊「OK」

2. **驗證設定**：
   - 這些資訊會記錄在每次 Commit 中
   - 可於 Git 歷史查看 Author 資訊

---

### 階段 B：建立本機倉庫並上傳到 GitHub

#### B-1：建立本機 Git 倉庫
1. **建立專案資料夾**：
   - 在 `c:\projects\` 建立 `temperature-converter` 資料夾
   
2. **在 SourceTree 初始化倉庫**：
   - SourceTree 上方選單：File → New...
   - 選擇「Create Local Repository」
   - 對話框設定：
     - **Destination Path**：瀏覽並選擇 `c:\projects\temperature-converter`
     - **Name**：temperature-converter（自動填入）
     - **Type**：Git
     - 勾選「Also create remote repository」（先不勾選，稍後手動加入）
   - 點擊「Create」
   
3. **確認初始化成功**：
   - SourceTree 會開啟該倉庫視窗
   - 左側 BRANCHES 區域會顯示 `main`（或 `master`）分支
   - 中間歷史區域為空（尚無 Commit）

#### B-2：建立初始檔案與 .gitignore
1. **建立 README.md**：
   - 在 `c:\projects\temperature-converter\` 建立 `README.md`
   - 內容：
     ```markdown
     # Temperature Converter
     
     Java 溫度轉換程式（攝氏 ↔ 華氏）
     
     ## 功能
     - 攝氏轉華氏：F = C × 9/5 + 32
     - 華氏轉攝氏：C = (F - 32) × 5/9
     ```

2. **建立 .gitignore**：
   - 在同一資料夾建立 `.gitignore` 檔案
   - 內容（Java 專案範例）：
     ```
     # Compiled class file
     *.class
     
     # Package Files
     *.jar
     *.war
     *.ear
     
     # IDE
     .idea/
     *.iml
     .vscode/
     
     # Build
     target/
     build/
     out/
     
     # OS
     .DS_Store
     Thumbs.db
     ```

#### B-3：第一次 Commit（Check-in）
1. **在 SourceTree 查看變更**：
   - 切換到「File Status」分頁（預設應已選取）
   - 下方「Unstaged files」區域會顯示：
     - `README.md`
     - `.gitignore`
   
2. **Stage 檔案**：
   - 勾選兩個檔案（或點擊「Stage All」按鈕）
   - 檔案會移至「Staged files」區域

3. **輸入 Commit 訊息並提交**：
   - 下方 Commit 訊息區域：
     - 輸入：`chore: initialize project with README and gitignore`
   - 點擊右下角「Commit」按鈕
   
4. **確認 Commit 成功**：
   - 切換到「History」分頁
   - 會看到第一個 Commit 節點，顯示：
     - Commit 訊息
     - Author（你的名字）
     - Date/Time
     - 分支標籤 `main`

#### B-4：在 GitHub 建立遠端倉庫
1. **登入 GitHub**：
   - 前往 https://github.com
   - 點擊右上角「+」→「New repository」

2. **設定倉庫**：
   - **Repository name**：`temperature-converter`
   - **Description**（選填）：`Java temperature conversion utility`
   - **Public** 或 **Private**：依需求選擇
   - **重要**：不要勾選「Initialize this repository with a README」（我們已有本機版本）
   - 點擊「Create repository」

3. **複製倉庫 URL**：
   - 建立完成後會顯示快速設定頁面
   - 確保選擇「HTTPS」
   - 複製 URL（格式：`https://github.com/你的帳號/temperature-converter.git`）

#### B-5：連結遠端倉庫並 Push
1. **在 SourceTree 加入 Remote**：
   - 上方選單：Repository → Repository Settings
   - 切換到「Remotes」分頁
   - 點擊「Add」按鈕
   - 設定：
     - **Remote name**：`origin`（預設名稱）
     - **URL / Path**：貼上剛才複製的 GitHub URL
     - **Username**（選填）：可留空，推送時會自動使用 PAT
   - 點擊「OK」→「OK」

2. **Push 到 GitHub**：
   - 點擊工具列「Push」按鈕（向上箭頭圖示）
   - Push 對話框：
     - **Remote**：origin
     - **Branch**：勾選 `main`（或 `master`）
   - 點擊「Push」
   - 若出現認證視窗，輸入 GitHub 使用者名稱與 PAT

3. **驗證上傳成功**：
   - 前往 GitHub 該倉庫頁面
   - 應可看到 `README.md` 與 `.gitignore`
   - Commit 歷史顯示初始提交

---

### 階段 C：建立功能分支並開發

#### C-1：建立功能分支
1. **在 SourceTree 建立分支**：
   - 確保目前在 `main` 分支（左側 BRANCHES → main 會有勾選標記）
   - 點擊工具列「Branch」按鈕（分岔路圖示）
   - Branch 對話框：
     - **New Branch**：`feature/celsius-to-fahrenheit`
     - **Checkout New Branch**：勾選（建立後立即切換）
   - 點擊「Create Branch」

2. **確認分支切換成功**：
   - 左側 BRANCHES 區域會新增 `feature/celsius-to-fahrenheit`
   - 該分支前方有 ✓ 勾選標記（表示目前工作分支）
   - 視窗標題列會顯示目前分支名稱

#### C-2：開發 Temperature.java（第一個功能）
1. **建立 Java 檔案**：
   - 在專案資料夾建立 `Temperature.java`
   - 內容：
     ```java
     public class Temperature {
         /**
          * 攝氏轉華氏
          * @param celsius 攝氏溫度
          * @return 華氏溫度
          */
         public static double celsiusToFahrenheit(double celsius) {
             return celsius * 9.0 / 5.0 + 32.0;
         }
         
         /**
          * 華氏轉攝氏
          * @param fahrenheit 華氏溫度
          * @return 攝氏溫度
          */
         public static double fahrenheitToCelsius(double fahrenheit) {
             return (fahrenheit - 32.0) * 5.0 / 9.0;
         }
     }
     ```

2. **在 SourceTree Commit 變更**：
   - 切換到「File Status」分頁
   - Unstaged files 顯示 `Temperature.java`
   - 勾選檔案 → Stage
   - Commit 訊息：`feat(temp): add celsius/fahrenheit conversion methods`
   - 點擊「Commit」

#### C-3：開發測試程式（第二個 Commit）
1. **建立 TemperatureTester.java**：
   - 內容：
     ```java
     public class TemperatureTester {
         public static void main(String[] args) {
             // 測試攝氏轉華氏
             double c = 0.0;
             double f = Temperature.celsiusToFahrenheit(c);
             System.out.println(c + "°C = " + f + "°F");  // 應輸出 32.0
             
             // 測試華氏轉攝氏
             double f2 = 98.6;
             double c2 = Temperature.fahrenheitToCelsius(f2);
             System.out.println(f2 + "°F = " + c2 + "°C");  // 應輸出 37.0
         }
     }
     ```

2. **Commit**：
   - Stage 檔案
   - Commit 訊息：`test(temp): add tester for temperature conversion`
   - Commit

3. **查看分支歷史**：
   - 切換到「History」分頁
   - 會看到：
     - 最新：`test(temp): add tester...`（feature/celsius-to-fahrenheit 標籤）
     - 其次：`feat(temp): add celsius/fahrenheit...`
     - 最舊：`chore: initialize project...`（main 標籤）

---

### 階段 D：同步與 Push 分支

#### D-1：與 main 對齊（模擬多人協作）
**情境說明**：開發期間，其他人可能已更新 `main`，我們需要先同步。

1. **Fetch 遠端更新**：
   - 點擊工具列「Fetch」按鈕（下載圖示）
   - 會拉取遠端所有分支的最新引用
   - 如果 `origin/main` 有更新，會在歷史圖中顯示

2. **切換到 main 並 Pull**（若有更新）：
   - 左側 BRANCHES → 右鍵 `main` → Checkout
   - 點擊「Pull」按鈕
   - Pull 對話框：
     - **Remote branch to pull**：origin/main
     - **Options**：選擇「Merge」或「Rebase」（建議 Merge）
   - 點擊「OK」

3. **切回功能分支並 Rebase/Merge**：
   - Checkout 回 `feature/celsius-to-fahrenheit`
   - 右鍵 `main` → 選擇「Merge main into current branch」
   - 若無衝突，會自動完成合併

#### D-2：Push 功能分支到 GitHub
1. **Push 分支**：
   - 確保目前在 `feature/celsius-to-fahrenheit`
   - 點擊「Push」按鈕
   - Push 對話框：
     - 勾選 `feature/celsius-to-fahrenheit`
   - 點擊「Push」

2. **驗證**：
   - 前往 GitHub 倉庫頁面
   - 點擊分支下拉選單，應可看到 `feature/celsius-to-fahrenheit`

---

### 階段 E：建立與處理 Pull Request

#### E-1：在 GitHub 建立 Pull Request
1. **開啟 PR 建立頁面**：
   - Push 後，GitHub 會顯示黃色提示條：「feature/celsius-to-fahrenheit had recent pushes」
   - 點擊「Compare & pull request」
   - 或手動：切換到該分支 → 點擊「Contribute」→「Open pull request」

2. **填寫 PR 資訊**：
   - **Title**：`feat: add temperature conversion functionality`
   - **Description**（範例）：
     ```markdown
     ## 變更內容
     - 新增 `Temperature.java`：提供攝氏/華氏轉換方法
     - 新增 `TemperatureTester.java`：測試程式
     
     ## 測試
     - 手動執行 TemperatureTester
     - 驗證：0°C = 32°F、98.6°F = 37°C
     
     ## 風險評估
     - 低風險：純新增功能，不影響既有程式碼
     ```
   - **Reviewers**：指派團隊成員（若為個人練習可略過）
   - **Assignees**：指派給自己
   - **Labels**：選擇 `enhancement`

3. **建立 PR**：
   - 點擊「Create pull request」
   - PR 頁面會顯示 Commits、Files changed、Checks（若有 CI）

#### E-2：審查與修改（模擬 Review 意見）
**情境**：Reviewer 要求新增輸入驗證。

1. **在本機繼續開發**：
   - 確保仍在 `feature/celsius-to-fahrenheit` 分支
   - 修改 `Temperature.java`，加入驗證：
     ```java
     public static double celsiusToFahrenheit(double celsius) {
         if (celsius < -273.15) {
             throw new IllegalArgumentException("溫度不可低於絕對零度 (-273.15°C)");
         }
         return celsius * 9.0 / 5.0 + 32.0;
     }
     ```

2. **Commit 並 Push**：
   - Stage 變更
   - Commit 訊息：`fix(temp): add absolute zero validation`
   - Push（選擇同一分支）

3. **PR 自動更新**：
   - 回到 GitHub PR 頁面
   - 會自動新增該 Commit 到 PR
   - Reviewer 可再次審查

#### E-3：合併 PR
1. **審查通過**：
   - Reviewer 點擊「Approve」
   - 若有 Branch Protection 規則，需滿足條件（如至少 1 個 Approve）

2. **選擇合併策略**：
   - **Merge commit**：保留所有 Commit 歷史（預設）
   - **Squash and merge**：壓縮成單一 Commit（推薦，保持歷史乾淨）
   - **Rebase and merge**：線性歷史

3. **執行合併**：
   - 點擊「Squash and merge」
   - 編輯最終 Commit 訊息：`feat: add temperature conversion functionality (#1)`
   - 點擊「Confirm squash and merge」

4. **刪除遠端分支**：
   - 合併完成後，GitHub 提示「Pull request successfully merged and closed」
   - 點擊「Delete branch」按鈕

---

### 階段 F：本機清理與同步

#### F-1：更新本機 main 分支
1. **Checkout 到 main**：
   - SourceTree 左側 BRANCHES → 右鍵 `main` → Checkout

2. **Pull 最新變更**：
   - 點擊「Pull」按鈕
   - 會拉取合併後的 `main`

3. **查看歷史**：
   - 切換到「History」分頁
   - 會看到 Squash 後的單一 Commit：`feat: add temperature conversion functionality (#1)`

#### F-2：刪除本機功能分支
1. **刪除分支**：
   - 左側 BRANCHES → 右鍵 `feature/celsius-to-fahrenheit`
   - 選擇「Delete feature/celsius-to-fahrenheit」
   - 確認對話框：點擊「OK」

2. **清理遠端追蹤分支**：
   - Repository → Repository Settings → Remotes
   - 點擊「Prune」按鈕（清理已刪除的遠端分支引用）

---

### 階段 G：版本標記與釋出

#### G-1：建立 Tag
1. **在 History 選擇 Commit**：
   - 切換到「History」分頁
   - 右鍵最新的 `main` Commit（含功能合併）

2. **建立 Tag**：
   - 選擇「Tag...」
   - Tag 對話框：
     - **Tag Name**：`v1.0.0`
     - **Tag Message**（選填）：`Release 1.0.0 - Temperature conversion`
   - 點擊「Add Tag」

3. **Push Tag 到 GitHub**：
   - 點擊「Push」按鈕
   - Push 對話框：
     - 勾選「Push all tags」（或僅勾選 `v1.0.0`）
   - 點擊「Push」

#### G-2：在 GitHub 建立 Release
1. **前往 Releases 頁面**：
   - GitHub 倉庫 → 右側「Releases」→「Create a new release」

2. **設定 Release**：
   - **Choose a tag**：選擇 `v1.0.0`
   - **Release title**：`v1.0.0 - Temperature Converter First Release`
   - **Description**：
     ```markdown
     ## 新增功能
     - 攝氏轉華氏轉換
     - 華氏轉攝氏轉換
     - 絕對零度輸入驗證
     
     ## 檔案
     - Temperature.java
     - TemperatureTester.java
     ```
   - 附加檔案（選用）：上傳編譯後的 `.jar` 或原始碼壓縮檔

3. **發布**：
   - 點擊「Publish release」

---

### 階段 H：進階技巧實戰

#### H-1：使用 Stash 暫存未完成工作
**情境**：正在開發新功能 `feature/kelvin-conversion`，突然需要切換分支修補緊急錯誤。

1. **暫存目前變更**：
   - File Status 顯示未提交的變更
   - 點擊工具列「Stash」按鈕（盒子圖示）
   - Stash 對話框：
     - **Message**：`WIP: kelvin conversion in progress`
   - 點擊「OK」

2. **切換分支處理緊急問題**：
   - Checkout 到其他分支（例如 `main` 或 `hotfix/...`）
   - 工作樹乾淨，可安心操作

3. **完成後套用 Stash**：
   - 切回 `feature/kelvin-conversion`
   - 左側「STASHES」區域 → 右鍵最新 Stash
   - 選擇「Apply Stash」（套用但保留 Stash）或「Pop Stash」（套用並刪除）

#### H-2：解決合併衝突
**情境**：兩個分支同時修改 `Temperature.java` 的同一方法。

1. **發現衝突**：
   - Merge 或 Pull 時，SourceTree 顯示「Conflicts」
   - File Status 分頁會標示衝突檔案（黃色驚嘆號）

2. **開啟合併工具**：
   - 右鍵衝突檔案 → 「Resolve Conflicts」→「Launch External Merge Tool」
   - 或直接右鍵 → 「Open in External Editor」手動編輯

3. **解決衝突**：
   - 合併工具顯示三個版本：
     - **Mine**（目前分支）
     - **Theirs**（合併來源）
     - **Base**（共同祖先）
   - 手動選擇保留的程式碼或合併兩者
   - 儲存並關閉

4. **標記已解決**：
   - 回到 SourceTree → 右鍵檔案 → 「Mark Resolved」
   - 所有衝突解決後，點擊「Commit」完成合併

#### H-3：查看與比較歷史
1. **查看特定 Commit 的變更**：
   - History 分頁 → 點選任一 Commit
   - 下方會顯示該 Commit 的檔案變更列表
   - 點擊檔案可查看 Diff（綠色 +新增、紅色 -刪除）

2. **比較兩個 Commit**：
   - 按住 Ctrl，點選兩個 Commit
   - 右鍵 → 「Diff Selected」
   - 會顯示兩者之間的所有差異

3. **Blame（追蹤程式碼作者）**：
   - File Status → 右鍵檔案 → 「Blame」
   - 或透過選單：Actions → Blame
   - 會顯示每一行的最後修改者與 Commit

---

### 階段 I：多遠端協作（Fork 與 Upstream）

#### I-1：Fork 開源專案
1. **在 GitHub Fork**：
   - 前往目標開源專案（例如 `https://github.com/original/repo`）
   - 點擊右上角「Fork」
   - Fork 會建立到你的帳號：`https://github.com/你的帳號/repo`

2. **Clone Fork 到本機**：
   - SourceTree → Clone → 貼上你 Fork 的 URL
   - Clone 後會自動建立 `origin` 指向你的 Fork

---
