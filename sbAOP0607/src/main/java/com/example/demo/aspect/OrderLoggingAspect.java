package com.example.demo.aspect;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Arrays;

/**
 * 訂單系統 Log 切面
 *
 * @Aspect  → 標記這個類別是一個切面
 * @Component → 讓 Spring 管理這個 Bean
 */
@Aspect
@Component
public class OrderLoggingAspect {

    private static final Logger log = LoggerFactory.getLogger(OrderLoggingAspect.class);

    // =====================================================
    // Pointcut 切入點定義
    // =====================================================

    /**
     * 攔截 service 套件下所有類別的所有方法
     *
     * execution 語法：execution(回傳型別 套件.類別.方法(參數))
     * *  → 任意單一元素
     * .. → 任意多個元素
     */
    @Pointcut("execution(* com.example.demo.service.*.*(..))")
    public void serviceLayer() {}

    /**
     * 只攔截 OrderService 的 createOrder 方法
     */
    @Pointcut("execution(* com.example.demo.service.OrderService.createOrder(..))")
    public void createOrderMethod() {}

    // =====================================================
    // @Before — 方法執行前
    // =====================================================

    @Before("serviceLayer()")
    public void logBefore(JoinPoint joinPoint) {
        String className = joinPoint.getTarget().getClass().getSimpleName();
        String methodName = joinPoint.getSignature().getName();
        Object[] args = joinPoint.getArgs();

        log.info("[BEFORE] {}.{}() 開始執行，參數: {}",
            className, methodName, Arrays.toString(args));
    }

    // =====================================================
    // @AfterReturning — 方法成功返回後
    // =====================================================

    @AfterReturning(
        pointcut = "serviceLayer()",
        returning = "result"      // 接收方法的回傳值
    )
    public void logAfterReturning(JoinPoint joinPoint, Object result) {
        String methodName = joinPoint.getSignature().getName();
        log.info("[SUCCESS] {}() 執行成功，回傳: {}", methodName, result);
    }

    // =====================================================
    // @AfterThrowing — 方法拋出例外後
    // =====================================================

    @AfterThrowing(
        pointcut = "serviceLayer()",
        throwing = "ex"           // 接收例外物件
    )
    public void logAfterThrowing(JoinPoint joinPoint, Throwable ex) {
        String methodName = joinPoint.getSignature().getName();
        log.error("[FAILED] {}() 執行失敗，原因: {}", methodName, ex.getMessage());
    }

    // =====================================================
    // @After — 方法執行後（無論成功失敗都執行）
    // =====================================================

    @After("serviceLayer()")
    public void logAfter(JoinPoint joinPoint) {
        String methodName = joinPoint.getSignature().getName();
        log.info("[AFTER] {}() 執行完畢（無論成功或失敗）", methodName);
    }

    // =====================================================
    // @Around — 包圍整個方法，效能計時
    // =====================================================

    /**
     * @Around 是最強大的 Advice：
     * - 可以在方法執行前後加入邏輯
     * - 可以修改傳入參數或回傳值
     * - 必須呼叫 joinPoint.proceed() 才會執行目標方法
     */
    @Around("createOrderMethod()")
    public Object measureExecutionTime(ProceedingJoinPoint joinPoint) throws Throwable {
        long startTime = System.currentTimeMillis();

        log.info("[AROUND] 開始計時...");

        Object result;
        try {
            // ← 執行目標方法（不呼叫這行，目標方法就不會執行！）
            result = joinPoint.proceed();
        } finally {
            long elapsed = System.currentTimeMillis() - startTime;
            log.info("[AROUND] createOrder() 執行耗時: {} ms", elapsed);
        }

        return result;
    }
}
