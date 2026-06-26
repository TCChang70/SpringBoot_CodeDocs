package config;

import jakarta.ws.rs.ApplicationPath;
import jakarta.ws.rs.core.Application;

@ApplicationPath("/api")
public class JaxrsApplication extends Application {
    // 自動掃描 @Path 與 @Provider 類別
}
