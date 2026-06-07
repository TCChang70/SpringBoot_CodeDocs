package com.example.demo.aspect;

import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.stereotype.Component;

/**
 * 自定義 Annotation 攔截示範
 * 使用場景：方法層級的細粒度控制
 */
@Aspect
@Component
public class SecurityAspect {

    /**
     * Pointcut 語法補充：
     *
     * within(套件..*)           → 攔截套件內所有類別
     * @annotation(標註類型)     → 攔截有特定標註的方法
     * args(型別)               → 攔截特定參數型別的方法
     * bean(beanName)           → 攔截特定 Bean 的方法
     */

    // 攔截所有加了 @Transactional 的方法（示範用）
    @Before("@annotation(org.springframework.transaction.annotation.Transactional)")
    public void checkTransactionalMethods() {
        log.info("[SECURITY] 交易方法被呼叫，進行安全性檢查...");
        // 實際應用：檢查 JWT Token、角色權限等
    }

    private static final org.slf4j.Logger log =
        org.slf4j.LoggerFactory.getLogger(SecurityAspect.class);
}
