以「電商購物車系統 (E-Commerce Shopping Cart)」為例，展示如何透過 **AI 導引（Prompt-Driven Development）** 帶領學員完成從資料庫 Schema 設計、Spring Boot Data JPA API 開發，到前端 SPA（以 Vue 3 / Axios 為例）對接的全流程。

## **階段一：資料庫結構設計與 Entity 自動生成**

### **1\. 給 AI 的提示詞 (Prompt)**

**\[Context\]** 我正在使用 Spring Boot 3.2 與 MySQL 開發電商購物車系統。  
**\[Task\]** 請幫我設計「購物車 (Cart)」與「購物車明細 (CartItem)」的 MySQL DDL，並轉換為 Java Spring Data JPA Entity。  
**\[Constraint\]**

1. 一個會員 (User) 對應一個 Cart (1:1)；一個 Cart 包含多個 CartItem (1:N)。  
2. 使用 Lombok，時間欄位採用 LocalDateTime，使用 jakarta.persistence.\*。  
3. 提供防範循環引用 (Circular Reference) 的註解設定。

### **2\. AI 產出與實作解析**

#### **MySQL DDL 腳本**

SQL  
CREATE TABLE cart (  
    id BIGINT AUTO\_INCREMENT PRIMARY KEY,  
    user\_id BIGINT NOT NULL UNIQUE,  
    created\_at DATETIME DEFAULT CURRENT\_TIMESTAMP,  
    updated\_at DATETIME DEFAULT CURRENT\_TIMESTAMP ON UPDATE CURRENT\_TIMESTAMP  
);

CREATE TABLE cart\_item (  
    id BIGINT AUTO\_INCREMENT PRIMARY KEY,  
    cart\_id BIGINT NOT NULL,  
    product\_id BIGINT NOT NULL,  
    quantity INT NOT NULL DEFAULT 1,  
    unit\_price DECIMAL(10, 2) NOT NULL,  
    created\_at DATETIME DEFAULT CURRENT\_TIMESTAMP,  
    CONSTRAINT fk\_cart\_item\_cart FOREIGN KEY (cart\_id) REFERENCES cart(id) ON DELETE CASCADE  
);

#### **Spring Data JPA Entity (CartItem.java)**

Java  
package com.example.cart.entity;

import jakarta.persistence.\*;  
import lombok.\*;  
import java.math.BigDecimal;  
import java.time.LocalDateTime;

@Entity  
@Table(name \= "cart\_item")  
@Getter  
@Setter  
@NoArgsConstructor  
@AllArgsConstructor  
@Builder  
public class CartItem {

    @Id  
    @GeneratedValue(strategy \= GenerationType.IDENTITY)  
    private Long id;

    @ManyToOne(fetch \= FetchType.LAZY)  
    @JoinColumn(name \= "cart\_id", nullable \= false)  
    private Cart cart;

    @Column(name \= "product\_id", nullable \= false)  
    private Long productId;

    @Column(nullable \= false)  
    private Integer quantity;

    @Column(name \= "unit\_price", nullable \= false)  
    private BigDecimal unit\_price;

    @Column(name \= "created\_at", updatable \= false)  
    private LocalDateTime createdAt;

    @PrePersist  
    protected void onCreate() {  
        this.createdAt \= LocalDateTime.now();  
    }  
}

**教學重點（AI Code Review 觀念）**：提醒學員檢視 @ManyToOne(fetch \= FetchType.LAZY) 是否設為延遲載入，避免一次載入過多無用資料，並說明 @ToString.Exclude 或 Lombok @Getter/@Setter 如何防止 JPA 雙向關聯引發的 StackOverflowError。

## **階段二：Service 層業務邏輯與 JPA N+1 問題修復**

### **1\. 給 AI 的提示詞 (Prompt)**

**\[Context\]** 我需要實現「查詢會員購物車內容」與「將商品加入購物車」的 Service 邏輯。  
**\[Task\]**

