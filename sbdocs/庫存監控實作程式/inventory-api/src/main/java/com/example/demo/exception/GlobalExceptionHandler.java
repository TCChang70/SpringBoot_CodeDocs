package com.example.demo.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

 // 資源不存在 → 404
 @ExceptionHandler(ResourceNotFoundException.class)
 public ResponseEntity<Map<String, Object>> handleNotFound(ResourceNotFoundException ex) {
     return buildError(HttpStatus.NOT_FOUND, "NOT_FOUND", ex.getMessage());
 }

 // 業務規則違反 → 400
 @ExceptionHandler(IllegalArgumentException.class)
 public ResponseEntity<Map<String, Object>> handleBadRequest(IllegalArgumentException ex) {
     return buildError(HttpStatus.BAD_REQUEST, "BAD_REQUEST", ex.getMessage());
 }

 // Bean Validation 錯誤 → 400
 @ExceptionHandler(MethodArgumentNotValidException.class)
 public ResponseEntity<Map<String, Object>> handleValidation(MethodArgumentNotValidException ex) {
     Map<String, String> fieldErrors = new HashMap<>();
     for (FieldError fe : ex.getBindingResult().getFieldErrors()) {
         fieldErrors.put(fe.getField(), fe.getDefaultMessage());
     }
     Map<String, Object> body = new HashMap<>();
     body.put("success", false);
     body.put("errorCode", "VALIDATION_ERROR");
     body.put("errors", fieldErrors);
     return ResponseEntity.badRequest().body(body);
 }

 private ResponseEntity<Map<String, Object>> buildError(
         HttpStatus status, String errorCode, String message) {
     Map<String, Object> body = new HashMap<>();
     body.put("success", false);
     body.put("errorCode", errorCode);
     body.put("message", message);
     return ResponseEntity.status(status).body(body);
 }
}
