package demo.example.service;


import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

@Service
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)  // 使用常數（推薦）
public class PrototypeService {
    
    private final String instanceId;
    
    public PrototypeService() {
        this.instanceId = java.util.UUID.randomUUID().toString();
        System.out.println("PrototypeService 建立新實例: " + instanceId);
    }
    
    public String getInstanceId() {
        return instanceId;
    }
    
    public String getMessage() {
        return "這是 Prototype 服務，實例 ID: " + instanceId;
    }
}
