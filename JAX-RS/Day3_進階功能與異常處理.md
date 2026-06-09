# Day 3 — 進階功能：Filter、Interceptor 與異常處理

> **學習時數**：6–8 小時  
> **前置要求**：完成 Day 2、CRUD API 可正常運作

---

## 學習目標

完成本日學習後，你將能夠：

1. 實作 `ContainerRequestFilter` 處理跨域（CORS）與 Logging
2. 實作 `ContainerResponseFilter` 統一加入回應 Header
3. 撰寫 `ExceptionMapper` 統一處理例外
4. 使用 `@NameBinding` 建立自訂標注 Filter
5. 實作 `ReaderInterceptor` / `WriterInterceptor` 攔截請求/回應 Body
6. 設計良好的全域錯誤回應格式

---

## 第一節：JAX-RS Filter 架構

### 1.1 Filter 請求/回應生命週期

```
Client
  │ HTTP Request
  ▼
[ContainerRequestFilter]    ← 可在此做認證、Logging、CORS
  │
  ▼
[JAX-RS Resource Method]    ← 業務邏輯
  │
  ▼
[ContainerResponseFilter]   ← 可在此加 Header、壓縮
  │
  ▼
Client HTTP Response
```

### 1.2 Request Filter — CORS 處理

```java
package com.example.filter;

import jakarta.ws.rs.container.*;
import jakarta.ws.rs.ext.Provider;
import java.io.IOException;

/**
 * CORS 過濾器 — 允許跨網域請求
 * 實際正式環境應限制 origin，不可用 "*"
 */
@Provider
@PreMatching   // 在路徑比對前先執行（處理 OPTIONS preflight）
public class CorsFilter implements ContainerRequestFilter,
                                    ContainerResponseFilter {

    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {
        // 處理 OPTIONS preflight 請求
        if ("OPTIONS".equalsIgnoreCase(requestContext.getMethod())) {
            requestContext.abortWith(
                javax.ws.rs.core.Response.ok().build()
            );
        }
    }

    @Override
    public void filter(ContainerRequestContext requestContext,
                       ContainerResponseContext responseContext) throws IOException {
        responseContext.getHeaders().add("Access-Control-Allow-Origin",  "*");
        responseContext.getHeaders().add("Access-Control-Allow-Methods", 
                                         "GET, POST, PUT, PATCH, DELETE, OPTIONS");
        responseContext.getHeaders().add("Access-Control-Allow-Headers", 
                                         "Content-Type, Authorization, X-Requested-With");
        responseContext.getHeaders().add("Access-Control-Expose-Headers", 
                                         "X-Total-Count, Location");
    }
}
```

### 1.3 Request Filter — 存取日誌

```java
package com.example.filter;

import jakarta.ws.rs.container.*;
import jakarta.ws.rs.ext.Provider;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.logging.Logger;

@Provider
public class RequestLoggingFilter implements ContainerRequestFilter {

    private static final Logger LOG = Logger.getLogger(RequestLoggingFilter.class.getName());

    @Override
    public void filter(ContainerRequestContext ctx) throws IOException {
        LOG.info(String.format("[%s] %s %s | IP: %s",
            LocalDateTime.now(),
            ctx.getMethod(),
            ctx.getUriInfo().getRequestUri(),
            // 從 X-Forwarded-For 或 remoteAddr 取客戶端 IP
            ctx.getHeaderString("X-Forwarded-For") != null
                ? ctx.getHeaderString("X-Forwarded-For")
                : "unknown"
        ));
    }
}
```

### 1.4 Response Filter — 加入自訂 Header

```java
package com.example.filter;

import jakarta.ws.rs.container.*;
import jakarta.ws.rs.ext.Provider;
import java.io.IOException;

@Provider
public class ResponseHeaderFilter implements ContainerResponseFilter {

    @Override
    public void filter(ContainerRequestContext req,
                       ContainerResponseContext res) throws IOException {
        res.getHeaders().add("X-API-Version", "1.0");
        res.getHeaders().add("X-Powered-By",  "Jersey JAX-RS");
    }
}
```

---

## 第二節：@NameBinding 自訂 Filter 標注

當 Filter 不需要套用在所有端點時，使用 `@NameBinding` 限制範圍：

### 2.1 定義自訂標注

```java
package com.example.annotation;

import jakarta.ws.rs.NameBinding;
import java.lang.annotation.*;

/**
 * 套用此標注的端點將執行執行時間日誌
 */
@NameBinding
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface Timed {}
```

### 2.2 實作對應 Filter

