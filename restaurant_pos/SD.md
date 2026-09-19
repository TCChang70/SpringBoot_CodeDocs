# 餐廳 POS 系統 — 系統設計書（SD）

| 項目 | 內容 |
| --- | --- |
| 文件版本 | v1.0 |
| 製表日期 | 2026-09-19 |
| 系統名稱 | Restaurant POS（點餐暨結帳管理系統） |
| 對應文件 | 《需求書.md》v1.0、《SA.md》v1.0 |

---

## 1. 設計目的

依《需求書》與《SA》進行系統設計，涵蓋：系統架構、程式架構、資料庫設計（DDL）、API 介面設計、畫面（UI）設計、安全設計、交易控管、部署架構與測試設計，供開發人員據以實作。

## 2. 系統架構設計

### 2.1 整體架構

```
┌────────────────────────────────────────────────┐
│  表現層（Presentation）                          │
│  登入 / 桌位開單 / 點餐 / 結帳 / 每日結帳 畫面     │
└───────────────┬────────────────────────────────┘
                │ HTTP (REST/JSON)
┌───────────────▼────────────────────────────────┐
│  應用層（Application / API）                     │
│  AuthController  MenuController  OrderController │
│  PaymentController ClosingController ...         │
└───────────────┬────────────────────────────────┘
                │ 交易管理（@Transactional）
┌───────────────▼────────────────────────────────┐
│  領域/服務層（Service / Domain）                 │
│  OrderService  PaymentService  ClosingService    │
│  業務規則 BR-01~07                               │
└───────────────┬────────────────────────────────┘
                │ ORM / JDBC
┌───────────────▼────────────────────────────────┐
│  資料存取層（Repository）                        │
│  EmployeeRepository TableRepository ...          │
└───────────────┬────────────────────────────────┘
                │ JDBC (utf8mb4, UTC+8)
┌───────────────▼────────────────────────────────┐
│  資料庫：MySQL 8.0 / restaurant_pos（7 張表）     │
└────────────────────────────────────────────────┘
```

### 2.2 建議技術棧

| 層次 | 建議技術 | 說明 |
| --- | --- | --- |
| 前端 | HTML/CSS/JavaScript（或 React/Vue/Vite） | 與 DB 逆向內容相互獨立 |
| 後端 | Java 17 + Spring Boot 3（MVC + JPA） | 或語言比照既有專案 |
| 資料庫 | MySQL 8.0（InnoDB） | 已確認 |
| 認證 | Session / JWT | 依單機 or 跨主機選擇 |
| 密碼 | Spring Security BCrypt | NFR-04 |

## 3. 程式架構設計（Package / Class 設計）

### 3.1 後端類別設計（以 Spring Boot 慣例為例）

```
com.restaurant.pos
├── config          SecurityConfig, WebConfig
├── controller      AuthController, EmployeeController, TableController,
│                   MenuController, OrderController, PaymentController,
│                   ClosingController, ReportController
├── service         AuthService, EmployeeService, TableService, MenuService,
│                   OrderService, PaymentService, ClosingService
├── repository      EmployeeRepository, RestaurantTableRepository,
│                   MenuItemRepository, OrderRepository, OrderItemRepository,
│                   PaymentRepository, DailyClosingRepository
├── entity          Employee, RestaurantTable, MenuItem, Order, OrderItem,
│                   Payment, DailyClosing
├── dto             LoginRequest, LoginResponse, OrderCreateRequest,
│                   OrderItemRequest, PaymentRequest, ClosingResult ...
└── exception       BusinessException, NotFoundException, DuplicateException
```

### 3.2 實體與資料表對應

| Entity | 對應資料表 | 主要關聯 |
| --- | --- | --- |
| Employee | employee | 1:N Order、Payment、DailyClosing |
| RestaurantTable | restaurant_table | 1:N Order |
| MenuItem | menu_item | 1:N OrderItem |
| Order | orders | 1:N OrderItem；1:1 Payment |
| OrderItem | order_item | N:1 Order、MenuItem |
| Payment | payment | 1:1 Order；N:1 Employee |
| DailyClosing | daily_closing | N:1 Employee |

## 4. 資料庫設計

### 4.1 邏輯模型

