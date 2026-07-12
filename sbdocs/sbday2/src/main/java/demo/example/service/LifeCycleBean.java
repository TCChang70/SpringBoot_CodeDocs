package demo.example.service;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.springframework.stereotype.Component;

@Component
public class LifeCycleBean {
    
    private String name;
    private boolean initialized;
    
    public LifeCycleBean() {
        this.name = "LifecycleBean";
        this.initialized = false;
        System.out.println("1. 建構子呼叫 - Bean 建立");
    }
    
    @PostConstruct
    public void postConstruct() {
        this.initialized = true;
        System.out.println("2. @PostConstruct 呼叫 - Bean 初始化完成");
        System.out.println("   Bean 名稱: " + name);
        System.out.println("   初始化狀態: " + initialized);
    }
    
    public void doSomething() {
        if (!initialized) {
            throw new IllegalStateException("Bean 尚未初始化");
        }
        System.out.println("3. 商業方法呼叫 - Bean 使用中");
    }
    
    @PreDestroy
    public void preDestroy() {
        this.initialized = false;
        System.out.println("4. @PreDestroy 呼叫 - Bean 即將銷毀");
        System.out.println("   清理資源...");
    }
    
    public String getName() {
        return name;
    }
    
    public boolean isInitialized() {
        return initialized;
    }
}