1. 請寫出 CartRepository 使用 JOIN FETCH 一次性載入 Cart 與 CartItem，避免 N+1 查詢問題。  
2. 撰寫 CartService 實現將商品加入購物車的邏輯（若商品已存在則累加數量）。  
   **\[Constraint\]** 必須使用 DTO 回傳，嚴禁將 Entity 直接曝露給前端。

### **2\. AI 產出與實作解析**

#### **Repository 層 (解決 N+1 效能陷阱)**

Java  
package com.example.cart.repository;

import com.example.cart.entity.Cart;  
import org.springframework.data.jpa.repository.JpaRepository;  
import org.springframework.data.jpa.repository.Query;  
import org.springframework.data.repository.query.Param;  
import java.util.Optional;

public interface CartRepository extends JpaRepository\<Cart, Long\> {

    // 使用 JOIN FETCH 解決 Lazy Loading 引發的 N+1 查詢效能問題  
    @Query("SELECT c FROM Cart c LEFT JOIN FETCH c.items WHERE c.userId \= :userId")  
    Optional\<Cart\> findByUserIdWithItems(@Param("userId") Long userId);

    Optional\<Cart\> findByUserId(Long userId);  
}

#### **Service 層 (商業邏輯處理)**

Java  
package com.example.cart.service;

import com.example.cart.dto.AddToCartRequest;  
import com.example.cart.dto.CartResponse;  
import com.example.cart.entity.Cart;  
import com.example.cart.entity.CartItem;  
import com.example.cart.repository.CartRepository;  
import lombok.RequiredArgsConstructor;  
import org.springframework.stereotype.Service;  
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service  
@RequiredArgsConstructor  
public class CartService {

    private final CartRepository cartRepository;

    @Transactional  
    public CartResponse addToCart(Long userId, AddToCartRequest request) {  
        // 1\. 取得或建立購物車  
        Cart cart \= cartRepository.findByUserId(userId)  
                .orElseGet(() \-\> cartRepository.save(Cart.builder().userId(userId).build()));

        // 2\. 檢查商品是否已存在於購物車中  
        Optional\<CartItem\> existingItem \= cart.getItems().stream()  
                .filter(item \-\> item.getProductId().equals(request.getProductId()))  
                .findFirst();

        if (existingItem.isPresent()) {  
            CartItem item \= existingItem.get();  
            item.setQuantity(item.getQuantity() \+ request.getQuantity());  
        } else {  
            CartItem newItem \= CartItem.builder()  
                    .cart(cart)  
                    .productId(request.getProductId())  
                    .quantity(request.getQuantity())  
                    .unitPrice(request.getUnitPrice())  
                    .build();  
            cart.getItems().add(newItem);  
        }

        // 3\. 轉為 DTO 回傳 (可使用 AI 自動產生的 Mapper)  
        return CartResponse.fromEntity(cart);  
    }  
}

## **階段三：RESTful API、驗證與全域例外攔截**

### **1\. 給 AI 的提示詞 (Prompt)**

**\[Context\]** 我正在撰寫 CartController 與全域例外處理器。  
**\[Task\]**

1. 提供加入購物車 API (POST /api/v1/cart/items) 與查詢購物車 API (GET /api/v1/cart)。  
2. 使用 Jakarta Validation 驗證請求參數（數量不可小於 1）。  
3. 使用 @RestControllerAdvice 攔截 @Valid 失敗的例外，統一回傳 JSON 格式。

### **2\. AI 產出與實作解析**

#### **Controller 與 DTO Validation**

Java  
package com.example.cart.controller;

import com.example.cart.dto.AddToCartRequest;  
import com.example.cart.dto.CartResponse;  
import com.example.cart.service.CartService;  
import jakarta.validation.Valid;  
import lombok.RequiredArgsConstructor;  
import org.springframework.http.ResponseEntity;  
import org.springframework.web.bind.annotation.\*;

@RestController  
@RequestMapping("/api/v1/cart")  
@RequiredArgsConstructor  
public class CartController {

