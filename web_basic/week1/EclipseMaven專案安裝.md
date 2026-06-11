# Eclipse Maven Web 應用程式專案建置完整教學文件

## 文件資訊
- **目標對象**：Java Web 開發初學者與進階開發者
- **文件類型**：IDE 整合開發環境教學文件
- **版本**：3.0 - Eclipse Maven WebApp 專業版

## 目錄
1. [環境準備與需求](#環境準備與需求)
2. [Eclipse 環境配置](#eclipse-環境配置)
3. [Maven Web 專案建立](#maven-web-專案建立)
4. [專案結構與配置](#專案結構與配置)
5. [相依性管理](#相依性管理)
6. [Web 應用程式開發](#web-應用程式開發)
7. [建置與部署](#建置與部署)
8. [除錯與測試](#除錯與測試)
9. [進階配置與優化](#進階配置與優化)
10. [故障排除指南](#故障排除指南)

---

## 環境準備與需求

### 系統需求

|      軟體組件      | 版本需求           |     下載來源     |   備註               |
|-------------------|-------------------|------------------|---------------------|
| **JDK**           | 8+ (建議 11 或 17) | Oracle / OpenJDK | 必須設定 JAVA_HOME  |
| **Eclipse IDE**   | 2023-06 或更新版本 | eclipse.org      | Enterprise Java 版本|
| **Apache Maven**  | 3.6.0+            | maven.apache.org | 內建或獨立安裝       |
| **Apache Tomcat** | 9.0+ 或 10.0+     | tomcat.apache.org | 用於部署測試        |

### 環境變數設定

#### Windows 系統設定
```batch name=setenv.bat
@echo off
echo ================================
echo  Eclipse Maven 環境變數設定
echo ================================

:: 設定 JAVA_HOME
set JAVA_HOME=C:\Program Files\Java\jdk-11.0.16
echo JAVA_HOME = %JAVA_HOME%

:: 設定 MAVEN_HOME (如果使用獨立 Maven)
set MAVEN_HOME=C:\apache-maven-3.9.4
echo MAVEN_HOME = %MAVEN_HOME%

:: 設定 CATALINA_HOME
set CATALINA_HOME=C:\apache-tomcat-9.0.80
echo CATALINA_HOME = %CATALINA_HOME%

:: 更新 PATH
set PATH=%JAVA_HOME%\bin;%MAVEN_HOME%\bin;%CATALINA_HOME%\bin;%PATH%

echo ================================
echo 環境變數設定完成！
echo ================================

:: 驗證安裝
java -version
mvn -version
```

#### Linux/macOS 系統設定
```bash name=setenv.sh
#!/bin/bash
echo "================================"
echo " Eclipse Maven 環境變數設定"
echo "================================"

# 設定環境變數
export JAVA_HOME=/usr/lib/jvm/java-11-openjdk
export MAVEN_HOME=/opt/apache-maven-3.9.4
export CATALINA_HOME=/opt/apache-tomcat-9.0.80

# 更新 PATH
export PATH=$JAVA_HOME/bin:$MAVEN_HOME/bin:$CATALINA_HOME/bin:$PATH

# 寫入到 profile
echo "export JAVA_HOME=$JAVA_HOME" >> ~/.bashrc
echo "export MAVEN_HOME=$MAVEN_HOME" >> ~/.bashrc
echo "export CATALINA_HOME=$CATALINA_HOME" >> ~/.bashrc
echo "export PATH=\$JAVA_HOME/bin:\$MAVEN_HOME/bin:\$CATALINA_HOME/bin:\$PATH" >> ~/.bashrc

echo "環境變數設定完成！"
echo "================================"

# 驗證安裝
java -version
mvn -version
```

---

## Eclipse 環境配置

### 1. Eclipse IDE 下載與安裝

#### 1.1 選擇正確版本
```
推薦版本：Eclipse IDE for Enterprise Java and Web Developers
- 包含完整的 Java EE 開發工具
- 內建 Maven 支援
- Web 開發相關插件
- 伺服器整合功能
```

#### 1.2 安裝步驟
1. 從 [eclipse.org](https://www.eclipse.org/downloads/) 下載 Eclipse Installer
2. 執行安裝程式，選擇 "Eclipse IDE for Enterprise Java and Web Developers"
3. 設定安裝目錄（建議：`C:\Eclipse` 或 `/opt/eclipse`）
4. 選擇 JRE 版本（使用已安裝的 JDK）

### 2. Eclipse 基本配置

#### 2.1 工作區設定
```
建議工作區路徑：
Windows: C:\Eclipse-Workspace
Linux/macOS: ~/Eclipse-Workspace
```

#### 2.2 Maven 整合配置

**步驟 1：檢查 Maven 設定**
```
Window → Preferences → Maven
├── Installations
│   ├── ☑ Embedded (推薦使用內建版本)
│   └── Add... (可添加外部 Maven)
├── User Interface
│   └── ☑ Open XML page in the POM editor by default
└── User Settings
    ├── Global Settings: ${maven.home}/conf/settings.xml
    └── User Settings: ${user.home}/.m2/settings.xml
```

**步驟 2：設定 Maven 本地儲存庫**
```xml name=settings.xml url=file:///${user.home}/.m2/settings.xml
<?xml version="1.0" encoding="UTF-8"?>
<settings xmlns="http://maven.apache.org/SETTINGS/1.0.0"
          xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
          xsi:schemaLocation="http://maven.apache.org/SETTINGS/1.0.0 
          http://maven.apache.org/xsd/settings-1.0.0.xsd">
  
  <!-- 本地儲存庫路徑 -->
  <localRepository>C:\Maven-Repository</localRepository>
  
  <!-- 代理設定（如需要） -->
  <!-- 
  <proxies>
    <proxy>
      <id>corporate-proxy</id>
      <active>true</active>
      <protocol>http</protocol>
      <host>proxy.company.com</host>
      <port>8080</port>
    </proxy>
  </proxies>
  -->
  
  <!-- 鏡像設定（加速下載） -->
  <mirrors>
    <mirror>
      <id>aliyun-maven</id>
      <mirrorOf>central</mirrorOf>
      <name>Aliyun Maven Central</name>
      <url>https://maven.aliyun.com/repository/central</url>
    </mirror>
  </mirrors>
  
  <!-- 預設配置 -->
  <profiles>
    <profile>
      <id>default</id>
      <properties>
        <maven.compiler.source>11</maven.compiler.source>
        <maven.compiler.target>11</maven.compiler.target>
        <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
      </properties>
    </profile>
  </profiles>
  
  <activeProfiles>
    <activeProfile>default</activeProfile>
  </activeProfiles>
</settings>
```

#### 2.3 伺服器配置

**步驟 1：新增 Tomcat 伺服器**
```
Window → Preferences → Server → Runtime Environments
→ Add... → Apache Tomcat v9.0
→ 選擇 Tomcat 安裝目錄
→ 選擇 JRE 版本
```

**步驟 2：設定伺服器屬性**
```
Servers 視圖 → 右鍵 → New → Server
├── Server Type: Tomcat v9.0 Server
├── Server name: Tomcat v9.0 Server at localhost
├── Server runtime environment: Apache Tomcat v9.0
└── Configuration: 使用預設配置
```

---

## Maven Web 專案建立

### 1. 使用 Maven Archetype 建立專案

#### 1.1 透過 Eclipse 精靈建立

**步驟詳解：**
```
File → New → Other → Maven → Maven Project
```

**配置參數：**
```
☑ Create a simple project (skip archetype selection) = false
☐ Use default Workspace location (自訂專案路徑)
Location: C:\Eclipse-Workspace\my-web-projects

Next →

Filter: webapp
選擇: maven-archetype-webapp
Group Id: org.apache.maven.archetypes
Artifact Id: maven-archetype-webapp
Version: 1.4 (最新版本)

Next →

Group Id: com.tcchang70.webapp     # 組織/公司域名
Artifact Id: my-first-webapp       # 專案名稱
Version: 1.0.0-SNAPSHOT           # 版本號
Package: com.tcchang70.webapp      # 預設套件名稱

Finish
```

#### 1.2 命令列建立方式（備選）

```bash
# 切換到工作目錄
cd C:\Eclipse-Workspace

# 使用 Maven archetype 建立專案
mvn archetype:generate \
  -DgroupId=com.tcchang70.webapp \
  -DartifactId=my-first-webapp \
  -DarchetypeArtifactId=maven-archetype-webapp \
  -DarchetypeVersion=1.4 \
  -DinteractiveMode=false

# 匯入到 Eclipse
# File → Import → Existing Maven Projects
```

### 2. 專案結構分析

#### 2.1 初始專案結構
```
my-first-webapp/
├── pom.xml                    # Maven 專案配置檔
├── src/
│   └── main/
│       ├── java/              # Java 原始碼目錄
│       │   └── com/
│       │       └── tcchang70/
│       │           └── webapp/
│       ├── resources/         # 資源檔目錄
│       │   ├── application.properties
│       │   └── log4j2.xml
│       └── webapp/           # Web 應用程式目錄
│           ├── WEB-INF/
│           │   └── web.xml   # Web 應用程式配置
│           └── index.jsp     # 預設首頁
└── target/                   # 編譯輸出目錄 (自動生成)
    ├── classes/
    ├── test-classes/
    └── my-first-webapp-1.0.0-SNAPSHOT.war
```

#### 2.2 建立完整目錄結構

```bash
# 在專案根目錄執行
mkdir -p src/main/java/com/tcchang70/webapp/controller
mkdir -p src/main/java/com/tcchang70/webapp/model
mkdir -p src/main/java/com/tcchang70/webapp/service
mkdir -p src/main/java/com/tcchang70/webapp/dao
mkdir -p src/main/java/com/tcchang70/webapp/util
mkdir -p src/main/resources/static/css
mkdir -p src/main/resources/static/js
mkdir -p src/main/resources/static/images
mkdir -p src/main/resources/templates
mkdir -p src/test/java/com/tcchang70/webapp
mkdir -p src/test/resources
```

---

## 專案結構與配置

### 1. POM.xml 完整配置

````xml name=pom.xml url=file:///C:/Eclipse-Workspace/my-first-webapp/pom.xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0" 
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 
         http://maven.apache.org/xsd/maven-4.0.0.xsd">
  
  <modelVersion>4.0.0</modelVersion>
  
  <!-- 專案基本資訊 -->
  <groupId>com.gjun.webapp</groupId>
  <artifactId>my-first-webapp</artifactId>
  <version>1.0.0-SNAPSHOT</version>
  <packaging>war</packaging>
  
  <name>My First Web Application</name>
  <description>使用 Eclipse + Maven 建立的第一個 Web 應用程式</description>
  <url>https://github.com/gjun/my-first-webapp</url>
  
  <!-- 開發者資訊 -->
  <developers>
    <developer>
      <id>Gjun</id>
      <name>Gjun</name>
      <email>gjun@example.com</email>
      <roles>
        <role>架構師</role>
        <role>開發者</role>
      </roles>
      <timezone>+8</timezone>
    </developer>
  </developers>
  
  <!-- 屬性配置 -->
  <properties>
    <!-- Java 版本 -->
    <maven.compiler.source>11</maven.compiler.source>
    <maven.compiler.target>11</maven.compiler.target>
    <java.version>11</java.version>
    
    <!-- 編碼設定 -->
    <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
    <project.reporting.outputEncoding>UTF-8</project.reporting.outputEncoding>
    
    <!-- 相依性版本 -->
    <servlet.api.version>4.0.1</servlet.api.version>
    <jsp.api.version>2.3.3</jsp.api.version>
    <jstl.version>1.2</jstl.version>
    <junit.version>5.9.3</junit.version>
    <mockito.version>5.4.0</mockito.version>
    <spring.version>5.3.23</spring.version>
    <jackson.version>2.15.2</jackson.version>
    <slf4j.version>2.0.7</slf4j.version>
    <logback.version>1.4.8</logback.version>
    
    <!-- Maven 插件版本 -->
    <maven.compiler.plugin.version>3.11.0</maven.compiler.plugin.version>
    <maven.war.plugin.version>3.4.0</maven.war.plugin.version>
    <maven.surefire.plugin.version>3.1.2</maven.surefire.plugin.version>
    <maven.failsafe.plugin.version>3.1.2</maven.failsafe.plugin.version>
    <tomcat.maven.plugin.version>2.2</tomcat.maven.plugin.version>
  </properties>
  
  <!-- 相依性管理 -->
  <dependencies>
    <!-- Servlet API -->
    <dependency>
      <groupId>javax.servlet</groupId>
      <artifactId>javax.servlet-api</artifactId>
      <version>${servlet.api.version}</version>
      <scope>provided</scope>
    </dependency>
    
    <!-- JSP API -->
    <dependency>
      <groupId>javax.servlet.jsp</groupId>
      <artifactId>javax.servlet.jsp-api</artifactId>
      <version>${jsp.api.version}</version>
      <scope>provided</scope>
    </dependency>
    
    <!-- JSTL -->
    <dependency>
      <groupId>javax.servlet</groupId>
      <artifactId>jstl</artifactId>
      <version>${jstl.version}</version>
    </dependency>
    
    <!-- Spring Framework (可選) -->
    <dependency>
      <groupId>org.springframework</groupId>
      <artifactId>spring-webmvc</artifactId>
      <version>${spring.version}</version>
    </dependency>
    
    <dependency>
      <groupId>org.springframework</groupId>
      <artifactId>spring-context</artifactId>
      <version>${spring.version}</version>
    </dependency>
    
    <!-- JSON 處理 -->
    <dependency>
      <groupId>com.fasterxml.jackson.core</groupId>
      <artifactId>jackson-databind</artifactId>
      <version>${jackson.version}</version>
    </dependency>
    
    <!-- 日誌系統 -->
    <dependency>
      <groupId>org.slf4j</groupId>
      <artifactId>slf4j-api</artifactId>
      <version>${slf4j.version}</version>
    </dependency>
    
    <dependency>
      <groupId>ch.qos.logback</groupId>
      <artifactId>logback-classic</artifactId>
      <version>${logback.version}</version>
    </dependency>
    
    <!-- 測試相依性 -->
    <dependency>
      <groupId>org.junit.jupiter</groupId>
      <artifactId>junit-jupiter</artifactId>
      <version>${junit.version}</version>
      <scope>test</scope>
    </dependency>
    
    <dependency>
      <groupId>org.mockito</groupId>
      <artifactId>mockito-core</artifactId>
      <version>${mockito.version}</version>
      <scope>test</scope>
    </dependency>
    
    <dependency>
      <groupId>org.springframework</groupId>
      <artifactId>spring-test</artifactId>
      <version>${spring.version}</version>
      <scope>test</scope>
    </dependency>
  </dependencies>
  
  <!-- 建置配置 -->
  <build>
    <finalName>my-first-webapp</finalName>
    
    <plugins>
      <!-- 編譯插件 -->
      <plugin>
        <groupId>org.apache.maven.plugins</groupId>
        <artifactId>maven-compiler-plugin</artifactId>
        <version>${maven.compiler.plugin.version}</version>
        <configuration>
          <source>${java.version}</source>
          <target>${java.version}</target>
          <encoding>${project.build.sourceEncoding}</encoding>
          <showWarnings>true</showWarnings>
          <showDeprecation>true</showDeprecation>
        </configuration>
      </plugin>
      
      <!-- War 打包插件 -->
      <plugin>
        <groupId>org.apache.maven.plugins</groupId>
        <artifactId>maven-war-plugin</artifactId>
        <version>${maven.war.plugin.version}</version>
        <configuration>
          <warSourceDirectory>src/main/webapp</warSourceDirectory>
          <failOnMissingWebXml>false</failOnMissingWebXml>
          <archive>
            <manifest>
              <addClasspath>true</addClasspath>
              <classpathPrefix>lib/</classpathPrefix>
            </manifest>
            <manifestEntries>
              <Built-By>TCChang70</Built-By>
              <Build-Time>${maven.build.timestamp}</Build-Time>
              <Implementation-Version>${project.version}</Implementation-Version>
            </manifestEntries>
          </archive>
        </configuration>
      </plugin>
      
      <!-- 單元測試插件 -->
      <plugin>
        <groupId>org.apache.maven.plugins</groupId>
        <artifactId>maven-surefire-plugin</artifactId>
        <version>${maven.surefire.plugin.version}</version>
        <configuration>
          <includes>
            <include>**/*Test.java</include>
            <include>**/*Tests.java</include>
          </includes>
          <argLine>-Xmx1024m</argLine>
        </configuration>
      </plugin>
      
      <!-- 整合測試插件 -->
      <plugin>
        <groupId>org.apache.maven.plugins</groupId>
        <artifactId>maven-failsafe-plugin</artifactId>
        <version>${maven.failsafe.plugin.version}</version>
        <configuration>
          <includes>
            <include>**/*IT.java</include>
            <include>**/*IntegrationTest.java</include>
          </includes>
        </configuration>
        <executions>
          <execution>
            <goals>
              <goal>integration-test</goal>
              <goal>verify</goal>
            </goals>
          </execution>
        </executions>
      </plugin>
      
      <!-- Tomcat 插件 -->
      <plugin>
        <groupId>org.apache.tomcat.maven</groupId>
        <artifactId>tomcat7-maven-plugin</artifactId>
        <version>${tomcat.maven.plugin.version}</version>
        <configuration>
          <port>8080</port>
          <path>/my-first-webapp</path>
          <uriEncoding>UTF-8</uriEncoding>
        </configuration>
      </plugin>
    </plugins>
  </build>
  
  <!-- 配置檔案 -->
  <profiles>
    <!-- 開發環境 -->
    <profile>
      <id>development</id>
      <activation>
        <activeByDefault>true</activeByDefault>
      </activation>
      <properties>
        <spring.profiles.active>development</spring.profiles.active>
        <log.level>DEBUG</log.level>
      </properties>
    </profile>
    
    <!-- 測試環境 -->
    <profile>
      <id>testing</id>
      <properties>
        <spring.profiles.active>testing</spring.profiles.active>
        <log.level>INFO</log.level>
      </properties>
    </profile>
    
    <!-- 生產環境 -->
    <profile>
      <id>production</id>
      <properties>
        <spring.profiles.active>production</spring.profiles.active>
        <log.level>WARN</log.level>
      </properties>
    </profile>
  </profiles>
</project>
````

### 2. Web.xml 配置

````xml name=web.xml url=file:///C:/Eclipse-Workspace/my-first-webapp/src/main/webapp/WEB-INF/web.xml
<?xml version="1.0" encoding="UTF-8"?>
<web-app xmlns="http://xmlns.jcp.org/xml/ns/javaee"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://xmlns.jcp.org/xml/ns/javaee 
         http://xmlns.jcp.org/xml/ns/javaee/web-app_4_0.xsd"
         version="4.0">
  
  <!-- 應用程式基本資訊 -->
  <display-name>My First Web Application</display-name>
  <description>
    基於 Eclipse + Maven 建立的現代化 Web 應用程式
    作者：gjun
    版本：1.0.0-SNAPSHOT
  </description>
  
  <!-- 編碼過濾器 -->
  <filter>
    <filter-name>CharacterEncodingFilter</filter-name>
    <filter-class>org.springframework.web.filter.CharacterEncodingFilter</filter-class>
    <init-param>
      <param-name>encoding</param-name>
      <param-value>UTF-8</param-value>
    </init-param>
    <init-param>
      <param-name>forceEncoding</param-name>
      <param-value>true</param-value>
    </init-param>
  </filter>
  
  <filter-mapping>
    <filter-name>CharacterEncodingFilter</filter-name>
    <url-pattern>/*</url-pattern>
  </filter-mapping>
  
  <!-- Spring MVC 前端控制器 -->
  <servlet>
    <servlet-name>DispatcherServlet</servlet-name>
    <servlet-class>org.springframework.web.servlet.DispatcherServlet</servlet-class>
    <init-param>
      <param-name>contextConfigLocation</param-name>
      <param-value>/WEB-INF/spring-mvc-config.xml</param-value>
    </init-param>
    <load-on-startup>1</load-on-startup>
  </servlet>
  
  <servlet-mapping>
    <servlet-name>DispatcherServlet</servlet-name>
    <url-pattern>/</url-pattern>
  </servlet-mapping>
  
  <!-- Spring 根容器配置 -->
  <context-param>
    <param-name>contextConfigLocation</param-name>
    <param-value>/WEB-INF/spring-context.xml</param-value>
  </context-param>
  
  <listener>
    <listener-class>org.springframework.web.context.ContextLoaderListener</listener-class>
  </listener>
  
  <!-- Session 配置 -->
  <session-config>
    <session-timeout>30</session-timeout>
    <cookie-config>
      <http-only>true</http-only>
      <secure>false</secure>
    </cookie-config>
  </session-config>
  
  <!-- 錯誤頁面配置 -->
  <error-page>
    <error-code>404</error-code>
    <location>/error/404.jsp</location>
  </error-page>
  
  <error-page>
    <error-code>500</error-code>
    <location>/error/500.jsp</location>
  </error-page>
  
  <!-- 歡迎頁面 -->
  <welcome-file-list>
    <welcome-file>index.jsp</welcome-file>
    <welcome-file>index.html</welcome-file>
  </welcome-file-list>
  
  <!-- MIME 類型設定 -->
  <mime-mapping>
    <extension>woff</extension>
    <mime-type>application/font-woff</mime-type>
  </mime-mapping>
  
  <mime-mapping>
    <extension>woff2</extension>
    <mime-type>application/font-woff2</mime-type>
  </mime-mapping>
</web-app>
````

---

## 相依性管理

### 1. 相依性範圍說明

| 範圍 | 說明 | 使用時機 | 範例 |
|------|------|----------|------|
| `compile` | 編譯、測試、執行時都需要 | 預設範圍 | Spring Framework |
| `provided` | 編譯、測試時需要，執行時由容器提供 | Servlet API | servlet-api |
| `runtime` | 執行、測試時需要 | 資料庫驅動 | MySQL Connector |
| `test` | 僅測試時需要 | 測試框架 | JUnit |
| `system` | 系統路徑相依性 | 少用 | 本地 JAR |

### 2. 常用相依性配置

```xml
<!-- 資料庫相關 -->
<dependency>
  <groupId>mysql</groupId>
  <artifactId>mysql-connector-java</artifactId>
  <version>8.0.33</version>
  <scope>runtime</scope>
</dependency>

<!-- MyBatis -->
<dependency>
  <groupId>org.mybatis</groupId>
  <artifactId>mybatis</artifactId>
  <version>3.5.13</version>
</dependency>

<!-- Apache Commons -->
<dependency>
  <groupId>org.apache.commons</groupId>
  <artifactId>commons-lang3</artifactId>
  <version>3.13.0</version>
</dependency>

<!-- Validation -->
<dependency>
  <groupId>org.hibernate.validator</groupId>
  <artifactId>hibernate-validator</artifactId>
  <version>6.2.5.Final</version>
</dependency>
```

---

## Web 應用程式開發

### 1. 建立首頁

````jsp name=index.jsp url=file:///C:/Eclipse-Workspace/my-first-webapp/src/main/webapp/index.jsp
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<!DOCTYPE html>
<html lang="zh-TW">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <meta http-equiv="X-UA-Compatible" content="IE=edge">
    <title>My First Web Application - 首頁</title>
    
    <!-- Bootstrap CSS -->
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
    <!-- Font Awesome -->
    <link href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.4.0/css/all.min.css" rel="stylesheet">
    <!-- 自訂樣式 -->
    <link href="${pageContext.request.contextPath}/css/style.css" rel="stylesheet">
    
    <style>
        body {
            font-family: 'Microsoft JhengHei', 'Segoe UI', Arial, sans-serif;
            background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
            min-height: 100vh;
        }
        
        .hero-section {
            background: rgba(255, 255, 255, 0.1);
            backdrop-filter: blur(10px);
            border-radius: 20px;
            padding: 60px 40px;
            text-align: center;
            margin-top: 100px;
            color: white;
            box-shadow: 0 15px 35px rgba(0, 0, 0, 0.1);
        }
        
        .hero-title {
            font-size: 3.5rem;
            font-weight: 700;
            margin-bottom: 20px;
            text-shadow: 2px 2px 4px rgba(0, 0, 0, 0.3);
        }
        
        .hero-subtitle {
            font-size: 1.3rem;
            margin-bottom: 30px;
            opacity: 0.9;
        }
        
        .feature-card {
            background: rgba(255, 255, 255, 0.95);
            border-radius: 15px;
            padding: 30px;
            margin-bottom: 20px;
            text-align: center;
            transition: transform 0.3s ease;
            box-shadow: 0 5px 15px rgba(0, 0, 0, 0.1);
        }
        
        .feature-card:hover {
            transform: translateY(-5px);
        }
        
        .feature-icon {
            font-size: 3rem;
            color: #667eea;
            margin-bottom: 20px;
        }
        
        .btn-custom {
            background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
            border: none;
            color: white;
            padding: 12px 30px;
            border-radius: 25px;
            font-weight: 600;
            text-transform: uppercase;
            letter-spacing: 1px;
            transition: all 0.3s ease;
        }
        
        .btn-custom:hover {
            transform: translateY(-2px);
            box-shadow: 0 8px 25px rgba(102, 126, 234, 0.4);
            color: white;
        }
        
        .footer {
            background: rgba(0, 0, 0, 0.8);
            color: white;
            text-align: center;
            padding: 20px 0;
            margin-top: 50px;
        }
    </style>
</head>
<body>
    <!-- 導覽列 -->
    <nav class="navbar navbar-expand-lg navbar-dark" style="background: rgba(0, 0, 0, 0.3);">
        <div class="container">
            <a class="navbar-brand" href="#">
                <i class="fas fa-rocket"></i> My First WebApp
            </a>
            <button class="navbar-toggler" type="button" data-bs-toggle="collapse" data-bs-target="#navbarNav">
                <span class="navbar-toggler-icon"></span>
            </button>
            <div class="collapse navbar-collapse" id="navbarNav">
                <ul class="navbar-nav ms-auto">
                    <li class="nav-item">
                        <a class="nav-link active" href="#"><i class="fas fa-home"></i> 首頁</a>
                    </li>
                    <li class="nav-item">
                        <a class="nav-link" href="#features"><i class="fas fa-star"></i> 功能</a>
                    </li>
                    <li class="nav-item">
                        <a class="nav-link" href="#about"><i class="fas fa-info-circle"></i> 關於</a>
                    </li>
                    <li class="nav-item">
                        <a class="nav-link" href="contact.jsp"><i class="fas fa-envelope"></i> 聯絡</a>
                    </li>
                </ul>
            </div>
        </div>
    </nav>

    <!-- 主要內容 -->
    <div class="container">
        <!-- Hero Section -->
        <div class="hero-section">
            <h1 class="hero-title">
                <i class="fas fa-code"></i> 歡迎來到我的第一個 Web 應用程式！
            </h1>
            <p class="hero-subtitle">
                使用 Eclipse + Maven 建立的現代化 Java Web 應用程式
            </p>
            <p style="font-size: 1.1rem; margin-bottom: 30px;">
                <i class="fas fa-calendar-alt"></i> 建立時間：<fmt:formatDate value="<%= new java.util.Date() %>" pattern="yyyy-MM-dd HH:mm:ss"/>
                <br>
                <i class="fas fa-user"></i> 開發者：TCChang70
                <br>
                <i class="fas fa-tag"></i> 版本：1.0.0-SNAPSHOT
            </p>
            <a href="#features" class="btn btn-custom btn-lg">
                <i class="fas fa-arrow-down"></i> 探索功能
            </a>
        </div>

        <!-- 功能介紹 -->
        <section id="features" class="row mt-5">
            <div class="col-md-4">
                <div class="feature-card">
                    <div class="feature-icon">
                        <i class="fas fa-tools"></i>
                    </div>
                    <h4>Maven 建置管理</h4>
                    <p>使用 Maven 進行相依性管理、建置和部署，提供標準化的專案結構。</p>
                    <a href="#" class="btn btn-outline-primary">了解更多</a>
                </div>
            </div>
            
            <div class="col-md-4">
                <div class="feature-card">
                    <div class="feature-icon">
                        <i class="fas fa-leaf"></i>
                    </div>
                    <h4>Spring MVC 框架</h4>
                    <p>整合 Spring MVC 框架，提供 MVC 架構和相依性注入功能。</p>
                    <a href="#" class="btn btn-outline-success">了解更多</a>
                </div>
            </div>
            
            <div class="col-md-4">
                <div class="feature-card">
                    <div class="feature-icon">
                        <i class="fas fa-mobile-alt"></i>
                    </div>
                    <h4>響應式設計</h4>
                    <p>使用 Bootstrap 框架，支援各種裝置和螢幕尺寸的響應式設計。</p>
                    <a href="#" class="btn btn-outline-info">了解更多</a>
                </div>
            </div>
        </section>

        <!-- 技術規格 -->
        <section id="about" class="mt-5">
            <div class="row">
                <div class="col-lg-8 mx-auto">
                    <div class="feature-card">
                        <h3><i class="fas fa-cogs"></i> 技術規格</h3>
                        <div class="row text-start mt-4">
                            <div class="col-md-6">
                                <h5>前端技術</h5>
                                <ul class="list-unstyled">
                                    <li><i class="fab fa-html5 text-danger"></i> HTML5</li>
                                    <li><i class="fab fa-css3-alt text-primary"></i> CSS3</li>
                                    <li><i class="fab fa-js-square text-warning"></i> JavaScript</li>
                                    <li><i class="fab fa-bootstrap text-purple"></i> Bootstrap 5</li>
                                </ul>
                            </div>
                            <div class="col-md-6">
                                <h5>後端技術</h5>
                                <ul class="list-unstyled">
                                    <li><i class="fab fa-java text-orange"></i> Java 11</li>
                                    <li><i class="fas fa-leaf text-success"></i> Spring MVC</li>
                                    <li><i class="fas fa-server text-secondary"></i> Apache Tomcat</li>
                                    <li><i class="fas fa-box text-info"></i> Maven</li>
                                </ul>
                            </div>
                        </div>
                        
                        <hr class="my-4">
                        
                        <div class="row">
                            <div class="col-md-12">
                                <h5>專案統計</h5>
                                <div class="row text-center">
                                    <div class="col-3">
                                        <h3 class="text-primary">1</h3>
                                        <small>專案數量</small>
                                    </div>
                                    <div class="col-3">
                                        <h3 class="text-success"><%= request.getSession().isNew() ? "1" : "2+" %></h3>
                                        <small>訪問次數</small>
                                    </div>
                                    <div class="col-3">
                                        <h3 class="text-info">5</h3>
                                        <small>功能模組</small>
                                    </div>
                                    <div class="col-3">
                                        <h3 class="text-warning">99%</h3>
                                        <small>完成度</small>
                                    </div>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </section>
    </div>

    <!-- 頁尾 -->
    <footer class="footer">
        <div class="container">
            <p>&copy; 2025 TCChang70. 使用 Eclipse + Maven 建立的 Web 應用程式.</p>
            <p>
                <small>
                    Session ID: <%= request.getSession().getId() %> | 
                    Server: <%= application.getServerInfo() %> |
                    Java Version: <%= System.getProperty("java.version") %>
                </small>
            </p>
        </div>
    </footer>

    <!-- Bootstrap JS -->
    <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/js/bootstrap.bundle.min.js"></script>
    <!-- 自訂 JavaScript -->
    <script src="${pageContext.request.contextPath}/js/app.js"></script>
    
    <script>
        // 頁面載入完成後執行
        document.addEventListener('DOMContentLoaded', function() {
            console.log('🚀 My First Web Application 載入完成！');
            console.log('開發者：TCChang70');
            console.log('建立時間：2025-10-15 08:03:24 UTC');
            
            // 平滑滾動
            document.querySelectorAll('a[href^="#"]').forEach(anchor => {
                anchor.addEventListener('click', function(e) {
                    e.preventDefault();
                    const target = document.querySelector(this.getAttribute('href'));
                    if (target) {
                        target.scrollIntoView({
                            behavior: 'smooth',
                            block: 'start'
                        });
                    }
                });
            });
        });
    </script>
</body>
</html>
````

### 2. 建立控制器

```java name=HomeController.java url=file:///C:/Eclipse-Workspace/my-first-webapp/src/main/java/com/tcchang70/webapp/controller/HomeController.java
package com.tcchang70.webapp.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

/**
 * 首頁控制器
 * 
 * @author gjun
 * @version 1.0.0
 * @since 2025-10-15
 */
@Controller
@RequestMapping("/")
public class HomeController {
    
    private static final Logger logger = LoggerFactory.getLogger(HomeController.class);
    
    /**
     * 首頁顯示
     */
    @GetMapping
    public String home(Model model, HttpServletRequest request) {
        logger.info("訪問首頁 - IP: {}", request.getRemoteAddr());
        
        // 添加頁面資料
        model.addAttribute("currentTime", LocalDateTime.now());
        model.addAttribute("version", "1.0.0-SNAPSHOT");
        model.addAttribute("developer", "TCChang70");
        
        return "index";
    }
    
    /**
     * API 端點 - 系統資訊
     */
    @GetMapping("/api/info")
    @ResponseBody
    public Map<String, Object> getSystemInfo(HttpServletRequest request) {
        logger.info("API 請求 - 系統資訊");
        
        Map<String, Object> info = new HashMap<>();
        info.put("applicationName", "My First Web Application");
        info.put("version", "1.0.0-SNAPSHOT");
        info.put("developer", "TCChang70");
        info.put("currentTime", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        info.put("sessionId", request.getSession().getId());
        info.put("serverInfo", request.getServletContext().getServerInfo());
        info.put("javaVersion", System.getProperty("java.version"));
        info.put("contextPath", request.getContextPath());
        
        return info;
    }
    
    /**
     * 健康檢查端點
     */
    @GetMapping("/health")
    @ResponseBody
    public Map<String, String> health() {
        Map<String, String> status = new HashMap<>();
        status.put("status", "UP");
        status.put("timestamp", LocalDateTime.now().toString());
        return status;
    }
}
```

### 3. Spring MVC 配置

````xml name=spring-mvc-config.xml url=file:///C:/Eclipse-Workspace/my-first-webapp/src/main/webapp/WEB-INF/spring-mvc-config.xml
<?xml version="1.0" encoding="UTF-8"?>
<beans xmlns="http://www.springframework.org/schema/beans"
       xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
       xmlns:context="http://www.springframework.org/schema/context"
       xmlns:mvc="http://www.springframework.org/schema/mvc"
       xsi:schemaLocation="
           http://www.springframework.org/schema/beans
           http://www.springframework.org/schema/beans/spring-beans.xsd
           http://www.springframework.org/schema/context
           http://www.springframework.org/schema/context/spring-context.xsd
           http://www.springframework.org/schema/mvc
           http://www.springframework.org/schema/mvc/spring-mvc.xsd">
    
    <!-- 啟用組件掃描 -->
    <context:component-scan base-package="com.tcchang70.webapp.controller" />
    
    <!-- 啟用 Spring MVC 註解 -->
    <mvc:annotation-driven>
        <mvc:message-converters>
            <bean class="org.springframework.http.converter.json.MappingJackson2HttpMessageConverter">
                <property name="supportedMediaTypes">
                    <list>
                        <value>application/json</value>
                        <value>text/json</value>
                    </list>
                </property>
            </bean>
        </mvc:message-converters>
    </mvc:annotation-driven>
    
    <!-- 靜態資源處理 -->
    <mvc:resources mapping="/css/**" location="/css/" />
    <mvc:resources mapping="/js/**" location="/js/" />
    <mvc:resources mapping="/images/**" location="/images/" />
    <mvc:resources mapping="/fonts/**" location="/fonts/" />
    
    <!-- 視圖解析器 -->
    <bean class="org.springframework.web.servlet.view.InternalResourceViewResolver">
        <property name="prefix" value="/WEB-INF/views/" />
        <property name="suffix" value=".jsp" />
        <property name="contentType" value="text/html;charset=UTF-8" />
    </bean>
    
    <!-- 檔案上傳解析器 -->
    <bean id="multipartResolver" class="org.springframework.web.multipart.commons.CommonsMultipartResolver">
        <property name="maxUploadSize" value="10485760" /> <!-- 10MB -->
        <property name="defaultEncoding" value="UTF-8" />
    </bean>
    
    <!-- 異常處理器 -->
    <bean class="org.springframework.web.servlet.handler.SimpleMappingExceptionResolver">
        <property name="exceptionMappings">
            <props>
                <prop key="java.lang.Exception">error/500</prop>
            </props>
        </property>
        <property name="defaultErrorView" value="error/500" />
    </bean>
</beans>
````

---

## 建置與部署

### 1. Maven 命令操作

#### 1.1 常用建置命令

```bash
# 清理專案
mvn clean

# 編譯專案
mvn compile

# 編譯測試程式碼
mvn test-compile

# 執行測試
mvn test

# 打包專案
mvn package

# 安裝到本地儲存庫
mvn install

# 部署到遠端儲存庫
mvn deploy

# 完整建置流程
mvn clean compile test package

# 跳過測試打包
mvn clean package -DskipTests

# 查看相依性樹狀圖
mvn dependency:tree

# 檢查更新
mvn versions:display-dependency-updates
```

#### 1.2 建置輸出說明

```
執行 mvn clean package 後的輸出結構：

target/
├── classes/                           # 編譯後的 Java 類別檔案
│   └── com/
│       └── tcchang70/
│           └── webapp/
│               └── controller/
│                   └── HomeController.class
├── test-classes/                      # 編譯後的測試類別檔案
├── maven-archiver/
│   └── pom.properties
├── my-first-webapp/                   # 展開的 WAR 檔案內容
│   ├── META-INF/
│   ├── WEB-INF/
│   │   ├── classes/
│   │   ├── lib/
│   │   └── web.xml
│   ├── css/
│   ├── js/
│   └── index.jsp
└── my-first-webapp.war               # 最終的 WAR 檔案
```

### 2. Eclipse 整合建置

#### 2.1 在 Eclipse 中建置

**方法一：Maven 目標執行**
```
Right-click on project → Run As → Maven build...
Goals: clean package
Profiles: development
```

**方法二：Run Configurations**
```
Run → Run Configurations → Maven Build
Name: Build WebApp
Base directory: ${workspace_loc:/my-first-webapp}
Goals: clean package
Profiles: development
```

#### 2.2 自動建置配置

```
Project → Properties → Builders
☑ Maven Project Builder (啟用)

Configure Workspace Settings:
Window → Preferences → Maven
☑ Automatically update Maven projects
☑ Download repository index updates on startup
```

### 3. Tomcat 部署

#### 3.1 Eclipse 內建 Tomcat 部署

**步驟 1：添加專案到伺服器**
```
Servers 視圖 → 右鍵 → Add and Remove...
選擇 my-first-webapp → Add → Finish
```

**步驟 2：啟動伺服器**
```
Servers 視圖 → 選擇 Tomcat → 右鍵 → Start
或直接點擊 Start 按鈕
```

**步驟 3：訪問應用程式**
```
URL: http://localhost:8080/my-first-webapp
```

#### 3.2 手動部署到 Tomcat

```bash
# 複製 WAR 檔案到 Tomcat webapps 目錄
cp target/my-first-webapp.war $CATALINA_HOME/webapps/

# 或者解壓縮後複製
cd target
unzip my-first-webapp.war -d my-first-webapp
cp -r my-first-webapp $CATALINA_HOME/webapps/

# 啟動 Tomcat
$CATALINA_HOME/bin/startup.sh
```

#### 3.3 Maven Tomcat 插件部署

```bash
# 使用 Maven Tomcat 插件啟動
mvn tomcat7:run

# 在背景執行
mvn tomcat7:run -Dmaven.tomcat.fork=true

# 停止
mvn tomcat7:shutdown
```

---

## 除錯與測試

### 1. Eclipse 除錯配置

#### 1.1 遠端除錯設定

**步驟 1：設定 Tomcat 除錯參數**
```bash
# 在 CATALINA_OPTS 中添加除錯參數
export CATALINA_OPTS="-Xdebug -Xrunjdwp:transport=dt_socket,address=8000,server=y,suspend=n"
```

**步驟 2：Eclipse 遠端除錯配置**
```
Run → Debug Configurations → Remote Java Application
Name: WebApp Debug
Project: my-first-webapp
Connection Type: Standard (Socket Attach)
Host: localhost
Port: 8000
```

#### 1.2 斷點設置與除錯

```java
// 在控制器方法中設置斷點
@GetMapping("/")
public String home(Model model, HttpServletRequest request) {
    logger.info("訪問首頁 - IP: {}", request.getRemoteAddr()); // 設置斷點
    
    // 檢查變數值
    String userAgent = request.getHeader("User-Agent");
    model.addAttribute("userAgent", userAgent);
    
    return "index"; // 設置斷點
}
```

### 2. 單元測試實作

#### 2.1 控制器測試

```java name=HomeControllerTest.java url=file:///C:/Eclipse-Workspace/my-first-webapp/src/test/java/com/tcchang70/webapp/controller/HomeControllerTest.java
package com.tcchang70.webapp.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.ui.Model;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * HomeController 單元測試
 * 
 * @author TCChang70
 */
@ExtendWith(MockitoExtension.class)
class HomeControllerTest {

    @InjectMocks
    private HomeController homeController;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpSession session;

    @Mock
    private Model model;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(homeController).build();
    }

    @Test
    void testHome() throws Exception {
        // 測試首頁訪問
        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(view().name("index"))
                .andExpect(model().attributeExists("currentTime"))
                .andExpect(model().attributeExists("version"))
                .andExpect(model().attributeExists("developer"));
    }

    @Test
    void testSystemInfo() throws Exception {
        // 測試系統資訊 API
        mockMvc.perform(get("/api/info"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$.applicationName").value("My First Web Application"))
                .andExpect(jsonPath("$.version").value("1.0.0-SNAPSHOT"))
                .andExpect(jsonPath("$.developer").value("TCChang70"));
    }

    @Test
    void testHealthCheck() throws Exception {
        // 測試健康檢查端點
        mockMvc.perform(get("/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    void testHomeWithMocks() {
        // 設定 Mock 行為
        when(request.getRemoteAddr()).thenReturn("127.0.0.1");
        when(request.getSession()).thenReturn(session);
        when(session.getId()).thenReturn("TEST_SESSION_ID");

        // 執行測試
        String viewName = homeController.home(model, request);

        // 驗證結果
        assertEquals("index", viewName);
        verify(model, atLeastOnce()).addAttribute(eq("currentTime"), any());
        verify(model).addAttribute("version", "1.0.0-SNAPSHOT");
        verify(model).addAttribute("developer", "TCChang70");
    }
}
```

#### 2.2 整合測試

```java name=WebAppIntegrationTest.java url=file:///C:/Eclipse-Workspace/my-first-webapp/src/test/java/com/tcchang70/webapp/WebAppIntegrationTest.java
package com.tcchang70.webapp;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Web 應用程式整合測試
 * 
 * @author TCChang70
 */
@SpringJUnitConfig
@WebAppConfiguration
@ContextConfiguration(locations = {
    "file:src/main/webapp/WEB-INF/spring-context.xml",
    "file:src/main/webapp/WEB-INF/spring-mvc-config.xml"
})
class WebAppIntegrationTest {

    @Autowired
    private WebApplicationContext wac;

    @Test
    void testApplicationContext() {
        // 測試應用程式上下文是否正確載入
        assertNotNull(wac);
        assertTrue(wac.getBeansOfType(HomeController.class).size() > 0);
    }

    @Test
    void testWebAppEndpoints() throws Exception {
        MockMvc mockMvc = MockMvcBuilders.webAppContextSetup(wac).build();

        // 測試首頁
        mockMvc.perform(get("/"))
                .andExpect(status().isOk());

        // 測試 API 端點
        mockMvc.perform(get("/api/info"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"));

        // 測試健康檢查
        mockMvc.perform(get("/health"))
                .andExpect(status().isOk());
    }
}
```

### 3. 效能測試

```bash name=performance-test.sh
#!/bin/bash

echo "======================================"
echo "  Web 應用程式效能測試"
echo "======================================"

# 基本配置
URL="http://localhost:8080/my-first-webapp"
CONCURRENT_USERS=10
TOTAL_REQUESTS=100

# 檢查應用程式是否運行
echo "檢查應用程式狀態..."
curl -s "$URL/health" | grep -q "UP" || {
    echo "❌ 應用程式未運行，請先啟動服務"
    exit 1
}

echo "✅ 應用程式運行正常"

# 使用 Apache Bench 進行壓力測試
echo "開始效能測試..."
echo "URL: $URL"
echo "併發用戶數: $CONCURRENT_USERS"
echo "總請求數: $TOTAL_REQUESTS"

ab -n $TOTAL_REQUESTS -c $CONCURRENT_USERS "$URL/" > performance-report.txt

# 分析結果
echo "======================================"
echo "  測試結果摘要"
echo "======================================"

grep "Time taken for tests" performance-report.txt
grep "Requests per second" performance-report.txt
grep "Time per request" performance-report.txt
grep "Failed requests