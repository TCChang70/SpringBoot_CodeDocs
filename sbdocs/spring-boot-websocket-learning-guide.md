# Spring Boot WebSocket 聊天室 — Server / Client 端完整教學

---

## 一、專案簡介與技術棧

### 主題

使用 Spring Boot 建立一個即時多人聊天室，包含 **Server 端**（Java WebSocket Handler）與 **Client 端**（HTML + JavaScript）。

### 目標

完成本教學後，你將學會：

1. 理解 WebSocket 協定與 HTTP 的差異
2. 使用 Spring WebSocket API 建立 Server 端
3. 使用 JavaScript 原生 WebSocket API 建立 Client 端
4. 管理多人連線的 Session 生命週期
5. 處理執行緒安全的連線列表

### 技術清單

| 技術 | 版本 | 用途 |
|------|------|------|
| Java | 17 | 程式語言 |
| Spring Boot | 4.1.1 | 應用框架 |
| Spring WebSocket | (隨 Spring Boot) | Server 端 WebSocket 支援 |
| Tomcat（內嵌） | (隨 Spring Boot) | WebSocket 容器 |
| Maven | 3.x | 建置工具 |
| JavaScript | ES6 | Client 端 WebSocket API |

### 專案架構圖

```
┌─────────────────────────────────────────────────┐
│                  Browser (Client)               │
│                                                 │
│   chatclient.html                               │
│     └── client.js                               │
│           ├── new WebSocket("ws://...")          │
│           ├── ws.onopen    → 連線成功            │
│           ├── ws.onmessage → 接收訊息            │
│           ├── ws.onerror   → 連線錯誤            │
│           └── ws.send()    → 發送訊息            │
└──────────────────────┬──────────────────────────┘
                       │ WebSocket (ws://)
                       ▼
┌─────────────────────────────────────────────────┐
│              Spring Boot Server                 │
│                                                 │
│   WebSocketConfig (@EnableWebSocket)            │
│     └── 註冊 /ws/chat 路由                      │
│                                                 │
│   ChatWebSocketHandler (TextWebSocketHandler)   │
│     ├── afterConnectionEstablished() → 加入列表  │
│     ├── handleTextMessage()          → 廣播訊息  │
│     └── afterConnectionClosed()      → 移除列表  │
│                                                 │
│   CopyOnWriteArrayList<WebSocketSession>        │
│     └── 管理所有已連線的 Session                 │
└─────────────────────────────────────────────────┘
```

---

## 二、環境準備

### 必要軟體

| 軟體 | 版本需求 | 安裝確認指令 |
|------|----------|-------------|
| JDK | 17+ | `java -version` |
| Maven | 3.6+ | `mvn -v` |
| IDE | Spring Tool Suite / IntelliJ IDEA | — |

### IDE 設定

- **IntelliJ IDEA**：安裝 Spring Boot 插件（若使用 Community 版）
- **Spring Tool Suite**：內建支援，直接匯入 Maven 專案

> ⚠️ 本專案不需要資料庫，所有資料都在記憶體中處理（Session 列表），因此不需要安裝 MySQL 或其他 DB。

---

## 三、建立專案骨架

### 3.1 建立 Maven 專案

使用 Spring Initializr（https://start.spring.io）建立專案：

- **Project**: Maven
- **Language**: Java
- **Spring Boot**: 4.1.1
- **Group**: `com.example`
- **Artifact**: `sbchat0826`
- **Package name**: `com.example.demo`
- **Java**: 17

勾選依賴：
- Spring Web（對應 `spring-boot-starter-webmvc`）

### 3.2 pom.xml 依賴說明

建立完成後，手動加入 WebSocket 依賴。以下逐一說明每個依賴的用途：

```xml
<properties>
    <java.version>17</java.version>
</properties>
<dependencies>
    <!-- Spring MVC + 內嵌 Tomcat -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-webmvc</artifactId>
    </dependency>

    <!-- Spring WebSocket 支援（手動加入） -->
    <dependency>
        <groupId>org.springframework</groupId>
        <artifactId>spring-websocket</artifactId>
    </dependency>

    <!-- 開發工具：修改程式碼後自動重啟 -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-devtools</artifactId>
        <scope>runtime</scope>
        <optional>true</optional>
    </dependency>

    <!-- 測試支援 -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-webmvc-test</artifactId>
        <scope>test</scope>
    </dependency>
</dependencies>
```