    private final CartService cartService;

    @PostMapping("/items")  
    public ResponseEntity\<CartResponse\> addToCart(  
            @RequestHeader("X-User-Id") Long userId,  
            @Valid @RequestBody AddToCartRequest request) {  
        return ResponseEntity.ok(cartService.addToCart(userId, request));  
    }  
}

#### **全域例外攔截器 (GlobalExceptionHandler.java)**

Java  
@RestControllerAdvice  
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)  
    public ResponseEntity\<Map\<String, String\>\> handleValidationExceptions(MethodArgumentNotValidException ex) {  
        Map\<String, String\> errors \= new HashMap\<\>();  
        ex.getBindingResult().getFieldErrors().forEach(error \-\>   
            errors.put(error.getField(), error.getDefaultMessage())  
        );  
        return ResponseEntity.status(HttpStatus.BAD\_REQUEST).body(errors);  
    }  
}

## **階段四：前端 SPA (Vue 3 / Axios) 異步對接**

### **1\. 給 AI 的提示詞 (Prompt)**

**\[Context\]** 後端購物車 API 已開發完畢。  
**\[Task\]** 請根據後端 DTO 格式，用 Vue 3 (Composition API) \+ Axios 撰寫前端購物車模組與簡易 UI。  
**\[Constraint\]** 需包含異步載入狀態 (Loading)、錯誤提示 (Error) 以及數量增減的按鈕事件。

### **2\. AI 產出與實作解析 (CartView.vue)**

HTML  
\<template\>  
  \<div class\="cart-container"\>  
    \<h2\>您的購物車\</h2\>  
      
    \<div v-if\="loading" class\="loading"\>資料載入中...\</div\>  
    \<div v-else-if\="error" class\="error-msg"\>{{ error }}\</div\>

    \<div v-else\>  
      \<table class\="cart-table"\>  
        \<thead\>  
          \<tr\>  
            \<th\>商品 ID\</th\>  
            \<th\>單價\</th\>  
            \<th\>數量\</th\>  
            \<th\>小計\</th\>  
          \</tr\>  
        \</thead\>  
        \<tbody\>  
          \<tr v-for\="item in cart.items" :key\="item.id"\>  
            \<td\>{{ item.productId }}\</td\>  
            \<td\>\${{ item.unitPrice }}\</td\>  
            \<td\>  
              \<button @click\="updateQuantity(item, \-1)"\>\-\</button\>  
              {{ item.quantity }}  
              \<button @click\="updateQuantity(item, 1)"\>\+\</button\>  
            \</td\>  
            \<td\>\${{ item.unitPrice \* item.quantity }}\</td\>  
          \</tr\>  
        \</tbody\>  
      \</table\>  
      \<div class\="cart-total"\>  
        \<strong\>總計：\${{ totalPrice }}\</strong\>  
      \</div\>  
    \</div\>  
  \</div\>  
\</template\>

\<script setup\>  
import { ref, computed, onMounted } from 'vue';  
import axios from 'axios';

const cart \= ref({ items: \[\] });  
const loading \= ref(false);  
const error \= ref(null);  
const currentUserId \= 1; // 模擬當前登入會員

// 計算總金額  
const totalPrice \= computed(() \=\> {  
  return cart.value.items.reduce((sum, item) \=\> sum \+ item.unitPrice \* item.quantity, 0);  
});

// 載入購物車內容  
const fetchCart \= async () \=\> {  
  loading.value \= true;  
  error.value \= null;  
  try {  
    const response \= await axios.get('/api/v1/cart', {  
      headers: { 'X-User-Id': currentUserId }  
    });  
    cart.value \= response.data;  
  } catch (err) {  
    error.value \= '無法載入購物車資料，請稍後再試。';  
  } finally {  
    loading.value \= false;  
  }  
};

onMounted(() \=\> {  
  fetchCart();  
});  
\</script\>

**來源**  
1\. [https\://github.com/techhan/DogDog](https://github.com/techhan/DogDog)