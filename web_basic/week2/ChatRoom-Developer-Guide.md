# WebSocket 聊天室開發者教學指南

## 目錄
1. [專案概述](#專案概述)
2. [技術棧](#技術棧)
3. [環境準備與設定](#環境準備與設定)
4. [後端實作 (Java WebSocket API)](#後端實作-java-websocket-api)
    - [4.1 建立 WebSocket 端點](#41-建立-websocket-端點)
    - [4.2 處理連線生命週期](#42-處理連線生命週期)
    - [4.3 核心挑戰：Session 管理](#43-核心挑戰session-管理)
5. [前端實作 (JavaScript)](#前端實作-javascript)
    - [5.1 HTML 結構](#51-html-結構)
    - [5.2 JavaScript 核心邏輯](#52-javascript-核心邏輯)
6. [Spring Boot 整合指南](#spring-boot-整合指南)
    - [6.1 問題：為何需要手動註冊？](#61-問題為何需要手動註冊)
    - [6.2 解決方案：使用 `EventListener` 註冊端點](#62-解決方案使用-eventlistener-註冊端點)
7. [如何運行與測試](#如何運行與測試)
8. [重要觀念與常見陷阱](#重要觀念與常見陷阱)
9. [未來改進方向](#未來改進方向)

---

## 1. 專案概述

本文件旨在引導開發者使用 Java WebSocket API (`javax.websocket`) 和原生 JavaScript 建立一個基本的即時聊天室。我們將從後端 WebSocket 伺服器的建立，到前端客戶端的互動邏輯，逐步完成整個應用。

**核心功能需求：**
- 使用者輸入名稱後可登入聊天室。
- 登入後，聊天室會顯示系統的歡迎訊息。
- 使用者可以發送訊息，所有在線使用者都能即時看到。
- 使用者離開後，系統能感知並處理連線關閉。

---

## 2. 技術棧

- **後端**:
    - Java
    - Java API for WebSocket (`javax.websocket.*`)
    - Spring Boot (用於整合與部署)
    - Apache Tomcat (作為 WebSocket 容器)
- **前端**:
    - HTML5
    - CSS3 (基礎樣式)
    - JavaScript (原生 WebSocket API)
- **資料格式**: JSON

---

## 3. 環境準備與設定

1.  **JDK**: 建議使用 Java 11 或更高版本。
2.  **Maven**: 用於專案依賴管理。
3.  **IDE**: 推薦使用 IntelliJ IDEA 或 Eclipse。
4.  **Spring Boot 專案設定**:
    - 建立一個新的 Spring Boot 專案。
    - 在 `pom.xml` 中，確保包含以下依賴：
      ```xml
      <!-- Web 應用程式基礎 -->
      <dependency>
          <groupId>org.springframework.boot</groupId>
          <artifactId>spring-boot-starter-web</artifactId>
      </dependency>
      
      <!-- 啟用內嵌 Tomcat 的 WebSocket 支援 -->
      <dependency>
          <groupId>org.springframework.boot</groupId>
          <artifactId>spring-boot-starter-websocket</artifactId>
      </dependency>
      ```
      **注意**: `spring-boot-starter-websocket` 會自動引入 `tomcat-embed-websocket`，這是讓標準 `@ServerEndpoint` 運作的關鍵。

---

## 4. 後端實作 (Java WebSocket API)

### 4.1 建立 WebSocket 端點

我們使用標準的 Java WebSocket API 註解來定義一個 WebSocket 伺服器端點。

**`ChatRoomServer.java`**
```java
import java.io.IOException;
import java.util.ArrayList;
import javax.websocket.EncodeException;
import javax.websocket.OnClose;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;

@ServerEndpoint("/ws/chat")
public class ChatRoomServer {
    // ... 程式碼內容 ...
}
```

- **`@ServerEndpoint("/ws/chat")`**: 這是最重要的註解。它將這個 Java 類別宣告為一個 WebSocket 端點，並指定客戶端需要連接的 URI 路徑。當客戶端嘗試連接 `ws://<your-server>/ws/chat` 時，容器就會將請求導向這個類別。

### 4.2 處理連線生命週期

WebSocket 有三個主要的生命週期事件，我們使用對應的註解來處理：

- **`@OnOpen`**: 當一個新的 WebSocket 連線建立時觸發。
  ```java
  @OnOpen
  public void onOpen(Session session) {
      System.out.println("Client connected");
      if (sessions == null) {
          sessions = new ArrayList<Session>();
      }
      sessions.add(session); // 將新的連線 session 加入我們的管理列表
      System.out.println("Current sessions size: " + sessions.size());
  }
  ```
  - **職責**: 追蹤新的客戶端連線，並將其 `Session` 物件儲存起來以便後續廣播訊息。

- **`@OnMessage`**: 當伺服器從客戶端接收到一則訊息時觸發。
  ```java
  @OnMessage
  public void onMessage(String message, Session session) throws IOException {
      System.out.println("User input: " + message);
      // 遍歷所有已儲存的 session
      for (Session s : sessions) {
          if (s.isOpen()) {
              // 將收到的訊息廣播給每一個連線中的客戶端
              s.getBasicRemote().sendText(message);
          }
      }
  }
  ```
  - **職責**: 接收客戶端訊息並將其廣播給所有在線使用者。

- **`@OnClose`**: 當一個 WebSocket 連線關閉時觸發。
  ```java
  @OnClose
  public void onClose(Session session) {
      System.out.println("Connection closed");
      if (sessions != null) {
          sessions.remove(session); // 從管理列表中移除已關閉的連線
      }
      System.out.println("Current sessions size: " + sessions.size());
  }
  ```
  - **職責**: 清理無效的連線，防止記憶體洩漏。

### 4.3 核心挑戰：Session 管理

在 `chatroom.md` 中提到一個關鍵問題：為什麼我們需要一個 `static ArrayList<Session> sessions` 來手動管理所有連線？

- **原因**: WebSocket 容器（如 Tomcat）為每一個新的 WebSocket 連線建立一個新的執行緒和一個新的 `ChatRoomServer` 物件實例。這意味著在 `onMessage` 方法中，`session.getOpenSessions()` 只會回傳當前這個實例自己的 `Session`，而不是伺服器上所有的連線。
- **解決方案**: 我們使用一個 `static` 的 `ArrayList`。因為 `static` 變數是類別層級的，它被所有 `ChatRoomServer` 的實例所共享。這樣，無論哪個連線觸發了 `onMessage`，它都能存取到包含所有連線的 `sessions` 列表，從而實現訊息廣播。
- **注意**: 這種 `static ArrayList` 的作法在多執行緒環境下不是執行緒安全的。在高併發場景下，應改用執行緒安全的集合，例如 `Collections.synchronizedList()` 或 `CopyOnWriteArrayList`。

---

## 5. 前端實作 (JavaScript)

### 5.1 HTML 結構

前端介面非常簡單，包含：
- 一個用於輸入使用者名稱和登入的表單。
- 一個用於輸入聊天訊息的表單。
- 一個用於顯示聊天內容的 `div`。

```html
<!DOCTYPE html>
<html>
<head>
    <title>WebSocket Test</title>
    <meta charset="UTF-8">      
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
    <script>
        // JavaScript 程式碼
    </script>
</body>
</html>
```

### 5.2 JavaScript 核心邏輯

前端的互動完全由 JavaScript 控制。

1.  **建立 WebSocket 連線**
    ```javascript
    function setWebSocket() {
        // 'ws://' 是 WebSocket 協議的標準前綴
        webSocket = new WebSocket('ws://localhost:8080/ws/chat');
        
        // 綁定事件處理器
        webSocket.onopen = function(event) { /* ... */ };
        webSocket.onmessage = function(event) { /* ... */ };
        webSocket.onerror = function(event) { /* ... */ };
    }
    ```

2.  **處理連線成功 (`onopen`)**
    ```javascript
    webSocket.onopen = function (event) {
        isConnectSuccess = true;
        infoWindow.innerHTML = "登入成功";
        
        // 關鍵步驟：連線成功後，立即向伺服器發送一則 "登入" 訊息
        var firstLoginInfo = {
            userName : "系統",
            message : userNameInput.value + " 登入了聊天室"
        };
        webSocket.send(JSON.stringify(firstLoginInfo));
    };
    ```
    - **重要**: 如 `chatroom.md` 中所述，客戶端在連線後最好立即發送一則訊息。這有助於確保雙向通訊管道完全建立，避免某些情況下伺服器先發送訊息而客戶端收不到的問題。

3.  **接收訊息 (`onmessage`)**
    ```javascript
    webSocket.onmessage = function (event) {
        // event.data 包含從伺服器傳來的字串
        var messageObject = JSON.parse(event.data);
        messageDisplay.innerHTML += "" + messageObject.userName + " 說 : " + messageObject.message + "<br/>";
    };
    ```
    - 我們約定前後端之間使用 JSON 格式通訊，因此在收到訊息後需要 `JSON.parse()`。

4.  **發送訊息 (`send`)**
    ```javascript
    function sendMessage() {
        if (webSocket && isConnectSuccess) {
            var messageInfo = {
                userName: userNameInput.value,
                message: userinput.value
            }
            // 發送前，將 JavaScript 物件轉換為 JSON 字串
            webSocket.send(JSON.stringify(messageInfo));
        }
    }
    ```

---

## 6. Spring Boot 整合指南

### 6.1 問題：為何需要手動註冊？

當使用 Spring Boot 內嵌的 Tomcat 伺服器時，它不一定會自動掃描並註冊使用 `javax.websocket.server.ServerEndpoint` 註解的類別。這與 Spring 自己提供的 WebSocket 支援 (`spring-websocket`) 機制不同。因此，我們需要一個方法來告訴 Tomcat：「嘿，這裡有一個 WebSocket 端點，請幫我註冊它。」

### 6.2 解決方案：使用 `EventListener` 註冊端點

`chatroom.md` 中提供了一個非常聰明的解決方案：

**`WebSockConfig.java`**
```java
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationContext;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.web.context.support.ServletWebServerApplicationContext;
import javax.servlet.ServletContext;
import java.lang.reflect.Method;

@Component
public class WebSockConfig {

    private final ApplicationContext applicationContext;

    public WebSockConfig(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void onApplicationReady() {
        // 1. 等待 Spring Boot 應用程式完全準備就緒
        if (!(applicationContext instanceof ServletWebServerApplicationContext swc)) {
            return;
        }
        ServletContext servletContext = swc.getServletContext();

        // 2. 從 ServletContext 中取得 WebSocket 容器
        Object containerObj = servletContext.getAttribute("jakarta.websocket.server.ServerContainer");
        if (containerObj == null) {
            containerObj = servletContext.getAttribute("javax.websocket.server.ServerContainer");
        }
        if (containerObj == null) {
            // 如果找不到容器，可能是缺少 'tomcat-embed-websocket' 依賴
            return;
        }

        try {
            // 3. 使用反射呼叫容器的 addEndpoint 方法，手動註冊我們的端點類別
            Method addEndpoint = containerObj.getClass().getMethod("addEndpoint", Class.class);
            addEndpoint.invoke(containerObj, ChatRoomServer.class);
            System.out.println("成功註冊 WebSocket 端點: ChatRoomServer");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
```
- **`@EventListener(ApplicationReadyEvent.class)`**: 這個方法會在 Spring Boot 應用程式啟動完成後執行，確保 `ServletContext` 已經可用。
- **`ServerContainer`**: 這是 `javax.websocket` API 的核心，負責管理 WebSocket 端點的生命週期。我們從 `ServletContext` 的屬性中找到它。
- **反射 (`Reflection`)**: 由於 `ServerContainer` 是一個介面，我們使用反射來呼叫它的 `addEndpoint` 方法，這樣更具通用性。

---

## 7. 如何運行與測試

1.  將 `ChatRoomServer.java`, `WebSockConfig.java` 放入你的 Spring Boot 專案中。
2.  將 `index.html` 檔案放入 `src/main/resources/static/` 目錄下。
3.  運行 Spring Boot 主應用程式。
4.  打開兩個不同的瀏覽器視窗或分頁，都訪問 `http://localhost:8080/index.html`。
5.  在兩個視窗中分別輸入不同的使用者名稱並登入。
6.  在一個視窗中發送訊息，觀察另一個視窗是否能即時收到。

---

## 8. 重要觀念與常見陷阱

- **執行緒安全**: `static ArrayList` 不是執行緒安全的。在高併發下可能出現問題。考慮使用 `CopyOnWriteArrayList` 或其他同步集合。
- **水平擴展**: 目前的架構將 Session 儲存在單一伺服器的記憶體中，無法水平擴展。若要支援多伺服器部署，需要將 Session 資訊儲存在外部共享儲存中，例如 Redis。
- **依賴問題**: 如果 WebSocket 無法運作，首先檢查 `pom.xml` 是否正確包含了 `spring-boot-starter-websocket` 依賴。
- **路徑問題**: 確保前端 `WebSocket` 建構子中的 URL (`ws://...`) 與後端 `@ServerEndpoint` 中定義的路徑一致。

---

## 9. 未來改進方向

- **執行緒安全**: 將 `ArrayList` 替換為執行緒安全的集合。
- **使用者認證**: 整合 Spring Security，在 WebSocket 握手階段進行使用者認證。
- **私人訊息**: 實作點對點訊息發送功能，而不僅僅是廣播。
- **訊息持久化**: 將聊天記錄儲存到資料庫中。
- **錯誤處理**: 增加更完善的前後端錯誤處理與提示。
- **心跳機制**: 增加心跳檢測，以處理網路中斷但未觸發 `@OnClose` 的「殭屍連線」。