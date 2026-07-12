package demo.example.controller;


import demo.example.service.PrototypeService;
import demo.example.service.SingletonService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/scope")
public class ScopeDemoController {
    
    private final SingletonService singleton1;
    private final SingletonService singleton2;
    private final PrototypeService prototype1;
    private final PrototypeService prototype2;
    
    public ScopeDemoController(SingletonService singleton1, 
                               SingletonService singleton2,
                               PrototypeService prototype1, 
                               PrototypeService prototype2) {
        this.singleton1 = singleton1;
        this.singleton2 = singleton2;
        this.prototype1 = prototype1;
        this.prototype2 = prototype2;
        
        // 在 Controller 建構時比較實例
        System.out.println("=== Scope 比較 ===");
        System.out.println("Singleton 相同實例: " + (singleton1 == singleton2));  // true
        System.out.println("Prototype 相同實例: " + (prototype1 == prototype2));  // false
    }
    
    @GetMapping("/compare")
    public Map<String, Object> compareScopes() {
        return Map.of(
            "singleton1Id", singleton1.getInstanceId(),
            "singleton2Id", singleton2.getInstanceId(),
            "singletonSameInstance", singleton1 == singleton2,
            "prototype1Id", prototype1.getInstanceId(),
            "prototype2Id", prototype2.getInstanceId(),
            "prototypeSameInstance", prototype1 == prototype2
        );
    }
    
    @GetMapping("/singleton")
    public String singletonDemo() {
        return singleton1.getMessage();
    }
    
    @GetMapping("/prototype")
    public String prototypeDemo() {
        return prototype1.getMessage();
    }
}
