package demo.example.service;

import org.springframework.stereotype.Service;

@Service
public class EmailNotificationService implements NotificationService {
    
    @Override
    public String sendNotification(String message) {
        return "Email 已發送: " + message;
    }
}
