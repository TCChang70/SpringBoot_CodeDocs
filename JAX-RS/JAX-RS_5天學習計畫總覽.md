# JAX-RS 系統程式設計與開發 — 5 天學習計畫總覽

> **適合對象**：具備 Java 基礎、了解 HTTP 協定與 MVC 概念的開發者  
> **預備知識**：Java SE 8+、Maven、基本 Servlet/JSP 概念、SQL 基礎  
> **學習目標**：能獨立設計、開發、測試、部署一套完整的 RESTful API 系統

---

## 課程架構一覽

| 天數 | 主題 | 核心技術 | 評估方式 |
|------|------|----------|----------|
| Day 1 | JAX-RS 入門與環境建置 | Maven、Jersey、基礎標注 | 測驗 10 題 + 實作 3 題 |
| Day 2 | HTTP 方法、資源設計與 JSON | CRUD、@PathParam、JSON | 測驗 10 題 + 實作 3 題 |
| Day 3 | 進階功能與異常處理 | Filter、ExceptionMapper | 測驗 10 題 + 實作 3 題 |
| Day 4 | 資料庫整合與 JPA | JPA、Repository 模式 | 測驗 10 題 + 實作 3 題 |
| Day 5 | 安全性、測試與最佳實踐 | JWT、REST Assured | 測驗 10 題 + 綜合實作 1 題 |

---

## 學習路徑圖

```
REST 概念基礎
    │
    ▼
Day 1：環境建置 + 第一支 API
    │
    ▼
Day 2：設計完整 CRUD API + JSON 輸出
    │
    ▼
Day 3：Filter、Interceptor、異常處理
    │
    ▼
Day 4：連接 MySQL/JPA 做資料持久化
    │
    ▼
Day 5：JWT 安全防護 + 測試 + 部署
```

---

## 技術堆疊

| 類別 | 技術 |
|------|------|
| Java EE API | JAX-RS 2.x (javax.ws.rs) |
| 實作框架 | Jersey 2.x (GlassFish RI) |
| 建置工具 | Maven 3.x |
| 容器 | Apache Tomcat 9 / GlassFish 5 |
| 資料庫 | MySQL 8.x |
| ORM | JPA 2.x + Hibernate |
| JSON | Jackson 2.x |
| 安全 | JWT (jjwt 0.11.x) |
| 測試 | JUnit 5 + REST Assured |

---

## 學習文件索引

1. [Day1\_JAX-RS入門與環境設置.md](./Day1_JAX-RS入門與環境設置.md)
2. [Day2\_HTTP方法與資源設計.md](./Day2_HTTP方法與資源設計.md)
3. [Day3\_進階功能與異常處理.md](./Day3_進階功能與異常處理.md)
4. [Day4\_資料庫整合與JPA.md](./Day4_資料庫整合與JPA.md)
5. [Day5\_安全性測試與最佳實踐.md](./Day5_安全性測試與最佳實踐.md)

---

## 評分標準

| 項目 | 比重 |
|------|------|
| 每日測驗（共 5 次，各 10 題） | 40% |
| 每日實作題（共 5 次） | 50% |
| 自主延伸挑戰 | 10% |

> 每日測驗答對 7 題以上視為通過；實作題需能成功呼叫 API 且回傳正確 HTTP 狀態碼。

---

## 開發環境快速確認清單

```bash
# 確認 JDK 版本（建議 JDK 11+）
java -version

# 確認 Maven
mvn -version

# 確認 MySQL 服務
mysql --version

# Tomcat 啟動後測試
curl http://localhost:8080/
```

---

*文件版本：1.0 | 建立日期：2026-03-14*
