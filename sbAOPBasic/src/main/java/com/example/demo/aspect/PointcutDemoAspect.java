package com.example.demo.aspect;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Unit 02：示範各種 Pointcut 用法
 *
 * 執行方式：
 *   啟動專案後呼叫 API，觀察哪些 Log 被觸發
 */
@Aspect
@Component
public class PointcutDemoAspect {

    private static final Logger log = LoggerFactory.getLogger(PointcutDemoAspect.class);

    // ── Pointcut 定義（可重複引用）────────────────────────────

    /** P1：攔截 ProductService 所有方法 */
    @Pointcut("execution(* com.example.demo.service.ProductService.*(..))")
    public void allProductServiceMethods() {}

    /** P2：攔截以 find 開頭的方法 */
    @Pointcut("execution(* com.example.demo.service.*.find*(..))")
    public void findMethods() {}

    /** P3：攔截 create 方法 */
    @Pointcut("execution(* com.example.demo.service.ProductService.create(..))")
    public void createMethod() {}

    /** P4：攔截有一個 Long 型別參數的方法 */
    @Pointcut("execution(* com.example.demo.service.*.*( Long ))")
    public void singleLongParamMethods() {}

    /** P5：組合 Pointcut（AND / OR / NOT）*/
    @Pointcut("allProductServiceMethods() && !findMethods()")
    public void writeOperations() {}   // 非查詢的所有方法

    // ── Advice（用 @Before 觸發，讓你看到哪個 Pointcut 命中）──

    @Before("allProductServiceMethods()")
    public void onAllProductService(JoinPoint jp) {
        log.info("[P1-ALL] 命中方法: {}", jp.getSignature().toShortString());
    }

    @Before("findMethods()")
    public void onFindMethods(JoinPoint jp) {
        log.info("[P2-FIND] 查詢方法被呼叫: {}", jp.getSignature().getName());
    }

    @Before("createMethod()")
    public void onCreateMethod(JoinPoint jp) {
        log.info("[P3-CREATE] 建立操作觸發");
    }

    @Before("singleLongParamMethods()")
    public void onSingleLongParam(JoinPoint jp) {
        log.info("[P4-LONG] 單一 Long 參數方法: {}", jp.getSignature().getName());
    }

    @Before("writeOperations()")
    public void onWriteOperations(JoinPoint jp) {
        log.info("[P5-WRITE] 寫入操作（排除查詢）: {}", jp.getSignature().getName());
    }
}