參照《需求書》§5，以下為對應之實作 DDL（與逆向所得資料庫一致）。

### 4.2 建表 DDL（MySQL 8.0）

```sql
CREATE DATABASE IF NOT EXISTS restaurant_pos
  DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE restaurant_pos;

CREATE TABLE employee (
  id         BIGINT       NOT NULL AUTO_INCREMENT,
  username   VARCHAR(50)  NOT NULL,
  password   VARCHAR(255) NOT NULL,
  name       VARCHAR(50)  NOT NULL,
  role       VARCHAR(20)  NOT NULL,
  active     TINYINT(1)   NOT NULL DEFAULT 1,
  created_at DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP
                          ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  UNIQUE KEY username (username)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE restaurant_table (
  id           BIGINT      NOT NULL AUTO_INCREMENT,
  table_number INT         NOT NULL,
  capacity     INT         NOT NULL,
  status       VARCHAR(20) NOT NULL DEFAULT 'AVAILABLE',
  created_at   DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at   DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP
                           ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  UNIQUE KEY table_number (table_number)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE menu_item (
  id          BIGINT        NOT NULL AUTO_INCREMENT,
  name        VARCHAR(100)  NOT NULL,
  category    VARCHAR(30)   NOT NULL,
  price       DECIMAL(10,2) NOT NULL,
  available   TINYINT(1)    NOT NULL DEFAULT 1,
  description VARCHAR(255)  DEFAULT NULL,
  created_at  DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at  DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP
                            ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE orders (
  id           BIGINT        NOT NULL AUTO_INCREMENT,
  table_id     BIGINT        NOT NULL,
  employee_id  BIGINT        NOT NULL,
  status       VARCHAR(20)   NOT NULL DEFAULT 'OPEN',
  total_amount DECIMAL(10,2) NOT NULL DEFAULT 0.00,
  created_at   DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at   DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP
                             ON UPDATE CURRENT_TIMESTAMP,
  paid_at      DATETIME      DEFAULT NULL,
  PRIMARY KEY (id),
  KEY fk_orders_table (table_id),
  KEY fk_orders_employee (employee_id),
  CONSTRAINT fk_orders_table    FOREIGN KEY (table_id)
    REFERENCES restaurant_table (id),
  CONSTRAINT fk_orders_employee FOREIGN KEY (employee_id)
    REFERENCES employee (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE order_item (
  id           BIGINT        NOT NULL AUTO_INCREMENT,
  order_id     BIGINT        NOT NULL,
  menu_item_id BIGINT        NOT NULL,
  quantity     INT           NOT NULL,
  unit_price   DECIMAL(10,2) NOT NULL,
  subtotal     DECIMAL(10,2) NOT NULL,
  sugar_level  VARCHAR(20)   DEFAULT NULL,
  ice_level    VARCHAR(20)   DEFAULT NULL,
  note         VARCHAR(255)  DEFAULT NULL,
  PRIMARY KEY (id),
  KEY fk_order_item_order (order_id),
  KEY fk_order_item_menu (menu_item_id),
  CONSTRAINT fk_order_item_order FOREIGN KEY (order_id)
    REFERENCES orders (id),
  CONSTRAINT fk_order_item_menu  FOREIGN KEY (menu_item_id)
    REFERENCES menu_item (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE payment (
  id             BIGINT        NOT NULL AUTO_INCREMENT,
  order_id       BIGINT        NOT NULL,
  payment_method VARCHAR(30)   NOT NULL,
  amount         DECIMAL(10,2) NOT NULL,
  paid_at        DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
  employee_id    BIGINT        NOT NULL,
  PRIMARY KEY (id),
  UNIQUE KEY order_id (order_id),
  KEY fk_payment_employee (employee_id),
  CONSTRAINT fk_payment_order    FOREIGN KEY (order_id)
    REFERENCES orders (id),
  CONSTRAINT fk_payment_employee FOREIGN KEY (employee_id)
    REFERENCES employee (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE daily_closing (
  id            BIGINT        NOT NULL AUTO_INCREMENT,
  closing_date  DATE          NOT NULL,
  total_orders  INT           NOT NULL DEFAULT 0,
  total_revenue DECIMAL(12,2) NOT NULL DEFAULT 0.00,
  cash_amount   DECIMAL(12,2) NOT NULL DEFAULT 0.00,
  card_amount   DECIMAL(12,2) NOT NULL DEFAULT 0.00,
  other_amount  DECIMAL(12,2) NOT NULL DEFAULT 0.00,
  employee_id   BIGINT        NOT NULL,
  closed_at     DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  UNIQUE KEY closing_date (closing_date),
  KEY fk_daily_closing_employee (employee_id),
  CONSTRAINT fk_daily_closing_employee FOREIGN KEY (employee_id)
    REFERENCES employee (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
```

