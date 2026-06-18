package com.example.financeapi;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Test tầng web cho giao dịch — tập trung vào BẢO MẬT và VALIDATION đầu vào.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class TransactionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void createTransaction_withoutToken_returns401() throws Exception {
        // Chưa đăng nhập -> không được tạo giao dịch.
        mockMvc.perform(post("/api/transactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"amount":1000,"categoryId":1,"occurredAt":"2026-06-01"}
                                """))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = "someone@test.com")   // giả lập đã đăng nhập
    void createTransaction_amountNotPositive_returns400() throws Exception {
        // amount = 0 vi phạm @Positive -> validation chặn trả 400 (trước khi vào service).
        mockMvc.perform(post("/api/transactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"amount":0,"categoryId":1,"occurredAt":"2026-06-01"}
                                """))
                .andExpect(status().isBadRequest());
    }
}
