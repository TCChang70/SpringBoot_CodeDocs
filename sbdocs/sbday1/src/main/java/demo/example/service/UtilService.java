package demo.example.service;

import org.springframework.stereotype.Component;

@Component
public class UtilService {
    
    public String getCurrentTime() {
        return "目前時間: " + java.time.LocalDateTime.now();
    }
    
    public String generateUuid() {
        return "UUID: " + java.util.UUID.randomUUID().toString();
    }
}