> ⚠️ **重點**：`spring-boot-starter-webmvc` **不包含** `spring-websocket`，必須手動加入。如果缺少此依賴，`@EnableWebSocket`、`WebSocketConfigurer`、`TextWebSocketHandler` 等類別都會找不到。

### 3.3 建立套件結構

```
src/main/java/com/example/demo/
├── Sbchat0826Application.java          ← 主啟動類
└── server/
    ├── WebSocketConfig.java            ← WebSocket 路由設定
    └── ChatWebSocketHandler.java       ← WebSocket 訊息處理器
```

### 3.4 主啟動類

```java
package com.example.demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class Sbchat0826Application {

    public static void main(String[] args) {
        SpringApplication.run(Sbchat0826Application.class, args);
    }
}
```

`@SpringBootApplication` 是 Spring Boot 的標準入口，它會：

1. 掃描同 Package 及子 Package 下的所有 `@Component` / `@Configuration` 類別
2. 啟用自動配置（Auto Configuration）
3. 啟用屬性綁定

---

## 四、Server 端實作

### 4.1 WebSocket 配置類 — `WebSocketConfig.java`

#### 概念

Spring WebSocket 需要一個 `@Configuration` 類別來告訴框架：

1. **哪個 Handler** 處理 WebSocket 訊息
2. **註冊到哪個路徑**（URL endpoint）
3. **允許哪些來源**（CORS 設定）

#### 程式碼

```java
package com.example.demo.server;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

@Configuration                          // 標記為 Spring 設定類別
@EnableWebSocket                       // 啟用 WebSocket 支援
public class WebSocketConfig implements WebSocketConfigurer {

    @Bean
    public ChatWebSocketHandler chatWebSocketHandler() {
        return new ChatWebSocketHandler();
    }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(chatWebSocketHandler(), "/ws/chat")  // 註冊 Handler 到 /ws/chat
                .setAllowedOrigins("*");                          // 允許所有來源連線
    }
}
```

#### 程式碼解說

| 註解 / 方法 | 作用 |
|-------------|------|
| `@Configuration` | 告訴 Spring 這是設定類別，Spring 會呼叫其中的 `@Bean` 方法來建立物件 |
| `@EnableWebSocket` | 啟用 Spring WebSocket 自動配置，讓 `WebSocketConfigurer` 被識別 |
| `WebSocketConfigurer` | 介面，實作 `registerWebSocketHandlers()` 來註冊路由 |
| `@Bean` | 將 `ChatWebSocketHandler` 註冊為 Spring Bean，讓 Spring 管理其生命週期 |
| `addHandler(handler, "/ws/chat")` | 將 Handler 綁定到 `/ws/chat` 這個 WebSocket endpoint |
| `setAllowedOrigins("*")` | 允許所有網域的 Client 連線（開發用，正式環境應限制來源） |

> ⚠️ **常見錯誤**：如果忘記加 `@EnableWebSocket`，Spring 不會啟動 WebSocket 支援，連線時會收到 HTTP 404。

### 4.2 WebSocket 訊息處理器 — `ChatWebSocketHandler.java`

#### 概念

`TextWebSocketHandler` 是 Spring 提供的抽象類別，用來處理 **純文字** 的 WebSocket 訊息。你需要覆寫三個生命週期方法：

```
連線建立 → afterConnectionEstablished()
收到訊息 → handleTextMessage()
連線關閉 → afterConnectionClosed()
```

每個連線都有一個 `WebSocketSession` 物件，代表一個 Client 的連線狀態。Server 透過 `session.sendMessage()` 發送訊息給特定 Client。

#### 程式碼

```java
package com.example.demo.server;

import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.concurrent.CopyOnWriteArrayList;

public class ChatWebSocketHandler extends TextWebSocketHandler {

    // 使用 CopyOnWriteArrayList 保證多執行緒安全
    private static final CopyOnWriteArrayList<WebSocketSession> sessions = new CopyOnWriteArrayList<>();

    // ① 有新的 Client 連線時觸發
    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        sessions.add(session);
        System.out.println("Client connected: " + session.getId());
        System.out.println("Current sessions size: " + sessions.size());
    }

    // ② 收到 Client 傳來的文字訊息時觸發
    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws IOException {
        String payload = message.getPayload();
        System.out.println("User input: " + payload);

        // 廣播：把訊息送給所有已連線的 Client
        for (WebSocketSession s : sessions) {
            if (s.isOpen()) {
                s.sendMessage(new TextMessage(payload));
            }
        }
    }

    // ③ Client 斷線時觸發
    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        sessions.remove(session);
        System.out.println("Connection closed: " + session.getId());
        System.out.println("Current sessions size: " + sessions.size());
    }
}
```

