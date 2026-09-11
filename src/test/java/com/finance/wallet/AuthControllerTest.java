package com.finance.wallet;

import com.finance.wallet.controller.AuthController;
import com.finance.wallet.entity.User;
import com.finance.wallet.service.JwtService;
import com.finance.wallet.service.UserService;

import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private JwtService jwtService;


    @Test
    void registerShouldReturnCreatedWhenRequestIsValid() throws Exception {

        User user = new User();
        user.setId(1L);

        when(userService.registerUser(
                eq("test@example.com"),
                eq("password123"),
                eq("Test User")
        )).thenReturn(user);

        mockMvc.perform(
                post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "email": "test@example.com",
                                    "password": "password123",
                                    "fullName": "Test User"
                                }
                                """)
        )
        .andExpect(status().isCreated());
    }

    @Test
    void registerShouldReturnBadRequestWhenEmailIsInvalid() throws Exception {

        mockMvc.perform(
                post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "email": "invalid-email",
                                    "password": "password123",
                                    "fullName": "Test User"
                                }
                                """)
        )
        .andExpect(status().isBadRequest());
    }


    @Test
    void loginShouldReturnTokenWhenCredentialsAreValid() throws Exception {

        User user = new User();
        user.setId(1L);
        user.setEmail("test@example.com");
        user.setRole("ROLE_USER");

        when(userService.loginUser(
                eq("test@example.com"),
                eq("password123")
        )).thenReturn(user);

        when(jwtService.generateToken(
                eq(1L),
                eq("ROLE_USER")
        )).thenReturn("test-jwt-token");

        mockMvc.perform(
                post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "email": "test@example.com",
                                    "password": "password123"
                                }
                                """)
        )
        .andExpect(status().isOk())
        .andExpect(
                org.springframework.test.web.servlet.result.MockMvcResultMatchers
                        .content()
                        .string("test-jwt-token")
        );
    }


    @Test
    void loginShouldReturnBadRequestWhenCredentialsAreInvalid() throws Exception {

        when(userService.loginUser(
                eq("test@example.com"),
                eq("wrongpassword")
        )).thenThrow(
                new IllegalArgumentException("Invalid email or password")
        );

        mockMvc.perform(
                post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "email": "test@example.com",
                                    "password": "wrongpassword"
                                }
                                """)
        )
        .andExpect(status().isBadRequest());
    }

}