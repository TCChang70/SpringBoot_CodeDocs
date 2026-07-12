package demo.example.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;

@Configuration
public class AppConfig {
    
    // @Bean：將方法回傳值註冊為 Spring Bean
    // 方法名稱即為 Bean 名稱
    @Bean
    public RestTemplate restTemplate() {
        System.out.println("建立 RestTemplate Bean");
        return new RestTemplate();
    }
    
    // @Bean 自訂名稱
    @Bean("currentTimeFormatter")
    public DateTimeFormatter dateTimeFormatter() {
        return DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    }
    
    // @Bean 方法參數注入：Spring 會自動注入已存在的 Bean
    @Bean
    public String appInfo(RestTemplate restTemplate) {
        return "App 使用 RestTemplate: " + restTemplate.getClass().getName();
    }
    
    // @Bean 帶有邏輯的配置
    @Bean
    public CorsFilter corsFilter() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(Arrays.asList("http://localhost:3000", "http://example.com"));
        config.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE"));
        config.setAllowedHeaders(Arrays.asList("*"));
        config.setAllowCredentials(true);
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        
        return new CorsFilter(source);
    }
    
    // 條件式 @Bean（與 @Conditional 搭配）
    @Bean
    public String environmentInfo() {
        LocalDateTime now = LocalDateTime.now();
        return "環境資訊 - 建立時間: " + now.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
    }
}
