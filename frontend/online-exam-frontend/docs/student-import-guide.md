# 學生資料批次匯入 — 使用說明與範例文件

> 本文件說明線上測驗系統「批次匯入學生帳號」功能的檔案格式、操作步驟與範例。
> 匯入範例檔可於場地「學生管理 → 批次匯入」頁面點「⬇ 下載匯入範例檔」取得，
> 檔名為 `student-import-template.csv`。

## 1. 功能對應

| 層 | 檔案 | 說明 |
|----|------|------|
| 後端 API | `online-exam-api/.../controller/UserController.java` | `POST /api/students/batch`（教師限定） |
| 後端 DTO | `BatchStudentsRequest` / `BatchStudentsResponse` | 批次請求與匯入結果 |
| 前端 | `online-exam-frontend/src/pages/teacher/StudentListPage.jsx` | 「批次匯入」面板（上傳/貼上/預覽/結果） |
| 前端 API | `online-exam-frontend/src/api/examApi.js` | `batchImportStudents(token, students)` |
| 範例檔 | `online-exam-frontend/public/examples/student-import-template.csv` | 可下載之匯入範例 |

## 2. 檔案格式（CSV）

- 編碼：UTF-8。
- 每一列一位學生，欄位順序固定為：`帳號, 姓名, 班級, 密碼`。
- 首列可為標題列（`帳號,姓名,班級,密碼`），系統會自動略過；亦可直接從資料列開始。
- 班級可留空（但仍建議填寫，方便教師依班級篩選）。
- 若欄位內容含逗號或雙引號，請以雙引號包夾（標準 CSV 規則）。

### 欄位規格（與單筆新增相同）

| 欄位 | 規則 |
|------|------|
| 帳號 | 必填，3–50 字元，全系統唯一 |
| 姓名 | 必填，2–100 字元 |
| 班級 | 選填，≤50 字元 |
| 密碼 | 必填，≥6 字元 |

## 3. 範例一：標準檔（含標題列）

```csv
帳號,姓名,班級,密碼
s109001,王小明,資工一甲,password123
s109002,陳小美,資工一甲,password123
s109003,林小華,資工一乙,password456
```

## 4. 範例二：純資料列（無標題列）

```csv
s110001,周大鵬,資工二丙,pass123456
s110002,吳小婷,資工二丙,pass123456
s110003,鄭小安,資工二丙,pass123456
```

## 5. 操作步驟

1. 以**教師**帳號登入，進入「學生管理」頁。
2. 點「⬆ 批次匯入」。
3. 下載匯入範例檔（或另存上述範例內容到 `.csv` 檔案）。
4. 點選「選擇檔案」上傳，或將內容直接貼到文字框。
5. 系統會先顯示**預覽表**與逐列檢查結果（缺欄位/過短/檔內重複會以紅字提示）。
6. 點「開始匯入（N 位）」。

## 6. 匯入結果說明

- 成功筆數：新增的學生數。
- 跳過筆數：因「帳號已存在於系統」或「該檔內重複」而未建立，並列出對應帳號。
- 若某筆資料不符欄位驗證規則（如密碼少於 6 字元），整批請求會回傳 400 並附欄位錯誤訊息，不會留下部分資料。

## 7. API 參考

### 請求

```
POST /api/students/batch
Authorization: Bearer <teacher token>
Content-Type: application/json
```

```json
{
  "students": [
    { "username": "s109001", "password": "password123", "displayName": "王小明", "className": "資工一甲" },
    { "username": "s109002", "password": "password123", "displayName": "陳小美", "className": "資工一甲" }
  ]
}
```

### 回應（201 Created）

```json
{
  "imported": 2,
  "students": [
    { "id": 6, "username": "s109001", "displayName": "王小明", "className": "資工一甲" },
    { "id": 7, "username": "s109002", "displayName": "陳小美", "className": "資工一甲" }
  ],
  "duplicates": []
}
```

若批次中有重複帳號（已存在/檔內重複），其帳號會出現在 `duplicates`，其餘照常建立。