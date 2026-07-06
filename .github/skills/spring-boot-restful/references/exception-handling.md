# 例外處理指南

## 自訂例外類別

```java
// 資源不存在（404）
public class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(String resource, Long id) {
        super(resource + " with id " + id + " not found");
    }
}

// 資料重複（409）
public class DuplicateResourceException extends RuntimeException {
    public DuplicateResourceException(String message) {
        super(message);
    }
}

// 業務規則違反（400）
public class BusinessException extends RuntimeException {
    private final String errorCode;

    public BusinessException(String errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    public String getErrorCode() { return errorCode; }
}
```

## 錯誤回應 DTO

```java
public class ErrorResponse {
    private boolean success = false;
    private String errorCode;
    private String message;
    private LocalDateTime timestamp;
    private Map<String, String> fieldErrors; // Bean Validation 錯誤

    // 建構子、getters、setters
}
```

## 全域例外處理器

```java
@RestControllerAdvice
public class GlobalExceptionHandler {

    // 404 資源不存在
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(ResourceNotFoundException ex) {
        ErrorResponse error = new ErrorResponse("NOT_FOUND", ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }

    // 409 資料衝突
    @ExceptionHandler(DuplicateResourceException.class)
    public ResponseEntity<ErrorResponse> handleDuplicate(DuplicateResourceException ex) {
        ErrorResponse error = new ErrorResponse("DUPLICATE", ex.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
    }

    // 400 Bean Validation 失敗
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException ex) {
        Map<String, String> fieldErrors = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(e ->
            fieldErrors.put(e.getField(), e.getDefaultMessage())
        );
        ErrorResponse error = new ErrorResponse("VALIDATION_FAILED", "請求資料驗證失敗");
        error.setFieldErrors(fieldErrors);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    // 400 @PathVariable / @RequestParam 型別錯誤
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> handleTypeMismatch(MethodArgumentTypeMismatchException ex) {
        ErrorResponse error = new ErrorResponse("TYPE_MISMATCH",
            "參數 '" + ex.getName() + "' 格式錯誤");
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    // 500 未預期例外
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneral(Exception ex) {
        ErrorResponse error = new ErrorResponse("INTERNAL_ERROR", "系統發生內部錯誤");
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }
}
```

## Controller 中使用

```java
@GetMapping("/{id}")
public ResponseEntity<EmployeeResponse> getById(@PathVariable Long id) {
    Employee employee = repository.findById(id)
        .orElseThrow(() -> new ResourceNotFoundException("Employee", id));
    return ResponseEntity.ok(toResponse(employee));
}

@PostMapping
public ResponseEntity<EmployeeResponse> create(@Valid @RequestBody EmployeeRequest request) {
    if (repository.existsByEmail(request.getEmail())) {
        throw new DuplicateResourceException("Email " + request.getEmail() + " 已存在");
    }
    // ... 儲存邏輯
}
```
