package demo.example.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/params")
public class ParameterController {
    
    // @PathVariable：從 URL 路徑取值
    // 範例：GET /api/params/path/123
    @GetMapping("/path/{id}")
    public ResponseEntity<Map<String, Object>> pathVariable(@PathVariable Long id) {
        return ResponseEntity.ok(Map.of(
            "type", "PathVariable",
            "value", id,
            "description", "從 URL 路徑取值"
        ));
    }
    
    // @RequestParam：從 URL 查詢參數取值
    // 範例：GET /api/params/query?name=Alice&age=25
    @GetMapping("/query")
    public ResponseEntity<Map<String, Object>> requestParam(
            @RequestParam String name,
            @RequestParam(defaultValue = "0") int age,
            @RequestParam(required = false) String email) {
        
        return ResponseEntity.ok(Map.of(
            "type", "RequestParam",
            "name", name,
            "age", age,
            "email", email != null ? email : "未提供",
            "description", "從 URL 查詢參數取值"
        ));
    }
    
    // @RequestHeader：從 HTTP 標頭取值
    // 範例：GET /api/params/header（需帶 Authorization 標頭）
    @GetMapping("/header")
    public ResponseEntity<Map<String, Object>> requestHeader(
            @RequestHeader("Authorization") String authorization,
            @RequestHeader(value = "User-Agent", required = false) String userAgent) {
        
        return ResponseEntity.ok(Map.of(
            "type", "RequestHeader",
            "authorization", authorization,
            "userAgent", userAgent != null ? userAgent : "未提供",
            "description", "從 HTTP 標頭取值"
        ));
    }
    
    // @RequestBody：從 HTTP Request Body 取值
    // 範例：POST /api/params/body
    @PostMapping("/body")
    public ResponseEntity<Map<String, Object>> requestBody(@RequestBody Map<String, Object> body) {
        return ResponseEntity.ok(Map.of(
            "type", "RequestBody",
            "receivedData", body,
            "description", "從 HTTP Request Body 取值"
        ));
    }
    
    // 綜合範例：多種參數混合使用
    // 範例：GET /api/params/mixed/123?name=Alice
    @GetMapping("/mixed/{id}")
    public ResponseEntity<Map<String, Object>> mixedParams(
            @PathVariable Long id,
            @RequestParam String name,
            @RequestHeader("Host") String host) {
        
        return ResponseEntity.ok(Map.of(
            "type", "Mixed",
            "pathVariable", id,
            "requestParam", name,
            "header", host,
            "description", "多種參數混合使用"
        ));
    }
}
