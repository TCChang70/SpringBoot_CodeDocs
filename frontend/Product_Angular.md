# 根據 ProductController.java 的 API 設計
```
我將建立 **現代 Angular (v17+)** 的前端檔案（AngularJS 1.x 已於 2021 年停止支援）。
```
## API 端點整理：
```
- `GET /products` — 取得所有商品
- `GET /products/generate/{num}` — 隨機產生商品
- `POST /products` — 新增商品
- `PUT /products/{id}` — 更新商品
- `DELETE /products/{id}` — 刪除商品 
```


## 建立完成！檔案結構如下：

```
frontend/src/app/
├── models/
│   └── product.model.ts          # Product 介面 {id, name}
├── services/
│   └── product.service.ts        # HTTP 呼叫 (GET/POST/PUT/DELETE)
├── components/product/
│   ├── product.component.ts      # 商品邏輯 (CRUD 操作)
│   ├── product.component.html    # 表單 + 表格 UI
│   └── product.component.css     # 樣式
├── app.module.ts                 # 模組設定
└── app.component.ts              # 根元件
```

---

## API 對應說明

| Angular 方法 | HTTP | Spring Boot 端點 |
|---|---|---|
| `loadProducts()` | GET | `/products` |
| `generate(num)` | GET | `/products/generate/{num}` |
| `create()` | POST | `/products` |
| `update()` | PUT | `/products/{id}` |
| `delete(id)` | DELETE | `/products/{id}` |

---

## 啟動步驟

```bash
# 1. 建立 Angular 專案（若尚未建立）
ng new frontend --routing=false --style=css

# 2. 將上述檔案覆蓋到對應路徑

# 3. 啟動前端（Spring Boot 需同時執行於 port 8080）
cd frontend
ng serve
```

> **注意**：Spring Boot 需要加上 `@CrossOrigin` 或設定 CORS Filter，才能讓 Angular（port 4200）存取後端（port 8080）。
