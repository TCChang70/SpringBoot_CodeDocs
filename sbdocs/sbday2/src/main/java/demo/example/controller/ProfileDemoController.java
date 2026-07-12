package demo.example.controller;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.env.Environment;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.sql.DataSource;
import java.util.*;

@RestController
@RequestMapping("/api/profile")
public class ProfileDemoController {
    
    private final Environment environment;
    private final DataSource dataSource;
    private final String message;
    
    public ProfileDemoController(Environment environment, DataSource dataSource, Map<String, String> messageMap) {
       String activeProfile = environment.getActiveProfiles().length > 0 ?
    		                  environment.getActiveProfiles()[0] : "default";
       this.environment=environment;
       this.dataSource=dataSource;
       this.message = messageMap.get(activeProfile + "Message");
}
    
    @GetMapping
    public Map<String, Object> getProfileInfo() {
        String[] activeProfiles = environment.getActiveProfiles();
        String defaultProfile = environment.getDefaultProfiles().length > 0 
            ? environment.getDefaultProfiles()[0] 
            : "None";
        
        return Map.of(
            "activeProfiles", activeProfiles,
            "defaultProfile", defaultProfile,
            "dataSourceClass", dataSource.getClass().getSimpleName(),
            "message", message,
            "environment", environment.getProperty("spring.profiles.active", "Not set")
        );
    }
    
    @GetMapping("/message")
    public String getMessage() {
        return message;
    }
}
