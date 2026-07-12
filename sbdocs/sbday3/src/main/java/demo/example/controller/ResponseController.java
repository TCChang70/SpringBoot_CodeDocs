package demo.example.controller;

import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/response")
public class ResponseController {
    
    // 基本回應
    @GetMapping("/basic")
    public ResponseEntity<String> basicResponse() {
        return ResponseEntity.ok("基本回應");
    }
    
    // 自訂狀態碼
    @GetMapping("/custom-status")
    public ResponseEntity<String> customStatus() {
        return ResponseEntity.status(201).body("自訂狀態碼 201");
    }
    
    // 自訂標頭
    @GetMapping("/custom-headers")
    public ResponseEntity<Map<String, Object>> customHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.add("X-Custom-Header", "自訂標頭值");
        headers.add("X-Request-Time", LocalDateTime.now().toString());
        
        Map<String, Object> body = new HashMap<>();
        body.put("message", "帶有自訂標頭的回應");
        body.put("timestamp", LocalDateTime.now());
        
        return ResponseEntity
                .ok()
                .headers(headers)
                .body(body);
    }
    
    // 完整 ResponseEntity 建構
    @GetMapping("/full")
    public ResponseEntity<Map<String, Object>> fullResponse() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.add("X-Response-Type", "Full");
        
        Map<String, Object> body = new HashMap<>();
        body.put("status", "success");
        body.put("message", "完整 ResponseEntity 回應");
        body.put("data", Map.of("key", "value"));
        body.put("timestamp", LocalDateTime.now());
        
        return ResponseEntity
                .status(HttpStatus.OK)
                .headers(headers)
                .body(body);
    }
    
    // 錯誤回應範例
    @GetMapping("/error")
    public ResponseEntity<Map<String, Object>> errorResponse() {
        Map<String, Object> errorBody = new HashMap<>();
        errorBody.put("error", "Bad Request");
        errorBody.put("message", "請求參數無效");
        errorBody.put("timestamp", LocalDateTime.now());
        
        return ResponseEntity
                .badRequest()
                .body(errorBody);
    }
    
    // 404 回應
    @GetMapping("/not-found")
    public ResponseEntity<Map<String, Object>> notFoundResponse() {
        Map<String, Object> body = new HashMap<>();
        body.put("error", "Not Found");
        body.put("message", "資源不存在");
        
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(body);
    }
    
    // 201 Created + Location 標頭
    @PostMapping("/created")
    public ResponseEntity<Map<String, Object>> createdResponse() {
        Map<String, Object> newResource = Map.of(
            "id", "123",
            "name", "新資源",
            "createdAt", LocalDateTime.now()
        );
        
        HttpHeaders headers = new HttpHeaders();
        headers.add("Location", "/api/response/created/123");
        
        return ResponseEntity
                .created(java.net.URI.create("/api/response/created/123"))
                .headers(headers)
                .body(newResource);
    }
    
    // 204 No Content
    @DeleteMapping("/no-content")
    public ResponseEntity<Void> noContentResponse() {
        // 執行刪除操作...
        return ResponseEntity.noContent().build();
    }
}
