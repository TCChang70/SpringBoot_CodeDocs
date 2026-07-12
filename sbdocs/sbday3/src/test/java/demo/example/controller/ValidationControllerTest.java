package demo.example.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class ValidationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void createProduct_success() throws Exception {
        String json = """
                {
                    "name": "測試產品",
                    "description": "這是一個測試產品",
                    "price": 99.99,
                    "stock": 10,
                    "contactEmail": "test@example.com"
                }
                """;

        mockMvc.perform(post("/api/validation/product")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.name").value("測試產品"))
                .andExpect(jsonPath("$.data.price").value(99.99))
                .andExpect(jsonPath("$.data.stock").value(10))
                .andExpect(jsonPath("$.data.contactEmail").value("test@example.com"));
    }

    @Test
    void createProduct_fail_emptyName() throws Exception {
        String json = """
                {
                    "name": "",
                    "description": "這是一個測試產品",
                    "price": 99.99,
                    "stock": 10
                }
                """;

        mockMvc.perform(post("/api/validation/product")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("驗證失敗"))
                .andExpect(jsonPath("$.data.name").exists());
    }

    @Test
    void createProduct_fail_negativePrice() throws Exception {
        String json = """
                {
                    "name": "測試產品",
                    "description": "這是一個測試產品",
                    "price": -10.0,
                    "stock": 10
                }
                """;

        mockMvc.perform(post("/api/validation/product")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.data.price").value("價格必須大於 0"));
    }

    @Test
    void createProduct_fail_invalidEmail() throws Exception {
        String json = """
                {
                    "name": "測試產品",
                    "description": "這是一個測試產品",
                    "price": 50.0,
                    "stock": 10,
                    "contactEmail": "not-an-email"
                }
                """;

        mockMvc.perform(post("/api/validation/product")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.data.contactEmail").value("電子郵件格式不正確"));
    }
}
