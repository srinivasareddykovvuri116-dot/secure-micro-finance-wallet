package com.finance.wallet.exception;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.finance.wallet.controller.AuthController;
import com.finance.wallet.repository.UserRepository;
import com.finance.wallet.service.JwtService;
import com.finance.wallet.service.UserService;

@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
class ValidationExceptionTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private UserRepository userRepository;

    @Test
    void shouldReturnBadRequestForInvalidRegistration() throws Exception {

        String invalidRequest = """
                {
                    "email": "invalid-email",
                    "password": "123",
                    "fullName": ""
                }
                """;

        mockMvc.perform(
                post("/api/auth/register")
                        .contentType("application/json")
                        .content(invalidRequest)
        )
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.message").exists());
    }
}