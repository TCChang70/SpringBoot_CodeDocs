package demo.example.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
public class MessageServiceConfig {
    
	@Bean
    @Profile("dev")
    public String devMessage() {
        return "🔧 開發環境訊息：歡迎來到開發環境！";
    }
    
    @Bean
    @Profile("test")
    public String testMessage() {
        return "🧪 測試環境訊息：測試環境運作正常！";
    }
    
    @Bean
    @Profile("prod")
    public String prodMessage() {
        return "🚀 正式環境訊息：歡迎使用正式環境！";
    }
    
    @Bean
    @Profile("default")
    public String defaultMessage() {
        return "⚙️ 預設環境訊息：歡迎使用預設環境！";
    }
}
