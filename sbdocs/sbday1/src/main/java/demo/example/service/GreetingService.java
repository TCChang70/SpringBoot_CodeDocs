package demo.example.service;


import org.springframework.stereotype.Service;

@Service
public class GreetingService {
    
    public String greet(String name) {
        if (name == null || name.trim().isEmpty()) {
            return "Hello, Guest! 歡迎使用 Spring Boot！";
        }
        return "Hello, " + name + "! 歡迎使用 Spring Boot！";
    }
    
    public String getWelcomeMessage() {
        return "歡迎來到 Spring Boot Revised 3rd 實作練習！";
    }
}
