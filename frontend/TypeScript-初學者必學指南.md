# TypeScript 初學者必學指南

> **對象**：有 JavaScript 基礎，想在 Angular 開發中活用 TypeScript 的開發者  
> **學習目標**：理解 TypeScript 核心型別系統、物件導向語法，以及用原生 `fetch` 與 Angular `HttpClient` 存取後端 API  
> **範例專案**：程式碼均與本工作區的商品管理系統（Product CRUD）整合說明

---
## 安裝 TypeScript

### 前置條件：安裝 Node.js

前往 [nodejs.org](https://nodejs.org) 下載並安裝（會附帶 `npm`）。

### 安裝 TypeScript

```bash
# 全域安裝（推薦）
npm install -g typescript

# 確認安裝成功
tsc --version
# 輸出：Version 5.x.x
```

### 編譯並執行 TypeScript

```bash
# 編譯 .ts → .js
tsc hello.ts

# 執行編譯後的 JS
node hello.js
```

### 更方便：用 `ts-node` 直接執行

```bash
# 安裝 ts-node（不需先編譯，直接執行 .ts）
npm install -g ts-node

# 直接執行
ts-node hello.ts
```

### Angular 專案中

Angular CLI 已自動處理 TypeScript 編譯，**不需要手動安裝或執行 `tsc`**：

```bash
npm install -g @angular/cli
ng new my-app    # 自動包含 TypeScript 設定
ng serve         # 自動編譯 .ts 並啟動
```

### 初始化 TypeScript 設定檔

```bash
# 在專案目錄產生 tsconfig.json
tsc --init
```

`tsconfig.json` 控制編譯選項，Angular 專案已內建此檔案。

---
## 目錄

1. [TypeScript 是什麼？與 JavaScript 的差異](#1-typescript-是什麼與-javascript-的差異)
2. [基本型別（Primitive Types）](#2-基本型別primitive-types)
3. [陣列、Tuple 與 Union 型別](#3-陣列tuple-與-union-型別)
4. [Interface 介面](#4-interface-介面)
5. [Class 類別與存取修飾詞](#5-class-類別與存取修飾詞)
6. [泛型（Generics）](#6-泛型generics)
7. [函式型別與箭頭函式](#7-函式型別與箭頭函式)
8. [可選屬性、非空斷言與可選鏈](#8-可選屬性非空斷言與可選鏈)
9. [模組系統：import / export](#9-模組系統import--export)
10. [非同步程式設計：Promise 與 async/await](#10-非同步程式設計promise-與-asyncawait)
11. [HTTP Fetch 存取後端 API](#11-http-fetch-存取後端-api)
12. [在 Angular 中使用 TypeScript（HttpClient + Observable）](#12-在-angular-中使用-typescripthttpclient--observable)
13. [TypeScript Decorator 裝飾器](#13-typescript-decorator-裝飾器)
14. [常見錯誤速查表](#14-常見錯誤速查表)
15. [練習題（Easy → Hard）](#15-練習題easy--hard)
16. [學習路線圖：下一步](#16-學習路線圖下一步)

---

## 1. TypeScript 是什麼？與 JavaScript 的差異

### 白話解釋

TypeScript 是 JavaScript 的**超集合（Superset）**——所有合法的 JS 都是合法的 TS，但 TS 多了「**靜態型別系統（Static Type System）**」。

```
JavaScript → 執行時才知道型別錯誤（程式崩潰）
TypeScript → 編譯時就知道型別錯誤（IDE 立即標紅）
```

### 核心差異對比

```typescript
// JavaScript（沒有型別保護）
function add(a, b) {
  return a + b;
}
add(1, "2");   // 回傳 "12"，沒有錯誤！

// TypeScript（有型別保護）
function add(a: number, b: number): number {
  return a + b;
}
add(1, "2");   // ❌ 編譯錯誤：Argument of type 'string' is not assignable to parameter of type 'number'
```

### TypeScript 在 Angular 中的角色

Angular 本身就是用 TypeScript 撰寫的，整個開發流程都建立在 TS 之上：

```
.ts 檔案
  ↓ tsc（TypeScript Compiler）
.js 檔案（瀏覽器可執行）
```

| TypeScript 功能 | Angular 中的用途 |
|----------------|----------------|
| Interface | 定義 API 回傳的資料格式（如 `Product`） |
| Class | Component、Service 的本體 |
| Decorator | `@Component`、`@Injectable`、`@Input` |
| Generics | `Observable<Product[]>`、`http.get<Product[]>()` |
| Access Modifier | `private productService: ProductService` |

---

## 2. 基本型別（Primitive Types）

### 內建型別宣告

```typescript
// 語法：變數名稱: 型別 = 值
let productName: string = "Honey";
let price: number = 99.9;
let isAvailable: boolean = true;
let description: null = null;        // 明確是 null
let stock: undefined = undefined;    // 明確是 undefined

// TypeScript 型別推斷（Type Inference）
// 有給初始值時，不用手動標型別，TS 會自動推斷
let category = "food";    // 推斷為 string
let count = 10;           // 推斷為 number

// 但函式參數不會自動推斷，一定要標型別
function setPrice(p: number) { ... }
```

### `any` 與 `unknown`（謹慎使用）

```typescript
// any — 關閉型別檢查（不推薦，相當於退回 JavaScript）
let data: any = "hello";
data = 123;        // ✅ 不報錯
data.foo.bar;      // ✅ 不報錯（但執行時可能崩潰）

// unknown — 安全的「不確定型別」（推薦代替 any）
let input: unknown = fetchData();
input.toUpperCase();                         // ❌ 編譯錯誤
if (typeof input === "string") {
  input.toUpperCase();                       // ✅ 型別縮窄後才能使用
}
```

### 型別別名（Type Alias）

```typescript
// 給型別取別名，讓語意更清楚
type ProductId = string;
type Price = number;

const id: ProductId = "P001";
const unitPrice: Price = 150;
```

### 常見陷阱

```typescript
// ❌ 宣告後賦值不同型別
let name: string = "Alice";
name = 123;   // 錯誤：Type 'number' is not assignable to type 'string'

// ❌ 忘記型別，IDE 無法提示
function process(data) { ... }   // data 被推斷為 any

// ✅ 明確標型別
function process(data: string): void { ... }
```

---

## 3. 陣列、Tuple 與 Union 型別

### 陣列（Array）

```typescript
// 寫法一：型別[]
let products: string[] = ["Honey", "Bee Wax", "Pollen"];
let prices: number[] = [100, 200, 300];

// 寫法二：Array<型別>（泛型寫法，等同上面）
let ids: Array<string> = ["P001", "P002"];

// 物件陣列（搭配 Interface）
interface Product {
  id: string;
  name: string;
}
let productList: Product[] = [
  { id: "P001", name: "Honey" },
  { id: "P002", name: "Bee Wax" }
];

// 常用陣列操作
productList.push({ id: "P003", name: "Pollen" });        // 新增至末尾
productList.filter(p => p.id !== "P001");                 // 過濾
productList.find(p => p.id === "P002");                   // 找到第一個
productList.some(p => p.name === "Honey");                // 存在性檢查
productList.map(p => p.name);                             // 轉換成 string[]
```

### Tuple（固定長度的陣列）

```typescript
// 兩個元素，第一個是 string，第二個是 number
let entry: [string, number] = ["P001", 150];

// 常用於解構
const [id, price] = entry;
console.log(id);    // "P001"
console.log(price); // 150
```

### Union 型別（聯合型別）

```typescript
// 可以是 string 或 number
let productId: string | number = "P001";
productId = 1001;    // ✅ 也可以

// 函式參數允許多種型別
function printId(id: string | number): void {
  if (typeof id === "string") {
    console.log(`ID: ${id.toUpperCase()}`);
  } else {
    console.log(`ID: ${id}`);
  }
}

// 可選值（value 或 null）
let selected: Product | null = null;
selected = { id: "P001", name: "Honey" };
```

---

## 4. Interface 介面

### 基本語法

```typescript
// product.model.ts — 本專案的資料模型定義
export interface Product {
  id: string;
  name: string;
}
```

### 可選屬性（Optional Property）

```typescript
export interface Product {
  id: string;
  name: string;
  price?: number;        // ? 代表可選，可以不提供
  description?: string;
}

// 以下兩種都合法
const p1: Product = { id: "P001", name: "Honey" };
const p2: Product = { id: "P002", name: "Bee Wax", price: 150 };
```

### 唯讀屬性（Readonly）

```typescript
interface ApiConfig {
  readonly baseUrl: string;    // 一旦設定不能再改
  timeout: number;
}

const config: ApiConfig = { baseUrl: "http://localhost:8080", timeout: 5000 };
config.baseUrl = "http://other.com";   // ❌ 錯誤：Cannot assign to 'baseUrl'
config.timeout = 10000;               // ✅ 可以
```

### Interface 擴充（Extends）

```typescript
interface BaseEntity {
  id: string;
  createdAt: string;
}

interface Product extends BaseEntity {
  name: string;
  price: number;
}
// Product 現在有：id, createdAt, name, price

const p: Product = {
  id: "P001",
  createdAt: "2026-01-01",
  name: "Honey",
  price: 150
};
```

### Interface vs Type Alias

```typescript
// Interface — 可以 extends，可以被 class implements
interface Printable {
  print(): void;
}

// Type Alias — 更靈活，可以定義 Union/Intersection
type Status = "active" | "inactive" | "pending";
type ProductOrNull = Product | null;

// 規則：物件形狀用 Interface，其他用 Type Alias
```

### 常見陷阱

```typescript
// ❌ 拼錯屬性名
const p: Product = { id: "001", Name: "Honey" };
// 錯誤：Object literal may only specify known properties, 'Name' does not exist

// ❌ 多餘屬性（直接賦值時 TS 嚴格檢查）
const p: Product = { id: "001", name: "Honey", extra: true };  // ❌

// ✅ 但透過中間變數可以繞過（不建議）
const raw = { id: "001", name: "Honey", extra: true };
const p: Product = raw;  // TS 只檢查必要屬性是否存在
```

---

## 5. Class 類別與存取修飾詞

### Access Modifiers（存取修飾詞）

```typescript
class ProductService {
  private apiUrl: string;          // 只有類別內部可存取
  protected baseUrl: string;       // 類別內部及子類別可存取
  public serviceName: string;      // 任何地方都可存取（預設）
  readonly version = "1.0";        // 只讀，初始化後不能改

  constructor(url: string) {
    this.apiUrl = url;
    this.baseUrl = "http://localhost:8080";
    this.serviceName = "ProductService";
  }
}

const svc = new ProductService("http://api.example.com");
svc.serviceName;   // ✅ public
svc.apiUrl;        // ❌ 錯誤：Property 'apiUrl' is private
```

### 建構子簡寫（Constructor Shorthand）

Angular 大量使用這個語法：

```typescript
// 完整寫法
class ProductComponent {
  private productService: ProductService;

  constructor(productService: ProductService) {
    this.productService = productService;
  }
}

// ✅ 簡寫：在 constructor 參數加 access modifier，自動宣告屬性
class ProductComponent {
  constructor(private productService: ProductService) {}
  // 等同上面，自動建立 this.productService 屬性
}
```

### 實作 Interface（implements）

```typescript
interface Loadable {
  isLoading: boolean;
  load(): void;
}

// 告訴 TypeScript 這個 class 保證實作所有 Interface 要求的成員
class ProductComponent implements Loadable {
  isLoading = false;

  load(): void {
    this.isLoading = true;
    // ... 載入資料
  }
}
```

### Getter / Setter

```typescript
class ProductComponent {
  private _products: Product[] = [];
  private _searchKeyword = "";

  // Getter — 讓 Template 可以用 filteredProducts 存取
  get filteredProducts(): Product[] {
    if (!this._searchKeyword) return this._products;
    return this._products.filter(p =>
      p.name.toLowerCase().includes(this._searchKeyword.toLowerCase())
    );
  }

  // Setter
  set searchKeyword(value: string) {
    this._searchKeyword = value.trim();
  }
}
```

---

## 6. 泛型（Generics）

### 泛型函式

```typescript
// 沒有泛型：每種型別都要寫一個函式
function firstString(arr: string[]): string { return arr[0]; }
function firstNumber(arr: number[]): number { return arr[0]; }

// ✅ 有泛型：一個函式處理所有型別
function first<T>(arr: T[]): T {
  return arr[0];
}

first<string>(["a", "b", "c"]);    // 回傳 string
first<number>([1, 2, 3]);          // 回傳 number
first<Product>(productList);       // 回傳 Product
```

### 泛型 Interface（Angular 中最常見）

```typescript
// API 回應的通用格式
interface ApiResponse<T> {
  data: T;
  status: number;
  message: string;
}

// 使用
const productResponse: ApiResponse<Product[]> = {
  data: [{ id: "P001", name: "Honey" }],
  status: 200,
  message: "success"
};

const singleProduct: ApiResponse<Product> = {
  data: { id: "P001", name: "Honey" },
  status: 200,
  message: "success"
};
```

### Angular 中的泛型應用

```typescript
// HttpClient 的泛型 — 告訴 TS 回傳資料的型別
this.http.get<Product[]>("http://localhost:8080/products");
//           ↑ 泛型參數：回傳值型別

// Observable 的泛型 — 告訴 TS Observable 會發出的值型別
getAll(): Observable<Product[]> { ... }
delete(id: string): Observable<Product> { ... }

// subscribe 時，data 的型別自動推斷
this.productService.getAll().subscribe({
  next: (data) => {
    // data 被推斷為 Product[]，有自動完成！
    this.products = data;
  }
});
```

### 常見陷阱

```typescript
// ❌ 忘記泛型，型別全變 any
this.http.get("http://localhost:8080/products");   // 回傳 Observable<Object>

// ✅ 一定要加泛型
this.http.get<Product[]>("http://localhost:8080/products"); // 回傳 Observable<Product[]>
```

---

## 7. 函式型別與箭頭函式

### 函式宣告方式

```typescript
// 一般函式
function createProduct(id: string, name: string): Product {
  return { id, name };
}

// 箭頭函式（Arrow Function）— Angular 中最常用
const createProduct = (id: string, name: string): Product => {
  return { id, name };
};

// 單行箭頭函式（隱式回傳）
const getName = (p: Product): string => p.name;

// 回傳 void（沒有回傳值）
const showMessage = (msg: string): void => {
  console.log(msg);
};
```

### 函式型別作為參數（Callback）

```typescript
// 定義函式型別
type Comparator = (a: Product, b: Product) => number;

function sortProducts(products: Product[], compareFn: Comparator): Product[] {
  return [...products].sort(compareFn);
}

// 使用
const sorted = sortProducts(products, (a, b) => a.name.localeCompare(b.name));
```

### 解構賦值（Destructuring）

```typescript
// 物件解構
const { id, name } = product;
console.log(id);    // product.id 的值

// 陣列解構
const [first, second, ...rest] = products;

// 函式參數解構（Angular callback 中常見）
this.productService.getAll().subscribe({
  next: (data) => this.products = data,   // data 是解構後的值
  error: (err) => console.error(err)
});
```

### Spread Operator 展開運算子

```typescript
// 複製物件（避免改到原始資料）
this.form = { ...product };           // ✅ 淺層複製

// 合併物件
const updated = { ...product, name: "New Name" };

// 陣列展開
const newProducts = [...this.products, newProduct];

// 為什麼要用 spread？
// ❌ 直接指定是參考（reference）
this.form = product;
this.form.name = "Changed";   // 也會改到 product！

// ✅ Spread 是複製新物件
this.form = { ...product };
this.form.name = "Changed";   // 不影響原始 product
```

---

## 8. 可選屬性、非空斷言與可選鏈

### 可選鏈（Optional Chaining）`?.`

```typescript
// 存取可能為 null/undefined 的屬性
const selectedProduct: Product | null = null;

// ❌ 危險：如果 selectedProduct 是 null，會崩潰
console.log(selectedProduct.name);   // TypeError: Cannot read property 'name' of null

// ✅ 可選鏈：如果為 null/undefined，直接回傳 undefined
console.log(selectedProduct?.name);  // undefined（不崩潰）

// 鏈式存取
user?.address?.city?.name
```

### 空值合併（Nullish Coalescing）`??`

```typescript
// ?? 在左邊是 null 或 undefined 時，使用右邊的預設值
const name = product?.name ?? "（未命名）";
const price = product?.price ?? 0;

// 和 || 的差異
const val1 = 0 || "default";    // "default"（0 是 falsy）
const val2 = 0 ?? "default";    // 0（0 不是 null/undefined）
```

### 非空斷言（Non-null Assertion）`!`

```typescript
// 告訴 TS「我確定這不是 null」（開發者自行負責）
const name = product!.name;     // 斷言 product 不是 null

// 在 Angular 中常見（@Input 的宣告）
@Input() product!: Product;
//               ↑ 告訴 TS：這個屬性一定會由父元件傳入

// 常見陷阱：過度使用 ! 會掩蓋問題
const p: Product | null = getProduct();
console.log(p!.name);   // 如果 p 真的是 null，執行時還是會崩潰
```

### 型別縮窄（Type Narrowing）— 更安全的做法

```typescript
// 用條件判斷縮窄型別範圍
function printProduct(p: Product | null): void {
  if (p === null) {
    console.log("無商品");
    return;
  }
  // 這裡 TS 已確定 p 是 Product（不是 null）
  console.log(p.name);
}
```

---

## 9. 模組系統：import / export

### Named Export（命名匯出）

```typescript
// product.model.ts
export interface Product {         // ← 加 export 讓外部可以匯入
  id: string;
  name: string;
}

export type ProductStatus = "active" | "inactive";
```

```typescript
// product.component.ts
import { Product, ProductStatus } from '../../models/product.model';
//       ↑ 用花括號選擇要匯入的名稱
```

### Default Export（預設匯出）— Angular 較少用

```typescript
// utils.ts
export default function formatPrice(price: number): string {
  return `$${price.toFixed(2)}`;
}

// 匯入時可以自定名稱
import formatPrice from './utils';
import myFormat from './utils';    // ✅ 也可以取不同名稱
```

### Angular 模組 vs ES 模組

```typescript
// ES 模組（檔案層級的 import/export）— TypeScript 語法
import { Component } from '@angular/core';
import { ProductService } from './services/product.service';

// Angular NgModule（@NgModule 裡的 imports）— Angular 框架概念
@NgModule({
  imports: [HttpClientModule, FormsModule]   // ← 這是 Angular 框架的模組，不是 ES 模組
})
```

---

## 10. 非同步程式設計：Promise 與 async/await

### Promise 基礎

```typescript
// Promise 代表「一個未來的值」
function fetchProduct(id: string): Promise<Product> {
  return new Promise((resolve, reject) => {
    // 模擬非同步操作
    setTimeout(() => {
      if (id === "P001") {
        resolve({ id: "P001", name: "Honey" });   // 成功
      } else {
        reject(new Error("商品不存在"));            // 失敗
      }
    }, 1000);
  });
}

// 使用 .then() / .catch()
fetchProduct("P001")
  .then(product => console.log(product.name))
  .catch(err => console.error(err.message));
```

### async/await（現代推薦寫法）

```typescript
// async 函式會回傳 Promise
async function loadProduct(id: string): Promise<Product | null> {
  try {
    const product = await fetchProduct(id);   // 等待 Promise 完成
    return product;
  } catch (error) {
    console.error("載入失敗", error);
    return null;
  }
}

// 呼叫 async 函式
async function main() {
  const product = await loadProduct("P001");
  if (product) {
    console.log(product.name);
  }
}
```

### Promise 並行執行

```typescript
// Promise.all — 所有都成功才繼續，有一個失敗就 catch
const [products, categories] = await Promise.all([
  fetchProducts(),
  fetchCategories()
]);

// Promise.allSettled — 無論成功失敗都等所有完成
const results = await Promise.allSettled([
  fetchProducts(),
  fetchCategories()
]);
results.forEach(result => {
  if (result.status === "fulfilled") console.log(result.value);
  else console.error(result.reason);
});
```

---

## 11. HTTP Fetch 存取後端 API

原生 `fetch` API 是瀏覽器內建的 HTTP 客戶端，不需要安裝任何套件。

### 基本 GET 請求

```typescript
// 型別定義
interface Product {
  id: string;
  name: string;
}

// 取得所有商品
async function getProducts(): Promise<Product[]> {
  const response = await fetch("http://localhost:8080/products");

  // 檢查 HTTP 狀態碼（200–299 為成功）
  if (!response.ok) {
    throw new Error(`HTTP error! status: ${response.status}`);
  }

  // 解析 JSON（回傳 Promise，需 await）
  const data: Product[] = await response.json();
  return data;
}

// 使用
try {
  const products = await getProducts();
  console.log(products);   // Product[] 型別
} catch (error) {
  console.error("取得商品失敗", error);
}
```

### POST — 新增資料

```typescript
async function createProduct(product: Product): Promise<Product> {
  const response = await fetch("http://localhost:8080/products", {
    method: "POST",
    headers: {
      "Content-Type": "application/json",    // 告訴後端 body 是 JSON
    },
    body: JSON.stringify(product),           // 將物件轉成 JSON 字串
  });

  if (!response.ok) {
    throw new Error(`新增失敗：${response.status}`);
  }

  return response.json() as Promise<Product>;
}
```

### PUT — 更新資料

```typescript
async function updateProduct(id: string, product: Product): Promise<Product> {
  const response = await fetch(`http://localhost:8080/products/${id}`, {
    method: "PUT",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify(product),
  });

  if (!response.ok) {
    throw new Error(`更新失敗：${response.status}`);
  }

  return response.json() as Promise<Product>;
}
```

### DELETE — 刪除資料

```typescript
async function deleteProduct(id: string): Promise<void> {
  const response = await fetch(`http://localhost:8080/products/${id}`, {
    method: "DELETE",
  });

  if (!response.ok) {
    throw new Error(`刪除失敗：${response.status}`);
  }
  // 204 No Content — 沒有回傳 body
}
```

### 完整的 Fetch 服務封裝

```typescript
// api.service.ts（純 TypeScript，不依賴 Angular）
const BASE_URL = "http://localhost:8080";

interface Product {
  id: string;
  name: string;
}

async function request<T>(
  path: string,
  options: RequestInit = {}
): Promise<T> {
  const response = await fetch(`${BASE_URL}${path}`, {
    headers: { "Content-Type": "application/json", ...options.headers },
    ...options,
  });
  if (!response.ok) {
    throw new Error(`Request failed: ${response.status} ${response.statusText}`);
  }
  // 204 No Content 沒有 body
  if (response.status === 204) return undefined as unknown as T;
  return response.json() as Promise<T>;
}

// 使用封裝後的 request 函式
const getAll = () => request<Product[]>("/products");
const create = (p: Product) => request<Product>("/products", { method: "POST", body: JSON.stringify(p) });
const update = (id: string, p: Product) => request<Product>(`/products/${id}`, { method: "PUT", body: JSON.stringify(p) });
const remove = (id: string) => request<void>(`/products/${id}`, { method: "DELETE" });

// 呼叫
async function main() {
  const products = await getAll();
  console.log(products);

  const newProduct = await create({ id: "P999", name: "New Item" });
  console.log("已新增：", newProduct);
}
```

### 常見陷阱

```typescript
// ❌ 忘記 await json()，拿到 Promise 物件而不是資料
const data = response.json();    // data 是 Promise<any>，不是資料！

// ✅ 一定要 await
const data = await response.json();

// ❌ 沒有檢查 response.ok，4xx/5xx 不會自動 throw
const res = await fetch(url);
const data = await res.json();   // 即使 404，也會執行（拿到錯誤訊息的 JSON）

// ✅ 先檢查 response.ok
if (!res.ok) throw new Error(`HTTP ${res.status}`);

// ❌ 忘記設定 Content-Type header，後端收不到 JSON
await fetch(url, { method: "POST", body: JSON.stringify(data) });  // 少 header！

// ✅
await fetch(url, {
  method: "POST",
  headers: { "Content-Type": "application/json" },
  body: JSON.stringify(data)
});
```

---

## 12. 在 Angular 中使用 TypeScript（HttpClient + Observable）

Angular 不使用原生 `fetch`，而是用 **`HttpClient`** 搭配 **`Observable`**（RxJS）。

### Observable vs Promise

```
Promise：只能發出一個值，完成後結束
Observable：可以發出多個值，可以取消訂閱

fetch / async-await → Promise
Angular HttpClient → Observable（更強大）
```

### HttpClient 完整使用方式

```typescript
// product.service.ts
import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable, throwError } from 'rxjs';
import { catchError, map } from 'rxjs/operators';
import { Product } from '../models/product.model';

@Injectable({ providedIn: 'root' })
export class ProductService {

  private apiUrl = 'http://localhost:8080/products';

  // HttpClient 由 Angular DI 自動注入
  constructor(private http: HttpClient) {}

  // GET — 取得所有商品
  getAll(): Observable<Product[]> {
    return this.http.get<Product[]>(this.apiUrl);
  }

  // GET — 取得單一商品
  getById(id: string): Observable<Product> {
    return this.http.get<Product>(`${this.apiUrl}/${id}`);
  }

  // POST — 新增商品
  create(product: Product): Observable<Product> {
    return this.http.post<Product>(this.apiUrl, product);
    // HttpClient 自動設定 Content-Type: application/json
  }

  // PUT — 更新商品
  update(id: string, product: Product): Observable<Product> {
    return this.http.put<Product>(`${this.apiUrl}/${id}`, product);
  }

  // DELETE — 刪除商品
  delete(id: string): Observable<Product> {
    return this.http.delete<Product>(`${this.apiUrl}/${id}`);
  }

  // GET with Query Parameters（查詢參數）
  generate(num: number): Observable<Product[]> {
    return this.http.get<Product[]>(`${this.apiUrl}/generate/${num}`);
    // 結果：GET http://localhost:8080/products/generate/3
  }
}
```

### 在 Component 中訂閱 Observable

```typescript
// product.component.ts
export class ProductComponent implements OnInit {
  products: Product[] = [];
  message = '';

  constructor(private productService: ProductService) {}

  ngOnInit(): void {
    this.loadProducts();
  }

  // 查詢
  loadProducts(): void {
    this.productService.getAll().subscribe({
      next: (data: Product[]) => {
        this.products = data;
      },
      error: (err: Error) => {
        this.message = `載入失敗：${err.message}`;
      }
    });
  }

  // 新增
  create(form: Product): void {
    this.productService.create({ ...form }).subscribe({
      next: (created: Product) => {
        this.products.push(created);
        this.message = `已新增：${created.name}`;
      },
      error: () => {
        this.message = '新增失敗';
      }
    });
  }

  // 刪除
  delete(id: string): void {
    if (!confirm(`確定要刪除 ${id}？`)) return;
    this.productService.delete(id).subscribe({
      next: () => {
        this.products = this.products.filter(p => p.id !== id);
        this.message = `已刪除 ID：${id}`;
      }
    });
  }
}
```

### fetch vs HttpClient 對比

| 特性 | 原生 fetch | Angular HttpClient |
|------|-----------|-------------------|
| 回傳型別 | `Promise<Response>` | `Observable<T>` |
| 型別支援 | 需手動 cast | 泛型直接指定 `get<Product[]>()` |
| Content-Type | 需手動設定 header | 自動設定 `application/json` |
| 錯誤處理 | 需手動檢查 `response.ok` | 4xx/5xx 自動 throw 到 `error` callback |
| 取消請求 | 需用 AbortController | `unsubscribe()` 即可取消 |
| Interceptor | 需自行包裝 | 內建 `HttpInterceptor` |

---

## 13. TypeScript Decorator 裝飾器

Angular 大量使用 TypeScript Decorator（以 `@` 開頭）。

### 常見 Angular Decorator

```typescript
// @Component — 把 class 標記為 Angular 元件
@Component({
  selector: 'app-product',
  templateUrl: './product.component.html',
  styleUrls: ['./product.component.css']
})
export class ProductComponent { }

// @Injectable — 把 class 標記為可注入的 Service
@Injectable({ providedIn: 'root' })
export class ProductService { }

// @Input — 讓屬性可從父元件接收資料
@Input() product!: Product;

// @Output — 讓屬性可向父元件發送事件
@Output() productDeleted = new EventEmitter<string>();

// @NgModule — 定義 Angular 模組
@NgModule({
  declarations: [AppComponent, ProductComponent],
  imports: [BrowserModule, FormsModule, HttpClientModule],
  bootstrap: [AppComponent]
})
export class AppModule { }
```

### Decorator 的原理（概念理解）

```typescript
// Decorator 本質是一個高階函式，接收目標並修改它
function Log(target: any, key: string, descriptor: PropertyDescriptor) {
  const original = descriptor.value;
  descriptor.value = function(...args: any[]) {
    console.log(`呼叫 ${key}，參數：`, args);
    return original.apply(this, args);
  };
  return descriptor;
}

class ProductComponent {
  @Log
  delete(id: string): void { /* ... */ }
}
// 每次呼叫 delete 時，自動 console.log
```

---

## 14. 常見錯誤速查表

| 錯誤訊息 | 原因 | 解決方式 |
|---------|------|---------|
| `Type 'string' is not assignable to type 'number'` | 型別不符 | 確認變數型別，或做型別轉換 `Number(str)` |
| `Object is possibly 'null'` | 變數可能是 null | 用 `?.` 可選鏈或 `if (x !== null)` 型別縮窄 |
| `Property 'x' does not exist on type 'Product'` | 存取未定義的屬性 | 在 Interface 新增屬性，或用可選屬性 `x?: string` |
| `Cannot find name 'ProductService'` | 忘記 import | 加入 `import { ProductService } from '...'` |
| `Expected 2 arguments, but got 1` | 函式參數數量不符 | 補齊參數，或把參數改為可選 `param?: string` |
| `Property has no initializer and is not definitely assigned` | 屬性沒有初始值 | 給預設值或加 `!` 非空斷言 |
| `Type 'Observable<Product[]>' is missing properties...` | 混淆 Observable 和 Array | 加 `.subscribe()` 才能拿到陣列 |
| `Cannot read properties of undefined` | 資料還未到達就存取 | 在 Template 用 `?.` 或確保屬性有初始值 |

---

## 15. 練習題（Easy → Hard）

### 題目一（Easy）— 宣告商品介面並過濾
> **需求**：定義 `Product` Interface（含 `id: string`、`name: string`、`price: number`），建立一個包含 5 筆商品的陣列，過濾出 `price > 100` 的商品，並用 `map` 取出所有商品名稱。

<details>
<summary>查看解答</summary>

```typescript
interface Product {
  id: string;
  name: string;
  price: number;
}

const products: Product[] = [
  { id: "P001", name: "Honey", price: 150 },
  { id: "P002", name: "Bee Wax", price: 80 },
  { id: "P003", name: "Pollen", price: 200 },
  { id: "P004", name: "Royal Jelly", price: 350 },
  { id: "P005", name: "Propolis", price: 50 }
];

const expensive = products.filter(p => p.price > 100);
const names = expensive.map(p => p.name);
console.log(names);   // ["Honey", "Pollen", "Royal Jelly"]
```

**學習重點**：Interface 讓 `filter` 與 `map` 的 callback 參數 `p` 有型別提示。
</details>

---

### 題目二（Easy）— async/await + fetch 取得商品

> **需求**：用原生 `fetch` + `async/await` 向 `http://localhost:8080/products` 發送 GET 請求，取得 `Product[]` 並印出每筆商品的 `name`。要有 `try/catch` 錯誤處理。

<details>
<summary>查看解答</summary>

```typescript
interface Product {
  id: string;
  name: string;
}

async function getProducts(): Promise<Product[]> {
  const response = await fetch("http://localhost:8080/products");
  if (!response.ok) {
    throw new Error(`HTTP error! status: ${response.status}`);
  }
  return response.json() as Promise<Product[]>;
}

async function main() {
  try {
    const products = await getProducts();
    products.forEach(p => console.log(p.name));
  } catch (error) {
    console.error("取得商品失敗：", error);
  }
}

main();
```

**學習重點**：`fetch` 的 `response.ok` 判斷 + `await response.json()` 解析 JSON。
</details>

---

### 題目三（Medium）— 泛型 API 封裝

> **需求**：建立一個泛型函式 `apiRequest<T>(url, options?): Promise<T>`，讓 `getAll`、`create`、`deleteById` 都透過這個函式發請求，避免重複程式碼。

<details>
<summary>查看解答</summary>

```typescript
interface Product {
  id: string;
  name: string;
}

async function apiRequest<T>(url: string, options: RequestInit = {}): Promise<T> {
  const response = await fetch(url, {
    headers: { "Content-Type": "application/json", ...options.headers },
    ...options,
  });
  if (!response.ok) {
    throw new Error(`Request failed: ${response.status}`);
  }
  if (response.status === 204) return undefined as unknown as T;
  return response.json() as Promise<T>;
}

const BASE = "http://localhost:8080/products";

async function getAll(): Promise<Product[]> {
  return apiRequest<Product[]>(BASE);
}

async function create(product: Product): Promise<Product> {
  return apiRequest<Product>(BASE, {
    method: "POST",
    body: JSON.stringify(product),
  });
}

async function deleteById(id: string): Promise<void> {
  return apiRequest<void>(`${BASE}/${id}`, { method: "DELETE" });
}
```

**學習重點**：泛型函式讓型別從呼叫端傳入，`<T>` 使回傳值型別得以保留。
</details>

---

### 題目四（Medium）— Angular HttpClient + 搜尋功能

> **需求**：在 Angular 的 `ProductService` 新增 `search(keyword: string): Observable<Product[]>` 方法，向 `GET /products?name=xxx` 發送查詢請求；並在 `ProductComponent` 中加入搜尋輸入框，使用者按下搜尋鈕時呼叫此方法並顯示結果。

<details>
<summary>查看解答</summary>

```typescript
// product.service.ts
search(keyword: string): Observable<Product[]> {
  const params = new HttpParams().set("name", keyword);
  return this.http.get<Product[]>(this.apiUrl, { params });
  // 實際請求：GET http://localhost:8080/products?name=honey
}
```

```typescript
// product.component.ts
searchKeyword = '';
searchResults: Product[] = [];

onSearch(): void {
  if (!this.searchKeyword.trim()) return;
  this.productService.search(this.searchKeyword).subscribe({
    next: (data) => this.searchResults = data,
    error: () => this.message = '搜尋失敗'
  });
}
```

```html
<!-- product.component.html -->
<input type="text" [(ngModel)]="searchKeyword" placeholder="搜尋商品" />
<button (click)="onSearch()">搜尋</button>

<ul>
  <li *ngFor="let p of searchResults">{{ p.id }} — {{ p.name }}</li>
</ul>
```

**學習重點**：`HttpParams` 的用法，以及 Angular `HttpClient` 自動處理 URL 編碼。
</details>

---

### 題目五（Hard）— 撰寫型別安全的 HttpClient Service

> **需求**：建立通用的 `CrudService<T>` 抽象類別，讓 `ProductService` 繼承後只需傳入 `apiUrl`，就能獲得完整的 `getAll`、`create`、`update`、`delete` 方法，且全部型別安全。

<details>
<summary>查看解答</summary>

```typescript
// crud.service.ts
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

export abstract class CrudService<T extends { id: string }> {
  constructor(
    protected http: HttpClient,
    protected apiUrl: string
  ) {}

  getAll(): Observable<T[]> {
    return this.http.get<T[]>(this.apiUrl);
  }

  getById(id: string): Observable<T> {
    return this.http.get<T>(`${this.apiUrl}/${id}`);
  }

  create(entity: T): Observable<T> {
    return this.http.post<T>(this.apiUrl, entity);
  }

  update(id: string, entity: T): Observable<T> {
    return this.http.put<T>(`${this.apiUrl}/${id}`, entity);
  }

  delete(id: string): Observable<T> {
    return this.http.delete<T>(`${this.apiUrl}/${id}`);
  }
}
```

```typescript
// product.service.ts
@Injectable({ providedIn: 'root' })
export class ProductService extends CrudService<Product> {
  constructor(http: HttpClient) {
    super(http, 'http://localhost:8080/products');
  }
  // 已繼承全部 CRUD 方法，也可在這裡新增特有方法
  generate(num: number): Observable<Product[]> {
    return this.http.get<Product[]>(`${this.apiUrl}/generate/${num}`);
  }
}
```

**學習重點**：泛型抽象類別的設計模式，`extends { id: string }` 為泛型加上約束（Generic Constraint）。
</details>

---

## 16. 學習路線圖：下一步

### 階段一：鞏固 TypeScript 基礎（完成本文件後）
- [ ] 完成所有練習題
- [ ] 閱讀本專案的 `product.model.ts`、`product.service.ts`、`product.component.ts` 並對照本文件說明
- [ ] 在 [TypeScript Playground](https://www.typescriptlang.org/play) 練習型別系統

### 階段二：進階 TypeScript 功能（1–2 週）

| 主題 | 說明 | 關鍵詞 |
|------|------|--------|
| **Utility Types** | 內建型別工具 | `Partial<T>`, `Required<T>`, `Pick<T>`, `Omit<T>` |
| **Conditional Types** | 條件型別推斷 | `T extends U ? X : Y` |
| **Mapped Types** | 遍歷型別屬性 | `{ [K in keyof T]: ... }` |
| **Type Guards** | 自訂型別縮窄 | `is` 關鍵字 |
| **Enums** | 枚舉常數 | `enum Status { Active, Inactive }` |
| **Namespace** | 大型專案組織 | `namespace MyApp { ... }` |

### 階段三：Angular + TypeScript 整合（2–4 週）

| 主題 | 說明 |
|------|------|
| RxJS Operators | `map`、`switchMap`、`debounceTime`、`catchError` |
| HTTP Interceptor | 統一加 JWT Token、處理全域錯誤 |
| Reactive Forms | `FormGroup`、`FormControl`、`Validators` |
| Angular Signals | Angular 17+ 的響應式新語法 |
| Standalone Components | 不需 NgModule 的新架構 |

### 推薦免費資源

| 資源 | 說明 | 連結 |
|------|------|------|
| TypeScript 官方文件 | 最完整的參考資料 | [typescriptlang.org](https://www.typescriptlang.org/docs/) |
| TypeScript Playground | 瀏覽器內直接執行 TS | [typescriptlang.org/play](https://www.typescriptlang.org/play) |
| RxJS 官方文件 | Observable / 操作符說明 | [rxjs.dev](https://rxjs.dev/) |
| Angular 官方文件 | Angular + TS 整合 | [angular.io](https://angular.io/docs) |
| Execute Program | TypeScript 互動式練習 | [executeprogram.com](https://www.executeprogram.com/) |

---

## 學習重點摘要

| 概念 | 英文術語 | 關鍵語法 | 本專案應用 |
|------|---------|---------|----------|
| 基本型別 | Primitive Types | `string`, `number`, `boolean` | `isEditing: boolean` |
| 介面 | Interface | `interface Product { ... }` | `product.model.ts` |
| 泛型 | Generics | `Array<T>`, `Observable<T>` | `http.get<Product[]>()` |
| 存取修飾詞 | Access Modifiers | `private`, `public`, `readonly` | `private productService` |
| 可選鏈 | Optional Chaining | `obj?.prop` | `product?.name` |
| 展開運算子 | Spread Operator | `{ ...obj }` | `this.form = { ...product }` |
| 非同步 | Async/Await | `async function`, `await` | — |
| HTTP 原生呼叫 | Fetch API | `fetch(url, options)` | 純 TS 環境 |
| HTTP Angular | HttpClient | `this.http.get<T>()` | `product.service.ts` |
| Observable 訂閱 | Observable | `.subscribe({ next, error })` | `product.component.ts` |
| 裝飾器 | Decorator | `@Component`, `@Injectable` | Angular 所有 class |

---

> **現在試試看！**
> 1. 開啟 [TypeScript Playground](https://www.typescriptlang.org/play) 並貼上「泛型函式」的範例程式碼執行
> 2. 在本專案的 `product.model.ts` 新增 `price?: number` 可選屬性，觀察 TypeScript 在哪些地方提示需要處理
> 3. 嘗試「題目二：async/await + fetch」，用瀏覽器 Console 直接測試（確保 Spring Boot 在 port 8080 運行）
> 4. 完成「題目三：泛型 API 封裝」，感受泛型如何消除重複程式碼