#### 程式碼解說

| 元素 | 說明 |
|------|------|
| `CopyOnWriteArrayList` | 線程安全的 List。因為多個 Client 同時連線/斷線會從不同執行緒呼叫，用一般 `ArrayList` 會產生 `ConcurrentModificationException` |
| `session.getId()` | 每個 WebSocket 連線的唯一識別碼，由 Tomcat 自動產生 |
| `message.getPayload()` | 取得 Client 傳來的原始文字內容 |
| `s.sendMessage(new TextMessage(...))` | 將文字訊息送給指定的 Client |
| `s.isOpen()` | 確認該 Client 仍在連線狀態，避免對已斷線的 Session 傳送訊息導致例外 |

#### 訊息廣播流程圖

```
Client A 發送 "Hello"
        │
        ▼
┌─ ChatWebSocketHandler ─────────────────────┐
│  handleTextMessage() 被觸發                  │
│                                             │
│  payload = "Hello"                          │
│                                             │
│  for each session in sessions:              │
│    ├── Session A (Client A) → sendText()   │
│    ├── Session B (Client B) → sendText()   │
│    └── Session C (Client C) → sendText()   │
└─────────────────────────────────────────────┘
        │
        ▼
所有 Client 都收到 "Hello"
```

> ⚠️ **執行緒安全陷阱**：本範例使用 `CopyOnWriteArrayList`，它在**讀取時複製底層陣列**，適合「讀取多、寫入少」的場景。若連線數量頻繁變動（大量上/下線），可考慮使用 `ConcurrentHashMap` + `synchronized` 或 `ReadWriteLock`。

---

## 五、Client 端實作

### 5.1 HTML 頁面 — `chatclient.html`

#### 概念

Client 端就是一個簡單的 HTML 頁面，放在 Spring Boot 的 `static/` 目錄下，Spring Boot 會自動提供靜態檔案服務。

#### 檔案位置

```
src/main/resources/static/
├── chatclient.html          ← 聊天室頁面
└── js/
    └── client.js            ← WebSocket 邏輯
```

#### 程式碼

```html
<!DOCTYPE html>
<html>
   <head>
        <title>WebSocket Test</title>
        <meta charset="UTF-8">
        <script src="js/client.js"></script>
    </head>
    <body>
        <div>
            <form id="chatRoomForm" onsubmit="return false;">
                聊天室
                名字: <input type="text" id="userNameInput" />
                <input type="button" id="loginBtn" value="登入" />
                <span id="infoWindow"></span>
                <input type="text" id="userinput" />
                <input type="submit" value="送出訊息" />
            </form>
        </div>
        <div id="messageDisplay"></div>
    </body>
</html>
```

#### 程式碼解說

| 元素 ID | 用途 |
|---------|------|
| `userNameInput` | 使用者輸入暱稱的文字框 |
| `loginBtn` | 登入按鈕，點擊後建立 WebSocket 連線 |
| `infoWindow` | 顯示狀態訊息（登入成功/失敗/請輸入名稱） |
| `userinput` | 使用者輸入聊天訊息的文字框 |
| `chatRoomForm` | 表單包裝，Submit 時攔截預設行為 |
| `messageDisplay` | 顯示所有聊天訊息的區塊 |

> ⚠️ **`onsubmit="return false;"`** 的作用是阻止表單的預設送出行為（避免頁面重新整理），實際的訊息送出由 JavaScript 控制。

### 5.2 JavaScript WebSocket 客戶端 — `client.js`

#### 概念

JavaScript 提供原生的 `WebSocket` 物件，讓瀏覽器可以直接建立 WebSocket 連線。整個 Client 邏輯的流程是：

```
使用者輸入名字 → 點擊登入 → new WebSocket() → onopen → 可以收發訊息
```

#### 程式碼

