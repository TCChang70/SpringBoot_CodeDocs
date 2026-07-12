package demo.example.service;

import demo.example.model.AppSetting;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class SettingService {
    
    private final Map<String, AppSetting> settings;
    private final List<String> auditLog;
    
    public SettingService() {
        this.settings = new ConcurrentHashMap<>();
        this.auditLog = new ArrayList<>();
    }
    
    @PostConstruct
    public void init() {
        System.out.println("初始化設定服務...");
        // 載入預設設定
        saveSetting("app.name", "Spring Boot 實作練習", "應用程式名稱");
        saveSetting("app.version", "1.0.0", "應用程式版本");
        saveSetting("app.max.users", "100", "最大使用者數量");
        System.out.println("設定服務初始化完成，載入 " + settings.size() + " 個預設設定");
    }
    
    @PreDestroy
    public void cleanup() {
        System.out.println("清理設定服務...");
        System.out.println("審計日誌共 " + auditLog.size() + " 筆記錄");
        settings.clear();
        auditLog.clear();
    }
    
    public AppSetting saveSetting(String key, String value, String description) {
        AppSetting setting = new AppSetting(key, value, description);
        settings.put(key, setting);
        auditLog.add("儲存設定: " + key + " = " + value + " [" + LocalDateTime.now() + "]");
        return setting;
    }
    
    public Optional<AppSetting> getSetting(String key) {
        auditLog.add("讀取設定: " + key + " [" + LocalDateTime.now() + "]");
        return Optional.ofNullable(settings.get(key));
    }
    
    public List<AppSetting> getAllSettings() {
        return new ArrayList<>(settings.values());
    }
    
    public boolean deleteSetting(String key) {
        if (settings.containsKey(key)) {
            settings.remove(key);
            auditLog.add("刪除設定: " + key + " [" + LocalDateTime.now() + "]");
            return true;
        }
        return false;
    }
    
    public List<String> getAuditLog() {
        return new ArrayList<>(auditLog);
    }
    
    public int getSettingCount() {
        return settings.size();
    }
}
