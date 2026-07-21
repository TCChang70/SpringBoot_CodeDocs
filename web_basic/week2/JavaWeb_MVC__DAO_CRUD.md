# Java Web MVC + MySQL DAO CRUD 學習文件

## 學習目標

1. MVC 架構設計模式
2. EL + JSTL 在 View 層的應用
3. DAO 設計模式與 JDBC 實作技巧
4. CRUD 完整功能串接

---

## 1. MVC 架構概覽

```
┌─────────────────────────────────────────────────┐
│                   MVC Pattern                   │
├──────────┬──────────────┬───────────────────────┤
│   View   │  Controller  │        Model          │
│  (JSP)   │  (Servlet)   │  (JavaBean / DAO)     │
├──────────┼──────────────┼───────────────────────┤
│ 顯示資料  │  接收請求    │  業務邏輯              │
│ 呈現畫面  │  呼叫 Model  │  資料庫操作            │
│ EL / JSTL│  轉發 View   │  封裝資料              │
└──────────┴──────────────┴───────────────────────┘
```

| 層 | 元件 | 職責 |
|---|---|---|
| View | JSP | EL + JSTL 呈現資料 |
| Controller | Servlet | 接收請求、呼叫 DAO、轉發 View |
| Model | JavaBean + DAO | 封裝資料、資料庫 CRUD |

---

## 2. DAO 設計模式

### 2.1 分層架構

```
Controller (Servlet)
    ↓
DAO Interface        ← 抽象依賴，便於抽換實作
    ↓
DAO Impl (JDBC)      ← 實作 CRUD 邏輯
    ↓
MySQL Database
```

### 2.2 為什麼用 DAO Interface？

```java
// 介面：定義 contract，Controller 只依賴介面
public interface UserDao {
    void insert(User user);
    User findById(int id);
    List<User> findAll();
    List<User> searchByName(String name);
    void update(User user);
    void delete(int id);
}

// 實作 1：MySQL
public class UserDaoMySqlImpl implements UserDao { ... }

// 實作 2：Oracle
public class UserDaoOracleImpl implements UserDao { ... }

// 實作 3：Mock (單元測試用)
public class UserDaoMockImpl implements UserDao { ... }
```

### 2.3 DAO 實作 — 進階 CRUD 技巧

