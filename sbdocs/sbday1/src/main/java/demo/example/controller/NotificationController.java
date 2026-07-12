package demo.example.controller;


import demo.example.service.NotificationService;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/notification")
public class NotificationController {
    
    private final NotificationService emailService;
    private final NotificationService smsService;
    
    public NotificationController(
            @Qualifier("emailNotificationService") NotificationService emailService,
            @Qualifier("smsNotificationService") NotificationService smsService) {
        this.emailService = emailService;
        this.smsService = smsService;
    }
    
    @GetMapping("/email")
    public String sendEmail() {
        return emailService.sendNotification("這是一封電子郵件");
    }
    
    @GetMapping("/sms")
    public String sendSms() {
        return smsService.sendNotification("這是一條簡訊");
    }
}
