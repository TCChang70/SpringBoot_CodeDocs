# Spring Web MVC — Java 注解設定完整學習文件

> **適合程度**：有 Java 基礎、初次接觸 Spring MVC 的學習者
> **學習目標**：完全不使用 XML，以 Java `@Configuration` 類別完成 Spring MVC 的所有設定
> **與 XML 版本的差異**：`web.xml` → `WebAppInitializer.java`；`*-servlet.xml` → `MvcConfig.java`

---

## 目錄

1. [Spring MVC 核心概念](#1-spring-mvc-核心概念)
2. [請求流程圖解](#2-請求流程圖解)
3. [XML vs 注解設定對照](#3-xml-vs-注解設定對照)
4. [專案建立步驟](#4-專案建立步驟)
5. [設定類別詳解](#5-設定類別詳解)
6. [Controller 注解大全](#6-controller-注解大全)
7. [常見錯誤與陷阱](#7-常見錯誤與陷阱)
8. [練習題](#8-練習題)

---

## 1. Spring MVC 核心概念

### 什麼是 Spring MVC？

Spring MVC（**Model-View-Controller**）是 Spring Framework 提供的 Web 框架，用於建構 Java Web 應用程式。

| 角色 | 英文 | 負責什麼 |
|------|------|----------|
| 模型 | Model | 存放資料與業務邏輯 |
| 視圖 | View | 呈現畫面（JSP、HTML） |
| 控制器 | Controller | 接收請求、處理邏輯、回傳結果 |

### 核心元件：DispatcherServlet

**DispatcherServlet**（前端控制器 Front Controller）是整個 Spring MVC 的入口點。

- 所有 HTTP 請求都先通過 DispatcherServlet
- 它讀取設定類別，將請求分派給對應的 Controller
- 取得 Controller 回傳的 View 名稱後，交由 ViewResolver 渲染畫面

---

## 2. 請求流程圖解

```
瀏覽器發出請求
      │
      ▼
 DispatcherServlet   ← 由 WebAppInitializer 以程式碼方式註冊
      │
      ▼
 HandlerMapping      ← 找到對應的 @Controller 方法
      │
      ▼
  Controller         ← 執行業務邏輯，回傳 View 名稱
      │
      ▼
 ViewResolver        ← 在 /WEB-INF/views/ 找到 .jsp 檔（由 MvcConfig 設定）
      │
      ▼
   View (JSP)        ← 渲染 HTML 回應給瀏覽器
```

---

## 3. XML vs 注解設定對照

| XML 設定方式 | Java 注解設定方式 | 說明 |
|-------------|-----------------|------|
| `web.xml` | `WebAppInitializer.java` | 註冊 DispatcherServlet、設定 URL 攔截 |
| `*-servlet.xml` | `MvcConfig.java` | Component Scan、ViewResolver、MVC 設定 |
| `<context:component-scan>` | `@ComponentScan("demo.springmvc")` | 掃描 Controller 所在 package |
| `<mvc:annotation-driven/>` | `@EnableWebMvc` | 啟用注解驅動（@RequestMapping 等） |
| `<bean class="InternalResourceViewResolver">` | `@Bean` 方法回傳 ViewResolver | 設定 JSP 前綴與後綴 |
| `<mvc:default-servlet-handler/>` | `configureDefaultServletHandling()` | 允許靜態資源通過 |

> **優點**：Java 設定可享受 IDE 自動補全、編譯期型別檢查，比 XML 更不易打錯。

---

## 4. 專案建立步驟

### Step 1 — 建立 Maven Web 專案

在 Eclipse / IntelliJ 中建立 **Maven Web Project**，確認專案結構如下（**不需要 `web.xml` 和 `*-servlet.xml`**）：

```
src/
  main/
    java/
      demo/springmvc/
        config/
          WebAppInitializer.java    ← 取代 web.xml
          MvcConfig.java            ← 取代 *-servlet.xml
        HelloWorldController.java
    webapp/
      WEB-INF/
        views/
          helloworld.jsp
pom.xml
```

---

### Step 2 — 設定 pom.xml 依賴

```xml
<dependencies>
    <!-- Spring MVC 核心（包含 spring-core、spring-beans、spring-context） -->
    <dependency>
        <groupId>org.springframework</groupId>
        <artifactId>spring-webmvc</artifactId>
        <version>5.2.11.RELEASE</version>
    </dependency>

    <!-- Servlet API（provided：由應用伺服器提供，不打包進 WAR） -->
    <dependency>
        <groupId>javax.servlet</groupId>
        <artifactId>javax.servlet-api</artifactId>
        <version>4.0.1</version>
        <scope>provided</scope>
    </dependency>

    <!-- JSP 標準標籤庫 -->
    <dependency>
        <groupId>javax.servlet</groupId>
        <artifactId>jstl</artifactId>
        <version>1.2</version>
    </dependency>

    <!-- 單元測試（可選） -->
    <dependency>
        <groupId>junit</groupId>
        <artifactId>junit</artifactId>
        <version>4.11</version>
        <scope>test</scope>
    </dependency>
</dependencies>
```

> **與 XML 版本的差異**：需額外加入 `javax.servlet-api`（scope=provided），因為 Java 設定類別需要繼承 Servlet API 的類別，而 XML 版本的 web.xml 不需要在 Java 程式中引用 Servlet API。

---

## 5. 設定類別詳解

### 5.1 WebAppInitializer.java — 取代 web.xml

`AbstractAnnotationConfigDispatcherServletInitializer` 是 Spring 提供的便捷基礎類別，Servlet 容器（Tomcat）啟動時會自動偵測並執行它，**完全不需要 web.xml**。

```java
package demo.springmvc.config;

import org.springframework.web.servlet.support.AbstractAnnotationConfigDispatcherServletInitializer;

public class WebAppInitializer
        extends AbstractAnnotationConfigDispatcherServletInitializer {

    /**
     * Root Application Context 的設定類別
     * （Service、Repository 等非 Web 的 Bean 放這裡）
     * 目前範例只有 MVC 層，回傳 null 即可。
     */
    @Override
    protected Class<?>[] getRootConfigClasses() {
        return null;
    }

    /**
     * DispatcherServlet 的設定類別
     * 對應原本的 *-servlet.xml
     */
    @Override
    protected Class<?>[] getServletConfigClasses() {
        return new Class<?>[] { MvcConfig.class };  // ← 指向 MvcConfig
    }

    /**
     * DispatcherServlet 攔截的 URL 模式
     * "/" 表示攔截所有請求（等同 web.xml 的 <url-pattern>/</url-pattern>）
     */
    @Override
    protected String[] getServletMappings() {
        return new String[] { "/" };
    }
}
```

**三個方法對應關係**：

| 方法 | 對應 web.xml 的設定 |
|------|-------------------|
| `getRootConfigClasses()` | `<context-param>` + `ContextLoaderListener` |
| `getServletConfigClasses()` | `<servlet>` 中的設定類別 |
| `getServletMappings()` | `<servlet-mapping>` 的 `<url-pattern>` |

---

### 5.2 MvcConfig.java — 取代 *-servlet.xml

```java
package demo.springmvc.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.ViewResolver;
import org.springframework.web.servlet.config.annotation.DefaultServletHandlerConfigurer;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.view.InternalResourceViewResolver;

@Configuration          // 標記此類為 Spring 設定類別（取代 XML beans）
@EnableWebMvc           // 啟用 Spring MVC 注解支援（取代 <mvc:annotation-driven/>）
@ComponentScan("demo.springmvc")  // 掃描 Controller（取代 <context:component-scan>）
public class MvcConfig implements WebMvcConfigurer {

    /**
     * ViewResolver：根據 Controller 回傳的字串組合出 JSP 路徑
     * 取代 XML 中的 <bean class="InternalResourceViewResolver">
     */
    @Bean
    public ViewResolver viewResolver() {
        InternalResourceViewResolver resolver = new InternalResourceViewResolver();
        resolver.setPrefix("/WEB-INF/views/");   // JSP 放置目錄
        resolver.setSuffix(".jsp");              // 自動加上副檔名
        return resolver;
    }

    /**
     * 讓靜態資源（CSS、JS、圖片）不被 DispatcherServlet 攔截
     * 取代 XML 中的 <mvc:default-servlet-handler/>
     */
    @Override
    public void configureDefaultServletHandling(DefaultServletHandlerConfigurer configurer) {
        configurer.enable();
    }
}
```

**ViewResolver 路徑組合範例**：

```
Controller 回傳值 → "helloworld"
prefix           → "/WEB-INF/views/"
suffix           → ".jsp"
實際路徑          → /WEB-INF/views/helloworld.jsp
```

---

## 6. Controller 注解大全

> Controller 本身的寫法與 XML 版本**完全相同**，設定的改變只影響 `config/` 目錄下的類別。

### 6.1 基本 Controller — `@Controller` + `@RequestMapping`

```java
package demo.springmvc;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller                           // 標記此類為 Spring MVC 控制器
public class HelloWorldController {

    @RequestMapping("/hello")         // 處理 /hello 的請求
    public String hello(Model model) {
        model.addAttribute("greeting", "Hello Spring MVC");
        return "helloworld";          // ViewResolver 解析為 /WEB-INF/views/helloworld.jsp
    }
}
```

**helloworld.jsp**

```jsp
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
<head>
    <title>Spring MVC - Hello World</title>
</head>
<body>
    <h1>${greeting}</h1>   <%-- 顯示 Model 中的 greeting 值 --%>
</body>
</html>
```

---

### 6.2 重新導向 — `redirect:`

當方法回傳值加上 `redirect:` 前綴，Spring 會執行 **HTTP 302 重新導向**，而非渲染 View。

```java
import org.springframework.web.bind.annotation.RequestMethod;

@Controller
public class RedirectExampleController {

    @RequestMapping(value = "/redirect", method = RequestMethod.GET)
    public String doRedirect() {
        return "redirect:/hello";   // ← 瀏覽器會被導向到 /hello
    }
}
```

**redirect vs forward 比較**：

| | `redirect:` | `forward:` |
|-|-------------|------------|
| HTTP 狀態碼 | 302 | 無額外請求 |
| 瀏覽器 URL | **會改變** | 不變 |
| 用途 | 跨 URL 跳轉 | 內部轉發（共用 Model） |

---

### 6.3 查詢參數 — `@RequestParam`

從 URL 的 `?key=value` 格式取得參數。

```java
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class RequestParamExampleController {

    // 對應 URL：/user?name=Alice  或  /user（name 預設為 "Guest"）
    @RequestMapping("/user")
    public String userInfo(
            Model model,
            @RequestParam(value = "name", defaultValue = "Guest") String name) {

        model.addAttribute("name", name);

        if ("admin".equals(name)) {
            model.addAttribute("email", "admin@demo.com");
        } else {
            model.addAttribute("email", "Not set");
        }
        return "userInfo";
    }
}
```

**常用屬性說明**：

| 屬性 | 說明 | 範例 |
|------|------|------|
| `value` | URL 參數名稱 | `"name"` |
| `defaultValue` | 若參數不存在時的預設值 | `"Guest"` |
| `required` | 是否必填（預設 true） | `required = false` |

**userInfo.jsp**

```jsp
<body>
    <h2>${name}</h2>
    Email: ${email}
</body>
```

---

### 6.4 路徑變數 — `@PathVariable`

從 URL 路徑本身提取變數，適合 RESTful 風格的 API。

```java
import org.springframework.web.bind.annotation.PathVariable;

@Controller
public class PathVariableExampleController {

    // 對應 URL：/web/fe/default/en/document/8080/spring-mvc
    @RequestMapping("/web/fe/{sitePrefix}/{language}/document/{id}/{naturalText}")
    public String documentView(
            Model model,
            @PathVariable("sitePrefix") String sitePrefix,
            @PathVariable("language") String language,
            @PathVariable("id") Long id,
            @PathVariable("naturalText") String naturalText) {

        model.addAttribute("sitePrefix", sitePrefix);
        model.addAttribute("language", language);
        model.addAttribute("id", id);
        model.addAttribute("naturalText", naturalText);

        String documentName = (id == 8080L) ? "Spring MVC for Beginners" : "Java Tutorial";
        model.addAttribute("documentName", documentName);

        return "documentView";
    }
}
```

**URL 匹配範例**：

```
/web/fe/default/en/document/8080/spring-mvc
              ↑       ↑          ↑      ↑
         sitePrefix language    id  naturalText
```

**documentView.jsp**

```jsp
<body>
    <h3>${documentName}</h3>
    Site Prefix: ${sitePrefix} <br>
    Language: ${language}      <br>
    ID: ${id}                  <br>
    Natural Text: ${naturalText}
</body>
```

---

### 6.5 直接寫回 HTTP 回應 — `@ResponseBody`

加上 `@ResponseBody` 後，回傳值**直接寫入 HTTP 回應 Body**，不再透過 ViewResolver 解析 View。

```java
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class ResponseBodyExampleController {

    @RequestMapping("/saveResult")
    @ResponseBody                       // ← 直接輸出字串，不找 JSP
    public String saveResult() {
        return "saved";                 // 瀏覽器收到純文字 "saved"
    }
}
```

> **進階補充**：在現代 Spring Boot 開發中，通常直接用 `@RestController`（等同於 `@Controller` + `@ResponseBody`）來建構 REST API，回傳 JSON 物件。

---

## 7. 常見錯誤與陷阱

| 錯誤情境 | 原因 | 解法 |
|----------|------|------|
| `404 Not Found` | `@RequestMapping` 路徑打錯，或 `@ComponentScan` package 設定錯誤 | 確認 URL 路徑與 `@ComponentScan` 的 package 名稱 |
| JSP 找不到（白畫面或錯誤） | `MvcConfig` 中 prefix/suffix 設定錯誤，JSP 放錯目錄 | 確認 `setPrefix`/`setSuffix` 路徑組合正確 |
| 靜態資源（CSS/JS）404 | DispatcherServlet 攔截了靜態資源請求 | 在 `MvcConfig` 中 override `configureDefaultServletHandling()` 並呼叫 `configurer.enable()` |
| `@RequestParam` 報錯 | 必填參數不存在時拋出 `MissingServletRequestParameterException` | 加上 `required = false` 或 `defaultValue` |
| redirect 後 Model 資料消失 | redirect 是新的 HTTP 請求，Model 不跨請求保留 | 改用 `RedirectAttributes` 傳遞 Flash Attribute |
| Tomcat 未偵測到 `WebAppInitializer` | 缺少 `javax.servlet-api` 依賴，或 Servlet 容器版本不支援 | 確認 `pom.xml` 加入 `javax.servlet-api`（scope=provided），Tomcat 需 7.0+ |
| `ClassNotFoundException: WebMvcConfigurer` | `spring-webmvc` 版本太舊（5.0 以前 `WebMvcConfigurer` 為抽象類別） | 使用 Spring 5.x 以上；或改繼承 `WebMvcConfigurerAdapter`（已棄用） |

---

## 8. 練習題

### Easy — 建立第一個 Hello World 頁面

**題目**：建立 `WebAppInitializer`、`MvcConfig` 兩個設定類別，並建立一個 `/greet` 路徑，Controller 將 `"Welcome to Spring!"` 放入 Model，顯示在 JSP 上。

<details>
<summary>提示</summary>

1. `WebAppInitializer` 繼承 `AbstractAnnotationConfigDispatcherServletInitializer`
2. `MvcConfig` 加上 `@Configuration`、`@EnableWebMvc`、`@ComponentScan`
3. Controller 中使用 `model.addAttribute("message", "Welcome to Spring!")`
4. JSP 中用 `${message}` 顯示

</details>

<details>
<summary>解答</summary>

```java
// config/WebAppInitializer.java
public class WebAppInitializer
        extends AbstractAnnotationConfigDispatcherServletInitializer {
    @Override
    protected Class<?>[] getRootConfigClasses() { return null; }

    @Override
    protected Class<?>[] getServletConfigClasses() {
        return new Class<?>[] { MvcConfig.class };
    }

    @Override
    protected String[] getServletMappings() { return new String[] { "/" }; }
}
```

```java
// config/MvcConfig.java
@Configuration
@EnableWebMvc
@ComponentScan("demo.springmvc")
public class MvcConfig implements WebMvcConfigurer {
    @Bean
    public ViewResolver viewResolver() {
        InternalResourceViewResolver r = new InternalResourceViewResolver();
        r.setPrefix("/WEB-INF/views/");
        r.setSuffix(".jsp");
        return r;
    }
}
```

```java
// GreetController.java
@Controller
public class GreetController {
    @RequestMapping("/greet")
    public String greet(Model model) {
        model.addAttribute("message", "Welcome to Spring!");
        return "greet";
    }
}
```

```jsp
<%-- /WEB-INF/views/greet.jsp --%>
<body>
    <h1>${message}</h1>
</body>
```

</details>

---

### Medium — 使用 @RequestParam 製作計算機

**題目**：建立 `/calc?a=10&b=5` 路徑，將兩數相加的結果顯示在頁面上。

<details>
<summary>提示</summary>

- 使用 `@RequestParam int a` 和 `@RequestParam int b` 接收參數
- 計算後將結果放入 Model

</details>

<details>
<summary>解答</summary>

```java
@Controller
public class CalcController {
    @RequestMapping("/calc")
    public String calc(
            Model model,
            @RequestParam int a,
            @RequestParam int b) {
        model.addAttribute("result", a + b);
        model.addAttribute("a", a);
        model.addAttribute("b", b);
        return "calcResult";
    }
}
```

```jsp
<%-- /WEB-INF/views/calcResult.jsp --%>
<body>
    <p>${a} + ${b} = ${result}</p>
</body>
```

</details>

---

### Hard — 使用 @PathVariable 設計商品查詢頁

**題目**：設計 `/shop/{category}/{productId}` 路徑，根據 `productId` 判斷商品名稱：
- `1001` → `"Java Book"`
- `1002` → `"Spring Book"`
- 其他 → `"Unknown Product"`

<details>
<summary>解答</summary>

```java
@Controller
public class ShopController {

    @RequestMapping("/shop/{category}/{productId}")
    public String product(
            Model model,
            @PathVariable("category") String category,
            @PathVariable("productId") Long productId) {

        model.addAttribute("category", category);
        model.addAttribute("productId", productId);

        String productName;
        if (productId == 1001L) {
            productName = "Java Book";
        } else if (productId == 1002L) {
            productName = "Spring Book";
        } else {
            productName = "Unknown Product";
        }
        model.addAttribute("productName", productName);

        return "productDetail";
    }
}
```

```jsp
<%-- /WEB-INF/views/productDetail.jsp --%>
<body>
    <h2>Category: ${category}</h2>
    <p>Product ID: ${productId}</p>
    <p>Product Name: ${productName}</p>
</body>
```

**測試 URL**：
- `/shop/books/1001` → Java Book
- `/shop/tech/1002` → Spring Book
- `/shop/misc/9999` → Unknown Product

</details>

---

## 學習筆記摘要

### 設定類別對照

| 類別 | 注解 | 取代的 XML | 說明 |
|------|------|-----------|------|
| `WebAppInitializer` | 繼承 `AbstractAnnotationConfigDispatcherServletInitializer` | `web.xml` | 註冊 DispatcherServlet |
| `MvcConfig` | `@Configuration` | `<beans>` 根元素 | Spring 設定容器 |
| `MvcConfig` | `@EnableWebMvc` | `<mvc:annotation-driven/>` | 啟用 MVC 注解支援 |
| `MvcConfig` | `@ComponentScan` | `<context:component-scan>` | 掃描 Controller |
| `@Bean viewResolver()` | `@Bean` | `<bean class="InternalResourceViewResolver">` | View 路徑解析 |
| `configureDefaultServletHandling()` | 覆寫方法 | `<mvc:default-servlet-handler/>` | 靜態資源放行 |

### Controller 注解對照

| 注解 / 設定 | 用途 | 關鍵字 |
|-------------|------|--------|
| `@Controller` | 標記 Spring MVC 控制器 | Bean 管理 |
| `@RequestMapping` | 對應 URL 路徑到方法 | 路由 Routing |
| `@RequestParam` | 取得 URL 查詢參數 `?key=value` | 查詢字串 Query String |
| `@PathVariable` | 取得 URL 路徑中的變數 `/{id}` | RESTful URL |
| `@ResponseBody` | 直接輸出到 HTTP 回應，不找 View | REST API |
| `redirect:` | HTTP 302 重新導向到另一個 URL | 重定向 |

---

> **現在試試看**：依照本文步驟，建立 `WebAppInitializer` 和 `MvcConfig` 兩個設定類別取代所有 XML，完成 Hello World 並成功在瀏覽器顯示 `"Hello Spring MVC"`，再嘗試完成三道練習題！
