package com.example.mediacollection;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * 應用程式啟動測試。
 *
 * 驗證 Spring Application Context 能正常載入，
 * 確保所有 Bean 注入與設定無錯誤。
 */
@SpringBootTest
class MediaCollectionApplicationTests {

    @Test
    void contextLoads() {
        // Spring Context 載入無例外即視為通過
    }
}
