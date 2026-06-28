<%@ page contentType="text/html; charset=UTF-8" %>
<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<title>EclipseLink JPA 三天課程 (JAX-RS + MySQL)</title>
<style>
body{font-family:Arial,sans-serif;margin:40px;line-height:1.6}
h1{color:#2c3e50;border-bottom:3px solid #3498db;padding-bottom:10px}
h2{color:#2980b9}
.day{background:#f8f9fa;border-left:4px solid #3498db;padding:15px;margin:15px 0;border-radius:0 8px 8px 0}
.endpoint{font-family:monospace;background:#ecf0f1;padding:3px 8px;border-radius:4px}
code{background:#ecf0f1;padding:2px 6px;border-radius:3px}
table{border-collapse:collapse;width:100%}
th,td{border:1px solid #ddd;padding:8px;text-align:left}
th{background:#3498db;color:white}
.tag{display:inline-block;padding:2px 8px;border-radius:12px;font-size:12px;margin:2px}
.tag-foundation{background:#e8f5e9;color:#2e7d32}
.tag-optimizer{background:#fff3e0;color:#e65100}
.tag-expert{background:#fce4ec;color:#c62828}
</style>
</head>
<body>
<h1>EclipseLink JPA 三天課程教材 (JAX-RS + MySQL)</h1>
<p>本教材使用 JAX-RS (Jersey) + EclipseLink JPA + MySQL，從基礎到專家級，涵蓋 JPA 核心概念、效能調校與高併發系統設計。</p>

<h2>課程大綱</h2>
<table>
<tr><th>天數</th><th>主題</th><th>核心技術</th></tr>
<tr>
    <td><span class="tag tag-foundation">Day 1</span></td>
    <td><strong>ORM 基礎與實體建構</strong></td>
    <td>@Entity, @OneToMany, @Embedded, CRUD, 交易管理</td>
</tr>
<tr>
    <td><span class="tag tag-optimizer">Day 2</span></td>
    <td><strong>高效查詢與性能調優</strong></td>
    <td>JPQL, JOIN FETCH, Criteria API, N+1 解決, Query Hints</td>
</tr>
<tr>
    <td><span class="tag tag-expert">Day 3</span></td>
    <td><strong>高可用性與系統級優化</strong></td>
    <td>@Version 樂觀鎖定, L2 Cache, Batch, DescriptorCustomizer</td>
</tr>
</table>

<h2>JAX-RS API 端點一覽</h2>
<p>所有 API 基底路徑：<code>/rs/...</code> (JAX-RS / Jersey)</p>

<div class="day">
<h3>Day 1：基礎 CRUD</h3>
<p><span class="endpoint">GET /rs/day1/customers</span> 客戶列表</p>
<p><span class="endpoint">GET /rs/day1/customers/id/{id}</span> 依 ID 查客戶</p>
<p><span class="endpoint">GET /rs/day1/customers/email?email=x</span> 依 Email 查詢</p>
<p><span class="endpoint">POST /rs/day1/customers</span> 新增客戶</p>
<p><span class="endpoint">PUT /rs/day1/customers</span> 更新客戶</p>
<p><span class="endpoint">DELETE /rs/day1/customers/id/{id}</span> 刪除客戶</p>
<p><span class="endpoint">GET /rs/day1/orders</span> 訂單列表</p>
<p><span class="endpoint">GET /rs/day1/orders/id/{id}</span> 依 ID 查訂單</p>
<p><span class="endpoint">GET /rs/day1/orders/customer/{id}</span> 依客戶查訂單</p>
<p><span class="endpoint">POST /rs/day1/orders</span> 新增訂單 (含訂單項目)</p>
<p><span class="endpoint">PUT /rs/day1/orders</span> 更新訂單</p>
<p><span class="endpoint">DELETE /rs/day1/orders/id/{id}</span> 刪除訂單</p>
<p><span class="endpoint">GET /rs/day1/products</span> 產品列表</p>
<p><span class="endpoint">GET /rs/day1/products/id/{id}</span> 依 ID 查產品</p>
<p><span class="endpoint">GET /rs/day1/products/search?name=x</span> 搜尋產品</p>
<p><span class="endpoint">POST /rs/day1/products</span> 新增產品</p>
<p><span class="endpoint">PUT /rs/day1/products</span> 更新產品</p>
<p><span class="endpoint">DELETE /rs/day1/products/id/{id}</span> 刪除產品</p>
</div>

<div class="day">
<h3>Day 2：查詢與調優</h3>
<p><span class="endpoint">GET /rs/day2/items</span> 商品列表 (基礎 JPQL)</p>
<p><span class="endpoint">GET /rs/day2/items/{id}</span> 單筆查詢</p>
<p><span class="endpoint">GET /rs/day2/items/category/{id}</span> 依分類查詢 (JOIN FETCH 解決 N+1)</p>
<p><span class="endpoint">GET /rs/day2/items/search?name=X&minPrice=Y</span> 動態條件查詢 (Criteria API)</p>
<p><span class="endpoint">GET /rs/day2/items/count</span> 統計總數</p>
<p><span class="endpoint">GET /rs/day2/items/hints</span> EclipseLink Query Hints 示範</p>
<p><span class="endpoint">GET /rs/day2/items/expensive?top=5</span> 前 N 貴商品</p>
<p><span class="endpoint">GET /rs/day2/search?name=X&minPrice=Y&tagNames=A,B</span> 進階動態查詢</p>
<p><span class="endpoint">GET /rs/day2/categories</span> 分類列表</p>
<p><span class="endpoint">POST /rs/day2/categories</span> 新增分類</p>
<p><span class="endpoint">POST /rs/day2/items</span> 新增商品</p>
<p><span class="endpoint">PUT /rs/day2/items</span> 更新商品</p>
<p><span class="endpoint">DELETE /rs/day2/items/{id}</span> 刪除商品</p>
</div>

<div class="day">
<h3>Day 3：高可用與並發</h3>
<p><span class="endpoint">POST /rs/day3/bookings/book</span> 訂票 (含 @Version 樂觀鎖定)</p>
<p><span class="endpoint">GET /rs/day3/bookings</span> 所有訂單</p>
<p><span class="endpoint">GET /rs/day3/bookings/{id}</span> 單筆訂單</p>
<p><span class="endpoint">GET /rs/day3/bookings/event/{eventId}</span> 依活動查訂單</p>
<p><span class="endpoint">GET /rs/day3/bookings/concurrency-test</span> 高併發模擬 (5 線程同時搶票)</p>
<p><span class="endpoint">GET /rs/day3/configs</span> 系統設定列表</p>
<p><span class="endpoint">GET /rs/day3/configs/key/{key}</span> 取系統設定 (L2 Cache)</p>
<p><span class="endpoint">GET /rs/day3/configs/cache-demo</span> L2 Cache 效能比較</p>
<p><span class="endpoint">POST /rs/day3/configs</span> 新增設定</p>
<p><span class="endpoint">PUT /rs/day3/configs/{id}</span> 更新設定</p>
<p><span class="endpoint">DELETE /rs/day3/configs/{id}</span> 刪除設定</p>
<p><span class="endpoint">GET /rs/day3/events</span> 活動列表</p>
<p><span class="endpoint">POST /rs/day3/events</span> 新增活動</p>
<p><span class="endpoint">GET /rs/day3/events/{id}</span> 依 ID 查活動</p>
<p><span class="endpoint">DELETE /rs/day3/events/{id}</span> 刪除活動</p>
</div>

<h2>資料庫連線 (MySQL)</h2>
<table>
<tr><th>項目</th><th>值</th></tr>
<tr><td>URL</td><td><code>jdbc:mysql://localhost:3306/eclipselink_course</code></td></tr>
<tr><td>User</td><td>root</td></tr>
<tr><td>Password</td><td>root</td></tr>
<tr><td>DDL</td><td>drop-and-create-tables (每次部署重建)</td></tr>
</table>

<h2>Postman 測試</h2>
<p>匯入專案中的 <code>postman/eclipselink-course.postman_collection.json</code> 即可測試所有 API。</p>

<h2>參考文件</h2>
<p><code>docs/project-architecture.md</code> — 完整架構參考</p>
<p><code>docs/day1-implementation-guide.md</code> — Day 1 實作細節</p>
<p><code>docs/day2-implementation-guide.md</code> — Day 2 實作細節</p>
<p><code>docs/day3-implementation-guide.md</code> — Day 3 實作細節</p>
</body>
</html>
