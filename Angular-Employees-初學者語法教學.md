# Angular 初學者語法教學 — 以員工管理系統為例

> **對象**：第一次學 Angular，有基本 TypeScript / HTML 基礎  
> **範例專案**：`mysql employees.md` 的 Angular 17+ 員工管理系統（Standalone Components）  
> **學習目標**：看懂並能自己動手寫員工管理系統的 Angular 前端程式碼

---

## 目錄

1. [Angular 17 核心觀念速覽](#1-angular-17-核心觀念速覽)
2. [Interface — 資料型別定義](#2-interface--資料型別定義)
3. [Service — 與後端溝通](#3-service--與後端溝通)
4. [Standalone Component — 畫面元件](#4-standalone-component--畫面元件)
5. [Signal — 反應式狀態管理](#5-signal--反應式狀態管理)
6. [Template 樣板語法（新式控制流）](#6-template-樣板語法新式控制流)
7. [Routing — 頁面導航](#7-routing--頁面導航)
8. [Reactive Forms — 表單驗證](#8-reactive-forms--表單驗證)
9. [完整資料流程圖](#9-完整資料流程圖)
10. [常見錯誤速查表](#10-常見錯誤速查表)
11. [練習題（Easy → Hard）](#11-練習題easy--hard)

---

## 1. Angular 17 核心觀念速覽

### 白話解釋

Angular 是 Google 推出的前端框架，用來建立「單頁應用程式（SPA, Single Page Application）」。

```
使用者的瀏覽器
┌────────────────────────────────────────────────────────┐
│  Angular App（port 4200）                              │
│                                                        │
│  ┌─────────────┐   inject   ┌──────────────────────┐  │
│  │  Component  │ ─────────► │      Service         │  │
│  │  (畫面邏輯) │ ◄───────── │   (HTTP 呼叫廚房)   │  │
│  └──────┬──────┘  Observable└──────────┬─────────── ┘  │
│         │ 綁定                         │ HttpClient     │
│  ┌──────▼──────┐               ┌───────▼──────────┐    │
│  │  Template   │               │  Spring Boot API │    │
│  │  (HTML 畫面)│               │  (port 8080)     │    │
│  └─────────────┘               └──────────────────┘    │
└────────────────────────────────────────────────────────┘
```

| 角色 | Angular 對應 | 員工系統例子 |
|------|-------------|------------|
| 資料格式定義 | `Interface` | `Employee`, `EmployeeRequest` |
| 畫面顯示 | `Component` + `Template` | `EmployeeListComponent` |
| HTTP 資料存取 | `Service` | `EmployeeService` |
| 頁面路徑設定 | `Routes` | `/employees`, `/employees/new` |

### Angular 17 vs 舊版重要差異

| 功能 | 舊版（≤16） | 新版（17+）|
|------|------------|-----------|
| 元件宣告 | `NgModule` | Standalone（獨立元件）|
| 條件顯示 | `*ngIf` | `@if` / `@else` |
| 列表渲染 | `*ngFor` | `@for` + `track` |
| 狀態管理 | 一般 class 屬性 | `signal<T>()` |
| 依賴注入 | `constructor(private ...)` | `inject()` 函式 |

---

## 2. Interface — 資料型別定義

### 概念說明

**Interface（介面）** 是 TypeScript 的型別藍圖，告訴程式碼「這份資料長什麼形狀」。對應後端 `Employee` 資料表的欄位。

### 語法結構

```typescript
export interface 介面名稱 {
  欄位名: 型別;
  選填欄位?: 型別;   // ? 代表可以不存在
  可為null欄位: 型別 | null;
}
```

### 員工系統實際程式碼

```typescript
// src/app/models/employee.model.ts

// ① 顯示用（對應後端 EmployeeResponse）
export interface Employee {
  employeeNumber: number;
  lastName:       string;
  firstName:      string;
  extension:      string;
  email:          string;
  officeCode:     string;
  reportsTo:      number | null;  // 總裁沒有上司，所以可以是 null
  jobTitle:       string;
  fullName?:      string;         // ? = 選填，後端衍生欄位
}

// ② 新增/修改用（對應後端 EmployeeRequest）
export interface EmployeeRequest {
  employeeNumber: number;
  lastName:       string;
  firstName:      string;
  extension:      string;
  email:          string;
  officeCode:     string;
  reportsTo:      number | null;
  jobTitle:       string;
}
```

### 為什麼要分兩個 Interface？

```
後端回傳（Employee）：包含 fullName（後端計算好的全名）
前端送出（EmployeeRequest）：不包含 fullName（前端不送衍生欄位）

這樣做可以精準控制「送出去的資料」和「收到的資料」的格式
```

### 常見陷阱

```typescript
// ❌ 直接用 any 型別 — 失去型別保護
let emp: any = { employeeNumber: 1 };
emp.nonExistField = 'oops';   // 不會報錯，但邏輯錯誤

// ✅ 使用 Interface — 有錯馬上提示
let emp: Employee = { employeeNumber: 1 };
// 錯誤：Property 'lastName' is missing in type '...'
```

> **現在試試看**：在 `employee.model.ts` 新增一個 `ApiError` interface，包含 `success: boolean`、`message: string`、`timestamp: string` 三個欄位。

---

## 3. Service — 與後端溝通

### 概念說明

**Service（服務）** 是專門處理「資料取得與處理」的 class，把 HTTP API 呼叫集中管理，讓 Component 只需要呼叫方法，不用在意底層細節。

### 語法結構

```typescript
import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

@Injectable({ providedIn: 'root' })   // ← 全域單例（整個 App 共用一份）
export class MyService {

  private http = inject(HttpClient);  // ← 注入 HttpClient（用來發 HTTP 請求）

  getData(): Observable<型別> {       // ← 回傳 Observable（非同步資料流）
    return this.http.get<型別>('URL');
  }
}
```

### 員工系統實際程式碼解析

```typescript
// src/app/core/services/employee.service.ts
import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Employee, EmployeeRequest } from '../../models/employee.model';

@Injectable({ providedIn: 'root' })
export class EmployeeService {

  // ① 後端 API 的基礎路徑
  private readonly BASE_URL = 'http://localhost:8080/api/v1/employees';

  // ② 使用 inject() 注入 HttpClient（Angular 17+ 推薦寫法）
  private http = inject(HttpClient);

  /** 取得全部員工 → GET /api/v1/employees */
  getAll(): Observable<Employee[]> {
    return this.http.get<Employee[]>(this.BASE_URL);
    //              ↑ 泛型：告訴 TypeScript 這個 API 回傳的是 Employee 陣列
  }

  /** 取得單一員工 → GET /api/v1/employees/1234 */
  getById(id: number): Observable<Employee> {
    return this.http.get<Employee>(`${this.BASE_URL}/${id}`);
    //                              ↑ Template literal 字串拼接
  }

  /** 依全名搜尋 → GET /api/v1/employees/search?name=Tom */
  search(name: string): Observable<Employee[]> {
    const params = new HttpParams().set('name', name);
    //                              ↑ 安全地加入 Query String 參數
    return this.http.get<Employee[]>(`${this.BASE_URL}/search`, { params });
  }

  /** 新增員工 → POST /api/v1/employees（帶 JSON body） */
  create(payload: EmployeeRequest): Observable<Employee> {
    return this.http.post<Employee>(this.BASE_URL, payload);
  }

  /** 更新員工 → PUT /api/v1/employees/1234（帶 JSON body） */
  update(id: number, payload: EmployeeRequest): Observable<Employee> {
    return this.http.put<Employee>(`${this.BASE_URL}/${id}`, payload);
  }

  /** 刪除員工 → DELETE /api/v1/employees/1234 */
  delete(id: number): Observable<void> {
    return this.http.delete<void>(`${this.BASE_URL}/${id}`);
    //                      ↑ void = 成功時後端不回傳任何 body（HTTP 204）
  }
}
```

### Observable 是什麼？

```
Observable（可觀察的資料流）= 還沒發生、等待中的非同步事件

你可以把它想像成「外賣訂單」：
  訂單建立 = http.get(...)     ← 建立 Observable（還沒送請求！）
  等待送達 = .subscribe(...)   ← 訂閱（這時才真正發 HTTP 請求）
  餐點送到 = next: (data) =>   ← 成功回呼
  訂單失敗 = error: (err) =>   ← 失敗回呼
```

### 啟用 HttpClient（必做設定）

```typescript
// src/app/app.config.ts  （Angular 17 Standalone App 的設定入口）
import { ApplicationConfig } from '@angular/core';
import { provideRouter } from '@angular/router';
import { provideHttpClient } from '@angular/common/http';
import { routes } from './app.routes';

export const appConfig: ApplicationConfig = {
  providers: [
    provideRouter(routes),
    provideHttpClient(),    // ← 沒有這行，HttpClient 無法注入！
  ],
};
```

### 常見陷阱

```typescript
// ❌ 忘記 .subscribe() → Observable 不會執行（沒有發 HTTP 請求）
this.employeeService.getAll();   // 什麼事都沒發生！

// ✅ 一定要 .subscribe() 才會觸發
this.employeeService.getAll().subscribe({
  next:  (data) => console.log(data),
  error: (err)  => console.error(err),
});
```

> **現在試試看**：在 `EmployeeService` 新增一個 `getByOffice(officeCode: string): Observable<Employee[]>` 方法，呼叫 `GET /api/v1/employees/search` 並帶入 officeCode 參數。

---

## 4. Standalone Component — 畫面元件

### 概念說明

**Standalone Component（獨立元件）** 是 Angular 17 的主流寫法。每個元件自己宣告需要哪些模組（`imports`），不再需要統一的 `NgModule` 管理。

### 語法結構

```typescript
import { Component, inject, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-xxx',           // ① 在 HTML 中用 <app-xxx> 引用
  standalone: true,              // ② 宣告為 Standalone（必填）
  imports: [CommonModule, ...],  // ③ 需要用到的模組
  templateUrl: './xxx.html',     // ④ 對應的 HTML 畫面檔案
})
export class XxxComponent implements OnInit {

  // 依賴注入（inject 函式寫法）
  private service = inject(XxxService);

  // Signal 狀態（詳見第 5 章）
  data = signal<型別[]>([]);

  // 生命週期鉤子：元件初始化完成後執行
  ngOnInit(): void {
    // 載入初始資料
  }
}
```

### 員工列表元件解析

```typescript
// src/app/features/employees/employee-list/employee-list.component.ts
import { Component, inject, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { EmployeeService } from '../../../core/services/employee.service';
import { Employee } from '../../../models/employee.model';

@Component({
  selector: 'app-employee-list',
  standalone: true,
  imports: [
    CommonModule,   // 提供 @if / @for / DatePipe 等基礎功能
    FormsModule,    // 提供 [(ngModel)] 雙向綁定（搜尋欄位用）
  ],
  templateUrl: './employee-list.component.html',
})
export class EmployeeListComponent implements OnInit {

  // ── 依賴注入（不再用 constructor 參數，改用 inject 函式）──
  private employeeService = inject(EmployeeService);
  private router           = inject(Router);

  // ── 狀態（Signal，詳見第 5 章）─────────────────────────
  employees  = signal<Employee[]>([]);   // 員工列表資料
  isLoading  = signal(false);            // 是否顯示「載入中」
  errorMsg   = signal('');               // 錯誤訊息（空字串=沒錯誤）

  // ── 一般屬性（用於 ngModel 雙向綁定）─────────────────
  searchName = '';

  // ── 生命週期鉤子 ──────────────────────────────────────
  ngOnInit(): void {
    this.loadAll();   // ← 元件第一次顯示時自動呼叫
  }

  // ── 方法 ─────────────────────────────────────────────
  loadAll(): void {
    this.isLoading.set(true);                  // 顯示「載入中」
    this.employeeService.getAll().subscribe({
      next:  (data) => {
        this.employees.set(data);              // 存入資料
        this.isLoading.set(false);             // 隱藏「載入中」
      },
      error: (err) => {
        this.errorMsg.set(err.error?.message ?? '載入失敗');
        //                          ↑ ?. = 可選鏈，err.error 可能是 undefined
        //                                      ?? = 空值合併，前面為 undefined 才用後面值
        this.isLoading.set(false);
      },
    });
  }

  onDelete(id: number): void {
    if (!confirm(`確定要刪除員工 #${id}？`)) return;  // 使用者取消就不執行
    this.employeeService.delete(id).subscribe({
      next:  () => this.loadAll(),              // 刪除成功 → 重新載入列表
      error: (err) => this.errorMsg.set(err.error?.message ?? '刪除失敗'),
    });
  }

  onEdit(id: number): void {
    this.router.navigate(['/employees', id]);  // 導航到編輯頁
  }

  onAdd(): void {
    this.router.navigate(['/employees/new']); // 導航到新增頁
  }
}
```

### Lifecycle Hooks 生命週期

```
元件創建階段
  ↓ constructor()   → 依賴注入發生，通常不在這裡寫邏輯
  ↓ ngOnInit()      ← 最常用！初始資料在這裡載入（DOM 已準備好）
  ↓
元件更新階段
  ↓ ngOnChanges()   → @Input 輸入值變化時觸發
  ↓
元件銷毀階段
  ↓ ngOnDestroy()   → 元件從畫面移除前觸發（清除訂閱、計時器）
```

---

## 5. Signal — 反應式狀態管理

### 概念說明

**Signal（信號）** 是 Angular 17 引入的新狀態管理方式。當 Signal 的值改變時，畫面會**自動更新**。

### 語法結構

```typescript
import { signal } from '@angular/core';

// ① 建立 Signal
const count = signal(0);            // 初始值為 0，型別自動推斷為 number
const name  = signal<string>('');   // 明確指定泛型型別

// ② 讀取值（呼叫函式）
console.log(count());   // ← 像呼叫函式一樣讀取（注意括號！）

// ③ 修改值
count.set(5);           // 設定為新值
count.update(v => v + 1);  // 根據舊值計算新值
```

### 員工系統實際用法

```typescript
// employee-list.component.ts
employees  = signal<Employee[]>([]);  // 型別：Employee 陣列，初始為空陣列
isLoading  = signal(false);           // 型別：boolean，初始為 false
errorMsg   = signal('');              // 型別：string，初始為空字串

// 修改
this.employees.set(data);             // 將員工列表設為 data
this.isLoading.set(true);             // 顯示載入狀態
this.errorMsg.set('載入失敗');         // 設定錯誤訊息

// 在 Template 中讀取（記得加括號！）
// {{ employees().length }}
// @if (isLoading()) { ... }
// @for (emp of employees(); ...) { ... }
```

### Signal vs 一般屬性

```typescript
// 一般屬性（舊寫法）：Angular 要做「髒值檢查」才知道值變了，效能較差
employees: Employee[] = [];
this.employees = data;          // 直接賦值

// Signal（新寫法）：Angular 精確知道哪個 Signal 改變，直接更新對應畫面
employees = signal<Employee[]>([]);
this.employees.set(data);       // 使用 .set()
```

### 常見陷阱

```typescript
// ❌ 忘記加括號讀取 Signal（在 Template 中）
@if (isLoading) { ... }     // 永遠是真（isLoading 是 Signal 物件，不是 boolean）

// ✅ 正確：加括號呼叫
@if (isLoading()) { ... }   // 呼叫 Signal 取得真正的 boolean 值
```

---

## 6. Template 樣板語法（新式控制流）

### 概念說明

Angular 17 引入全新的**控制流語法**（`@if`、`@for`），取代舊版的 `*ngIf`、`*ngFor` 指令。語法更接近一般程式語言，更易讀。

### 語法對照表

| 功能 | 舊寫法（≤16） | 新寫法（17+）|
|------|-------------|------------|
| 條件顯示 | `<div *ngIf="條件">` | `@if (條件) { ... }` |
| 條件否則 | `<ng-template #else>` | `@else { ... }` |
| 列表渲染 | `*ngFor="let x of list"` | `@for (x of list; track x.id)` |
| 空列表處理 | 手動寫 `*ngIf` | `@empty { ... }` |

### 員工列表 HTML 完整解析

```html
<!-- employee-list.component.html -->
<div class="container mt-4">
  <h2>員工管理</h2>

  <!-- ① 搜尋列：[(ngModel)] 雙向綁定 + 事件綁定 -->
  <div class="d-flex gap-2 mb-3">
    <input class="form-control w-auto"
           [(ngModel)]="searchName"
           placeholder="搜尋姓名..."
           (keyup.enter)="onSearch()" />
    <!--   ↑ 按 Enter 鍵觸發 onSearch()  -->

    <button class="btn btn-outline-primary" (click)="onSearch()">搜尋</button>
    <button class="btn btn-success ms-auto" (click)="onAdd()">＋ 新增員工</button>
  </div>

  <!-- ② @if：有錯誤訊息才顯示警告框 -->
  @if (errorMsg()) {
    <div class="alert alert-danger">{{ errorMsg() }}</div>
    <!--                              ↑ {{ }} 插值：顯示 Signal 的字串值 -->
  }

  <!-- ③ @if：載入中顯示提示 -->
  @if (isLoading()) {
    <p>載入中...</p>
  }

  <!-- ④ 資料表格（非載入中才顯示） -->
  @if (!isLoading()) {
    <table class="table table-hover table-bordered">
      <thead class="table-dark">
        <tr>
          <th>#</th>
          <th>全名</th>
          <th>分機</th>
          <th>Email</th>
          <th>辦公室</th>
          <th>職稱</th>
          <th>操作</th>
        </tr>
      </thead>
      <tbody>
        <!-- ⑤ @for：迴圈渲染每位員工 -->
        @for (emp of employees(); track emp.employeeNumber) {
        <!--              ↑ Signal 加()  track = 追蹤唯一識別欄位，提升渲染效能 -->
          <tr>
            <td>{{ emp.employeeNumber }}</td>
            <td>{{ emp.fullName }}</td>
            <td>{{ emp.extension }}</td>
            <td>{{ emp.email }}</td>
            <td>{{ emp.officeCode }}</td>
            <td>{{ emp.jobTitle }}</td>
            <td>
              <!-- ⑥ 事件綁定：傳入員工編號 -->
              <button class="btn btn-sm btn-warning me-1"
                      (click)="onEdit(emp.employeeNumber)">編輯</button>
              <button class="btn btn-sm btn-danger"
                      (click)="onDelete(emp.employeeNumber)">刪除</button>
            </td>
          </tr>

        } @empty {
          <!-- ⑦ @empty：列表為空時顯示 -->
          <tr><td colspan="7" class="text-center">無資料</td></tr>
        }
      </tbody>
    </table>
  }
</div>
```

### 五大 Template 語法速查

| 語法 | 用途 | 員工系統範例 |
|------|------|------------|
| `{{ 變數 }}` | 顯示值（插值） | `{{ emp.fullName }}` |
| `[屬性]="值"` | 動態設定 HTML 屬性 | `[disabled]="isEditing()"` |
| `(事件)="方法()"` | 監聽 DOM 事件 | `(click)="onDelete(emp.employeeNumber)"` |
| `[(ngModel)]="屬性"` | 雙向綁定（需要 FormsModule） | `[(ngModel)]="searchName"` |
| `[formGroup] / formControlName` | 響應式表單綁定 | `[formGroup]="form"` |

---

## 7. Routing — 頁面導航

### 概念說明

**Routing（路由）** 讓 Angular SPA 模擬多頁面的行為。不同的 URL 對應不同的 Component，但頁面不重新載入。

### 路由設定

```typescript
// src/app/app.routes.ts
import { Routes } from '@angular/router';
import { EmployeeListComponent } from './features/employees/employee-list/employee-list.component';
import { EmployeeFormComponent } from './features/employees/employee-form/employee-form.component';

export const routes: Routes = [
  // 根路徑 → 自動跳轉到 /employees
  { path: '',              redirectTo: 'employees', pathMatch: 'full' },
  // /employees → 顯示員工列表
  { path: 'employees',     component: EmployeeListComponent },
  // /employees/new → 顯示新增表單
  { path: 'employees/new', component: EmployeeFormComponent },
  // /employees/1234 → 顯示編輯表單（:id 是動態路徑參數）
  { path: 'employees/:id', component: EmployeeFormComponent },
];
```

### 程式碼導航（Router）

```typescript
// 在 Component 中注入 Router
private router = inject(Router);

// 導航到固定路徑
this.router.navigate(['/employees']);

// 導航到動態路徑（/employees/1234）
this.router.navigate(['/employees', emp.employeeNumber]);

// 導航到新增頁
this.router.navigate(['/employees/new']);
```

### 讀取路徑參數（ActivatedRoute）

```typescript
// employee-form.component.ts
private route = inject(ActivatedRoute);

ngOnInit(): void {
  // 取得 URL 中的 :id 參數
  const id = this.route.snapshot.paramMap.get('id');
  //                    ↑ snapshot = 當前路由狀態快照
  //                                 paramMap = 路徑參數的 Map

  if (id) {
    // URL 是 /employees/1234 → id = '1234'（字串）
    this.employeeId = +id;  // + 號將字串轉為數字
    this.isEditMode.set(true);
    // 載入該員工資料...
  }
  // URL 是 /employees/new → id = null
}
```

### 路由比對順序規則

```typescript
// ⚠️ 順序很重要！/employees/new 必須排在 /employees/:id 前面
// 否則 'new' 會被當成 :id 的值！

{ path: 'employees/new', component: EmployeeFormComponent },  // ← 先比對這個
{ path: 'employees/:id', component: EmployeeFormComponent },  // ← 再比對這個
```

---

## 8. Reactive Forms — 表單驗證

### 概念說明

**Reactive Forms（響應式表單）** 用程式碼定義表單結構與驗證規則，比 `ngModel` 更適合複雜表單。

### 語法結構

```typescript
import { FormBuilder, Validators } from '@angular/forms';

// ① 注入 FormBuilder（表單建構器）
private fb = inject(FormBuilder);

// ② 建立表單群組
form = this.fb.group({
  欄位名: [初始值, [驗證規則...]],
});
```

### 常用 Validators（驗證器）

| Validator | 說明 | 員工系統用途 |
|-----------|------|------------|
| `Validators.required` | 必填 | 所有必填欄位 |
| `Validators.email` | Email 格式 | `email` 欄位 |
| `Validators.min(1)` | 最小值 | `employeeNumber > 0` |
| `Validators.maxLength(50)` | 最大字元數 | `firstName`, `lastName` |

### 員工表單元件解析

```typescript
// employee-form.component.ts

private fb              = inject(FormBuilder);
private employeeService = inject(EmployeeService);
private route           = inject(ActivatedRoute);
private router          = inject(Router);

isEditMode = signal(false);
errorMsg   = signal('');
employeeId: number | null = null;

// ① 定義表單（對應 EmployeeRequest 的每個欄位）
form = this.fb.group({
  employeeNumber: [null as number | null, [Validators.required, Validators.min(1)]],
  firstName:      ['', [Validators.required, Validators.maxLength(50)]],
  lastName:       ['', [Validators.required, Validators.maxLength(50)]],
  extension:      ['', [Validators.required, Validators.maxLength(10)]],
  email:          ['', [Validators.required, Validators.email, Validators.maxLength(100)]],
  officeCode:     ['', [Validators.required, Validators.maxLength(10)]],
  reportsTo:      [null as number | null],   // 無驗證（總裁可不填）
  jobTitle:       ['', [Validators.required, Validators.maxLength(50)]],
});

ngOnInit(): void {
  const id = this.route.snapshot.paramMap.get('id');
  if (id) {
    this.isEditMode.set(true);
    this.employeeId = +id;

    // ② 編輯模式：鎖定員工編號（主鍵不可改）
    this.form.get('employeeNumber')?.disable();

    // ③ 載入現有資料填入表單
    this.employeeService.getById(this.employeeId).subscribe({
      next:  (emp) => this.form.patchValue(emp),
      //                         ↑ patchValue：只更新有對應欄位的值（部分更新）
      error: (err) => this.errorMsg.set(err.error?.message ?? '載入失敗'),
    });
  }
}

onSubmit(): void {
  // ④ 送出前先確認表單是否通過所有驗證
  if (this.form.invalid) {
    this.form.markAllAsTouched();   // 顯示所有未觸碰欄位的錯誤訊息
    return;
  }

  // ⑤ 取得表單值（getRawValue 包含 disabled 欄位）
  const payload = { ...this.form.getRawValue() } as any;

  // ⑥ 判斷新增或更新
  const request$ = this.isEditMode()
    ? this.employeeService.update(this.employeeId!, payload)
    : this.employeeService.create(payload);

  request$.subscribe({
    next:  () => this.router.navigate(['/employees']),  // 成功 → 返回列表
    error: (err) => this.errorMsg.set(err.error?.message ?? '儲存失敗'),
  });
}

// ⑦ 快速存取表單控制器（Template 用）
get f() { return this.form.controls; }
```

### 員工表單 HTML 解析

```html
<!-- employee-form.component.html -->

<!-- [formGroup]：將表單綁定到 TypeScript 的 form 物件 -->
<form [formGroup]="form" (ngSubmit)="onSubmit()">

  <!-- 員工編號欄位 -->
  <div class="mb-3">
    <label class="form-label">員工編號</label>
    <!-- formControlName：指定對應 form.group 中的哪個控制器 -->
    <input class="form-control" formControlName="employeeNumber" type="number" />

    <!-- 只有「欄位有誤 AND 已被觸碰過」才顯示錯誤訊息 -->
    @if (f['employeeNumber'].invalid && f['employeeNumber'].touched) {
      <div class="text-danger small">員工編號為必填正整數</div>
    }
  </div>

  <!-- 動態標題：新增 or 編輯 -->
  <h2>{{ isEditMode() ? '編輯員工' : '新增員工' }}</h2>

  <!-- 送出按鈕：type="submit" 觸發 (ngSubmit) -->
  <button class="btn btn-primary" type="submit">
    {{ isEditMode() ? '更新' : '新增' }}
  </button>
  <button class="btn btn-secondary" type="button" (click)="onCancel()">取消</button>

</form>
```

### `patchValue` vs `setValue`

```typescript
// patchValue：部分更新（只更新有對應 key 的欄位，其餘保持原值）
this.form.patchValue({ firstName: 'John' });   // ✅ 只更新 firstName

// setValue：全量更新（必須包含所有欄位，否則報錯）
this.form.setValue({ firstName: 'John' });     // ❌ 缺少其他欄位會報錯
```

---

## 9. 完整資料流程圖

```
使用者操作（點擊「載入員工列表」）
      │
      ▼
EmployeeListComponent.loadAll()          ← Component 呼叫 Service
      │
      ▼
EmployeeService.getAll()                 ← Service 建立 Observable
      │ .subscribe()
      ▼
HttpClient.get<Employee[]>(BASE_URL)     ← 發出 HTTP GET 請求
      │
      ▼ (非同步等待後端回應)
Spring Boot：GET /api/v1/employees       ← 後端處理
      │ 回傳 JSON 陣列
      ▼
next: (data) =>
  employees.set(data)                    ← Signal 更新
      │
      ▼
Angular 偵測到 Signal 變化               ← 自動重新渲染
      │
      ▼
Template：@for (emp of employees(); ...) ← 畫面顯示員工列表
```

---

## 10. 常見錯誤速查表

| 錯誤訊息 | 原因 | 解決方式 |
|---------|------|---------|
| `NullInjectorError: No provider for HttpClient` | 未啟用 HttpClient | 在 `app.config.ts` 加 `provideHttpClient()` |
| `Can't bind to 'ngModel'` | 未匯入 FormsModule | 在元件 `imports` 加 `FormsModule` |
| `Can't bind to 'formGroup'` | 未匯入 ReactiveFormsModule | 在元件 `imports` 加 `ReactiveFormsModule` |
| `ERROR: 'app-xxx' is not a known element` | 元件未在父元件 imports 宣告 | 將 `XxxComponent` 加入父元件的 `imports` 陣列 |
| `TypeError: this.employees is not a function` | Signal 讀取忘記加括號 | 改為 `this.employees()` |
| `Observable 呼叫後沒反應` | 忘記 `.subscribe()` | 加上 `.subscribe({ next: ... })` |
| CORS 錯誤 | 後端未允許 Angular 來源 | 後端 `CorsConfig` 加入 `http://localhost:4200` |

---

## 11. 練習題（Easy → Hard）

---

### Easy — 新增搜尋功能

**需求**：在 `EmployeeListComponent` 的搜尋列，支援清空搜尋（點擊「清除」按鈕後顯示全部員工）。

**提示**：
- 新增 `onClear()` 方法，將 `searchName` 設為 `''` 並呼叫 `loadAll()`
- 在 Template 新增按鈕 `(click)="onClear()"`

<details>
<summary>查看解答</summary>

```typescript
// employee-list.component.ts
onClear(): void {
  this.searchName = '';
  this.loadAll();
}
```

```html
<!-- employee-list.component.html -->
<button class="btn btn-outline-secondary" (click)="onClear()">清除</button>
```

</details>

---

### Medium — 顯示上司姓名

**需求**：員工列表新增「上司」欄位，若 `reportsTo` 為 `null` 顯示「（無）」，否則顯示上司的員工編號（進階：從列表中查找上司的 `fullName`）。

**提示**：
- 在 Template 使用三元運算子：`{{ emp.reportsTo ?? '（無）' }}`
- 進階：建立 `getManagerName(id: number | null): string` 方法，用 `employees().find(...)` 查找

<details>
<summary>查看解答</summary>

```typescript
// employee-list.component.ts
getManagerName(reportsTo: number | null): string {
  if (reportsTo === null) return '（無）';
  const manager = this.employees().find(e => e.employeeNumber === reportsTo);
  return manager?.fullName ?? `#${reportsTo}`;
}
```

```html
<!-- employee-list.component.html 表格新增欄位 -->
<th>上司</th>
<!-- ... -->
<td>{{ getManagerName(emp.reportsTo) }}</td>
```

</details>

---

### Hard — 新增 officeCode 下拉過濾

**需求**：在員工列表上方新增辦公室代碼的 `<select>` 下拉選單，選擇後自動過濾只顯示該辦公室的員工（前端過濾即可，不需要新的 API）。

**提示**：
1. 新增 `selectedOffice = signal('')` 狀態
2. 建立 `computed(() => ...)` 計算出辦公室代碼的不重複列表
3. 建立 `filteredEmployees = computed(() => ...)` 依 `selectedOffice` 過濾
4. Template 改用 `filteredEmployees()` 渲染表格

<details>
<summary>查看解答</summary>

```typescript
import { Component, inject, OnInit, signal, computed } from '@angular/core';

// ...
selectedOffice = signal('');

officeCodes = computed(() =>
  [...new Set(this.employees().map(e => e.officeCode))].sort()
);

filteredEmployees = computed(() => {
  const office = this.selectedOffice();
  if (!office) return this.employees();
  return this.employees().filter(e => e.officeCode === office);
});
```

```html
<select [(ngModel)]="selectedOfficeValue" (change)="selectedOffice.set(selectedOfficeValue)">
  <option value="">全部辦公室</option>
  @for (code of officeCodes(); track code) {
    <option [value]="code">辦公室 {{ code }}</option>
  }
</select>

<!-- 表格改用 filteredEmployees() -->
@for (emp of filteredEmployees(); track emp.employeeNumber) {
  ...
}
```

</details>

---

## 學習路線圖：下一步

完成本文件後，建議按以下順序繼續學習：

```
基礎（已完成）
  ├── Interface 型別定義
  ├── HttpClient + Observable
  ├── Standalone Component
  ├── Signal 狀態管理
  ├── Template 控制流（@if / @for）
  └── Reactive Forms

核心（下一步）
  ├── Angular Router 進階（守衛 Guard、懶載入 Lazy Loading）
  ├── RxJS 運算子（map / switchMap / catchError）
  ├── @Input / @Output（父子元件溝通）
  └── computed / effect（Signal 衍生狀態）

進階
  ├── Angular Signals Store（狀態管理）
  ├── HTTP 攔截器（Interceptor）
  ├── 環境設定（environment.ts）
  └── 單元測試（Jasmine + Karma）
```

> **推薦資源**：
> - [Angular 官方文件](https://angular.dev)（英文，最新 17+ 語法）
> - 練習平台：[StackBlitz](https://stackblitz.com)（線上執行 Angular，不需安裝）
