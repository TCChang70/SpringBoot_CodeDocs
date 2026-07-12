package demo.example.service;

import org.springframework.stereotype.Service;

@Service  // 預設是 Singleton Scope
public class SingletonService {
    
    private final String instanceId;
    
    public SingletonService() {
        this.instanceId = java.util.UUID.randomUUID().toString();
        System.out.println("SingletonService 建立實例: " + instanceId);
    }
    
    public String getInstanceId() {
        return instanceId;
    }
    
    public String getMessage() {
        return "這是 Singleton 服務，實例 ID: " + instanceId;
    }
}