```javascript
window.onload = function () {
    // ===== 1. 取得所有 DOM 元件 =====
    var loginBtn = document.getElementById("loginBtn");
    var userNameInput = document.getElementById("userNameInput");
    var infoWindow = document.getElementById("infoWindow");
    var userinput = document.getElementById("userinput");
    var chatRoomForm = document.getElementById("chatRoomForm");
    var messageDisplay = document.getElementById("messageDisplay");

    var webSocket;              // WebSocket 實例
    var isConnectSuccess = false; // 連線狀態旗標

    // ===== 2. 登入按鈕事件 =====
    loginBtn.addEventListener("click", function () {
        if (userNameInput.value && userNameInput.value !== "") {
            setWebSocket();     // 名字非空才建立連線
        } else {
            infoWindow.innerHTML = "請輸入名稱";
        }
    });

    // ===== 3. 表單送出事件 =====
    chatRoomForm.addEventListener("submit", function () {
        sendMessage();
        return false;           // 阻止表單預設重新整理
    });

    // ===== 4. 發送訊息 =====
    function sendMessage() {
        if (webSocket && isConnectSuccess) {
            var messageInfo = {
                userName: userNameInput.value,   // 暱稱
                message: userinput.value         // 訊息內容
            };
            webSocket.send(JSON.stringify(messageInfo)); // 序列化為 JSON 並送出
        } else {
            infoWindow.innerHTML = "未登入";
        }
    }

    // ===== 5. 建立 WebSocket 連線 =====
    function setWebSocket() {
        // 建立連線（連到 Server 的 /ws/chat 端點）
        webSocket = new WebSocket('ws://localhost:8080/ws/chat');

        // --- 連線錯誤 ---
        webSocket.onerror = function (event) {
            loginBtn.disabled = false;
            userNameInput.disabled = false;
            infoWindow.innerHTML = "登入失敗";
        };

        // --- 連線成功 ---
        webSocket.onopen = function (event) {
            isConnectSuccess = true;
            loginBtn.disabled = true;       // 登入後禁用按鈕
            userNameInput.disabled = true;  // 登入後禁用名字輸入
            infoWindow.innerHTML = "登入成功";

            // 送一條「進入聊天室」的系統訊息
            var firstLoginInfo = {
                userName: "系統",
                message: userNameInput.value + " 登入了聊天室"
            };
            webSocket.send(JSON.stringify(firstLoginInfo));
        };

        // --- 收到伺服器訊息 ---
        webSocket.onmessage = function (event) {
            var messageObject = JSON.parse(event.data); // 反序列化 JSON
            messageDisplay.innerHTML +=
                messageObject.userName + " 說 : " +
                messageObject.message + "<br/>";
        };
    }
};
```

#### JavaScript WebSocket API 速查

| API | 說明 |
|-----|------|
| `new WebSocket(url)` | 建立 WebSocket 連線，url 格式為 `ws://host:port/path` |
| `ws.send(data)` | 發送資料給 Server（本專案發送 JSON 字串） |
| `ws.onopen` | 連線成功時觸發的回呼函式 |
| `ws.onmessage` | 收到 Server 訊息時觸發，`event.data` 為訊息內容 |
| `ws.onerror` | 連線發生錯誤時觸發 |
| `ws.onclose` | 連線關閉時觸發 |
| `ws.readyState` | 連線狀態：`0`=連線中, `1`=已連線, `2`=關閉中, `3`=已關閉 |

#### 訊息格式（JSON Protocol）

Client 和 Server 之間使用 JSON 格式傳遞訊息：

```json
{
    "userName": "小明",
    "message": "大家好！"
}
```

| 欄位 | 型別 | 說明 |
|------|------|------|
| `userName` | String | 發送者的暱稱，或 "系統" 表示系統訊息 |
| `message` | String | 訊息內容 |

---

## 六、訊息流程完整追蹤

### 6.1 連線建立流程

```
時間軸 ──────────────────────────────────────────────►

Browser                                          Server
  │                                                │
  │  ① new WebSocket("ws://localhost:8080/ws/chat")│
  │─────── HTTP Upgrade Request ──────────────────►│
  │                                                │
  │  ② WebSocketConfig 找到 /ws/chat 對應的 Handler│
  │  ③ ChatWebSocketHandler 被建立（或從 Bean 池取出）│
  │                                                │
  │  ④ afterConnectionEstablished(session)          │
  │     → sessions.add(session)                    │
  │                                                │
  │◄───── 101 Switching Protocols ─────────────────│
  │                                                │
  │  ⑤ ws.onopen 觸發                              │
  │     → isConnectSuccess = true                  │
  │     → 發送「登入了聊天室」系統訊息              │
  │─────── JSON { userName:"系統", message:"..." } ►│
  │                                                │
  │  ⑥ handleTextMessage() 觸發                    │
  │     → 廣播給所有已連線 Client                   │
  │◄───── JSON { userName:"系統", message:"..." } ─│
  │                                                │
```

### 6.2 訊息廣播流程

