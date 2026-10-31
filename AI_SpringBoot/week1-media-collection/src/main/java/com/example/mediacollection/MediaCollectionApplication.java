package com.example.mediacollection;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 個人圖書/影音收藏管理系統 - 主程式入口
 *
 * 系統功能：管理個人書籍與影音收藏，提供完整 CRUD REST API。
 * 技術棧：Spring Boot 3.3 / Java 21 / Spring Data JPA / H2 Database
 */
@SpringBootApplication
public class MediaCollectionApplication {

    public static void main(String[] args) {
        SpringApplication.run(MediaCollectionApplication.class, args);
    }
}
