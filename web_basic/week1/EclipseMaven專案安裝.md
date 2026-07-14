# Eclipse Maven Web 專案建置指南

## 目錄
1. [環境準備](#環境準備)
2. [Eclipse 配置](#eclipse-配置)
3. [Maven 專案建立](#maven-專案建立)
4. [專案配置](#專案配置)
5. [建置與部署](#建置與部署)
6. [故障排除](#故障排除)

---

## 環境準備

### 軟體需求

| 軟體 | 版本需求 | 備註 |
|------|----------|------|
| **JDK** | 11 或以上 | 必須設定 JAVA_HOME |
| **Eclipse IDE** | 2023-06 或更新 | Enterprise Java 版本 |
| **Apache Maven** | 3.6.0+ | Eclipse 內建或獨立安裝 |
| **Apache Tomcat** | 10.1.x | 用於部署測試 |

### 環境變數設定

#### Windows
```batch
@echo off
set JAVA_HOME=C:\Program Files\Java\jdk-11.0.16
set MAVEN_HOME=C:\apache-maven-3.9.4
set CATALINA_HOME=C:\apache-tomcat-10.1.0
set PATH=%JAVA_HOME%\bin;%MAVEN_HOME%\bin;%CATALINA_HOME%\bin;%PATH%

java -version
mvn -version
```

#### Linux/macOS
```bash
export JAVA_HOME=/usr/lib/jvm/java-11-openjdk
export MAVEN_HOME=/opt/apache-maven-3.9.4
export CATALINA_HOME=/opt/apache-tomcat-10.1.0
export PATH=$JAVA_HOME/bin:$MAVEN_HOME/bin:$CATALINA_HOME/bin:$PATH

java -version
mvn -version
```

---

## Eclipse 配置

### 1. 安裝 Eclipse

下載 **Eclipse IDE for Enterprise Java and Web Developers**

### 2. Maven 設定

```
Window → Preferences → Maven
├── Installations: 選擇內建版本
└── User Settings: 檢查 settings.xml 路徑
```

### 3. 新增 Tomcat 10.1 伺服器

```
Window → Preferences → Server → Runtime Environments
→ Add... → Apache Tomcat v10.1
→ 選擇 Tomcat 安裝目錄
→ 選擇 JDK 11
```

---

## Maven 專案建立

### 1. 使用 Eclipse 精靈建立

```
File → New → Other → Maven → Maven Project

Next →
Filter: webapp
選擇: maven-archetype-webapp

Next →
Group Id: com.example
Artifact Id: my-webapp
Version: 1.0.0-SNAPSHOT

Finish
```

### 2. 命令列建立（備選）

```bash
mvn archetype:generate \
  -DgroupId=com.example \
  -DartifactId=my-webapp \
  -DarchetypeArtifactId=maven-archetype-webapp \
  -DarchetypeVersion=1.5 \
  -DinteractiveMode=false
```

### 3. 專案結構

```
my-webapp/
├── pom.xml
├── src/main/
│   ├── java/
│   │   └── com/example/
│   ├── resources/
│   └── webapp/
│       ├── WEB-INF/
│       │   └── web.xml
│       └── index.jsp
└── target/
```

---

## 專案配置

### 1. pom.xml 配置

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 
         http://maven.apache.org/xsd/maven-4.0.0.xsd">
  
  <modelVersion>4.0.0</modelVersion>
  <groupId>com.example</groupId>
  <artifactId>my-webapp</artifactId>
  <version>1.0.0-SNAPSHOT</version>
  <packaging>war</packaging>
  
  <properties>
    <maven.compiler.source>11</maven.compiler.source>
    <maven.compiler.target>11</maven.compiler.target>
    <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
  </properties>
  
  <dependencies>
    <!-- Jakarta Servlet API (Tomcat 10.1) -->
    <dependency>
      <groupId>jakarta.servlet</groupId>
      <artifactId>jakarta.servlet-api</artifactId>
      <version>6.0.0</version>
      <scope>provided</scope>
    </dependency>
    
    <!-- Jakarta JSP API (Tomcat 10.1) -->
    <dependency>
      <groupId>jakarta.servlet.jsp</groupId>
      <artifactId>jakarta.servlet.jsp-api</artifactId>
      <version>3.0.0</version>
      <scope>provided</scope>
    </dependency>
    
    <!-- JSTL -->
    <dependency>
      <groupId>jakarta.servlet.jsp.jstl</groupId>
      <artifactId>jakarta.servlet.jsp.jstl-api</artifactId>
      <version>3.0.0</version>
    </dependency>
    
    <!-- 測試 -->
    <dependency>
      <groupId>org.junit.jupiter</groupId>
      <artifactId>junit-jupiter</artifactId>
      <version>5.9.3</version>
      <scope>test</scope>
    </dependency>
  </dependencies>
  
  <build>
    <finalName>my-webapp</finalName>
    <plugins>
      <plugin>
        <groupId>org.apache.maven.plugins</groupId>
        <artifactId>maven-compiler-plugin</artifactId>
        <version>3.11.0</version>
        <configuration>
          <source>11</source>
          <target>11</target>
          <encoding>UTF-8</encoding>
        </configuration>
      </plugin>
      
      <plugin>
        <groupId>org.apache.maven.plugins</groupId>
        <artifactId>maven-war-plugin</artifactId>
        <version>3.4.0</version>
        <configuration>
          <failOnMissingWebXml>false</failOnMissingWebXml>
        </configuration>
      </plugin>
    </plugins>
  </build>
</project>
```

### 2. web.xml 配置 (Jakarta EE 10)

```xml
<?xml version="1.0" encoding="UTF-8"?>
<web-app xmlns="https://jakarta.ee/xml/ns/jakartaee"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="https://jakarta.ee/xml/ns/jakartaee
         https://jakarta.ee/xml/ns/jakartaee/web-app_6_0.xsd"
         version="6.0">
  
  <display-name>My Web Application</display-name>
  
  <!-- 編碼過濾器 -->
  <filter>
    <filter-name>CharacterEncodingFilter</filter-name>
    <filter-class>com.example.EncodingFilter</filter-class>
    <init-param>
      <param-name>encoding</param-name>
      <param-value>UTF-8</param-value>
    </init-param>
  </filter>
  
  <filter-mapping>
    <filter-name>CharacterEncodingFilter</filter-name>
    <url-pattern>/*</url-pattern>
  </filter-mapping>
  
  <!-- Session 配置 -->
  <session-config>
    <session-timeout>30</session-timeout>
  </session-config>
  
  <!-- 歡迎頁面 -->
  <welcome-file-list>
    <welcome-file>index.jsp</welcome-file>
  </welcome-file-list>
</web-app>
```

### 3. 範例 Servlet

```java
package com.example;

import jakarta.servlet.*;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;

@WebServlet("/hello")
public class HelloServlet extends HttpServlet {
    
    @Override
    protected void doGet(HttpServletRequest request, 
                        HttpServletResponse response) 
            throws ServletException, IOException {
        
        response.setContentType("text/html;charset=UTF-8");
        String name = request.getParameter("name");
        
        if (name == null || name.trim().isEmpty()) {
            name = "訪客";
        }
        
        response.getWriter().println(
            "<h1>Hello, " + name + "!</h1>" +
            "<p>歡迎使用 Jakarta EE 10 + Tomcat 10.1</p>"
        );
    }
}
```

### 4. 範例 JSP

```jsp
<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="zh-TW">
<head>
    <meta charset="UTF-8">
    <title>我的 Web 應用程式</title>
</head>
<body>
    <h1>歡迎來到我的 Web 應用程式</h1>
    <p>使用 Eclipse + Maven + Tomcat 10.1 建立</p>
    
    <form action="hello" method="get">
        <label>請輸入姓名：</label>
        <input type="text" name="name" placeholder="輸入姓名">
        <button type="submit">送出</button>
    </form>
</body>
</html>
```

---

## 建置與部署

### 1. Maven 常用指令

```bash
# 清理並打包
mvn clean package

# 跳過測試打包
mvn clean package -DskipTests

# 執行測試
mvn test

# 查看相依性
mvn dependency:tree
```

### 2. Eclipse 建置

```
Right-click → Run As → Maven build...
Goals: clean package
```

### 3. Tomcat 部署

#### 方法一：Eclipse 內建部署
```
Servers 視圖 → 右鍵 Tomcat → Add and Remove...
選擇專案 → Add → Start
```

#### 方法二：手動部署
```bash
# 複製 WAR 到 Tomcat
cp target/my-webapp.war $CATALINA_HOME/webapps/

# 啟動 Tomcat
$CATALINA_HOME/bin/startup.sh
```

### 4. 測試應用程式

```
http://localhost:8080/my-webapp/
http://localhost:8080/my-webapp/hello?name=World
```

---

## 故障排除

### 1. 編譯錯誤：package jakarta.servlet does not exist

**解決方案**：
- 確認 pom.xml 使用 `jakarta.servlet-api` 而非 `javax.servlet-api`
- 執行 `mvn clean compile`

### 2. 404 錯誤

**解決方案**：
- 確認專案已部署到 Tomcat
- 檢查 URL 路徑是否正確
- 確認 Servlet 有 `@WebServlet` 註解

### 3. 500 錯誤

**解決方案**：
- 檢查 Tomcat logs 目錄
- 確認 Java 程式碼語法正確
- 檢查 web.xml 配置

### 4. 中文亂碼

**解決方案**：
```java
request.setCharacterEncoding("UTF-8");
response.setContentType("text/html;charset=UTF-8");
```

```jsp
<%@ page contentType="text/html;charset=UTF-8" %>
```

### 5. Maven 依賴下載失敗

**解決方案**：
```bash
# 強制更新
mvn clean install -U

# 檢查網路或更換鏡像
# 修改 ~/.m2/settings.xml 加入阿里雲鏡像
```

---

**文件結束**