```java
package com.example.filter;

import com.example.annotation.Timed;
import jakarta.ws.rs.container.*;
import jakarta.ws.rs.ext.Provider;
import java.io.IOException;
import java.util.logging.Logger;

@Provider
@Timed
public class TimingFilter implements ContainerRequestFilter,
                                      ContainerResponseFilter {

    private static final Logger LOG = Logger.getLogger(TimingFilter.class.getName());
    private static final String START_TIME = "startTime";

    @Override
    public void filter(ContainerRequestContext ctx) throws IOException {
        ctx.setProperty(START_TIME, System.currentTimeMillis());
    }

    @Override
    public void filter(ContainerRequestContext req,
                       ContainerResponseContext res) throws IOException {
        Long start = (Long) req.getProperty(START_TIME);
        if (start != null) {
            long elapsed = System.currentTimeMillis() - start;
            LOG.info(String.format("[TIMING] %s %s → %d ms",
                req.getMethod(),
                req.getUriInfo().getPath(),
                elapsed));
            res.getHeaders().add("X-Response-Time", elapsed + "ms");
        }
    }
}
```

### 2.3 在 Resource 上使用

```java
@Path("/employees")
public class EmployeeResource {

    @GET
    @Timed        // 只有這個方法會計時
    public Response getAll() { ... }

    @POST         // 這個方法不計時
    public Response create(Employee emp, @Context UriInfo info) { ... }
}
```

---

## 第三節：統一例外處理

### 3.1 自訂例外類別

```java
package com.example.exception;

/**
 * 資源不存在例外
 */
public class ResourceNotFoundException extends RuntimeException {
    private final String resourceType;
    private final Object resourceId;

    public ResourceNotFoundException(String resourceType, Object resourceId) {
        super(resourceType + " not found: " + resourceId);
        this.resourceType = resourceType;
        this.resourceId   = resourceId;
    }

    public String getResourceType() { return resourceType; }
    public Object getResourceId()   { return resourceId; }
}
```

```java
package com.example.exception;

/**
 * 輸入驗證例外
 */
public class ValidationException extends RuntimeException {
    private final String field;

    public ValidationException(String field, String reason) {
        super("Validation failed for [" + field + "]: " + reason);
        this.field = field;
    }

    public String getField() { return field; }
}
```

### 3.2 錯誤回應 DTO

```java
package com.example.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDateTime;

/**
 * 統一錯誤回應格式
 */
public class ErrorResponse {

    private int    status;
    private String error;
    private String message;
    private String path;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime timestamp = LocalDateTime.now();

    public ErrorResponse(int status, String error, String message, String path) {
        this.status  = status;
        this.error   = error;
        this.message = message;
        this.path    = path;
    }

    // getters...
    public int    getStatus()    { return status; }
    public String getError()     { return error; }
    public String getMessage()   { return message; }
    public String getPath()      { return path; }
    public LocalDateTime getTimestamp() { return timestamp; }
}
```

### 3.3 ExceptionMapper 實作

```java
package com.example.exception;

import com.example.model.ErrorResponse;
import jakarta.ws.rs.core.*;
import jakarta.ws.rs.ext.*;

/**
 * ResourceNotFoundException → 404
 */
@Provider
public class ResourceNotFoundExceptionMapper
        implements ExceptionMapper<ResourceNotFoundException> {

    @Context
    private UriInfo uriInfo;

    @Override
    public Response toResponse(ResourceNotFoundException ex) {
        ErrorResponse err = new ErrorResponse(
            404, "Not Found", ex.getMessage(),
            uriInfo.getRequestUri().getPath()
        );
        return Response.status(Response.Status.NOT_FOUND)
                       .entity(err)
                       .type(MediaType.APPLICATION_JSON)
                       .build();
    }
}
```

```java
package com.example.exception;

import com.example.model.ErrorResponse;
import jakarta.ws.rs.core.*;
import jakarta.ws.rs.ext.*;

/**
 * ValidationException → 400
 */
@Provider
public class ValidationExceptionMapper
        implements ExceptionMapper<ValidationException> {

    @Context
    private UriInfo uriInfo;

    @Override
    public Response toResponse(ValidationException ex) {
        ErrorResponse err = new ErrorResponse(
            400, "Bad Request", ex.getMessage(),
            uriInfo.getRequestUri().getPath()
        );
        return Response.status(Response.Status.BAD_REQUEST)
                       .entity(err)
                       .type(MediaType.APPLICATION_JSON)
                       .build();
    }
}
```

