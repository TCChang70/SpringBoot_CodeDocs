package com.example.demo.dto;

public class ApiResponse<T> {

 private boolean success;
 private String  message;
 private T       data;

 public static <T> ApiResponse<T> ok(T data) {
     ApiResponse<T> res = new ApiResponse<>();
     res.success = true;
     res.message = "success";
     res.data    = data;
     return res;
 }

 public static <T> ApiResponse<T> ok(String message, T data) {
     ApiResponse<T> res = new ApiResponse<>();
     res.success = true;
     res.message = message;
     res.data    = data;
     return res;
 }

 public boolean isSuccess() { return success; }
 public String  getMessage() { return message; }
 public T       getData()    { return data; }
}
