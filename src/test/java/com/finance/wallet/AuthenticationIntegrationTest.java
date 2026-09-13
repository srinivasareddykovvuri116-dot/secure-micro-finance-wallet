package com.finance.wallet;

import com.finance.wallet.entity.User;
import com.finance.wallet.repository.AuditLogRepository;
import com.finance.wallet.repository.UserRepository;
import com.finance.wallet.repository.WalletRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.junit.jupiter.api.Assertions.assertFalse;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class AuthenticationIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private WalletRepository walletRepository;

    @Autowired
    private AuditLogRepository auditLogRepository;

    @BeforeEach
    void setUp() {

        // Audit logs reference users, so delete them first.
        auditLogRepository.deleteAll();

        // Wallets reference users, so delete wallets before users.
        walletRepository.deleteAll();

        // Now users can be safely deleted.
        userRepository.deleteAll();
    }

    @Test
    void registerLoginAndAccessProtectedEndpoint() throws Exception {

        // 1. Register a real user
        mockMvc.perform(
                post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "email": "auth@test.com",
                                    "password": "Password123",
                                    "fullName": "Auth Test User"
                                }
                                """)
        )
        .andExpect(status().isCreated());

        // 2. Login using the registered credentials
        String token = mockMvc.perform(
                post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "email": "auth@test.com",
                                    "password": "Password123"
                                }
                                """)
        )
        .andExpect(status().isOk())
        .andReturn()
        .getResponse()
        .getContentAsString();

        // 3. Verify that a JWT was returned
        assertFalse(token.isBlank());

        // 4. Use the JWT to access the protected endpoint
        mockMvc.perform(
                get("/api/user")
                        .header(
                                "Authorization",
                                "Bearer " + token
                        )
        )
        .andExpect(status().isOk())
        .andExpect(
                content().string("USER access granted")
        );
    }

    @Test
    void protectedEndpointShouldRejectRequestWithoutToken()
            throws Exception {

        mockMvc.perform(
                get("/api/user")
        )
        .andExpect(status().isUnauthorized());
    }

    @Test
    void userShouldBeForbiddenFromAccessingAdminEndpoint()
            throws Exception {

        mockMvc.perform(
                get("/api/admin/audit-logs")
                        .with(
                                org.springframework.security.test.web.servlet.request
                                        .SecurityMockMvcRequestPostProcessors
                                        .user("1")
                                        .roles("USER")
                        )
        )
        .andExpect(status().isForbidden());
    }

    @Test
    void protectedEndpointShouldRejectInvalidToken()
            throws Exception {

        mockMvc.perform(
                get("/api/user")
                        .header(
                                "Authorization",
                                "Bearer invalid.jwt.token"
                        )
        )
        .andExpect(status().isUnauthorized());
    }

    @Test
    void protectedEndpointShouldRejectTamperedToken()
            throws Exception {

        // 1. Register a real user
        mockMvc.perform(
                post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "email": "tampered@test.com",
                                    "password": "Password123",
                                    "fullName": "Tampered Token User"
                                }
                                """)
        )
        .andExpect(status().isCreated());

        // 2. Login and get a valid JWT
        String token = mockMvc.perform(
                post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "email": "tampered@test.com",
                                    "password": "Password123"
                                }
                                """)
        )
        .andExpect(status().isOk())
        .andReturn()
        .getResponse()
        .getContentAsString();

        // 3. Tamper with the JWT
        String tamperedToken =
                token.substring(0, token.length() - 1)
                + (token.endsWith("a") ? "b" : "a");

        // 4. Try to access the protected endpoint
        mockMvc.perform(
                get("/api/user")
                        .header(
                                "Authorization",
                                "Bearer " + tamperedToken
                        )
        )
        .andExpect(status().isUnauthorized());
    }

    @Test
    void suspendedUserCannotAccessProtectedEndpointWithExistingToken()
            throws Exception {

        String email = "suspended@example.com";

        // 1. Register user
        mockMvc.perform(
                post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "email": "suspended@example.com",
                                    "password": "Password@123",
                                    "fullName": "Suspended User"
                                }
                                """)
        )
        .andExpect(status().isCreated());

        // 2. Login and obtain JWT
        MvcResult loginResult = mockMvc.perform(
                post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "email": "suspended@example.com",
                                    "password": "Password@123"
                                }
                                """)
        )
        .andExpect(status().isOk())
        .andReturn();

        String token = loginResult.getResponse().getContentAsString();

        // 3. Find user
        User user = userRepository.findByEmail(email)
                .orElseThrow();

        // 4. Suspend the user
        user.setActive(false);
        userRepository.save(user);

        // 5. Existing token should no longer work
        mockMvc.perform(
                get("/api/user")
                        .header(
                                "Authorization",
                                "Bearer " + token
                        )
        )
        .andExpect(status().isUnauthorized());
    }
}