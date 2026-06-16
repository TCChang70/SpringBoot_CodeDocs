# MiniClinic 迷你診所系統

一個以 Spring Boot 實作的輕量診所管理系統，支援醫師登入、病患管理、掛號與看診流程。

## 線上 Demo

> https://miniclinic-你的帳號.onrender.com

## 技術棧

- **後端**：Spring Boot 4.x、Spring Data JPA、Thymeleaf
- **本機資料庫**：SQLite
- **雲端資料庫**：PostgreSQL（Render）
- **容器化**：Docker（multi-stage build）
- **部署平台**：Render

## 本機執行步驟

1. 確保已安裝 Java 17+ 與 Maven 3.9+
2. Clone 專案：
   ```bash
   git clone https://github.com/你的帳號/miniclinic.git
   cd miniclinic