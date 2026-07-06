# Spring Boot 實作練習題庫 — Day 1 ~ 10

## 練習題索引

| Day | 主題 | 練習難度 | 檔案 |
|-----|------|----------|------|
| 01 | Maven / Spring IoC / DI | ⭐⭐ | [day01-exercises.md](./day01-exercises.md) |
| 02 | Bean Scope / Lifecycle / Config | ⭐⭐⭐ | [day02-exercises.md](./day02-exercises.md) |
| 03 | Spring MVC / REST API | ⭐⭐ | [day03-exercises.md](./day03-exercises.md) |
| 04 | AutoConfig / Properties / Profile | ⭐⭐ | [day04-exercises.md](./day04-exercises.md) |
| 05 | Employee CRUD 綜合實作 | ⭐⭐⭐ | [day05-exercises.md](./day05-exercises.md) |
| 06 | JpaRepository / 命名查詢 / @Query | ⭐⭐⭐ | [day06-exercises.md](./day06-exercises.md) |
| 07 | 關聯映射 / JPQL / N+1 | ⭐⭐⭐⭐ | [day07-exercises.md](./day07-exercises.md) |
| 08 | @Transactional / 傳播 / 隔離 | ⭐⭐⭐⭐ | [day08-exercises.md](./day08-exercises.md) |
| 09 | Flyway 版本管理 / 多資料源 | ⭐⭐⭐⭐ | [day09-exercises.md](./day09-exercises.md) |
| 10 | 訂單系統整合實作 | ⭐⭐⭐⭐⭐ | [day10-exercises.md](./day10-exercises.md) |

---

## 如何使用這份練習題庫

1. **先閱讀當天教材**（`springboot-dayXX-xxx.md`）再做練習
2. **嘗試自己完成題目**，不要直接看解答
3. 遇到卡關時先看**提示（Hint）**，再看解答
4. 完成後對照解答，注意**解析說明**裡標示的關鍵觀念
5. 每天附有**挑戰題（Challenge）**，適合想更進一步的學習者

---

## 建議學習順序

```
Day 01 → Day 02 → Day 03 → Day 04
              ↓
         Day 05 (CRUD 綜合)
              ↓
    Day 06 → Day 07 → Day 08
              ↓
         Day 09 → Day 10 (系統整合)
```

---

## 環境準備

- **JDK 17+**（建議 JDK 21）
- **Maven 3.8+** 或 IntelliJ IDEA 內建 Maven
- **MySQL 8.0+**（Day 5 以後需要）
- **Postman** 或 `curl`（測試 REST API）
- **Spring Initializr**：https://start.spring.io

### 快速建立 MySQL 測試環境（Docker）

```bash
docker run -d \
  --name springboot-mysql \
  -e MYSQL_ROOT_PASSWORD=1234 \
  -e MYSQL_DATABASE=employee_db \
  -p 3306:3306 \
  mysql:8.0
```