### 4.3 交易（Transaction）與一致性設計

| 情境 | 交易邊界 | 保證規則 |
| --- | --- | --- |
| 點餐/加點 | 新增 order_item + 更新 orders.total_amount | 單一交易；BR-01、BR-02 |
| 結帳收款 | 新增 payment + 更新 orders(PAID, paid_at) | 單一交易；BR-03、BR-04 |
| 取消訂單 | 更新 orders(status=CANCELLED) | 須檢查非 PAID |
| 每日結帳 | 讀取統計 + 寫入 daily_closing | 唯一鍵 closing_date 防止重複結帳 |

### 4.4 關鍵報表 SQL（每日結帳）

```sql
-- 計算指定日期之結帳統計（orders.status = 'PAID' 且 paid_at 屬於該日）
SELECT DATE(paid_at)                     AS closing_date,
       COUNT(DISTINCT o.id)              AS total_orders,
       SUM(o.total_amount)               AS total_revenue,
       COALESCE(SUM(CASE p.payment_method WHEN 'CASH'         THEN p.amount END), 0) AS cash_amount,
       COALESCE(SUM(CASE p.payment_method WHEN 'CREDIT_CARD'  THEN p.amount END), 0) AS card_amount,
       COALESCE(SUM(CASE p.payment_method WHEN 'LINE_PAY'     THEN p.amount END), 0) AS other_amount
FROM orders o
JOIN payment p ON p.order_id = o.id
WHERE o.status = 'PAID'
  AND DATE(o.paid_at) = ?
GROUP BY DATE(o.paid_at);
```

## 5. API 設計（REST）

通用規範：
- Base Path：`/api`
- 回傳格式：`{ "code": 0, "message": "success", "data": {...} }`
- 需登入者於 Header 帶認證資訊（Authorization: Bearer <token> 或 Session Cookie）。
- 錯誤碼：`0` 成功；`4001` 參數錯誤；`401` 未授權；`4041` 資料不存在；`4091` 唯一鍵/重複；`500` 系統錯誤。

### 5.1 認證 Auth

| Method | Path | 說明 | 輸入 | 輸出 |
| --- | --- | --- | --- | --- |
| POST | /api/auth/login | 登入 | username, password | token, role, name |
| POST | /api/auth/logout | 登出 | - | - |
| GET | /api/auth/me | 取得目前登入者 | - | id, username, name, role |

### 5.2 員工 Management

| Method | Path | 說明 | 權限 |
| --- | --- | --- | --- |
| GET | /api/employees | 員工列表 | ADMIN |
| POST | /api/employees | 新增員工 | ADMIN |
| PATCH | /api/employees/{id}/active | 停用/啟用 | ADMIN |

### 5.3 桌位 Management

| Method | Path | 說明 | 權限 |
| --- | --- | --- | --- |
| GET | /api/tables | 桌位列表（含狀態） | 全部 |
| POST | /api/tables | 新增桌位 | ADMIN |
| PATCH | /api/tables/{id}/status | 變更桌位狀態 | ADMIN |

### 5.4 菜單 Management

| Method | Path | 說明 | 權限 |
| --- | --- | --- | --- |
| GET | /api/menu-items | 菜單列表（可依 category 篩選） | 全部 |
| POST | /api/menu-items | 新增菜單 | ADMIN |
| PUT | /api/menu-items/{id} | 修改菜單 | ADMIN |
| PATCH | /api/menu-items/{id}/available | 上架/停售 | ADMIN |

### 5.5 點餐 Order