```java
package com.example.dao.impl;

import com.example.dao.UserDao;
import com.example.model.User;
import com.example.util.DBUtil;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class UserDaoImpl implements UserDao {

    // === CREATE：回傳自增 ID ===
    @Override
    public void insert(User user) {
        String sql = "INSERT INTO users (name, email, age, created_at) VALUES (?, ?, ?, NOW())";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, user.getName());
            ps.setString(2, user.getEmail());
            ps.setInt(3, user.getAge());
            ps.executeUpdate();

            // 取得自動生成的 ID 並回寫到 user 物件
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    user.setId(rs.getInt(1));
                }
            }
        } catch (SQLException e) {
            // 包裝為 RuntimeException，上層 Controller 統一處理
            throw new RuntimeException("新增使用者失敗：" + e.getMessage(), e);
        }
    }

    // === CREATE：批次新增（Batch Insert）===
    @Override
    public void batchInsert(List<User> users) {
        String sql = "INSERT INTO users (name, email, age) VALUES (?, ?, ?)";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            // 關閉自動提交，批次完成後一次 commit
            conn.setAutoCommit(false);

            for (User user : users) {
                ps.setString(1, user.getName());
                ps.setString(2, user.getEmail());
                ps.setInt(3, user.getAge());
                ps.addBatch();
            }

            int[] results = ps.executeBatch();
            conn.commit();

        } catch (SQLException e) {
            throw new RuntimeException("批次新增失敗", e);
        }
    }

    // === READ：單筆查詢（回傳 Optional 避免 null）===
    @Override
    public Optional<User> findById(int id) {
        String sql = "SELECT * FROM users WHERE id = ? AND deleted = 0";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("查詢使用者失敗", e);
        }
        return Optional.empty();
    }

    // === READ：多條件查詢（動態 SQL）===
    @Override
    public List<User> search(String name, String email, Integer minAge, Integer maxAge) {
        // 使用 StringBuilder 動態組裝 SQL
        StringBuilder sql = new StringBuilder("SELECT * FROM users WHERE 1=1");
        List<Object> params = new ArrayList<>();

        if (name != null && !name.isEmpty()) {
            sql.append(" AND name LIKE ?");
            params.add("%" + name + "%");
        }
        if (email != null && !email.isEmpty()) {
            sql.append(" AND email LIKE ?");
            params.add("%" + email + "%");
        }
        if (minAge != null) {
            sql.append(" AND age >= ?");
            params.add(minAge);
        }
        if (maxAge != null) {
            sql.append(" AND age <= ?");
            params.add(maxAge);
        }
        sql.append(" ORDER BY id DESC");
        sql.append(" LIMIT ? OFFSET ?");  // 分頁
        params.add(pageSize);
        params.add((pageNum - 1) * pageSize);

        List<User> list = new ArrayList<>();
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {

            // 動態設定參數
            for (int i = 0; i < params.size(); i++) {
                Object param = params.get(i);
                if (param instanceof String) {
                    ps.setString(i + 1, (String) param);
                } else if (param instanceof Integer) {
                    ps.setInt(i + 1, (Integer) param);
                }
            }

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("搜尋使用者失敗", e);
        }
        return list;
    }

    // === READ：分頁查詢 ===
    @Override
    public PageResult<User> findPage(int pageNum, int pageSize) {
        // 同時回傳分頁資料與總筆數
        String countSql = "SELECT COUNT(*) FROM users WHERE deleted = 0";
        String dataSql = "SELECT * FROM users WHERE deleted = 0 ORDER BY id DESC LIMIT ? OFFSET ?";

        int total = 0;
        List<User> list = new ArrayList<>();

        try (Connection conn = DBUtil.getConnection()) {
            // 查詢總筆數
            try (PreparedStatement ps = conn.prepareStatement(countSql);
                 ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    total = rs.getInt(1);
                }
            }

            // 查詢分頁資料
            try (PreparedStatement ps = conn.prepareStatement(dataSql)) {
                ps.setInt(1, pageSize);
                ps.setInt(2, (pageNum - 1) * pageSize);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        list.add(mapRow(rs));
                    }
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("分頁查詢失敗", e);
        }

        return new PageResult<>(list, total, pageNum, pageSize);
    }

    // === UPDATE：部分更新（只更新非 null 欄位）===
    @Override
    public void updateSelective(User user) {
        // 只更新有值的欄位，減少不必要的資料庫寫入
        StringBuilder sql = new StringBuilder("UPDATE users SET ");
        List<Object> params = new ArrayList<>();

        if (user.getName() != null) {
            sql.append("name = ?, ");
            params.add(user.getName());
        }
        if (user.getEmail() != null) {
            sql.append("email = ?, ");
            params.add(user.getEmail());
        }
        if (user.getAge() > 0) {
            sql.append("age = ?, ");
            params.add(user.getAge());
        }

        // 去除最後的逗號與空白
        sql.setLength(sql.length() - 2);
        sql.append(" WHERE id = ?");
        params.add(user.getId());

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {

            for (int i = 0; i < params.size(); i++) {
                Object param = params.get(i);
                if (param instanceof String) {
                    ps.setString(i + 1, (String) param);
                } else if (param instanceof Integer) {
                    ps.setInt(i + 1, (Integer) param);
                }
            }
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("更新使用者失敗", e);
        }
    }

    // === DELETE：軟刪除（Soft Delete）===
    @Override
    public void delete(int id) {
        // 不真正刪除 data，設定 deleted flag
        String sql = "UPDATE users SET deleted = 1, deleted_at = NOW() WHERE id = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("刪除使用者失敗", e);
        }
    }

    // === DELETE：物理刪除（真刪除）===
    @Override
    public void hardDelete(int id) {
        String sql = "DELETE FROM users WHERE id = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("永久刪除使用者失敗", e);
        }
    }

    // === 事務操作範例：轉帳（ACID）===
    @Override
    public void transferMoney(int fromId, int toId, int amount) {
        String deductSql = "UPDATE users SET balance = balance - ? WHERE id = ? AND balance >= ?";
        String addSql = "UPDATE users SET balance = balance + ? WHERE id = ?";

        Connection conn = null;
        try {
            conn = DBUtil.getConnection();
            conn.setAutoCommit(false);  // 開始事務

            // 扣款
            try (PreparedStatement ps = conn.prepareStatement(deductSql)) {
                ps.setInt(1, amount);
                ps.setInt(2, fromId);
                ps.setInt(3, amount);
                int rows = ps.executeUpdate();
                if (rows == 0) {
                    throw new RuntimeException("餘額不足");
                }
            }

            // 入帳
            try (PreparedStatement ps = conn.prepareStatement(addSql)) {
                ps.setInt(1, amount);
                ps.setInt(2, toId);
                ps.executeUpdate();
            }

            conn.commit();  // 全部成功才提交
        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback();  // 任一失敗則回滾
                } catch (SQLException ex) {
                    throw new RuntimeException("回滾失敗", ex);
                }
            }
            throw new RuntimeException("轉帳失敗", e);
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException e) {
                    // ignore
                }
            }
        }
    }

    // === ResultSet → JavaBean 對映 ===
    private User mapRow(ResultSet rs) throws SQLException {
        User user = new User();
        user.setId(rs.getInt("id"));
        user.setName(rs.getString("name"));
        user.setEmail(rs.getString("email"));
        user.setAge(rs.getInt("age"));
        user.setCreatedAt(rs.getTimestamp("created_at"));
        return user;
    }
}
```

