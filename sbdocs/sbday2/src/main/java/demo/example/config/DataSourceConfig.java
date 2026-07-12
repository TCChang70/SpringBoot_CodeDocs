package demo.example.config;

import javax.sql.DataSource;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
public class DataSourceConfig {
    
    @Bean
    @Profile("dev")
    public DataSource devDataSource() {
        System.out.println("🔧 使用開發環境資料庫 (H2)");
        org.h2.jdbcx.JdbcDataSource ds = new org.h2.jdbcx.JdbcDataSource();
        ds.setURL("jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1");
        ds.setUser("sa");
        ds.setPassword("");
        return ds;
    }
    
    @Bean
    @Profile("test")
    public DataSource testDataSource() {
        System.out.println("🧪 使用測試環境資料庫 (H2)");
        org.h2.jdbcx.JdbcDataSource ds = new org.h2.jdbcx.JdbcDataSource();
        ds.setURL("jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1");
        ds.setUser("sa");
        ds.setPassword("");
        return ds;
    }
    
    @Bean
    @Profile("prod")
    public DataSource prodDataSource() {
        System.out.println("🚀 使用正式環境資料庫 (MySQL)");
        // 正式環境使用 HikariCP 連線池
        com.zaxxer.hikari.HikariConfig config = new com.zaxxer.hikari.HikariConfig();
        config.setJdbcUrl("jdbc:mysql://localhost:3306/mydb");
        config.setUsername("root");
        config.setPassword("1234");
        config.setMaximumPoolSize(10);
        return new com.zaxxer.hikari.HikariDataSource(config);
    }
    
    @Bean
    @Profile("default")
    public DataSource defaultDataSource() {
        System.out.println("⚙️ 使用預設資料庫");
        org.h2.jdbcx.JdbcDataSource ds = new org.h2.jdbcx.JdbcDataSource();
        ds.setURL("jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1");
        ds.setUser("sa");
        ds.setPassword("");
        return ds;
    }
}
