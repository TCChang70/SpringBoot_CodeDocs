package demo.example.controller;


import demo.example.service.LifeCycleBean;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/lifecycle")
public class LifecycleDemoController {
    
    private final LifeCycleBean lifecycleBean;
    
    public LifecycleDemoController(LifeCycleBean lifecycleBean) {
        this.lifecycleBean = lifecycleBean;
        System.out.println("=== Controller 建構，注入 LifecycleBean ===");
    }
    
    @GetMapping
    public Map<String, Object> getLifecycleInfo() {
        // 呼叫商業方法
        lifecycleBean.doSomething();
        
        return Map.of(
            "name", lifecycleBean.getName(),
            "initialized", lifecycleBean.isInitialized(),
            "message", "請查看主控台輸出，觀察生命週期順序"
        );
    }
}
