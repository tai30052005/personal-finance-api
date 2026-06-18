package com.example.financeapi;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Test tầng web cho xác thực — gửi HTTP thật (giả lập) qua MockMvc,
 * đi qua đầy đủ Controller -> Service -> Repository -> H2.
 *
 * @SpringBootTest        : nạp toàn bộ Spring context.
 * @AutoConfigureMockMvc  : cấu hình sẵn MockMvc để "gọi" API mà không cần server thật.
 * @ActiveProfiles("test"): dùng cấu hình H2 trong application-test.yml.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void register_thenLogin_success() throws Exception {
        String body = """
                {"email":"alice@test.com","password":"secret123"}
                """;

        // Đăng ký -> 201 + có token
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andExpect(jsonPath("$.email").value("alice@test.com"));

        // Đăng nhập đúng mật khẩu -> 200 + có token
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty());
    }

    @Test
    void login_wrongPassword_returns401() throws Exception {
        // Đăng ký trước
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"bob@test.com","password":"correct-password"}
                                """))
                .andExpect(status().isCreated());

        // Đăng nhập sai mật khẩu -> 401
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"bob@test.com","password":"wrong-password"}
                                """))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void register_invalidInput_returns400() throws Exception {
        // Email sai định dạng + mật khẩu quá ngắn -> 400
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"not-an-email","password":"123"}
                                """))
                .andExpect(status().isBadRequest());
    }
}