| Method | Path | 說明 | 權限 |
| --- | --- | --- | --- |
| POST | /api/orders | 開立訂單 | STAFF/ADMIN |
| GET | /api/orders/{id} | 訂單詳情（含明細） | STAFF/ADMIN |
| POST | /api/orders/{id}/items | 點餐/加點 | STAFF/ADMIN |
| POST | /api/orders/{id}/cancel | 取消訂單 | STAFF/ADMIN |

**POST /api/orders （開單）請求範例**
```json
{ "tableId": 1, "employeeId": 2 }
```

**POST /api/orders/{id}/items （加點）請求範例**
```json
{
  "items": [
    { "menuItemId": 5, "quantity": 2, "sugarLevel": "半糖", "iceLevel": "微冰", "note": "" }
  ]
}
```

### 5.6 結帳 Payment

| Method | Path | 說明 | 權限 |
| --- | --- | --- | --- |
| POST | /api/orders/{id}/payment | 結帳收款 | STAFF/ADMIN |

**請求範例**
```json
{ "paymentMethod": "CASH", "amount": 90.00, "employeeId": 2 }
```

### 5.7 每日結帳 Closing

| Method | Path | 說明 | 權限 |
| --- | --- | --- | --- |
| GET | /api/closings?date=yyyy-MM-dd | 查詢結帳紀錄 | ADMIN |
| POST | /api/closings | 執行每日結帳 | ADMIN |

**POST /api/closings 請求範例**
```json
{ "closingDate": "2026-09-19", "employeeId": 1 }
```

### 5.8 報表 Report

| Method | Path | 說明 | 權限 |
| --- | --- | --- | --- |
| GET | /api/reports/transactions?from=&to= | 交易明細查詢 | ADMIN |
| GET | /api/reports/daily-summary | 每日營收彙總 | ADMIN |

## 6. 使用者介面（UI）設計

### 6.1 畫面清單

| 畫面編號 | 畫面名稱 | 角色 |
| --- | --- | --- |
| SC-01 | 登入畫面 | 全部 |
| SC-02 | 主選單（依角色顯示功能） | 全部 |
| SC-03 | 員工管理 | ADMIN |
| SC-04 | 桌位管理 | ADMIN |
| SC-05 | 菜單管理 | ADMIN |
| SC-06 | 桌位開單 | STAFF/ADMIN |
| SC-07 | 點餐/加點 | STAFF/ADMIN |
| SC-08 | 結帳收款 | STAFF/ADMIN |
| SC-09 | 每日結帳 | ADMIN |
| SC-10 | 交易查詢/報表 | ADMIN |

### 6.2 SC-01 登入畫面（示意）

```
┌────────────────────────────┐
│      Restaurant POS         │
│  ────────────────────────   │
│  帳號 [          ]          │
│  密碼 [          ]          │
│  [     登  入     ]         │
│  ────────────────────────   │
│  錯誤訊息顯示區              │
└────────────────────────────┘
```

### 6.3 SC-06/07 開單與點餐畫面（示意）

```
┌──────────────────────────────────────────────────────┐
│ 桌位: 01  |  訂單 #123 (OPEN)  |  開單: staff01       │
├───────────────────────┬──────────────────────────────┤
│   菜單 [FOOD][DRINK]  │  本單明細                     │
│ ┌───────────────────┐ │  名稱      數量  單價  小計   │
│ │ 紅燒牛肉麵  $65   │ │  紅茶(半糖/微冰) 2 40 80   │
│ │ 炒飯      $50   │ │  滷肉飯         1 35 35   │
│ │ 紅茶      $40   │ │                            │
│ │ 綠茶      $40   │ │                            │
│ └───────────────────┘ │  ─────────────────────    │
│  數量[2]  糖度[半糖]    │  合計  $115               │
│  冰塊[微冰] 備註[ ]     │ [加點]  [取消]  [結帳]    │
│  [加入本單]             │                            │
└───────────────────────┴──────────────────────────────┘
```

### 6.4 SC-08 結帳收款畫面（示意）

```
┌──────────────────────────┐
│  訂單 #123  合計 $115     │
│  ──────────────────────  │
│  付款方式                  │
│  (*)現金   ( )信用卡  ( )Line Pay │
│  收款金額 [115]            │
│  [   確認收款   ]          │
│  成功後：訂單狀態→PAID     │
└──────────────────────────┘
```

