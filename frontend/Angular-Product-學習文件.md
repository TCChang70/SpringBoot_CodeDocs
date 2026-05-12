# Angular 商品管理系統 — 學習文件

> 基於 `ProductController.java` (Spring Boot) 所建立的 Angular 前端專案  
> 適合對象：有基礎 TypeScript / JavaScript 概念，初次學習 Angular 的開發者

---

## 目錄

1. [專案架構總覽](#1-專案架構總覽)
2. [核心概念一：Interface 資料模型](#2-核心概念一interface-資料模型)
3. [核心概念二：Service 與 HttpClient](#3-核心概念二service-與-httpclient)
4. [核心概念三：Component 元件](#4-核心概念三component-元件)
5. [核心概念四：Template 樣板語法](#5-核心概念四template-樣板語法)
6. [核心概念五：NgModule 模組設定](#6-核心概念五ngmodule-模組設定)
7. [資料流動完整路徑](#7-資料流動完整路徑)
8. [常見錯誤與解決方式](#8-常見錯誤與解決方式)
9. [練習題](#9-練習題)
10. [學習重點摘要](#10-學習重點摘要)

---

## 1. 專案架構總覽

```
frontend/src/app/
├── models/
│   └── product.model.ts          ← 資料型別定義 (TypeScript Interface)
├── services/
│   └── product.service.ts        ← HTTP 呼叫邏輯 (與後端溝通)
├── components/product/
│   ├── product.component.ts      ← 商業邏輯 (CRUD 操作)
│   ├── product.component.html    ← 使用者介面 (HTML 樣板)
│   └── product.component.css     ← 視覺樣式
├── app.module.ts                 ← 模組設定 (整合所有功能)
└── app.component.ts              ← 根元件 (入口點)
```

> **架構概念（白話）**：  
> Angular 的標準分層就像餐廳：  
> - **Interface（資料模型）** = 菜單格式（定義資料長什麼樣）  
> - **Service** = 廚房（負責取得/處理資料）  
> - **Component** = 服務生（負責顯示資料、處理使用者操作）  
> - **Module** = 餐廳本身（把所有人組合起來）

---

## 2. 核心概念一：Interface 資料模型

### 檔案：[models/product.model.ts](src/app/models/product.model.ts)

```typescript
export interface Product {
  id: string;
  name: string;
}
```

### 逐行解析

| 程式碼 | 說明 |
|--------|------|
| `export` | 讓其他檔案可以 `import` 這個型別 |
| `interface Product` | 宣告一個名為 `Product` 的**介面（Interface）**，用來描述物件的「形狀」 |
| `id: string` | 屬性 `id`，型別為字串 |
| `name: string` | 屬性 `name`，型別為字串 |

### 關鍵概念：Interface vs Class

```typescript
// ✅ Interface — 只定義「形狀」，無法建立實體，常用於型別提示
interface Product {
  id: string;
  name: string;
}

// ✅ Class — 有行為（方法），可用 new 建立實體
class Product {
  constructor(public id: string, public name: string) {}
}

// 本專案使用 Interface，因為資料來自後端，不需要在前端 new 物件
```

### 對應 Java
```java
// Java Product.java
public class Product {
    String id, name;
    // getter / setter...
}
```
TypeScript 的 `interface` 就是 Java `class` 的「只保留欄位定義」的版本。

### 常見陷阱
```typescript
// ❌ 錯誤：少了必要屬性
const p: Product = { id: '1' };        // 編譯錯誤：缺少 name

// ✅ 正確
const p: Product = { id: '1', name: 'Honey' };
```

---

## 3. 核心概念二：Service 與 HttpClient

### 檔案：[services/product.service.ts](src/app/services/product.service.ts)

```typescript
import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Product } from '../models/product.model';

@Injectable({ providedIn: 'root' })
export class ProductService {

  private apiUrl = 'http://localhost:8080/products';

  constructor(private http: HttpClient) {}

  getAll(): Observable<Product[]> {
    return this.http.get<Product[]>(this.apiUrl);
  }

  generate(num: number): Observable<Product[]> {
    return this.http.get<Product[]>(`${this.apiUrl}/generate/${num}`);
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

### 逐區塊解析

#### ① `@Injectable({ providedIn: 'root' })`
```typescript
@Injectable({ providedIn: 'root' })
```
- **裝飾器（Decorator）**：以 `@` 開頭，用來標記這個 class 的特殊行為
- `providedIn: 'root'` 表示這個 Service 在**整個應用程式只建立一份**（Singleton 單例模式）
- 類比 Java：就像 Spring 的 `@Service`

#### ② `constructor(private http: HttpClient)`
```typescript
constructor(private http: HttpClient) {}
```
- **依賴注入（Dependency Injection, DI）**：Angular 自動把 `HttpClient` 的實體傳進來
- `private` 讓 `http` 只能在這個 class 內使用
- 無需手動 `new HttpClient()`，Angular 幫你管理

#### ③ `Observable<Product[]>` — 非同步回傳值
```typescript
getAll(): Observable<Product[]> {
  return this.http.get<Product[]>(this.apiUrl);
}
```
- `Observable` 是 **RxJS** 的核心概念，代表「未來會送來的資料流」
- `get<Product[]>` 是泛型（Generic）：告訴 TypeScript 回傳值是 `Product` 陣列
- HTTP 呼叫是**非同步**的，不會馬上拿到資料，需要用 `.subscribe()` 等待結果

#### ④ Template Literal（樣板字串）
```typescript
`${this.apiUrl}/generate/${num}`
// 結果：'http://localhost:8080/products/generate/3'
```
- 用反引號 `` ` `` 包起來
- `${}` 內可放任意 JavaScript 運算式

### Observable 使用流程圖

```
Service 回傳 Observable
        ↓
Component 呼叫 .subscribe()
        ↓
   HTTP 請求送出
        ↓
   後端回應資料
        ↓
next callback 收到資料 → 更新畫面
error callback 發生錯誤 → 顯示錯誤訊息
```

### 常見陷阱
```typescript
// ❌ 忘記 subscribe — 什麼都不會發生！
this.productService.getAll();

// ✅ 正確 — 一定要 subscribe 才會觸發 HTTP 請求
this.productService.getAll().subscribe({
  next: (data) => console.log(data),
  error: (err) => console.error(err)
});
```

---

## 4. 核心概念三：Component 元件

### 檔案：[components/product/product.component.ts](src/app/components/product/product.component.ts)

```typescript
@Component({
  selector: 'app-product',         // HTML 標籤名稱
  templateUrl: './product.component.html',
  styleUrls: ['./product.component.css']
})
export class ProductComponent implements OnInit {

  products: Product[] = [];
  form: Product = { id: '', name: '' };
  isEditing = false;

  constructor(private productService: ProductService) {}

  ngOnInit(): void {
    this.loadProducts();           // 頁面載入時自動執行
  }
  // ... CRUD 方法
}
```

### 關鍵概念逐一拆解

#### ① `@Component` 裝飾器
| 屬性 | 說明 |
|------|------|
| `selector: 'app-product'` | 在 HTML 中用 `<app-product>` 插入這個元件 |
| `templateUrl` | 指向 HTML 樣板檔案 |
| `styleUrls` | 指向 CSS 樣式檔案（陣列，可多個） |

#### ② `implements OnInit` — 生命週期鉤子（Lifecycle Hook）
```typescript
export class ProductComponent implements OnInit {
  ngOnInit(): void {
    this.loadProducts();   // 元件初始化完成後執行
  }
}
```
Angular 元件的生命週期順序：
```
constructor()  →  ngOnInit()  →  （資料變動時）ngOnChanges()  →  ngOnDestroy()
    ↑                 ↑
 依賴注入          ✅ 在這裡呼叫 API，取得初始資料
```

#### ③ Spread Operator `{ ...product }` — 物件複製
```typescript
startEdit(product: Product): void {
  this.form = { ...product };   // 複製物件，避免直接參考
}
```
```typescript
// 沒有 spread — form 和 product 指向同一物件！
this.form = product;           // ❌ 修改 form 會同時改到 product

// 有 spread — 建立一個全新物件
this.form = { ...product };    // ✅ 安全複製
```

#### ④ `.subscribe()` 的結構化寫法
```typescript
this.productService.create({ ...this.form }).subscribe({
  next: (p) => {               // 成功時執行
    this.products.push(p);
    this.showMessage(`已新增：${p.name}`);
    this.resetForm();
  },
  error: () => this.showMessage('新增失敗', true)  // 失敗時執行
});
```

#### ⑤ `Array.filter()` 實現刪除
```typescript
// 刪除後，從陣列移除對應元素（不重新呼叫 API）
this.products = this.products.filter(p => p.id !== id);
```
```
原始陣列：[{id:'1'}, {id:'2'}, {id:'3'}]
執行 filter(p => p.id !== '2')
結果陣列：[{id:'1'}, {id:'3'}]        ← id='2' 被過濾掉
```

#### ⑥ `Array.findIndex()` 實現更新
```typescript
const idx = this.products.findIndex(p => p.id === updated.id);
if (idx !== -1) this.products[idx] = updated;
```
- `findIndex` 回傳符合條件的**第一個索引**
- 若找不到則回傳 `-1`，需要加 `!== -1` 的防護判斷

---

## 5. 核心概念四：Template 樣板語法

### 檔案：[components/product/product.component.html](src/app/components/product/product.component.html)

Angular 樣板語法速查表（本專案使用的所有語法）：

| 語法 | 類型 | 說明 | 範例 |
|------|------|------|------|
| `{{ }}` | 插值（Interpolation） | 顯示變數值 | `{{ p.name }}` |
| `[disabled]` | 屬性綁定（Property Binding） | 動態設定 HTML 屬性 | `[disabled]="isEditing"` |
| `(click)` | 事件綁定（Event Binding） | 監聽使用者事件 | `(click)="delete(p.id)"` |
| `[(ngModel)]` | 雙向綁定（Two-way Binding） | 表單欄位同步 | `[(ngModel)]="form.name"` |
| `*ngIf` | 結構指令（Structural Directive） | 條件顯示 | `*ngIf="message"` |
| `*ngFor` | 結構指令 | 迴圈渲染 | `*ngFor="let p of products"` |
| `#templateRef` | 樣板參考（Template Reference） | 標記 ng-template | `#createMode` |

### 逐段解析

#### ① 插值與條件顯示
```html
<!-- 若 message 有值才顯示（空字串 = false） -->
<div *ngIf="message" class="message">{{ message }}</div>
```

#### ② 動態標題（三元運算子）
```html
<h2>{{ isEditing ? '✏️ 編輯商品' : '➕ 新增商品' }}</h2>
```
`isEditing` 為 `true` → 顯示「編輯商品」；`false` → 顯示「新增商品」

#### ③ `[(ngModel)]` 雙向綁定原理
```html
<input type="text" [(ngModel)]="form.name" />
```
```
使用者輸入文字
      ↓
form.name 自動更新（DOM → TypeScript）

form.name 被程式修改
      ↓
畫面上的 input 自動更新（TypeScript → DOM）
```
`[(ngModel)]` = `[ngModel]`（屬性綁定）+ `(ngModelChange)`（事件綁定）的縮寫，稱為「**香蕉在盒子裡**」語法。

#### ④ `ng-container` 與 `ng-template`
```html
<!-- ng-container：不渲染任何 HTML 標籤，只用來放指令 -->
<ng-container *ngIf="isEditing; else createMode">
  <button (click)="update()">更新</button>
  <button (click)="resetForm()">取消</button>
</ng-container>

<!-- ng-template：定義「備用內容」，不會直接顯示 -->
<ng-template #createMode>
  <button (click)="create()">新增</button>
</ng-template>
```

#### ⑤ `*ngFor` 迴圈渲染
```html
<tr *ngFor="let p of products">
  <td>{{ p.id }}</td>
  <td>{{ p.name }}</td>
  <td>
    <button (click)="startEdit(p)">編輯</button>
    <button (click)="delete(p.id)">刪除</button>
  </td>
</tr>
```
等同 JavaScript 的 `products.forEach(p => { /* 渲染一行 */ })`

#### ⑥ `*ngIf` 的 else 用法
```html
<table *ngIf="products.length > 0; else noData">
  <!-- 有資料時顯示表格 -->
</table>

<ng-template #noData>
  <p>目前沒有商品資料</p>   <!-- 無資料時顯示提示 -->
</ng-template>
```

---

## 6. 核心概念五：NgModule 模組設定

### 檔案：[app.module.ts](src/app/app.module.ts)

```typescript
@NgModule({
  declarations: [AppComponent, ProductComponent],  // 宣告元件
  imports: [
    BrowserModule,       // 提供瀏覽器基本功能
    HttpClientModule,    // 啟用 HttpClient（Service 需要）
    FormsModule          // 啟用 ngModel（Template 需要）
  ],
  bootstrap: [AppComponent]
})
export class AppModule {}
```

### 為什麼需要 NgModule？

| `@NgModule` 屬性 | 用途 | 本專案對應 |
|----------------|------|-----------|
| `declarations` | 宣告這個模組包含哪些元件 | `ProductComponent` |
| `imports` | 引入其他模組的功能 | `HttpClientModule`, `FormsModule` |
| `bootstrap` | 指定應用程式的起始元件 | `AppComponent` |

### 常見錯誤：忘記匯入 FormsModule
```
ERROR: Can't bind to 'ngModel' since it isn't a known property of 'input'
```
解決：在 `app.module.ts` 的 `imports` 加入 `FormsModule`。

---

## 7. 資料流動完整路徑

以「新增商品」為例：

```
使用者填寫表單
    ↓ [(ngModel)] 雙向綁定
form.id / form.name 更新
    ↓ 點擊「新增」按鈕
(click)="create()" 觸發
    ↓
ProductComponent.create()
    ↓ 呼叫 Service
ProductService.create({ id, name })
    ↓ HttpClient
POST http://localhost:8080/products  (JSON body)
    ↓ Spring Boot 處理
ProductController.createProduct()
    ↓ 回傳新商品 JSON
Observable 發出值
    ↓ .subscribe() next callback
products.push(p)  ← 更新陣列
    ↓ Angular 自動偵測變更
*ngFor 重新渲染表格，新商品出現在畫面上
```

---

## 8. 常見錯誤與解決方式

| 錯誤訊息 | 原因 | 解決方式 |
|---------|------|---------|
| `Can't bind to 'ngModel'` | 未匯入 `FormsModule` | `app.module.ts` 加入 `FormsModule` |
| `NullInjectorError: No provider for HttpClient` | 未匯入 `HttpClientModule` | `app.module.ts` 加入 `HttpClientModule` |
| `CORS error` | 後端未設定跨來源允許 | Spring Boot 加上 `@CrossOrigin` |
| `Cannot read properties of undefined` | subscribe 前資料還未到達 | 用 `?.` 可選鏈運算子，或初始化陣列 |
| 表單修改影響到列表資料 | 未用 `{ ...product }` 複製 | `startEdit` 改用 spread operator |

---

## 9. 練習題

### 題目一（Easy）— 擴充 Product 欄位
> **需求**：在 `Product` 介面加入 `price: number`，並讓表單支援輸入價格。

**提示**：需修改 `product.model.ts` 和 `product.component.html`

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

```html
<!-- product.component.html 表單區塊加入 -->
<label>價格：
  <input type="number" [(ngModel)]="form.price" placeholder="商品價格" />
</label>
```

```typescript
// product.component.ts 初始化更新
form: Product = { id: '', name: '', price: 0 };
```
</details>

---

### 題目二（Medium）— 加入搜尋功能
> **需求**：在商品列表上方加入搜尋框，即時過濾顯示符合商品名稱的商品。

**提示**：使用 `*ngFor` 搭配 TypeScript 的 `Array.filter()`，或直接在樣板使用 Angular Pipe。

<details>
<summary>查看解答</summary>

```typescript
// product.component.ts
searchKeyword = '';

get filteredProducts(): Product[] {
  return this.products.filter(p =>
    p.name.toLowerCase().includes(this.searchKeyword.toLowerCase())
  );
}
```

```html
<!-- product.component.html -->
<input type="text" [(ngModel)]="searchKeyword" placeholder="搜尋商品名稱..." />

<!-- 將 *ngFor 改為使用 filteredProducts -->
<tr *ngFor="let p of filteredProducts">
```
</details>

---

### 題目三（Hard）— 加入確認對話框 Service
> **需求**：目前刪除使用 `window.confirm()`（原生瀏覽器對話框，無法測試）。請改用 Angular Service 建立一個可注入的確認機制。

**提示**：建立 `ConfirmService`，使用 `Subject<boolean>` 和 `*ngIf` 自訂對話框元件。

---

## 10. 學習重點摘要

| 主題 | 關鍵詞 | 本專案應用位置 |
|------|--------|--------------|
| **Interface（介面）** | TypeScript 型別定義 | `product.model.ts` |
| **Decorator（裝飾器）** | `@Injectable`, `@Component`, `@NgModule` | 所有檔案 |
| **Dependency Injection（依賴注入）** | constructor 注入 | Service、Component |
| **Observable / RxJS** | `.subscribe()`, `next`, `error` | `product.service.ts` |
| **Generic（泛型）** | `get<Product[]>()` | HTTP 呼叫 |
| **Two-way Binding（雙向綁定）** | `[(ngModel)]` | 表單欄位 |
| **Structural Directive（結構指令）** | `*ngIf`, `*ngFor` | 樣板 |
| **Lifecycle Hook（生命週期鉤子）** | `ngOnInit` | Component 初始化 |
| **Spread Operator** | `{ ...obj }` | 物件複製防污染 |

---

### 現在試試看！

1. 執行 `ng serve` 啟動前端（確保 Spring Boot 在 port 8080 運行）
2. 新增一筆 `{ id: "99", name: "Test" }` 商品
3. 觀察 Network tab（F12 → Network）確認 POST 請求的 payload
4. 嘗試完成「題目一：擴充 price 欄位」

---

> **參考資源**
> - [Angular 官方文件](https://angular.io/docs)
> - [RxJS 官方文件](https://rxjs.dev/)
> - [TypeScript 官方文件](https://www.typescriptlang.org/docs/)
> - 練習平台：[StackBlitz](https://stackblitz.com/) 可直接在瀏覽器跑 Angular