### 2.4 DAO 與 Controller 的整合：分頁範例

```java
// Servlet 中
int pageNum = 1;
int pageSize = 10;
String pageStr = req.getParameter("page");
if (pageStr != null) {
    pageNum = Integer.parseInt(pageStr);
}
List<User> users = userDao.findPage(pageNum, pageSize);
int total = userDao.count();
req.setAttribute("users", users);
req.setAttribute("total", total);
req.setAttribute("pageNum", pageNum);
req.setAttribute("pageSize", pageSize);
req.getRequestDispatcher("/WEB-INF/views/userList.jsp").forward(req, resp);
```

---

## 3. DAO 設計模式補充

### 3.1 DAO 工廠（簡易版）

```java
package com.example.dao.factory;

import com.example.dao.UserDao;
import com.example.dao.impl.UserDaoImpl;

public class DaoFactory {
    private static UserDao userDao;

    public static UserDao getUserDao() {
        if (userDao == null) {
            userDao = new UserDaoImpl();
        }
        return userDao;
    }
}

// Controller 中使用
UserDao userDao = DaoFactory.getUserDao();
```

### 3.2 Service 層封裝（選用）

```java
package com.example.service;

import com.example.dao.UserDao;
import com.example.dao.factory.DaoFactory;
import com.example.model.User;
import java.util.List;

public class UserService {
    private UserDao userDao = DaoFactory.getUserDao();

    public void registerUser(String name, String email, int age) {
        // 業務邏輯：檢查 Email 是否重複
        List<User> exists = userDao.search(null, email, null, null);
        if (!exists.isEmpty()) {
            throw new RuntimeException("Email 已被註冊");
        }
        userDao.insert(new User(name, email, age));
    }

    public void changeEmail(int userId, String newEmail) {
        User user = userDao.findById(userId)
            .orElseThrow(() -> new RuntimeException("使用者不存在"));
        user.setEmail(newEmail);
        userDao.updateSelective(user);
    }
}
```

---

## 4. Controller — CRUD 路由設計

