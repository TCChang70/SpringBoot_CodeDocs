# SourceTree 多人協作工作流程文件

> 本文件以商業團隊實際開發流程為基礎，說明如何透過 SourceTree 搭配 Git Flow 分支策略，管理多名開發者協同開發的完整操作步驟。

---

## 目錄

1. [分支策略概覽（Git Flow）](#1-分支策略概覽git-flow)
2. [環境準備與初始設定](#2-環境準備與初始設定)
3. [主分支建立與初始程式碼](#3-主分支建立與初始程式碼)
4. [功能分支建立（三位開發者）](#4-功能分支建立三位開發者)
5. [各開發者獨立開發](#5-各開發者獨立開發)
6. [提交 Pull Request（PR）](#6-提交-pull-requestpr)
7. [Code Review 與 Merge](#7-code-review-與-merge)
8. [衝突解決（Conflict Resolution）](#8-衝突解決conflict-resolution)
---

## 1. 分支策略概覽（Git Flow）

商業開發標準採用 **Git Flow** 分支模型，以下為各分支職責：

```
main (生產環境)
  └── release/x.x.x (發版準備)
        └── develop (開發主線)
              ├── feature/A-v1  (開發者 A 功能)
              ├── feature/B-v1  (開發者 B 功能)
              └── feature/C-v1  (開發者 C 功能)
hotfix/xxx (緊急修復，從 main 分出)
```

| 分支類型 | 命名範例 | 目的 |
|---|---|---|
| `main` | `main` | 生產穩定版本，只接受 merge |
| `develop` | `develop` | 所有功能整合測試基準 |
| `feature` | `feature/A-v1` | 單一功能開發，完成後合併回 develop |
| `release` | `release/1.0.0` | 發版前最終測試與修正 |
| `hotfix` | `hotfix/fix-login` | 生產環境緊急修復 |

---

## 2. 環境準備與初始設定

### 2.1 安裝 SourceTree

1. 前往 [https://www.sourcetreeapp.com](https://www.sourcetreeapp.com) 下載安裝
2. 登入 Atlassian 帳號（免費）
3. 設定 Git 使用者資訊

### 2.2 SourceTree 全域設定

```
工具列 → Tools → Options → General
  Name: 你的名稱（例如：Developer-A）
  Email: your_email@company.com
```

### 2.3 SSH Key 設定（推薦）

```
工具列 → Tools → Create or Import SSH Keys
  → Generate（產生 RSA 2048 Key）
  → Save public key 到 GitHub/GitLab SSH Keys 設定
  → Save private key 到本機（.ppk 格式）
```

### 2.4 Clone 遠端倉庫

```
SourceTree 主畫面
  → New → Clone from URL
  → Source URL: https://github.com/your-org/your-project.git
  → Destination Path: D:\Projects\your-project
  → Clone
```

---

## 3. 主分支建立與初始程式碼

### 3.1 主分支（main）初始程式碼

由 Tech Lead 或 Repository Owner 建立主分支基礎程式碼：

**HelloWorld.java**
```java
public class HelloWorld {
    public static void main(String[] args) {
        System.out.println("Hello, World!");
    }
}
```

### 3.2 在 SourceTree 提交初始 Commit

```
左側 WORKSPACE → File Status（未追蹤檔案顯示在此）
  → 勾選所有新增檔案（Stage All）
  → 下方 Commit message 欄位輸入：
      "Initial commit: Add HelloWorld entry point"
  → 點擊 Commit 按鈕
```

### 3.3 推送至遠端 main

```
工具列 → Push
  → Remote: origin
  → Branch: main
  → Push
```

### 3.4 建立 develop 分支（整合基準線）

```
工具列 → Branch
  → New Branch: develop
  → Checkout New Branch: ✅
  → Create Branch

工具列 → Push → Branch: develop → Push
```

---

## 4. 功能分支建立（三位開發者）

> 每位開發者在自己的本機 Clone 後，從 `develop` 建立各自的功能分支。

### 4.1 開發者 A 建立 feature/A-v1

```
SourceTree 左側 → BRANCHES → 右鍵 develop → Checkout develop

工具列 → Branch
  → New Branch: feature/A-v1
  → Checkout New Branch: ✅
  → Create Branch
```

### 4.2 開發者 B 建立 feature/B-v1

```
（同上操作，Branch Name: feature/B-v1）
```

### 4.3 開發者 C 建立 feature/C-v1

```
（同上操作，Branch Name: feature/C-v1）
```

### 4.4 分支狀態確認

完成後，左側 `BRANCHES` 應顯示：
```
▼ LOCAL
    main
    develop
  ▶ feature/A-v1   ← 目前所在（粗體）
    feature/B-v1
    feature/C-v1
```

---

## 5. 各開發者獨立開發

### 5.1 開發者 A（feature/A-v1）

在 `feature/A-v1` 分支新增 **ClassA.java**：

```java
public class ClassA {
    private String name;
    private int version;

    public ClassA(String name, int version) {
        this.name = name;
        this.version = version;
    }

    public String getInfo() {
        return name + " - v" + version;
    }
}
```

**SourceTree 提交步驟：**
```
File Status → 勾選 ClassA.java → Stage Selected
Commit message: "feat(A): Add ClassA with name and version fields"
→ Commit
→ Push → feature/A-v1
```

### 5.2 開發者 B（feature/B-v1）

在 `feature/B-v1` 分支新增 **ClassB.java**：

```java
public class ClassB {
    private String category;

    public ClassB(String category) {
        this.category = category;
    }

    public String getCategory() {
        return category;
    }

    public void process() {
        System.out.println("Processing category: " + category);
    }
}
```

**SourceTree 提交步驟：**
```
File Status → 勾選 ClassB.java → Stage Selected
Commit message: "feat(B): Add ClassB with category processing logic"
→ Commit
→ Push → feature/B-v1
```

### 5.3 開發者 C（feature/C-v1）

在 `feature/C-v1` 分支新增 **ClassC.java**：

```java
public class ClassC {
    private boolean active;

    public ClassC(boolean active) {
        this.active = active;
    }

    public boolean isActive() {
        return active;
    }

    public void toggleStatus() {
        active = !active;
        System.out.println("Status changed to: " + active);
    }
}
```

**SourceTree 提交步驟：**
```
File Status → 勾選 ClassC.java → Stage Selected
Commit message: "feat(C): Add ClassC with active status toggle"
→ Commit
→ Push → feature/C-v1
```

### 5.4 Commit Message 商業規範（Conventional Commits）

```
格式：<type>(<scope>): <subject>

type:
  feat     → 新功能
  fix      → 修正 Bug
  refactor → 重構（不影響功能）
  docs     → 文件更新
  test     → 測試相關
  chore    → 設定、建置工具

範例：
  feat(auth): Add JWT token validation
  fix(api): Handle null response from user service
  docs(readme): Update installation instructions
```

---

## 6. 提交 Pull Request（PR）

> 商業流程中，功能分支開發完成後不直接 merge，需經過 **Pull Request → Code Review → Approve → Merge** 流程。

### 6.1 在 GitHub/GitLab 建立 PR

1. 前往 GitHub/GitLab 網站
2. 點擊 **"Compare & pull request"**（通常推送後會自動出現提示）
3. 填寫 PR 表單：

```
Title: [Feature] Add ClassA with version management
Base branch: develop
Compare branch: feature/A-v1

Description:
## 變更說明
- 新增 ClassA 類別
- 包含 name、version 屬性
- 提供 getInfo() 方法

## 測試項目
- [ ] 本機編譯通過
- [ ] Unit Test 通過
- [ ] 無 Lint 錯誤

Reviewers: @tech-lead, @developer-B
Labels: feature, review-needed
```

### 6.2 三個功能分支 PR 一覽

| PR | 標題 | 來源分支 | 目標分支 | 狀態 |
|---|---|---|---|---|
| #1 | Add ClassA | feature/A-v1 | develop | 待審查 |
| #2 | Add ClassB | feature/B-v1 | develop | 待審查 |
| #3 | Add ClassC | feature/C-v1 | develop | 待審查 |

---

## 7. Code Review 與 Merge

### 7.1 Reviewer 審查流程

```
GitHub PR 頁面
  → Files changed（查看差異）
  → 點擊行號旁 + 號 → 新增 Review Comment
  → 全部審查完成後：
      Review changes → Approve / Request changes → Submit review
```

### 7.2 PR Approve 後在 SourceTree 執行 Merge

**方式一：在 GitHub 網頁直接 Merge（推薦）**
```
PR 頁面 → Merge pull request → Confirm merge
選項：
  - Create a merge commit（保留所有 commit 歷史）✅ 商業建議
  - Squash and merge（將多個 commit 合併為一）
  - Rebase and merge（線性歷史）
```

**方式二：在 SourceTree 本機 Merge**
```
切換到 develop 分支：
  左側 BRANCHES → 雙擊 develop

合併 feature/A-v1：
  左側 BRANCHES → 右鍵 feature/A-v1 → Merge feature/A-v1 into current branch
  選項：
    ✅ Create a commit even if fast forward is possible
  → OK → Push develop
```

### 7.3 Merge 完成後刪除功能分支

```
遠端分支刪除：
  GitHub PR 頁面 → Delete branch（PR 合併後出現按鈕）

本機分支刪除（SourceTree）：
  左側 BRANCHES → 右鍵 feature/A-v1 → Delete → Force Delete ✅
```

---

## 8. 衝突解決（Conflict Resolution）

> 當兩位開發者修改同一個檔案的相同區段時，merge 會產生衝突，必須手動解決。

### 8.1 衝突情境模擬

開發者 A 和開發者 B 都修改了 `HelloWorld.java`：

**feature/A-v1 的版本：**
```java
public class HelloWorld {
    public static void main(String[] args) {
        System.out.println("Hello from A!");  // A 的修改
    }
}
```

**feature/B-v1 的版本：**
```java
public class HelloWorld {
    public static void main(String[] args) {
        System.out.println("Hello from B!");  // B 的修改
    }
}
```

### 8.2 SourceTree 衝突標示

當 A 合併完後，B 嘗試合併時，SourceTree 會顯示：

```
File Status
  ⚠️ HelloWorld.java  [Conflicted]
```

### 8.3 解決衝突步驟

**步驟 1：開啟衝突檔案**
```
SourceTree File Status
  → 右鍵 HelloWorld.java
  → Resolve Conflicts → Launch External Merge Tool
  （或選擇 Open in Editor 手動編輯）
```

**步驟 2：衝突標記說明**
```java
public class HelloWorld {
    public static void main(String[] args) {
<<<<<<< HEAD (develop)
        System.out.println("Hello from A!");
=======
        System.out.println("Hello from B!");
>>>>>>> feature/B-v1
    }
}
```

**步驟 3：與開發者溝通後，確定保留內容**
```java
// 協商後保留兩者訊息
public class HelloWorld {
    public static void main(String[] args) {
        System.out.println("Hello from A and B!");
    }
}
```

**步驟 4：標記衝突已解決並提交**
```
SourceTree → File Status
  → 右鍵 HelloWorld.java → Mark Resolved
  → Stage All
  → Commit message: "merge: Resolve conflict in HelloWorld - combine A and B output"
  → Commit → Push
```

---