```
Client A（小明）              Server              Client B（小華）
     │                         │                        │
     │ send("Hello everyone")  │                        │
     │──── JSON ──────────────►│                        │
     │                         │                        │
     │                    handleTextMessage()            │
     │                    for each session:              │
     │                         │                        │
     │◄──── JSON ─────────────│──── JSON ──────────────►│
     │  (小明 說: Hello)       │   (小明 說: Hello)     │
     │                         │                        │
```

### 6.3 斷線流程

```
Client B 關閉瀏覽器
     │
     ▼
afterConnectionClosed(sessionB)
  → sessions.remove(sessionB)
  → 此後廣播不會再送給 Client B
```

---

## 七、Server 端 vs Client 端 API 對照表

| 功能 | Server 端（Java） | Client 端（JavaScript） |
|------|-------------------|------------------------|
| 建立連線 | Spring 自動處理（依賴 Handler） | `new WebSocket(url)` |
| 發送訊息 | `session.sendMessage(new TextMessage(...))` | `ws.send(data)` |
| 接收訊息 | `handleTextMessage(session, message)` | `ws.onmessage = function(event)` |
| 連線成功 | `afterConnectionEstablished(session)` | `ws.onopen = function(event)` |
| 連線關閉 | `afterConnectionClosed(session, status)` | `ws.onclose = function(event)` |
| 連線錯誤 | 由 Spring 框架處理例外 | `ws.onerror = function(event)` |
| 取得訊息內容 | `message.getPayload()` | `event.data` |
| 取得連線 ID | `session.getId()` | 無直接 API |
| 關閉連線 | `session.close()` | `ws.close()` |

---

## 八、執行與驗證

### 8.1 啟動 Server

```bash
cd sbchat0826
./mvnw spring-boot:run
```

或在 IDE 中直接執行 `Sbchat0826Application.java` 的 `main()` 方法。

確認 Console 出現類似以下訊息：

```
Tomcat started on port(s): 8080
Mapped "{[/ws/chat]}" ...
```

### 8.2 開啟 Client

1. 瀏覽器開啟 `http://localhost:8080/chatclient.html`
2. 在「名字」欄位輸入暱稱（例如：小明）
3. 點擊「登入」
4. 預期看到：`登入成功` + `系統 說 : 小明 登入了聊天室`

### 8.3 多人測試

1. 開啟**第二個瀏覽器視窗**（或用無痕模式），同樣訪問 `http://localhost:8080/chatclient.html`
2. 用不同暱稱登入（例如：小華）
3. 兩個視窗都應該看到「小華 登入了聊天室」
4. 在任一視窗輸入訊息並送出，另一個視窗應即時收到

### 8.4 Server Console 輸出範例

```
Client connected: 0
Current sessions size: 1
User input: {"userName":"小明","message":"大家好"}
Connection closed: 0
Current sessions size: 0
```

---

## 九、關鍵觀念整理

### WebSocket vs HTTP

| 特性 | HTTP | WebSocket |
|------|------|-----------|
| 連線模式 | 一問一答（Request-Response） | 持久連線（Bidirectional） |
| 誰能先發訊息 | 只有 Client 能先發 | 任何一方都能主動發 |
| 適合場景 | 表單提交、API 查詢 | 即時聊天、遊戲、股票行情 |
| 連線開銷 | 每次請求都建立新連線 | 一次建連，持續使用 |
| 協定升級 | — | 從 HTTP 101 Upgrade 而來 |

### 為什麼用 `CopyOnWriteArrayList`？

```
一般 ArrayList：
  Thread A: for (s : list) { ... }     // 正在迭代
  Thread B: list.remove(s);            // 同時修改 → ConcurrentModificationException ❌

CopyOnWriteArrayList：
  Thread A: for (s : list) { ... }     // 迭代的是「快照」
  Thread B: list.remove(s);            // 修改的是新陣列 → 互不影響 ✅
```

### 為什麼需要 `setAllowedOrigins("*")`？

Spring WebSocket 預設會檢查 Origin 標頭，如果不是來自同源請求會被拒絕。開發階段設定 `"*"` 允許所有來源；正式環境應改為指定允許的網域：

```java
registry.addHandler(chatWebSocketHandler(), "/ws/chat")
        .setAllowedOrigins("https://yourdomain.com");
```

---

## 十、常見問題排解

