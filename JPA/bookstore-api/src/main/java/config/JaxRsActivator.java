package config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.ws.rs.ApplicationPath;
import jakarta.ws.rs.core.Application;
import java.util.Set;

@OpenAPIDefinition(
    info = @Info(
        title = "Bookstore API",
        version = "1.0.0",
        description = "書籍管理 REST API（JAX-RS + JPA + SQLite）。所有 API 統一回應格式："
            + "成功 {\"success\":true,\"data\":...}，失敗 {\"success\":false,\"error\":\"訊息\"}。",
        contact = @Contact(name = "Bookstore Team")
    ),
    tags = {
        @Tag(name = "書籍", description = "書籍 CRUD 操作")
    }
)
@ApplicationPath("/api")   // 所有 API 前綴：http://localhost:8080/bookstore-api/api/
public class JaxRsActivator extends Application {

    /**
     * 明確註冊 JAX-RS 資源與 Provider，不依賴 classpath 掃描。
     * - BookController：書籍 CRUD 端點
     * - JacksonConfig：JSON 序列化設定
     * - OpenApiResource：Swagger / OpenAPI 文件端點（/api/openapi.json、/api/openapi.yaml）
     */
    @Override
    public Set<Class<?>> getClasses() {
        return Set.of(
            controller.BookController.class,
            JacksonConfig.class,
            io.swagger.v3.jaxrs2.integration.resources.OpenApiResource.class
        );
    }
}