```java
@WebServlet("/user/*")
public class UserServlet extends HttpServlet {

    private UserDao userDao = new UserDaoImpl();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        String pathInfo = req.getPathInfo();

        switch (pathInfo) {
            case "/list":
            case "/":
                handleList(req, resp);
                break;
            case "/add":
                forwardForm(req, resp, null);
                break;
            case "/edit":
                handleEdit(req, resp);
                break;
            case "/delete":
                handleDelete(req, resp);
                break;
            case "/search":
                handleSearch(req, resp);
                break;
            default:
                resp.sendError(HttpServletResponse.SC_NOT_FOUND);
        }
    }

    private void handleList(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        int pageNum = parseInt(req.getParameter("page"), 1);
        int pageSize = parseInt(req.getParameter("size"), 10);
        List<User> users = userDao.findPage(pageNum, pageSize);
        int total = userDao.count();
        req.setAttribute("users", users);
        req.setAttribute("total", total);
        req.setAttribute("pageNum", pageNum);
        req.setAttribute("pageSize", pageSize);
        req.getRequestDispatcher("/WEB-INF/views/userList.jsp").forward(req, resp);
    }

    private void handleEdit(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        int id = Integer.parseInt(req.getParameter("id"));
        userDao.findById(id).ifPresentOrElse(
            user -> forwardForm(req, resp, user),
            () -> { throw new RuntimeException("使用者不存在，ID：" + id); }
        );
    }

    private void handleDelete(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {

        int id = Integer.parseInt(req.getParameter("id"));
        userDao.delete(id);  // 軟刪除
        resp.sendRedirect(req.getContextPath() + "/user/list");
    }

    private void handleSearch(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        String name = req.getParameter("name");
        String email = req.getParameter("email");
        int pageNum = parseInt(req.getParameter("page"), 1);
        int pageSize = parseInt(req.getParameter("size"), 10);

        List<User> users = userDao.search(name, email, null, null);
        req.setAttribute("users", users);
        req.setAttribute("searchName", name);
        req.setAttribute("searchEmail", email);
        req.getRequestDispatcher("/WEB-INF/views/userList.jsp").forward(req, resp);
    }

    private void forwardForm(HttpServletRequest req, HttpServletResponse resp, User user)
            throws ServletException, IOException {

        req.setAttribute("user", user);
        req.getRequestDispatcher("/WEB-INF/views/userForm.jsp").forward(req, resp);
    }

    private int parseInt(String val, int defaultVal) {
        try { return Integer.parseInt(val); }
        catch (NumberFormatException e) { return defaultVal; }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        req.setCharacterEncoding("UTF-8");

        String name = req.getParameter("name");
        String email = req.getParameter("email");
        int age = Integer.parseInt(req.getParameter("age"));
        String idParam = req.getParameter("id");

        if (idParam == null || idParam.isEmpty()) {
            userDao.insert(new User(name, email, age));
        } else {
            User user = new User(name, email, age);
            user.setId(Integer.parseInt(idParam));
            userDao.updateSelective(user);
        }

        resp.sendRedirect(req.getContextPath() + "/user/list");
    }
}
```

---

## 5. View — JSP CRUD 頁面

### 5.1 使用者列表（含分頁與搜尋）

```jsp
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib uri="jakarta.tags.core" prefix="c" %>
<%@ taglib uri="jakarta.tags.functions" prefix="fn" %>
<!DOCTYPE html>
<html lang="zh-TW">
<head>
    <meta charset="UTF-8">
    <title>使用者管理</title>
</head>
<body>
    <h1>使用者管理</h1>

    <%-- 搜尋列 --%>
    <form action="${pageContext.request.contextPath}/user/search" method="get">
        <input type="text" name="name" placeholder="姓名" value="<c:out value='${searchName}' />" />
        <input type="text" name="email" placeholder="Email" value="<c:out value='${searchEmail}' />" />
        <button type="submit">搜尋</button>
    </form>

    <a href="${pageContext.request.contextPath}/user/add">新增使用者</a>

    <table border="1">
        <thead>
            <tr>
                <th>ID</th>
                <th>姓名</th>
                <th>Email</th>
                <th>年齡</th>
                <th>建立時間</th>
                <th>操作</th>
            </tr>
        </thead>
        <tbody>
            <c:forEach var="user" items="${users}" varStatus="status">
                <tr>
                    <td>${user.id}</td>
                    <td><c:out value="${user.name}" /></td>
                    <td><c:out value="${user.email}" /></td>
                    <td>${user.age}</td>
                    <td>${user.createdAt}</td>
                    <td>
                        <c:url var="editUrl" value="/user/edit">
                            <c:param name="id" value="${user.id}" />
                        </c:url>
                        <c:url var="deleteUrl" value="/user/delete">
                            <c:param name="id" value="${user.id}" />
                        </c:url>
                        <a href="${editUrl}">編輯</a>
                        <a href="${deleteUrl}" onclick="return confirm('確定刪除？')">刪除</a>
                    </td>
                </tr>
            </c:forEach>

            <c:if test="${empty users}">
                <tr><td colspan="6">無資料</td></tr>
            </c:if>
        </tbody>
    </table>

    <%-- 分頁 --%>
    <c:if test="${total > pageSize}">
        <div>
            <c:if test="${pageNum > 1}">
                <a href="?page=${pageNum - 1}">上一頁</a>
            </c:if>
            <c:forEach var="i" begin="1" end="${Math.ceil(total / pageSize)}">
                <c:choose>
                    <c:when test="${i == pageNum}"><strong>${i}</strong></c:when>
                    <c:otherwise><a href="?page=${i}">${i}</a></c:otherwise>
                </c:choose>
            </c:forEach>
            <c:if test="${pageNum * pageSize < total}">
                <a href="?page=${pageNum + 1}">下一頁</a>
            </c:if>
            <span>共 ${total} 筆，第 ${pageNum}/${Math.ceil(total / pageSize)} 頁</span>
        </div>
    </c:if>
</body>
</html>
```