### 6.5 SC-09 每日結帳畫面（示意）

```
┌────────────────────────────────┐
│  結帳日期 [2026-09-19] (僅能結一次)│
│  ───────────────────────────── │
│  訂單數        7                │
│  總營收        $1,520           │
│  現金          $ 900            │
│  信用卡        $ 420            │
│  其他(Line Pay)$ 200            │
│  執行員工      admin             │
│  [  執行結帳  ]  [ 列印報表 ]    │
└────────────────────────────────┘
```

## 7. 安全設計

| 項目 | 設計 |
| --- | --- |
| 密碼儲存 | BCrypt 雜湊（欄位長度 255） |
| 認證機制 | 登入後簽發 Token（JWT）或 Session，`/api` 需帶認證 |
| 授權控制 | Controller 依 `role` 進行權限檢查（ADMIN 專屬 API 設角色白名單） |
| 停用處理 | `active=0` 之使用者憑證立即失效 |
| SQL 注入 | 全數以 Prepared Statement / JPA 參數綁定 |
| 資料脫敏 | API 回傳不包含密碼欄位 |
| 時區 | JVM、JDBC 連接皆設定 `Asia/Taipei` |

## 8. 部署架構

```
┌── 單機部署（小型門市）─────────────────────────┐
│  Web/API Server（Spring Boot jar）─┬─ MySQL 8.0 │
│  Browser（前端 UI）────────────────┘            │
└───────────────────────────────────────────────┘
```

- 連線字串需含 `characterEncoding=utf8mb4`、`serverTimezone=Asia/Taipei`。
- 備份：每日 mysqldump 排程（含 `restaurant_pos` 全部資料表）。
- 日誌：交易（登入、開單、取消、收款、結帳）記錄操作者與時間，供稽核。

## 9. 測試設計

### 9.1 單元/整合測試（對應業務規則）

| 測試編號 | 測試項目 | 輸入 | 期望結果 |
| --- | --- | --- | --- |
| T-01 | BR-01 小計計算 | unit=40, qty=2 | subtotal=80.00 |
| T-02 | BR-02 訂單總額 | 明細 35 + 80 | total_amount=115.00 |
| T-03 | BR-03 一單一付款 | 已付款訂單再收款 | 拒絕（4091） |
| T-04 | BR-04 付款狀態流 | 收款成功 | orders.status=PAID 且 paid_at 非空 |
| T-05 | FR-02 桌號唯一 | 重複桌號 | 拒絕（4091） |
| T-06 | FR-01 帳號唯一/密碼雜湊 | 重複 username | 拒絕；庫存為 BCrypt 非明碼 |
| T-07 | FR-03 停售下單 | available=0 之項目 | 拒絕下單 |
| T-08 | FR-04 已付款加點 | 對 PAID 訂單加點 | 拒絕 |
| T-09 | FR-06 重複結帳 | 同日二次結帳 | 拒絕（唯一鍵） |
| T-10 | NFR-05 時區 | 結帳含時間 | 顯示 UTC+8 |

### 9.2 驗收對應表

| 測試編號 | 對應需求/規則 |
| --- | --- |
| T-01、T-02 | FR-04（BR-01、BR-02） |
| T-03、T-04 | FR-05（BR-03、BR-04） |
| T-05 | FR-02 |
| T-06 | FR-01、NFR-04 |
| T-07 | FR-03 |
| T-08 | FR-04 |
| T-09 | FR-06 |
| T-10 | NFR-05 |

## 10. 設計假設與注意事項

1. 目前資料庫 `orders.status` 未見 `OCCUPIED` 等桌位狀態，桌位與訂單狀態流於實作時可擴充，但不得破壞既有唯一鍵/外鍵約束。
2. `payment_method` 實作時建議以 Enum 或查表驗證，確保日後統計 `cash_amount / card_amount / other_amount` 正確對應。
3. 取消訂單僅限 `OPEN` 狀態；已付款訂單之退款屬後續需求，本次未涵蓋。
4. 每日結帳以 `paid_at` 的日期為歸屬日計算。