# Swagger / OpenAPI 學習文件（bookstore-api）

> 本文是《[BookStore學習文件](BookStore學習文件.md)》的 **Swagger 主題獨立版**，完整說明
> bookstore-api 如何用 Swagger（OpenAPI 3）從程式碼註解**自動產生互動式 API 文件**。
> 所有內容與專案原始碼一致，可與 `src/` 對照閱讀。

---

## 目錄

1. [Swagger 是什麼](#1-swagger-是什麼)
2. [三個組成部分](#2-三個組成部分)
3. [瀏覽器開啟即有文件](#3-瀏覽器開啟即有文件)
4. [文件是怎麼自動產生的](#4-文件是怎麼自動產生的)
5. [需要修改的檔案（程式碼總覽）](#5-需要修改的檔案程式碼總覽)
6. [常用註解對照](#6-常用註解對照)
7. [在 Swagger UI 直接試打 API](#7-在-swagger-ui-直接試打-api)
8. [新增一個 API 的步驟](#8-新增一個-api-的步驟)
9. [注意事項與常見問題](#9-注意事項與常見問題)
10. [延伸練習](#10-延伸練習)

---

## 1. Swagger 是什麼

開發 REST API 的痛點之一：「文件要另外維護」。程式改了，文件常常忘了更新。

Swagger（OpenAPI 3）的作法是改用**程式碼註解**標記，由框架在執行時**自動產生**一份
可互動的 API 文件——前端或測試人員打開網頁就能直接呼叫 API，不必先讀文件、也不用開 Postman。

- **OpenAPI**：一套描述 API 的開放標準（網址、參數、回應格式都有規範定義）。
- **Swagger**：實作這套標準的工具生態系，本專案用的是 `swagger-core` 與 `swagger-ui`。

---

## 2. 三個組成部分

| 部分 | 角色 | 本專案版本 |
|------|------|-----------|
| **swagger-core**（`swagger-jaxrs2-jakarta`） | 讀取註解，產生 OpenAPI 3 規格 | 2.2.37 |
| **OpenApiResource** | JAX-RS 現成資源類別，提供 `/api/openapi.json\|yaml` 兩個端點 | 隨 swagger-core |
| **swagger-ui（WebJar）** | 純前端靜態網頁，載入規格後渲染成互動介面 | 5.32.0（`org.webjars:swagger-ui`，jar 內附靜態檔） |

> **版本注意**：Tomcat 10 是 `jakarta.*` 命名空間，必須用 **jakarta 版**
> `swagger-jaxrs2-jakarta`。舊的 `swagger-jaxrs2-servlet-initializer-v2` 是 `javax` 版，
> 裝進 Tomcat 10 會無法初始化（找不到 `javax.servlet.ServletContainerInitializer`）。

---

## 3. 瀏覽器開啟即有文件

部署並啟動後，瀏覽器開啟：

| 網址 | 內容 |
|------|------|
| `http://localhost:8080/bookstore-api/swagger-ui/` | 互動式 UI：列出所有 API，可「Try it out」直接呼叫 |
| `http://localhost:8080/bookstore-api/api/openapi.json` | 標準 OpenAPI 規格（JSON） |
| `http://localhost:8080/bookstore-api/api/openapi.yaml` | 標準 OpenAPI 規格（YAML） |

Swagger UI 首頁會顯示每個端點的方法、摘要、參數與回應格式，頁面右上角可切換查看 JSON / YAML 原始規格。

---

## 4. 文件是怎麼自動產生的

```
程式碼註解（@Operation、@Parameter、@ApiResponse、@Schema...）
   │
   ▼
swagger-core 掃描 resourcePackages 指定的套件
   │
   ▼
組出 OpenAPI 3 規格（JSON / YAML）
   │
   ▼
OpenApiResource 端點輸出（/api/openapi.json|yaml）
   │
   ▼
swagger-ui 前端載入規格 → 渲染成互動網頁
```

1. 在 `JaxRsActivator.getClasses()` 註冊 `OpenApiResource`（等同多掛了兩個 REST 端點）。
2. swagger-core 依 `src/main/resources/openapi.yaml` 的 `resourcePackages` 掃描 `controller` 與 `config` 套件。
3. 讀取類別/方法上的註解，組出規格。
4. 每次請求規格都重新產生（`cacheTTL: 0`），改完註解**重新整理頁面**即可看到更新，開發時不必重啟。

---

## 5. 需要修改的檔案（程式碼總覽）

加入 Swagger 需要修改/新增 4 個地方，以下逐一列出完整內容。

### 5.1 `pom.xml` — 加入依賴

在 `<properties>` 宣告版本：

```xml
<jackson.version>2.19.2</jackson.version>
<swagger.version>2.2.37</swagger.version>
```

在 `<dependencies>` 加入（會一併帶入 swagger-annotations、jackson 相關、slf4j 等）：

```xml
<!-- Swagger / OpenAPI 3 -->
<!-- 提供 io.swagger.v3.oas.annotations 註解 + JAX-RS 資源 OpenApiResource（/api/openapi.json|yaml） -->
<dependency>
    <groupId>io.swagger.core.v3</groupId>
    <artifactId>swagger-jaxrs2-jakarta</artifactId>
    <version>${swagger.version}</version>
</dependency>

<!-- Swagger UI（WebJar）：jar 內附 META-INF/resources/webjars/swagger-ui/<版本>/ 靜態資源，
     Tomcat 自動在 /webjars/** 提供，不需要手動抽出或額外 servlet -->
<dependency>
    <groupId>org.webjars</groupId>
    <artifactId>swagger-ui</artifactId>
    <version>5.32.0</version>
</dependency>
```

### 5.2 `src/main/resources/openapi.yaml` — 掃描設定

決定「要掃描哪些套件」。**`config` 套件必須包含**，否則 `JaxRsActivator` 上的
`@OpenAPIDefinition` 不會被讀到，規格會缺少 `info`（標題/版本）資訊。

```yaml
# Swagger / OpenAPI 3 產生設定
# 只掃描 controller 與 config 套件（config 含 @OpenAPIDefinition 的 JaxRsActivator）
# 避免把 /openapi 自身也列進去
resourcePackages:
  - controller
  - config

# 輸出排版
prettyPrint: true

# 快取秒數：0 表示每次呼叫都重新解析（開發方便，正式可改大）
cacheTTL: 0

# 依 OpenAPI 規範重新排序
sortOutput: true
```

### 5.3 `config/JaxRsActivator.java` — 註冊資源 + OpenAPI 定義

完整內容：

```java
package config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.ws.rs.ApplicationPath;
import jakarta.ws.rs.core.Application;
import java.util.Set;

@OpenAPIDefinition(
	     info = @Info(
	        title = "Bookstore SQLite API",
	        version = "1.0.0",
	        description = "書籍管理 REST API（JAX-RS + JPA + SQLite）。所有 API 統一回應格式："
	            + "成功 {\"success\":true,\"data\":...}，失敗 {\"success\":false,\"error\":\"訊息\"}。",
	        contact = @Contact(name = "Bookstore Team")
	        ),
	     servers = {
	             @Server(url = "/mvsqlite0803", description = "本機 mvsqlite0803 部署路徑（context 為 /mvsqlite0803）")
	     },
	     tags = {
	             @Tag(name = "書籍", description = "書籍 CRUD 操作")
	    }
	)
@ApplicationPath("/api")   // 所有 API 前綴：http://localhost:8080/bookstore-api/api/
public class JaxRsActivator extends Application {

    /**
     * 明確註冊 JAX-RS 資源與 Provider，不依賴 classpath 掃描。
     * - BookController：書籍 CRUD 端點
     * - JacksonConfig：JSON 序列化設定
     * - OpenApiResource：Swagger / OpenAPI 文件端點（/api/openapi.json、/api/openapi.yaml）
     */
    @Override
    public Set<Class<?>> getClasses() {
        return Set.of(
            controller.BookController.class,
            JacksonConfig.class,
            io.swagger.v3.jaxrs2.integration.resources.OpenApiResource.class
        );
    }
}
```

**重點：**
- `@OpenAPIDefinition` 設定整份文件的 `info`（title / version / description / contact）與分組 `tags`。
- `getClasses()` 註冊 `OpenApiResource`，等於多掛了 `/api/openapi.json` 與 `/api/openapi.yaml` 兩個端點。

### 5.4 `webapp/swagger-ui/index.html` — UI 入口

Swagger UI 的前端資源（css / js / favicon）**不再手動放進專案**，而是透過 **WebJar** 依賴
（`org.webjars:swagger-ui:5.32.0`）直接取得：

- webjar 的 jar 內含 `META-INF/resources/webjars/swagger-ui/5.32.0/`，Tomcat 會自動把
  jar 內這段資源掛到 `/webjars/**`，所以 `/bookstore-api/webjars/swagger-ui/5.32.0/swagger-ui.css`
  直接就能存取（**不需要額外 servlet 設定**）。
- 仍保留一個自訂 `index.html` 當入口，是因為 webjar 預設的 `swagger-initializer.js`
  寫死連到 `https://petstore.swagger.io/v2/swagger.json`（也不支援 `?url=`），
  必須由我們的頁面把規格指向 `/api/openapi.json`。

`webapp/swagger-ui/` 只保留這一個檔，其餘全部來自 jar：

```html
<!DOCTYPE html>
<html lang="zh-Hant">
<head>
  <meta charset="utf-8">
  <meta name="viewport" content="width=device-width, initial-scale=1">
  <title>Bookstore API — Swagger UI</title>
  <!-- 靜態資源全部來自 swagger-ui WebJar（META-INF/resources/webjars/swagger-ui/5.32.0/） -->
  <link rel="stylesheet" href="../webjars/swagger-ui/5.32.0/swagger-ui.css">
  <link rel="icon" type="image/png" href="../webjars/swagger-ui/5.32.0/favicon-32x32.png">
</head>
<body>
  <div id="swagger-ui"></div>

  <script src="../webjars/swagger-ui/5.32.0/swagger-ui-bundle.js"></script>
  <script src="../webjars/swagger-ui/5.32.0/swagger-ui-standalone-preset.js"></script>

  <script>
    window.onload = function () {
      // 以目前頁面的網址計算 OpenAPI 文件位置：
      // 頁面在 /<context>/swagger-ui/，文件在 /<context>/api/openapi.json
      var specUrl = new URL("../api/openapi.json", window.location.href).href;

      window.ui = SwaggerUIBundle({
        url: specUrl,
        dom_id: "#swagger-ui",
        deepLinking: true,
        presets: [
          SwaggerUIBundle.presets.apis,
          SwaggerUIStandalonePreset
        ],
        layout: "StandaloneLayout"
      });
    };
  </script>
</body>
</html>
```

> **為什麼用 `../webjars/...`？** 頁面位於 `/bookstore-api/swagger-ui/`，`../webjars/...`
> 會解析成 `/bookstore-api/webjars/...`，與 context path 無關都能正確載入。
>
> **換版本注意**：升級 webjar 時要同步改兩處——`pom.xml` 的 `<version>`，以及
> index.html 裡 3 個 `5.32.0` 路徑，否則 404。

---

## 6. 常用註解對照

| 註解 | 掛在哪 | 作用 |
|------|--------|------|
| `@OpenAPIDefinition` | Application 類別 | 整份文件的 title / version / contact |
| `@Tag` | 類別 | 分組名稱與說明 |
| `@Operation` | 方法 | 摘要（summary）、詳細說明（description） |
| `@Parameter` | 方法參數 | 描述參數：`in`（QUERY/PATH）、型別、example |
| `@ApiResponse` | 方法 | 各狀態碼（200/201/400/404）代表的意思 |
| `@Schema` | 實體欄位 | 描述 JSON 欄位：型別、範例、必填、唯讀 |

對照 `BookController` 的實際寫法：

```java
@GET
@Operation(summary = "查詢書籍清單",
           description = "查詢全部書籍，支援分類篩選、價格區間、分頁")
@ApiResponse(responseCode = "200", description = "查詢成功，data 為書籍陣列")
public Response getAll(
        @Parameter(in = ParameterIn.QUERY, description = "分類名稱（不分大小寫）", example = "小說")
        @QueryParam("category") String category, ...) { ... }
```

實體欄位的 `@Schema`（對照 `entity/Book.java`）：

```java
@Id
@GeneratedValue(strategy = GenerationType.IDENTITY)
@Schema(description = "主鍵（自動產生，寫入時不需填）", example = "1", accessMode = Schema.AccessMode.READ_ONLY)
private Long id;

@Column(nullable = false, length = 200)
@Schema(description = "書名", example = "Java 程式設計", requiredMode = Schema.RequiredMode.REQUIRED)
private String title;
```

**`@Schema` 常用屬性：**
- `description`：欄位說明。
- `example`：範例值，Swagger UI 會自動代入。
- `requiredMode = REQUIRED`：標記必填（對應 `@Column(nullable = false)`）。
- `accessMode = READ_ONLY`：標記唯讀（id、createdAt、updatedAt），UI 輸入時不會出現。

---

## 7. 在 Swagger UI 直接試打 API

1. 開啟 `/swagger-ui/`。
2. 展開任一端點（例如 `POST /api/books`）。
3. 按 **Try it out**，編輯 JSON，按 **Execute**。
4. 下方會顯示實際請求網址、HTTP 狀態碼與回應內容。

前端/測試人員不用先讀文件、也不用開 Postman，就能驗證 API 行為。

---

## 8. 新增一個 API 的步驟

只要在方法上加註解，重新整理 `swagger-ui` 就會自動出現，**不需要改任何設定檔**：

```java
@GET
@Path("/{id}")
@Operation(summary = "依 id 查詢單本書籍")
@ApiResponse(responseCode = "200", description = "查詢成功，data 為書籍物件")
public Response getById(@PathParam("id") Long id) { ... }
```

1. 在 `BookController`（或新控制器）加方法，依慣例加上 `@Operation`、`@Parameter`、`@ApiResponse`。
2. 需要在前端顯示欄位說明時，在 `Book` 實體對應欄位加 `@Schema`。
3. 若新增了**新的控制器套件**，記得把它加進 `openapi.yaml` 的 `resourcePackages`。
4. 重新整理 `/swagger-ui/` 即可看到新端點。

---

## 9. 注意事項與常見問題

1. **一定要用 jakarta 版**：Tomcat 10 只能吃 `swagger-jaxrs2-jakarta`；`javax` 版會無法初始化。
2. **`@OpenAPIDefinition` 沒生效？** 檢查 `openapi.yaml` 的 `resourcePackages` 是否有包含
   放 `JaxRsActivator` 的套件（本專案是 `config`）。沒包含時規格會缺少 `info.title`。
3. **改註解後 UI 沒變？** `cacheTTL: 0` 表示不緩存，重新整理頁面即可；若還不行，
   檢查瀏覽器快取（強制重新整理）。
4. **不想自動掃描 / 想限制掃描範圍？** 用 `resourcePackages` / `resourceClasses` 明確指定，
   避免把 `/openapi` 自身或不相干類別列進規格。
5. **swagger-ui 的靜態檔**：bundle.js / css / favicon 都來自 WebJar（`org.webjars:swagger-ui`，
   路徑 `/webjars/swagger-ui/<版本>/`），不要手改也不要手動抽出；只有入口 `index.html` 由我們
   自訂，`specUrl` 與 webjar 路徑都用相對路徑（`../`）才能在不同 context path 下都正確。

---

## 10. 延伸練習

1. **加入 operationId 管理**：在 `@Operation(operationId = "...")` 明確命名，方便前端工具引用。
2. **描述 request body**：`@RequestBody` 的 `content = @Content(schema = @Schema(implementation = Book.class))`
   可讓 UI 顯示「請求本體結構」的輸入範例。
3. **使用 `@Schema` 進階**：`example` 配合 `example` 屬性讓 UI 預填完整 JSON。
4. **正式環境快取**：`cacheTTL` 改成較大秒數，減少每次解析規格的開銷。
5. **串接前端工具**：OpenAPI 規格可匯入 Postman、OpenAPI Generator 等工具自動產生前端 API 程式碼。
