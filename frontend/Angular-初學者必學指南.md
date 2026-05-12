# Angular 初學者必學指南

> **對象**：有 HTML + JavaScript / TypeScript 基礎，第一次學習 Angular 的開發者  
> **學習目標**：理解 Angular 的七大核心概念，能獨立閱讀、修改並開發 Angular 應用程式  
> **範例專案**：本文件的所有程式碼均取自本工作區的商品管理系統（Product CRUD）

---

## 目錄

1. [Angular 是什麼？建立正確心智模型](#1-angular-是什麼建立正確心智模型)
2. [開發環境準備](#2-開發環境準備)
3. [必學概念 1：Component 元件](#3-必學概念-1component-元件)
4. [必學概念 2：Template 樣板語法](#4-必學概念-2template-樣板語法)
5. [必學概念 3：Service 與依賴注入](#5-必學概念-3service-與依賴注入)
6. [必學概念 4：HTTP 呼叫與 Observable](#6-必學概念-4http-呼叫與-observable)
7. [必學概念 5：Interface 資料模型](#7-必學概念-5interface-資料模型)
8. [必學概念 6：NgModule 模組系統](#8-必學概念-6ngmodule-模組系統)
9. [必學概念 7：Lifecycle Hooks 生命週期](#9-必學概念-7lifecycle-hooks-生命週期)
10. [完整資料流動圖解](#10-完整資料流動圖解)
11. [常見錯誤速查表](#11-常見錯誤速查表)
12. [練習題（Easy → Hard）](#12-練習題easy--hard)
13. [學習路線圖：下一步](#13-學習路線圖下一步)

---

## 1. Angular 是什麼？建立正確心智模型

### 白話解釋

Angular 是 Google 推出的**前端框架（Front-end Framework）**，用來建立「單頁應用程式（SPA, Single Page Application）」。

> 一般網頁：每次操作都重新載入整個頁面（向後端要新的 HTML）  
> Angular SPA：頁面只載入一次，之後只更新「需要改變的部分」（畫面由 JavaScript 控制）

### Angular 的設計哲學：分而治之

Angular 強迫開發者把程式碼拆分成明確的角色：

```
┌──────────────────────────────────────────────────────┐
│                   Angular 應用程式                    
│                                                      │
│  ┌──────────┐    呼叫     ┌──────────┐   HTTP    ┌──────┐
│  │Component │ ────────►     Service   ────────►    後端 
│  │（服務生） │ ◄────────    （廚房）   ◄────────     API   
│  │顯示資料  │  Observable   取得資料    JSON      └──────┘
│  └────┬─────┘            └──────────┘
│       │ 綁定
│  ┌────▼─────┐
│  │ Template │
│  │（菜單/   │
│  │  畫面）  │
│  └──────────┘
└────────────────────────────────────────────────────┘
```

| 角色 | Angular 對應 | 類比 |
|------|-------------|------|
| 資料格式定義 | Interface | 菜單的格式規範 |
| 畫面顯示 | Component + Template | 服務生 + 桌上的菜單 |
| 資料取得 / 處理 | Service | 廚房 |
| 功能整合 | NgModule | 整間餐廳 |

---

## 2. 開發環境準備

### 安裝步驟

```bash
# 1. 安裝 Node.js（v16+ 建議）
# 前往 https://nodejs.org 下載並安裝

# 2. 安裝 Angular CLI（Command Line Interface，命令列工具）
npm install -g @angular/cli

# 3. 確認安裝成功
ng version

# 4. 建立新專案
ng new my-app --routing=false --style=css

# 5. 進入專案目錄並啟動
cd my-app
ng serve
# 瀏覽器開啟 http://localhost:4200 即可看到預設頁面
```

### 專案目錄結構（本工作區）

```
frontend/src/app/
├── models/
│   └── product.model.ts          ← 資料型別定義（Interface）
├── services/
│   └── product.service.ts        ← HTTP 呼叫（與後端溝通）
├── components/product/
│   ├── product.component.ts      ← 元件邏輯（TypeScript）
│   ├── product.component.html    ← 元件畫面（Template）
│   └── product.component.css     ← 元件樣式
├── app.module.ts                 ← 模組設定（整合所有功能）
└── app.component.ts              ← 根元件（應用程式入口）
```

### 常用 Angular CLI 指令

```bash
ng serve                          # 啟動開發伺服器（port 4200）
ng generate component my-comp     # 產生新元件（可縮寫 ng g c my-comp）
ng generate service my-service    # 產生新服務（可縮寫 ng g s my-service）
ng build                          # 建置生產版本
ng test                           # 執行單元測試
```

---

## 3. 必學概念 1：Component 元件

**Component（元件）** 是 Angular 最核心的積木。每個 Component 負責管理畫面的「一塊區域」。

### 元件的三個組成部分

每個元件由三個檔案組成（或寫在同一個 `.ts` 檔）：

| 檔案 | 職責 |
|------|------|
| `.ts`（TypeScript） | 邏輯：資料、方法、API 呼叫 |
| `.html`（Template） | 畫面：HTML + Angular 樣板語法 |
| `.css`（Style） | 樣式：只作用於此元件 |

### 最小元件範例

```typescript
// app.component.ts — 根元件（最簡單的例子）
import { Component } from '@angular/core';

@Component({
  selector: 'app-root',           // ① HTML 標籤名稱
  template: '<app-product></app-product>'  // ② 直接寫 HTML（小元件適用）
})
export class AppComponent {}      // ③ class 本身（邏輯寫在這）
```

### 完整元件解析（商品管理系統）

```typescript
// product.component.ts
import { Component, OnInit } from '@angular/core';
import { ProductService } from '../../services/product.service';
import { Product } from '../../models/product.model';

@Component({
  selector: 'app-product',
  templateUrl: './product.component.html',   // ← 外部 HTML 檔案
  styleUrls: ['./product.component.css']     // ← 外部 CSS 檔案（陣列）
})
export class ProductComponent implements OnInit {

  // ── 屬性（資料狀態）──────────────────────────
  products: Product[] = [];         // 商品陣列，初始為空
  message = '';                     // 操作成功/失敗訊息
  generateNum = 3;                  // 隨機產生數量（預設 3）
  form: Product = { id: '', name: '' };  // 表單資料
  isEditing = false;                // 編輯模式開關

  // ── 依賴注入 ─────────────────────────────────
  constructor(private productService: ProductService) {}
  //                 ↑ Angular 自動建立並注入 ProductService

  // ── 生命週期鉤子 ──────────────────────────────
  ngOnInit(): void {
    this.loadProducts();            // 元件初始化時自動呼叫
  }

  // ── 方法（操作邏輯）──────────────────────────
  loadProducts(): void {
    this.productService.getAll().subscribe({
      next: (data) => this.products = data,
      error: () => this.showMessage('無法載入商品列表', true)
    });
  }
  // ... 其他 CRUD 方法
}
```

### `@Component` 裝飾器屬性說明

```typescript
@Component({
  selector: 'app-product',         // 在 HTML 中用 <app-product> 引用
  templateUrl: './...',            // 樣板檔案路徑
  styleUrls: ['./...']             // 樣式檔案路徑（陣列，可多個）
})
```

> **裝飾器（Decorator）**：以 `@` 開頭，是 TypeScript 加在 class 上的「附加標籤」，告訴 Angular 這個 class 扮演什麼角色。

### 如何在畫面上使用元件？

```html
<!-- app.component.ts 的 template -->
<app-product></app-product>
<!--    ↑ 對應 product.component.ts 的 selector: 'app-product' -->
```

### 常見陷阱

```typescript
// ❌ 忘記在 app.module.ts 的 declarations 宣告元件
// → 錯誤：'app-product' is not a known element

// ✅ 解決：在 app.module.ts 加入
declarations: [AppComponent, ProductComponent]
```

---

## 4. 必學概念 2：Template 樣板語法

Template（樣板）是 Angular 的 HTML，加上了特殊語法讓畫面能「動起來」。

### 五大核心語法

#### ① 插值（Interpolation）`{{ }}`

將 TypeScript 中的變數值顯示在畫面上：

```html
<!-- product.component.html -->
<h2>商品列表（共 {{ products.length }} 筆）</h2>
<!-- 輸出：商品列表（共 5 筆） -->

{{ isEditing ? '✏️ 編輯商品' : '➕ 新增商品' }}
<!-- 三元運算子也可以放在 {{ }} 內 -->
```

#### ② 屬性綁定（Property Binding）`[ ]`

用 TypeScript 的值動態設定 HTML 屬性：

```html
<!-- [disabled] 的值由 TypeScript 的 isEditing 決定 -->
<input type="text" [(ngModel)]="form.id" [disabled]="isEditing" />

<!-- 等同於 JavaScript 中 element.disabled = isEditing -->
```

#### ③ 事件綁定（Event Binding）`( )`

監聽 HTML 事件，觸發 TypeScript 的方法：

```html
<button (click)="generate()">產生</button>
<!--         ↑ 點擊時呼叫 component 的 generate() 方法 -->

<button (click)="delete(p.id)">刪除</button>
<!--                ↑ 可以傳入參數 -->
```

#### ④ 雙向綁定（Two-way Binding）`[( )]`

表單欄位與 TypeScript 屬性雙向同步（「香蕉在盒子裡」語法）：

```html
<input type="text" [(ngModel)]="form.name" placeholder="商品名稱" />
<!--                      ↑ form.name 改變 → input 更新；input 改變 → form.name 更新 -->

<input type="number" [(ngModel)]="generateNum" min="1" max="20" />
```

```
DOM → TypeScript：使用者輸入文字 → form.name 自動更新
TypeScript → DOM：程式修改 form.name → input 顯示的值自動更新
```

> **前置條件**：需要在 `app.module.ts` 匯入 `FormsModule`，否則會出現錯誤。

#### ⑤ 結構指令（Structural Directive）`*ngIf` / `*ngFor`

**`*ngIf`** — 條件性顯示 DOM 元素：

```html
<!-- message 有值（非空字串）才顯示 div -->
<div *ngIf="message" class="message">{{ message }}</div>

<!-- if/else 寫法 -->
<table *ngIf="products.length > 0; else noData">
  <!-- 有資料時顯示表格 -->
</table>

<ng-template #noData>
  <p>目前沒有商品資料</p>
</ng-template>
```

**`*ngFor`** — 迴圈渲染列表：

```html
<tr *ngFor="let p of products">
  <td>{{ p.id }}</td>
  <td>{{ p.name }}</td>
  <td>
    <button (click)="startEdit(p)">編輯</button>
    <button (click)="delete(p.id)">刪除</button>
  </td>
</tr>
<!-- 等同 JavaScript：products.forEach(p => { 渲染一行 }) -->
```

### 特殊標籤：`ng-container` 與 `ng-template`

```html
<!-- ng-container：不渲染 HTML 標籤，只做邏輯判斷用 -->
<ng-container *ngIf="isEditing; else createMode">
  <button (click)="update()">更新</button>
  <button (click)="resetForm()">取消</button>
</ng-container>

<!-- ng-template：定義「備用 / 條件顯示」的 HTML 片段 -->
<ng-template #createMode>
  <button (click)="create()">新增</button>
</ng-template>
```

### 樣板語法速查表

| 語法 | 類型 | 說明 | 本專案範例 |
|------|------|------|-----------|
| `{{ value }}` | 插值 | 顯示變數 | `{{ p.name }}` |
| `[attr]="expr"` | 屬性綁定 | 動態 HTML 屬性 | `[disabled]="isEditing"` |
| `(event)="fn()"` | 事件綁定 | 監聽事件 | `(click)="delete(p.id)"` |
| `[(ngModel)]="v"` | 雙向綁定 | 表單同步 | `[(ngModel)]="form.name"` |
| `*ngIf="cond"` | 結構指令 | 條件顯示 | `*ngIf="message"` |
| `*ngFor="let x of arr"` | 結構指令 | 迴圈渲染 | `*ngFor="let p of products"` |

### 常見陷阱

```html
<!-- ❌ 混淆 [] 和 () -->
<button [click]="create()">新增</button>   <!-- 屬性綁定，沒有效果！ -->
<input (ngModel)="form.name" />            <!-- 事件綁定，不是雙向！ -->

<!-- ✅ 正確 -->
<button (click)="create()">新增</button>
<input [(ngModel)]="form.name" />
```

---

## 5. 必學概念 3：Service 與依賴注入

### 為什麼需要 Service？

**問題**：如果 HTTP 呼叫邏輯寫在 Component 裡，當多個 Component 需要同樣的資料就得複製程式碼。

**解法**：把資料操作抽出來放進 **Service（服務）**，Component 只負責顯示。

```
沒有 Service（❌ 不好維護）：
  ComponentA → HTTP 呼叫 → 後端
  ComponentB → HTTP 呼叫 → 後端（重複！）

有 Service（✅ 正確方式）：
  ComponentA → ProductService → 後端
  ComponentB → ProductService（共用 Service！）
```

### Service 程式碼解析

```typescript
// product.service.ts
import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Product } from '../models/product.model';

@Injectable({
  providedIn: 'root'    // ← 全域單例（Singleton）：整個 App 共用同一個實體
})
export class ProductService {

  private apiUrl = 'http://localhost:8080/products';

  constructor(private http: HttpClient) {}
  //           ↑ Angular 自動注入 HttpClient，不需手動 new

  getAll(): Observable<Product[]> {
    return this.http.get<Product[]>(this.apiUrl);
  }

  create(product: Product): Observable<Product> {
    return this.http.post<Product>(this.apiUrl, product);
  }

  update(id: string, product: Product): Observable<Product> {
    return this.http.put<Product>(`${this.apiUrl}/${id}`, product);
  }

  delete(id: string): Observable<Product> {
    return this.http.delete<Product>(`${this.apiUrl}/${id}`);
  }
}
```

### 依賴注入（Dependency Injection, DI）

**白話解釋**：你需要某個工具（服務），不用自己去做（`new`），直接在 `constructor` 聲明需要它，Angular 會自動準備好給你。

```typescript
// ❌ 手動建立（不使用 DI）
export class ProductComponent {
  private service = new ProductService(new HttpClient(...));  // 複雜且難測試
}

// ✅ 依賴注入（Angular 方式）
export class ProductComponent {
  constructor(private productService: ProductService) {}
  // Angular 看到 constructor 參數，自動找到並注入對應的 Service
}
```

### 與 Java Spring 的對比

| Java Spring | Angular |
|------------|---------|
| `@Service` | `@Injectable({ providedIn: 'root' })` |
| `@Autowired` | `constructor(private svc: MyService)` |
| Spring IoC Container | Angular Injector |

### 常見陷阱

```typescript
// ❌ 忘記 @Injectable 裝飾器 → Service 無法被注入
export class ProductService { ... }

// ✅ 一定要有 @Injectable
@Injectable({ providedIn: 'root' })
export class ProductService { ... }
```

---

## 6. 必學概念 4：HTTP 呼叫與 Observable

### Observable 是什麼？

**Observable（可觀察物件）** 是 **RxJS** 函式庫的核心，代表「一個未來會送來的資料流」。

類比：Observable 就像「外送訂單」——你下單後不會馬上拿到餐點，而是「訂閱」等待配送到門（`.subscribe()`）。

```
傳統 callback：
  getData(callback) → 等待 → callback(data)

Promise（ES6）：
  getData().then(data => ...).catch(err => ...)

Observable（RxJS）：
  getData().subscribe({ next: data => ..., error: err => ... })
```

### HTTP 呼叫的五種方法

```typescript
// product.service.ts — 對應 RESTful API

// GET 取得資料
getAll(): Observable<Product[]> {
  return this.http.get<Product[]>(this.apiUrl);
}

// GET 帶路徑參數
generate(num: number): Observable<Product[]> {
  return this.http.get<Product[]>(`${this.apiUrl}/generate/${num}`);
  // 結果：GET http://localhost:8080/products/generate/3
}

// POST 新增（Body 傳 JSON）
create(product: Product): Observable<Product> {
  return this.http.post<Product>(this.apiUrl, product);
}

// PUT 更新（路徑 + Body）
update(id: string, product: Product): Observable<Product> {
  return this.http.put<Product>(`${this.apiUrl}/${id}`, product);
}

// DELETE 刪除（路徑參數）
delete(id: string): Observable<Product> {
  return this.http.delete<Product>(`${this.apiUrl}/${id}`);
}
```

### `.subscribe()` 的正確寫法

```typescript
// product.component.ts — 在 Component 中訂閱 Observable

// 基礎寫法
this.productService.getAll().subscribe({
  next: (data) => {           // 成功收到資料時執行
    this.products = data;
  },
  error: (err) => {           // 發生錯誤時執行
    this.showMessage('載入失敗', true);
  }
});

// 完整範例：新增商品
create(): void {
  if (!this.form.id || !this.form.name) {
    this.showMessage('ID 與名稱不可為空', true);
    return;
  }
  this.productService.create({ ...this.form }).subscribe({
    next: (p) => {
      this.products.push(p);              // 更新本地陣列
      this.showMessage(`已新增：${p.name}`);
      this.resetForm();
    },
    error: () => this.showMessage('新增失敗', true)
  });
}
```

### 泛型（Generic）在 HTTP 呼叫的作用

```typescript
// <Product[]> 告訴 TypeScript 回傳值的型別
this.http.get<Product[]>(url)
//           ↑ 泛型參數：回傳陣列中每個元素都是 Product 型態

// 有了泛型，TypeScript 會自動提示屬性
data.forEach(p => p.name)  // ← p 被推斷為 Product 型別，有自動完成
```

### 常見陷阱

```typescript
// ❌ 忘記 subscribe！返回的 Observable 不會執行任何 HTTP 請求！
this.productService.getAll();   // 什麼都不會發生

// ✅ 一定要 subscribe 才會送出 HTTP 請求
this.productService.getAll().subscribe({ next: (data) => ... });

// ❌ 未匯入 HttpClientModule
// → 錯誤：NullInjectorError: No provider for HttpClient!

// ✅ 在 app.module.ts 的 imports 加入 HttpClientModule
```

---

## 7. 必學概念 5：Interface 資料模型

### TypeScript Interface 是什麼？

**Interface（介面）** 定義物件的「形狀（Shape）」——有哪些屬性、各是什麼型別。它**不會產生任何執行時的程式碼**，只在編譯階段做型別檢查。

```typescript
// product.model.ts
export interface Product {
  id: string;
  name: string;
}
```

### Interface vs Class

```typescript
// Interface — 只定義形狀，不能 new
interface Product {
  id: string;
  name: string;
}
const p: Product = { id: '1', name: 'Honey' };  // ✅ 直接用物件字面值

// Class — 有行為，可以 new
class ProductModel {
  constructor(public id: string, public name: string) {}
  getLabel(): string { return `[${this.id}] ${this.name}`; }
}
const q = new ProductModel('2', 'Bee');    // ✅ 用 new 建立

// 本專案使用 Interface，因為資料來自後端 JSON，不需要在前端 new 物件
```

### Interface 帶來的好處

```typescript
// ① 自動完成（IDE 提示屬性）
const p: Product = { id: '1', name: 'Honey' };
p.   // → IDE 提示：id、name

// ② 編譯時錯誤（立即發現問題）
const p: Product = { id: '1' };         // ❌ 錯誤：缺少必要屬性 name
const q: Product = { id: 1, name: 'A' }; // ❌ 錯誤：id 應為 string，不是 number

// ③ 函式參數保護
create(product: Product): Observable<Product> {
  // 傳入的 product 一定有 id 和 name，TypeScript 保證
}
```

### 與 Java 的對比

```java
// Java（Product.java）
public class Product {
    private String id;
    private String name;
    // getter / setter...
}
```

```typescript
// TypeScript（product.model.ts）
export interface Product {
  id: string;
  name: string;
}
// TypeScript Interface ≈ Java 的「只保留欄位定義，去掉 getter/setter」版本
```

### 常見陷阱

```typescript
// ❌ 屬性少了（必填）
const p: Product = { id: '1' };              // 編譯錯誤！

// ❌ 屬性型別錯誤
const p: Product = { id: 1, name: 'A' };    // id 應是 string！

// ✅ 正確：所有必填屬性都要提供，型別要對
const p: Product = { id: '1', name: 'Honey' };
```

---

## 8. 必學概念 6：NgModule 模組系統

### NgModule 的作用

**NgModule（模組）** 是把相關的 Component、Service、第三方功能「組合打包」的容器。每個 Angular 應用程式至少有一個根模組（`AppModule`）。

```typescript
// app.module.ts
import { NgModule } from '@angular/core';
import { BrowserModule } from '@angular/platform-browser';
import { HttpClientModule } from '@angular/common/http';
import { FormsModule } from '@angular/forms';

import { AppComponent } from './app.component';
import { ProductComponent } from './components/product/product.component';

@NgModule({
  declarations: [              // ① 宣告屬於這個模組的 Component
    AppComponent,
    ProductComponent
  ],
  imports: [                   // ② 引入外部模組提供的功能
    BrowserModule,             //   → 提供瀏覽器基本能力（*ngIf, *ngFor 等）
    HttpClientModule,          //   → 啟用 HttpClient（Service 中 HTTP 呼叫需要）
    FormsModule                //   → 啟用 ngModel（表單雙向綁定需要）
  ],
  bootstrap: [AppComponent]   // ③ 指定應用程式的起始元件
})
export class AppModule {}
```

### 三個最重要的屬性

| 屬性 | 說明 | 忘記加的後果 |
|------|------|------------|
| `declarations` | 宣告此模組管理的 Component | 使用 `<app-product>` 時報錯：`is not a known element` |
| `imports: [FormsModule]` | 啟用 `[(ngModel)]` 雙向綁定 | 報錯：`Can't bind to 'ngModel'` |
| `imports: [HttpClientModule]` | 啟用 `HttpClient` | 報錯：`NullInjectorError: No provider for HttpClient` |

### 常見陷阱

```typescript
// ❌ 新建 Component 但忘記加入 declarations
@NgModule({
  declarations: [AppComponent],   // ProductComponent 沒加！
  // ...
})

// ✅ 每個新 Component 都要加入 declarations
@NgModule({
  declarations: [AppComponent, ProductComponent],
  // ...
})
```

> **小技巧**：使用 `ng generate component` 指令建立元件時，Angular CLI 會**自動**把元件加入 `declarations`，省去手動修改的步驟。

---

## 9. 必學概念 7：Lifecycle Hooks 生命週期

**Lifecycle Hooks（生命週期鉤子）** 是 Angular 在元件「生命」的特定時刻自動呼叫的方法，讓你可以在正確的時間點執行程式碼。

### 生命週期順序

```
元件建立
    ↓
constructor()           — 依賴注入在這裡完成，但 DOM 還沒準備好
    ↓
ngOnChanges()           — 輸入屬性（@Input）有變化時呼叫（可能多次）
    ↓
ngOnInit()              — ✅ 最常用！初始化完成，適合呼叫 API 取得資料
    ↓
ngDoCheck()             — 每次變更偵測時呼叫（進階，慎用）
    ↓
ngAfterViewInit()       — 元件的 HTML 檢視完全初始化後呼叫
    ↓
（使用中，資料變更 → 畫面更新）
    ↓
ngOnDestroy()           — 元件即將被銷毀時呼叫（清理訂閱、計時器）
```

### 本專案使用的 Lifecycle Hook

```typescript
// product.component.ts
export class ProductComponent implements OnInit {
  //                              ↑ 宣告實作 OnInit 介面（TypeScript 型別安全）

  ngOnInit(): void {
    this.loadProducts();   // 元件初始化時自動執行一次
  }
}
```

### 為什麼不在 constructor 裡呼叫 API？

```typescript
// ❌ 在 constructor 呼叫 API（不建議）
constructor(private productService: ProductService) {
  this.loadProducts();   // 可以，但此時 @Input 屬性還未初始化
}

// ✅ 在 ngOnInit 呼叫 API（最佳實踐）
ngOnInit(): void {
  this.loadProducts();   // 所有 @Input 都已就緒，最安全的時機點
}
```

### 常見陷阱

```typescript
// ❌ 宣告 implements OnInit 但忘記寫 ngOnInit 方法
// → TypeScript 會報錯：Class 'ProductComponent' incorrectly implements interface 'OnInit'

// ✅ 有 implements 就要有對應的方法
export class ProductComponent implements OnInit {
  ngOnInit(): void { ... }   // 一定要實作
}
```

---

## 10. 完整資料流動圖解

以「**刪除商品**」為完整範例，追蹤整個流程：

```
① 使用者點擊「刪除」按鈕
          ↓
② Template 事件綁定觸發
   (click)="delete(p.id)"
          ↓
③ ProductComponent.delete(id) 方法被呼叫
   ┌─────────────────────────────────────────┐
   │ delete(id: string): void {              │
   │   if (!confirm(`確定要刪除 ${id}？`))   │
   │     return;   ← 使用者取消就停止        │
   │   this.productService.delete(id)        │
   │     .subscribe({ ... });                │
   │ }                                       │
   └─────────────────────────────────────────┘
          ↓
④ ProductService.delete(id) 被呼叫
   ┌─────────────────────────────────────────┐
   │ delete(id: string): Observable<Product>│
   │   return this.http.delete<Product>(    │
   │     `http://localhost:8080/products/1` │
   │   );                                   │
   └─────────────────────────────────────────┘
          ↓
⑤ HTTP DELETE 請求送出 → Spring Boot 後端
   DELETE http://localhost:8080/products/1
          ↓
⑥ 後端刪除成功，回傳被刪除的商品 JSON
          ↓
⑦ Observable 發出值，subscribe 的 next callback 執行
   ┌─────────────────────────────────────────┐
   │ next: () => {                           │
   │   // 從本地陣列過濾掉刪除的商品         │
   │   this.products = this.products         │
   │     .filter(p => p.id !== id);          │
   │   this.showMessage(`已刪除 ID：${id}`); │
   │ }                                       │
   └─────────────────────────────────────────┘
          ↓
⑧ products 陣列改變，Angular 自動偵測變更
          ↓
⑨ *ngFor 重新渲染表格，刪除的項目從畫面消失
```

### 四種 CRUD 操作對比

| 操作 | HTTP Method | Service 方法 | Component 更新策略 |
|------|------------|-------------|------------------|
| 查詢 | GET | `getAll()` | `this.products = data` |
| 新增 | POST | `create(product)` | `this.products.push(p)` |
| 更新 | PUT | `update(id, product)` | `this.products[idx] = updated` |
| 刪除 | DELETE | `delete(id)` | `this.products.filter(...)` |

---

## 11. 常見錯誤速查表

| 錯誤訊息 | 原因 | 解決方式 |
|---------|------|---------|
| `Can't bind to 'ngModel' since it isn't a known property` | 未匯入 `FormsModule` | `app.module.ts` → `imports: [FormsModule]` |
| `NullInjectorError: No provider for HttpClient` | 未匯入 `HttpClientModule` | `app.module.ts` → `imports: [HttpClientModule]` |
| `'app-product' is not a known element` | 未在 `declarations` 宣告元件 | `app.module.ts` → `declarations: [ProductComponent]` |
| `CORS error` in console | 後端未允許跨來源請求 | Spring Boot 的 Controller 加上 `@CrossOrigin` |
| `Cannot read properties of undefined` | subscribe 前資料還未到達 | 用 `?.` 可選鏈，或確保屬性有初始值 |
| 表單改動影響到列表中的原始資料 | 直接指定參考而非複製 | `this.form = { ...product }` 使用 Spread Operator |
| Observable 沒有送出 HTTP 請求 | 忘記呼叫 `.subscribe()` | 在 Component 中呼叫 `.subscribe({ next: ... })` |
| `Property 'x' does not exist on type 'Product'` | 存取 Interface 未定義的屬性 | 在 `product.model.ts` 的 Interface 新增該屬性 |

---

## 12. 練習題（Easy → Hard）

### 題目一（Easy）— 顯示商品總筆數標題
> **需求**：在商品列表標題顯示目前商品總數。例如「商品列表（共 5 筆）」。

**提示**：使用插值語法 `{{ }}` 和陣列的 `.length` 屬性。

<details>
<summary>查看解答</summary>

```html
<!-- product.component.html -->
<h2>商品列表（共 {{ products.length }} 筆）</h2>
```

**學習重點**：插值（Interpolation）可以執行任何 JavaScript 運算式，包含屬性存取。
</details>

---

### 題目二（Easy）— 擴充 Product 加入 price 欄位
> **需求**：在 `Product` 介面加入 `price: number`，並讓表單支援輸入價格，列表也要顯示價格。

**提示**：需同時修改 `product.model.ts`、`product.component.ts`、`product.component.html`。

<details>
<summary>查看解答</summary>

```typescript
// product.model.ts
export interface Product {
  id: string;
  name: string;
  price: number;   // 新增
}
```

```typescript
// product.component.ts — 更新初始值
form: Product = { id: '', name: '', price: 0 };
```

```html
<!-- product.component.html — 表單加欄位 -->
<label>價格：
  <input type="number" [(ngModel)]="form.price" placeholder="商品價格" />
</label>

<!-- 列表表頭加欄位 -->
<th>價格</th>

<!-- 列表資料加欄位 -->
<td>{{ p.price }}</td>
```

**學習重點**：Interface 欄位改變後，所有使用到的地方（component、template）都要同步更新，TypeScript 的型別檢查會幫你找到遺漏處。
</details>

---

### 題目三（Medium）— 加入即時搜尋功能
> **需求**：在商品列表上方加入搜尋框，使用者輸入關鍵字時即時過濾商品名稱。

**提示**：使用 `get` 屬性（getter）搭配 `Array.filter()`，並將 `*ngFor` 改為使用計算後的陣列。

<details>
<summary>查看解答</summary>

```typescript
// product.component.ts — 加入搜尋屬性和 getter
searchKeyword = '';

get filteredProducts(): Product[] {
  if (!this.searchKeyword) return this.products;
  return this.products.filter(p =>
    p.name.toLowerCase().includes(this.searchKeyword.toLowerCase())
  );
}
```

```html
<!-- product.component.html — 加入搜尋框 -->
<input type="text" [(ngModel)]="searchKeyword" placeholder="搜尋商品名稱..." />

<!-- 將 *ngFor 改用 filteredProducts -->
<tr *ngFor="let p of filteredProducts">
  ...
</tr>
```

**學習重點**：TypeScript 的 `get` 屬性可以讓 Template 像存取普通屬性一樣，使用計算後的值。
</details>

---

### 題目四（Medium）— 防止重複新增相同 ID
> **需求**：在 `create()` 方法中加入驗證，若 `form.id` 已存在於 `products` 中，顯示錯誤訊息並阻止新增。

**提示**：使用 `Array.some()` 方法檢查是否已有相同 ID。

<details>
<summary>查看解答</summary>

```typescript
// product.component.ts — 在 create() 方法的驗證區塊加入
create(): void {
  if (!this.form.id || !this.form.name) {
    this.showMessage('ID 與名稱不可為空', true);
    return;
  }

  // 新增：檢查 ID 是否重複
  const isDuplicate = this.products.some(p => p.id === this.form.id);
  if (isDuplicate) {
    this.showMessage(`ID "${this.form.id}" 已存在`, true);
    return;
  }

  this.productService.create({ ...this.form }).subscribe({ ... });
}
```

**學習重點**：`Array.some()` 會在找到第一個符合條件的元素後立刻返回 `true`，適合用在「存在性檢查」。
</details>

---

### 題目五（Hard）— 建立新元件：ProductBadge
> **需求**：建立 `ProductBadgeComponent`，接收一個 `product` 輸入（`@Input`），顯示 `[ID] Name` 格式的徽章標籤，並在商品列表每行加入此元件。

**提示**：學習 `@Input()` 裝飾器，讓父元件傳資料給子元件。

<details>
<summary>查看解答</summary>

```bash
# 先用 CLI 建立元件（會自動更新 app.module.ts）
ng generate component components/product-badge
```

```typescript
// product-badge.component.ts
import { Component, Input } from '@angular/core';
import { Product } from '../../models/product.model';

@Component({
  selector: 'app-product-badge',
  template: `<span class="badge">[{{ product.id }}] {{ product.name }}</span>`
})
export class ProductBadgeComponent {
  @Input() product!: Product;   // 接收父元件傳入的資料
}
```

```html
<!-- product.component.html — 在 *ngFor 中使用子元件 -->
<tr *ngFor="let p of products">
  <td><app-product-badge [product]="p"></app-product-badge></td>
  <td>
    <button (click)="startEdit(p)">編輯</button>
    <button (click)="delete(p.id)">刪除</button>
  </td>
</tr>
```

**學習重點**：`@Input()` 是父子元件溝通的標準方式。父元件用屬性綁定 `[product]="p"` 傳入資料，子元件用 `@Input()` 接收。
</details>

---

## 13. 學習路線圖：下一步

掌握本文件的七大核心概念後，建議依以下順序繼續學習：

### 阶段一：鞏固基礎（完成本文件後）
- [ ] 完成所有練習題
- [ ] 自行建立一個新的 CRUD 功能（例如：訂單管理）
- [ ] 閱讀並理解本專案的所有原始碼

### 阶段二：進階 Angular 功能（1–2 週）

| 主題 | 說明 | 關鍵詞 |
|------|------|--------|
| **Routing（路由）** | 多頁切換，不重新載入頁面 | `RouterModule`, `<router-outlet>`, `routerLink` |
| **@Input / @Output** | 父子元件通訊 | `@Input()`, `@Output()`, `EventEmitter` |
| **Reactive Forms** | 更強大的表單控制 | `FormGroup`, `FormControl`, `Validators` |
| **Pipe** | 資料格式化（日期、貨幣等） | `DatePipe`, `CurrencyPipe`, 自訂 Pipe |
| **RxJS 進階** | 多個 Observable 的組合操作 | `map`, `switchMap`, `combineLatest` |

### 阶段三：實戰與生態系（2–4 週）
| 主題 | 說明 |
|------|------|
| Angular Material | Google 官方 UI 元件庫 |
| NgRx | 全域狀態管理（類似 Redux） |
| Standalone Components | Angular 17+ 的新架構（不需要 NgModule） |
| Unit Testing | 使用 Jasmine + Karma 撰寫單元測試 |
| Angular Signals | Angular 17+ 的響應式新語法 |

### 推薦免費資源

| 資源 | 說明 | 連結 |
|------|------|------|
| Angular 官方文件 | 最權威的參考資料 | [angular.io/docs](https://angular.io/docs) |
| Angular 官方教學 | Tour of Heroes（官方入門練習） | [angular.io/tutorial](https://angular.io/tutorial) |
| RxJS 官方文件 | Observable / 操作符說明 | [rxjs.dev](https://rxjs.dev/) |
| StackBlitz | 瀏覽器內直接執行 Angular | [stackblitz.com](https://stackblitz.com/) |
| TypeScript 官方文件 | Interface / 泛型語法 | [typescriptlang.org/docs](https://www.typescriptlang.org/docs/) |

---

## 學習重點摘要

| 概念 | 英文術語 | 本專案對應檔案 | 關鍵語法 |
|------|---------|--------------|---------|
| 元件 | Component | `product.component.ts` | `@Component`, `selector` |
| 樣板語法 | Template Syntax | `product.component.html` | `{{ }}`, `[]`, `()`, `[()]` |
| 結構指令 | Structural Directive | `product.component.html` | `*ngIf`, `*ngFor` |
| 服務 | Service | `product.service.ts` | `@Injectable` |
| 依賴注入 | Dependency Injection | `constructor(private ...)` | constructor 參數 |
| HTTP 呼叫 | HTTP Client | `product.service.ts` | `this.http.get/post/put/delete` |
| 可觀察物件 | Observable | `.subscribe()` | `next`, `error` |
| 介面 | Interface | `product.model.ts` | `interface Product { ... }` |
| 模組 | NgModule | `app.module.ts` | `declarations`, `imports` |
| 生命週期 | Lifecycle Hook | `product.component.ts` | `ngOnInit()` |

---

> **現在試試看！**
> 1. 執行 `ng serve` 啟動 Angular（確保 Spring Boot 在 port 8080 運行）
> 2. 按 F12 開啟瀏覽器開發者工具 → Network 標籤，觀察 HTTP 請求
> 3. 完成「**題目二：擴充 price 欄位**」，體驗 TypeScript 型別錯誤如何引導你修改所有相關位置
> 4. 嘗試「**題目三：即時搜尋**」，感受 `[(ngModel)]` 雙向綁定的強大