| 症狀 | 可能原因 | 解決方式 |
|------|----------|----------|
| `ws://localhost:8080/ws/chat` 連線失敗（404） | 未加 `@EnableWebSocket` 註解 | 在 `WebSocketConfig` 加上 `@EnableWebSocket` |
| `ws://localhost:8080/ws/chat` 連線失敗（404） | 缺少 `spring-websocket` 依賴 | 在 `pom.xml` 加入 `spring-websocket` |
| 連線成功但收不到訊息 | Client 送出 JSON 但 Server 未正確解析 | 確認 `handleTextMessage()` 有收到並廣播 |
| `ConcurrentModificationException` | 使用 `ArrayList` 管理 Session | 改用 `CopyOnWriteArrayList` |
| 跨網域連線被拒絕 | CORS 未允許 | 加 `.setAllowedOrigins("*")` |
| 修改程式碼後連線異常 | Spring Boot DevTools 自動重啟中 | 等待重啟完成，或關閉 DevTools |

---

## 十一、練習題

### 練習一：顯示連線人數（難度：★）

**目標**：當有人登入或斷線時，在 Server Console 顯示目前連線人數（本專案已有此功能，請確認能正確運作）。

**完成標準**：
- 登入時顯示 `Client connected: <session_id>` 和 `Current sessions size: N`
- 斷線時顯示 `Connection closed: <session_id>` 和 `Current sessions size: N`

---

### 練習二：改善 Client 端介面（難度：★★）

**目標**：修改 `chatclient.html` 與 `client.js`，讓聊天室更易用。

**需求**：
1. 登入後將「名字」欄位和「登入」按鈕隱藏（用 CSS `display:none`）
2. 將 `messageDisplay` 設定固定高度並加上 `overflow-y: auto`（自動捲動）
3. 訊息顯示格式改為 `[時間] 暱稱：訊息內容`

**提示**：
```javascript
// 取得當前時間
var now = new Date();
var timeStr = now.getHours() + ":" +
              String(now.getMinutes()).padStart(2, '0');
```

---

### 練習三：加入「使用者離開」訊息（難度：★★）

**目標**：當 Client 斷線時，廣播一條離開訊息給其他 Client。

**需求**：
1. 修改 `ChatWebSocketHandler.afterConnectionClosed()` 方法
2. 廣播格式：`{"userName":"系統","message":"小明 離開了聊天室"}`

**挑戰**：`afterConnectionClosed()` 中的 `session` 已經關閉，無法透過它取得使用者名稱。你需要自行設計一個 `Map<String, String>`（session ID → userName），在登入時記錄、斷線時查詢。

> ⚠️ 暫存使用者名稱的 Map 也必須是線程安全的（例如 `ConcurrentHashMap`）。

---

### 練習四：私訊功能（難度：★★★）

**目標**：實現「只發送給特定使用者」的私訊功能。

**需求**：
1. 訊息格式改為：`{"userName":"小明","message":"Hello","to":"小華"}`
2. Server 端判斷 `to` 欄位：
   - 若為空或不存在 → 廣播給所有人（目前行為）
   - 若為特定使用者 → 只送給該使用者
3. 需要一個 `Map<userName, WebSocketSession>` 來快速查找目標使用者

**完成標準**：
- 發送 `{"userName":"小明","message":"嗨","to":"小華"}` 時，只有小華能收到
- 發送 `{"userName":"小明","message":"大家好","to":""}` 時，所有人都收到

---

### 練習五：訊息持久化（難度：★★★）

**目標**：將聊天紀錄存到資料庫，新使用者加入時可看到最近 10 則歷史訊息。

**需求**：
1. 建立 `ChatMessage` Entity（`id`, `userName`, `message`, `createdAt`）
2. 建立 `ChatMessageRepository`（JPA Repository）
3. 在 `handleTextMessage()` 中儲存訊息到資料庫
4. 在 `afterConnectionEstablished()` 中查詢最近 10 則訊息並送給新加入的 Client
5. 在 `pom.xml` 加入 `spring-boot-starter-data-jpa` 和 H2 資料庫依賴

---

## 十二、完整檔案清單

| 檔案路徑 | 說明 |
|----------|------|
| `pom.xml` | Maven 專案設定與依賴 |
| `src/main/java/com/example/demo/Sbchat0826Application.java` | Spring Boot 主啟動類 |
| `src/main/java/com/example/demo/server/WebSocketConfig.java` | WebSocket 路由配置 |
| `src/main/java/com/example/demo/server/ChatWebSocketHandler.java` | WebSocket 訊息處理器 |
| `src/main/resources/application.properties` | 應用設定 |
| `src/main/resources/static/chatclient.html` | 聊天室 HTML 頁面 |
| `src/main/resources/static/js/client.js` | WebSocket Client JavaScript |
