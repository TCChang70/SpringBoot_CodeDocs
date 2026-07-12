package demo.example.controller;

import demo.example.model.AppSetting;
import demo.example.service.SettingService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/settings")
public class SettingController {
    
    private final SettingService settingService;
    
    public SettingController(SettingService settingService) {
        this.settingService = settingService;
    }
    
    @PostMapping
    public ResponseEntity<AppSetting> createSetting(@RequestBody AppSetting setting) {
        AppSetting created = settingService.saveSetting(
            setting.getKey(), 
            setting.getValue(), 
            setting.getDescription()
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }
    
    @GetMapping("/{key}")
    public ResponseEntity<AppSetting> getSetting(@PathVariable String key) {
        return settingService.getSetting(key)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    
    @GetMapping
    public ResponseEntity<List<AppSetting>> getAllSettings() {
        return ResponseEntity.ok(settingService.getAllSettings());
    }
    
    @DeleteMapping("/{key}")
    public ResponseEntity<Void> deleteSetting(@PathVariable String key) {
        if (settingService.deleteSetting(key)) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }
    
    @GetMapping("/audit")
    public ResponseEntity<List<String>> getAuditLog() {
        return ResponseEntity.ok(settingService.getAuditLog());
    }
    
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getStats() {
        return ResponseEntity.ok(Map.of(
            "totalSettings", settingService.getSettingCount(),
            "totalAuditLogs", settingService.getAuditLog().size()
        ));
    }
}
