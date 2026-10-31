這套小考題目專為評估學員「是否真正理解 AI 生成的程式碼」**與**「是否具備獨立 Code Review 能力」而設計。題目聚焦於購物車專案中最容易出現的 AI 盲點（如 N+1 效能、版本相容性、JPA 實體生命周期與前後端驗證）。

### **第 1 題：Spring Data JPA 效能陷阱（N+1 查詢問題）**

**題目**： 在購物車專案中，AI 幫學員產出了以下查詢 API 的程式碼：

Java  
// CartRepository.java  
public interface CartRepository extends JpaRepository\<Cart, Long\> {  
    Optional\<Cart\> findByUserId(Long userId);  
}

// CartService.java  
@Transactional(readOnly \= true)  
public CartResponse getCart(Long userId) {  
    Cart cart \= cartRepository.findByUserId(userId)  
            .orElseThrow(() \-\> new ResourceNotFoundException("Cart not found"));  
      
    // 進入轉換邏輯，觸發 cart.getItems() 遍歷  
    return CartResponse.fromEntity(cart);  
}

請問當這個購物車內有 10 項商品 (CartItem) 時，SQL 日誌會執行幾次查詢？這屬於什麼效能問題？在 JPA 中應如何引導 AI 生成正確的修復程式碼？  
**解答**：

> 1. **執行次數**：會執行 **11 次** 查詢（1 次查詢 Cart 主表 \+ 10 次延遲載入查詢各個 CartItem）。  
> 2. **效能問題**：屬於典型 **N+1 查詢問題**（N+1 Select Problem）。  
> 3. **修復引導 Prompt**：「請重構 CartRepository，使用 JPQL 的 JOIN FETCH (例如 SELECT c FROM Cart c LEFT JOIN FETCH c.items WHERE c.userId \= :userId) 或 @EntityGraph(attributePaths \= {"items"}) 來實現單次 SQL 語法直接一次性載入 Cart 與 CartItem。」  
* **測驗重點**：考核學員是否具備檢視 SQL 日誌的習慣，以及理解 JPA 延遲載入 (FetchType.LAZY) 造成的效能陷阱。

### **第 2 題：Spring Boot 2.x 與 3.x 版本與套件遷移**

**題目**： 學員讓 AI 自動補全 CartItem.java Entity 時，AI 輸出了以下 package import：

Java  
import javax.persistence.Entity;  
import javax.persistence.Table;  
import javax.persistence.Id;  
import javax.validation.constraints.Min;

專案在啟動或編譯時直接報錯失敗。請問問題出在哪裡？應該要求 AI 替換成哪一個規範的套件？  
**解答**：

> 1. **問題原因**：AI 產生了 **Spring Boot 2.x (Java EE / javax.\*)** 的舊版語法。而本課程使用的 **Spring Boot 3.x** 已經升級至 **Jakarta EE 10** 規範。  
> 2. **正確套件**：應全面將 javax.persistence.\* 與 javax.validation.\* 替換為 **jakarta.persistence.\*** 與 **jakarta.validation.\***。  
* **測驗重點**：考核學員是否了解 AI 的「版本幻覺（Version Misconfiguration）」，並具備將舊版套件修正為 Spring Boot 3.x 規範的能力。

### **第 3 題：JPA 實體循環引用與無限遞迴（Stack Overflow）**

**題目**： 當學員在 Controller 直接將 Cart Entity 回傳給前端時（例如 return ResponseEntity.ok(cart)），或在 Entity 上直接加上 Lombok @Data / @ToString 註解，系統在執行時爆出了 StackOverflowError。請問這是什麼原因導致的？最佳的架構解法為何？  
**解答**：

> 1. **問題原因**：Cart (1) 與 CartItem (N) 建立了雙向關聯。當 Jackson 將物件序列化為 JSON，或 Lombok 自動生成 toString() / equals() 時，會造成 **Cart 呼叫 CartItem，CartItem 又呼叫 Cart 的無限循環遞迴**，最終導致記憶體堆疊溢位 (StackOverflowError)。  
> 2. **最佳解法**：  
   * **架構隔離**：嚴禁將 Entity 直接曝露給 Controller 回傳，必須使用 **DTO (Data Transfer Object)** 進行資料封裝。  
   * **雙向關聯控制**：若需序列化或列印，使用 @ToString.Exclude 排除關聯欄位，或在 JPA 屬性加上 @JsonIgnore / @JsonManagedReference / @JsonBackReference。  
* **測驗重點**：考核學員對 JPA 雙向關聯與 Controller-Service-DTO 分層架構（Layered Architecture）的理解。

### **第 4 題：浮點數計算精度與資料庫欄位型別**

**題目**： 在設計購物車商品的「單價 (unit\_price)」與「總金額 (total\_price)」時，若 AI 生成的 Java 程式碼使用 double 或 float，而 MySQL DDL 生成了 DOUBLE 型別，這在商業電商系統中會帶來什麼嚴重的併發與計算問題？正確的型別配置應為何？  
**解答**：

> 1. **潛在問題**：float 與 double 在電腦中採用 IEEE 754 浮點數表示法，存在**二進位轉十進位的精度丟失問題**（例如 0.1 \+ 0.2 會變成 0.30000000000000004），這會導致結帳金額計算不精準，產生帳務誤差。  
> 2. **正確型別**：  
   * **Java 端**：應統一使用 **java.math.BigDecimal**。  
   * **MySQL 端**：應統一使用 **DECIMAL(p, s)**（如 DECIMAL(10, 2)）。  
* **測驗重點**：考核學員是否能主動審查 AI 在「金融/電商核心資料」上的型別選用安全性，避免盲目接受 AI 給出的預設基本型態。

### **第 5 題：前後端驗證與 API 安全性防護**

**題目**： 學員在前端 Vue 3 表單已經撰寫了 validation 邏輯，規定「商品數量不能小於 1」。於是學員認為後端 API 不需要再做驗證。 請問這種想法在資安與系統架構上有何嚴重隱患？如何要求 AI 在 Spring Boot 後端補充適當的 Validation 攔截？  
**解答**：

> 1. **安全隱患**：**「永遠不能信任前端傳來的任何資料」**。攻擊者可以輕易繞過前端 UI，透過 Postman、cURL 或直接修改 HTTP Request Body 傳送 negative quantity (quantity: \-999)，造成購物車總金額變成負數（逆向刷退/套利漏洞）。  
> 2. **後端補強方式**：  
   * 在 DTO 欄位加上註解：@Min(value \= 1, message \= "商品數量至少為 1") Integer quantity;  
   * 在 Controller 接收參數處加上 **@Valid** 或 **@Validated** 註解。  
   * 搭配 @RestControllerAdvice 攔截 MethodArgumentNotValidException，統一回傳 400 Bad Request 訊息。  
* **測驗重點**：考核學員的防禦性程式設計 (Defensive Programming) 觀念與 Validation 在 Spring API 中的實際落地方式。