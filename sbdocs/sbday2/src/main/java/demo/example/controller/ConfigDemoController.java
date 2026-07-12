package demo.example.controller;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@RestController
@RequestMapping("/api/config")
public class ConfigDemoController {
    
    private final RestTemplate restTemplate;
    private final DateTimeFormatter formatter;
    private final String appInfo;
    
    public ConfigDemoController(
            org.springframework.web.client.RestTemplate restTemplate,
            @Qualifier("currentTimeFormatter") DateTimeFormatter formatter,
            String appInfo) {
        this.restTemplate = restTemplate;
        this.formatter = formatter;
        this.appInfo = appInfo;
    }
    
    @GetMapping
    public String getConfigInfo() {
        String currentTime = LocalDateTime.now().format(formatter);
        return String.format(
            "%s\n目前時間: %s\nRestTemplate 類型: %s",
            appInfo,
            currentTime,
            restTemplate.getClass().getSimpleName()
        );
    }
}