---

## 6. 錯誤處理與驗證

### 6.1 Controller 層驗證

```java
private void validateUserInput(String name, String email, String ageStr, Map<String, String> errors) {
    if (name == null || name.trim().isEmpty()) {
        errors.put("name", "姓名不得為空");
    } else if (name.length() > 50) {
        errors.put("name", "姓名不得超過 50 字");
    }

    if (email == null || !email.matches("^[\\w.-]+@[\\w.-]+\\.\\w{2,}$")) {
        errors.put("email", "Email 格式錯誤");
    }

    try {
        int age = Integer.parseInt(ageStr);
        if (age < 0 || age > 150) {
            errors.put("age", "年齡須介於 0~150");
        }
    } catch (NumberFormatException e) {
        errors.put("age", "年齡須為數字");
    }
}
```

### 6.2 錯誤頁面顯示

```jsp
<c:if test="${not empty errors}">
    <ul style="color:red;">
        <c:forEach var="err" items="${errors}">
            <li>${err.value}</li>
        </c:forEach>
    </ul>
</c:if>
```

---

## 7. 資料庫端 CRUD 對照

| CRUD | SQL | DAO 方法 | HTTP |
|---|---|---|---|
| Create | INSERT INTO ... VALUES (?) | `insert(user)` | POST /user |
| Read (單筆) | SELECT * FROM ... WHERE id = ? | `findById(id)` | — |
| Read (多筆) | SELECT * FROM ... LIMIT ? OFFSET ? | `findPage(page, size)` | GET /user/list |
| Read (搜尋) | SELECT * FROM ... WHERE name LIKE ? | `search(name, email, ...)` | GET /user/search |
| Update | UPDATE ... SET ... WHERE id = ? | `updateSelective(user)` | POST /user (含 id) |
| Delete (軟) | UPDATE ... SET deleted=1 WHERE id=? | `delete(id)` | GET /user/delete |
| Delete (硬) | DELETE FROM ... WHERE id = ? | `hardDelete(id)` | — |

---

## 8. DAO 設計原則總結

| 原則 | 說明 |
|---|---|
| Interface first | 先定義 DAO 介面，再實作 |
| 回傳 `Optional` | 代替可能為 null 的查詢結果 |
| 軟刪除優先 | 保留資料可追溯，必要時才物理刪除 |
| 批次操作 | 大量資料使用 `addBatch() + commit()` |
| 動態 SQL | 多條件查詢用 StringBuilder 組合 |
| PreparedStatement | 防止 SQL Injection |
| try-with-resources | 確保資源釋放 |
| 事務管理 | ACID 操作需手動控制 commit/rollback |
| Controller 瘦身 | DAO 拋出 RuntimeException，由統一機制處理 |
