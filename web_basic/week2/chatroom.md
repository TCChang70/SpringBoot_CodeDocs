# Apache Tomcat Websocket 聊天室教學
WebSocket可以在伺服器和瀏覽器之間建立雙向通訊，今天要來用
WebSocket來實現一個簡單的聊天室做範例

需求：
1.網頁上面登入後聊天室會出現登入訊息。
2.登入後可在訊息框中輸入訊息並送出，送出後聊天室會出現訊息。
3.聊天室出現的訊息任何已登入聊天室的人都會看到訊息。
4.登入後的人只能看到登入後有人打的訊息。


1. Java採用了Annotation的寫法。
2.在Java中，Session有一個方法叫做getBasicRemote()，理論上可以傳回所有已連接的Session，但實測不管開了幾個網頁、開了幾個WebSocket連接，永遠都是回傳個數為0的Set，推測是因為每個連接都被一個新產生的Thread來處理，類似Servlet，所以每個Session都是各別的Thread的物件，
所以我在這用Static的ArrayList<Session> sessions來儲存每個連接Session。
參考：Java Websocket API - Fetch all Websocket Session(s) to a ServerEndpoint
3.如果建立了WebSocket連接，但沒使用WebSocket.send()，並且Server先發了訊息給Client端，Client端會無法收到訊息，並且Client端要再send()訊息時會出錯，錯誤訊息為:WebSocket connection to 'ws://XXX' failed: Invalid frame header
所以此例在登入後(即建立了WebSocket連接)馬上由Client向Server發了登入訊息，


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
    //用來存放WebSocket已連接的Socket
    static ArrayList<Session> sessions;
 
    @OnMessage
    public void onMessage(String message, Session session) throws IOException,
            InterruptedException, EncodeException {
        System.out.println("User input: " + message);
        //session.getBasicRemote().sendText("Hello world Mr. " + message);
        //for (Session s : session.getOpenSessions()) {
        for (Session s : sessions) {    //對每個連接的Client傳送訊息
            if (s.isOpen()) {
                s.getBasicRemote().sendText(message);
            }
        }
    }
 
    @OnOpen
    public void onOpen(Session session) {
        //紀錄連接到sessions中
        System.out.println("Client connected");        
        if (sessions == null) {
            sessions = new ArrayList<Session>();
        }
        sessions.add(session);
        System.out.println("Current sessions size: " + sessions.size());
    }
 
    @OnClose
    public void onClose(Session session) {
        //將連接從sessions中移除
        System.out.println("Connection closed");
        if (sessions == null) {
            sessions = new ArrayList<Session>();
        }
        sessions.remove(session);
        System.out.println("Current sessions size: " + sessions.size());
    }
}

Html client:

<!DOCTYPE html>
<html>
   <head>
        <title>WebSocket Test</title>
        <meta charset="UTF-8">      
        <script src="js/wsclient.js"></script>
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

Javascript:

window.onload = function () {
    //獲取DOM元件
    var loginBtn = document.getElementById("loginBtn");
    var userNameInput = document.getElementById("userNameInput");
    var infoWindow = document.getElementById("infoWindow");
    var userinput = document.getElementById("userinput");
    var chatRoomForm = document.getElementById("chatRoomForm");
    var messageDisplay = document.getElementById("messageDisplay");
 
    var webSocket;
    var isConnectSuccess = false;
 
    //設置登入鈕的動作，沒有登出，登入才可發言
    loginBtn.addEventListener("click", function () {
        //檢查有無輸入名稱
        if (userNameInput.value && userNameInput.value !== "") {
            setWebSocket();  //設置WebSocket連接
        } else {
            infoWindow.innerHTML = "請輸入名稱";
        }
 
    });
    //Submit Form時送出訊息
    chatRoomForm.addEventListener("submit", function () {
        sendMessage();
        return false;
    });
    //使用webSocket擁有的function, send(), 送出訊息
    function sendMessage() {
        //檢查WebSocket連接狀態
        if (webSocket && isConnectSuccess) {
            var messageInfo = {
                userName: userNameInput.value,
                message: userinput.value
            }
            webSocket.send(JSON.stringify(messageInfo));
        } else {
            infoWindow.innerHTML = "未登入";
        }
    }
 
    //設置WebSocket
    function setWebSocket() {
        //開始WebSocket連線
        webSocket = new WebSocket('ws://localhost:8080/ws/chat');
        //以下開始偵測WebSocket的各種事件
         
        //onerror , 連線錯誤時觸發  
        webSocket.onerror = function (event) {
            loginBtn.disabled = false;
            userNameInput.disabled = false;
            infoWindow.innerHTML = "登入失敗";
        };
 
        //onopen , 連線成功時觸發
        webSocket.onopen = function (event) {
            isConnectSuccess = true;
            loginBtn.disabled = true;
            userNameInput.disabled = true;
            infoWindow.innerHTML = "登入成功";
             
            //送一個登入聊天室的訊息
            var firstLoginInfo = {
                userName : "系統",
                message : userNameInput.value + " 登入了聊天室"
            };
            webSocket.send(JSON.stringify(firstLoginInfo));
        };
 
        //onmessage , 接收到來自Server的訊息時觸發
        webSocket.onmessage = function (event) {
            var messageObject = JSON.parse(event.data);
            messageDisplay.innerHTML += "" + messageObject.userName + " 說 : " + messageObject.message+"<br/>";
        };
    }
};



 
如果上述程式是Spring Boot 專案
需要加入註冊ServerContainer 並註冊 Endpoint
/**
 * 在 ApplicationReadyEvent 觸發時再去找 ServerContainer 並註冊 Endpoint。
 * 同時列出所有 ServletContext attribute（協助偵錯）。
 */
@Component
public class WebSockConfig {

    private static final Logger logger = LoggerFactory.getLogger(WebSockConfig.class);

    private final ApplicationContext applicationContext;

    public WebSockConfig(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void onApplicationReady() {
        logger.info("ApplicationReadyEvent 嘗試搜尋 ServerContainer ...");

        if (!(applicationContext instanceof ServletWebServerApplicationContext swc)) {
            logger.error("沒有 ServletWebServerApplicationContext. 跳出 WebSocket.");
            return;
        }

        ServletContext servletContext = swc.getServletContext();
        if (servletContext == null) {
            logger.error("沒有 ServletContext.");
            return;
        }

        // 列出所有 attribute，協助確認 key 與時序
        logger.info("Listing ServletContext attributes for debugging:");
        Enumeration<String> names = servletContext.getAttributeNames();
        while (names.hasMoreElements()) {
            String name = names.nextElement();
            Object val = servletContext.getAttribute(name);
            logger.info("  attribute(屬性): {} -> {}", name, val != null ? val.getClass().getName() : "null");
        }

        // 常見的 attribute keys
        Object containerObj = servletContext.getAttribute("jakarta.websocket.server.ServerContainer");
        if (containerObj == null) {        	
            containerObj = servletContext.getAttribute("javax.websocket.server.ServerContainer");
        }

        if (containerObj == null) {
            logger.warn("No ServerContainer found in ServletContext. WebSocket won't be available.");
            logger.warn("If using embedded Tomcat: ensure 'tomcat-embed-websocket' is on the classpath.");
            return;
        }

        try {
            // 以 reflection 找 addEndpoint(Class) 並註冊
            Method addEndpoint = containerObj.getClass().getMethod("addEndpoint", Class.class);
            addEndpoint.invoke(containerObj, ChatRoomServer.class);
            logger.info("註冊 ChatRoomServer via ServerContainer: {}", containerObj.getClass().getName());
        } catch (NoSuchMethodException nsme) {
            logger.error("ServerContainer 沒有 addEndpoint(Class).", nsme);
        } catch (Exception e) {
            logger.error("Failed to register endpoint via reflection", e);
        }
    }
}
