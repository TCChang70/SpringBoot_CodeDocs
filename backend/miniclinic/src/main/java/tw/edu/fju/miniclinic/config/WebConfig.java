package tw.edu.fju.miniclinic.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import tw.edu.fju.miniclinic.interceptor.LoginRequiredInterceptor;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Autowired
    private LoginRequiredInterceptor loginInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(loginInterceptor)
            .addPathPatterns(
                "/dashboard",
                "/dashboard/**",
                "/api/auth/me",
                "/api/appointments/*/status",
                "/password",       
                "/password/**"
            )
           .excludePathPatterns(
                 "/login",
                "/logout",
                "/api/stats",          // ← 新增此行：統計端點不需要認證
                 "/api/health",         // ← 新增此行：健康檢查也不需要認證
                "/api/doctors",        // ← 新增此行：醫師清單不需要認證（T02/T03）
                "/api/doctors/**"      // ← 新增此行：醫師詳情不需要認證
            );
    }
}