```java
package com.example.exception;

import com.example.model.ErrorResponse;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.*;
import jakarta.ws.rs.ext.*;
import java.util.logging.*;

/**
 * 全域捕獲 — 未預期的 Exception → 500
 */
@Provider
public class GlobalExceptionMapper
        implements ExceptionMapper<Throwable> {

    private static final Logger LOG = Logger.getLogger(GlobalExceptionMapper.class.getName());

    @Context
    private UriInfo uriInfo;

    @Override
    public Response toResponse(Throwable ex) {
        // 如果是 JAX-RS 的 WebApplicationException，直接傳遞
        if (ex instanceof WebApplicationException) {
            return ((WebApplicationException) ex).getResponse();
        }

        LOG.log(Level.SEVERE, "Unhandled exception", ex);

        ErrorResponse err = new ErrorResponse(
            500, "Internal Server Error",
            "An unexpected error occurred",     // 不要洩漏堆疊給客戶端
            uriInfo.getRequestUri().getPath()
        );
        return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                       .entity(err)
                       .type(MediaType.APPLICATION_JSON)
                       .build();
    }
}
```

### 3.4 在 Resource 中使用自訂例外

```java
@GET
@Path("/{id}")
public Response getById(@PathParam("id") int id) {
    Employee emp = DB.get(id);
    if (emp == null) {
        // 不再手動建構 Response，直接丟出例外
        throw new ResourceNotFoundException("Employee", id);
    }
    return Response.ok(emp).build();
}

@POST
public Response create(Employee emp, @Context UriInfo uriInfo) {
    if (emp.getName() == null || emp.getName().isBlank()) {
        throw new ValidationException("name", "Name must not be empty");
    }
    // ...
}
```

---

## 第四節：ReaderInterceptor 與 WriterInterceptor

### 4.1 ReaderInterceptor — 讀取請求 Body

常見用途：計算請求體大小、解密請求 Body。

```java
package com.example.interceptor;

import jakarta.ws.rs.ext.*;
import java.io.IOException;
import java.util.logging.Logger;

@Provider
public class RequestBodySizeInterceptor implements ReaderInterceptor {

    private static final Logger LOG = Logger.getLogger(RequestBodySizeInterceptor.class.getName());

    @Override
    public Object aroundReadFrom(ReaderInterceptorContext ctx)
            throws IOException, jakarta.ws.rs.WebApplicationException {

        // 讀取前記錄
        LOG.info("[BODY IN] Content-Type: " + ctx.getMediaType());

        // 繼續執行（必須呼叫）
        Object entity = ctx.proceed();

        LOG.info("[BODY IN] Entity type: " + (entity != null ? entity.getClass().getSimpleName() : "null"));
        return entity;
    }
}
```

### 4.2 WriterInterceptor — 寫出回應 Body

常見用途：壓縮回應、加密輸出。

```java
package com.example.interceptor;

import jakarta.ws.rs.ext.*;
import java.io.IOException;
import java.util.logging.Logger;

@Provider
public class ResponseLoggingInterceptor implements WriterInterceptor {

    private static final Logger LOG = Logger.getLogger(ResponseLoggingInterceptor.class.getName());

    @Override
    public void aroundWriteTo(WriterInterceptorContext ctx)
            throws IOException, jakarta.ws.rs.WebApplicationException {

        LOG.info("[BODY OUT] Writing entity: " + ctx.getEntity());
        ctx.proceed();  // 必須呼叫，否則回應不會寫出
    }
}
```

---

## 第五節：內容協商 (Content Negotiation)

### 5.1 同一端點支援多種格式

```java
@GET
@Path("/{id}")
@Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
public Response getById(@PathParam("id") int id) {
    Employee emp = DB.get(id);
    if (emp == null) throw new ResourceNotFoundException("Employee", id);
    return Response.ok(emp).build();
    // 客戶端 Accept: application/xml → 回傳 XML
    // 客戶端 Accept: application/json → 回傳 JSON
}
```

**Employee 需加上 JAXB 標注支援 XML：**
```java
import jakarta.xml.bind.annotation.XmlRootElement;

@XmlRootElement   // 支援 XML 序列化
public class Employee {
    // ...
}
```

```bash
# 測試：指定 Accept 要求 XML
curl -H "Accept: application/xml" http://localhost:8080/jaxrs-demo/api/employees/1
```

---

## Day 3 評估測驗（共 10 題）

---

**題目 1**（單選）`@PreMatching` 標注在 `ContainerRequestFilter` 上的效果是？

- A. Filter 只在 POST 方法執行
- B. **Filter 在 JAX-RS 路徑比對之前執行** ✓
- C. Filter 優先於其他所有 Filter
- D. Filter 只處理 OPTIONS 方法

---

**題目 2**（單選）`ExceptionMapper<T>` 的 `toResponse()` 方法用途是？

- A. 把 Response 轉換成例外
- B. **把捕獲到的例外轉換成 HTTP Response** ✓
- C. 攔截請求 Body
- D. 驗證輸入資料

---

**題目 3**（單選）處理未預期的伺服器錯誤（如 `NullPointerException`），應回傳哪個狀態碼？

- A. 400
- B. 404
- C. 401
- D. **500** ✓

---

**題目 4**（單選）`@NameBinding` 的主要用途是？

- A. 讓 Filter 只攔截 POST 請求
- B. **讓 Filter 只套用在加了自訂標注的 Resource/方法上** ✓
- C. 為 Resource 定義別名路徑
- D. 設定依賴注入綁定名稱

---

**題目 5**（單選）在 `WriterInterceptor.aroundWriteTo()` 中，若忘記呼叫 `ctx.proceed()`，會發生什麼？

- A. 程式拋出 IllegalStateException
- B. 回應狀態碼自動變成 500
- C. **回應 Body 不會被寫出，客戶端收到空回應** ✓
- D. 攔截器自動略過

---

**題目 6**（是非）一個類別可以同時實作 `ContainerRequestFilter` 和 `ContainerResponseFilter`。

**答：是（True）** ✓

---

**題目 7**（單選）`ContainerRequestContext.abortWith(Response)` 的效果是？

- A. 記錄錯誤並繼續執行
- B. **中斷請求處理，直接回傳指定的 Response** ✓
- C. 把請求轉發給另一個端點
- D. 設定請求的優先級

---

**題目 8**（單選）為防止安全資訊洩漏，`GlobalExceptionMapper` 在回傳 500 錯誤時，應注意什麼？

- A. 一定要包含完整 Stack Trace
- B. 必須用 XML 格式
- C. **不應在回應中包含例外詳細訊息或 Stack Trace** ✓
- D. 必須重新拋出例外

---

**題目 9**（填空）在 CORS Filter 中，允許前端 JavaScript 讀取自訂回應 Header（如 `X-Total-Count`），需設定哪個 Header？  
答：**`Access-Control-Expose-Headers`**

---

**題目 10**（簡答）解釋 `ContainerRequestFilter`、`ReaderInterceptor` 和 `ExceptionMapper` 三者的執行時機差異。

**參考答案：**
- `ContainerRequestFilter`：在請求 Body 被讀取**之前**執行，通常用於認證、日誌、CORS。
- `ReaderInterceptor`：包裹在請求 Body **反序列化**過程中，可攔截、修改原始 Body 內容。
- `ExceptionMapper`：當資源方法或其他組件拋出例外後**才**執行，負責將例外轉換為適當的 HTTP 回應。

---

## Day 3 實作題目

### 實作一：全域例外處理系統

**需求：**
1. 建立 `ResourceNotFoundException`、`ValidationException` 自訂例外類別
2. 建立 `ErrorResponse` DTO（包含 `status`、`error`、`message`、`path`、`timestamp`）
3. 建立對應的 `ExceptionMapper`（NOT_FOUND → 404、Validation → 400、Throwable → 500）
4. 使用 Postman 測試：呼叫不存在的 ID，確認回傳 JSON 格式的 404

**驗收標準：**
```json
// GET /api/employees/999
// HTTP 404
{
  "status": 404,
  "error": "Not Found",
  "message": "Employee not found: 999",
  "path": "/jaxrs-demo/api/employees/999",
  "timestamp": "2026-03-14 10:00:00"
}
```

---

### 實作二：CORS + Logging Filter

**需求：**
1. 建立 `CorsFilter` 允許跨域請求
2. 建立 `RequestLoggingFilter` 記錄每個請求的方法、路徑、時間
3. 建立 `ResponseHeaderFilter` 在所有回應加入 `X-API-Version: 1.0`
4. 用瀏覽器的 DevTools 驗證 CORS Header 存在

---

### 實作三：@Timed 計時 Filter

**需求：**
1. 建立 `@Timed` 自訂 `@NameBinding` 標注
2. 建立 `TimingFilter` 計算端點執行時間
3. 在 `getAll()` 端點加上 `@Timed`，其他端點不加
4. 確認只有 `GET /api/employees` 的回應包含 `X-Response-Time` Header

---

## 延伸挑戰（選做）

實作 **請求體大小限制** Filter：
- 若請求的 `Content-Length` 超過 1MB（1,048,576 bytes），回傳 `413 Payload Too Large`
- 在 `ContainerRequestFilter` 中實作，對 POST、PUT 方法有效

---

*Day 3 完成 ✓ → 繼續 [Day 4](./Day4_資料庫整合與JPA.md)*
