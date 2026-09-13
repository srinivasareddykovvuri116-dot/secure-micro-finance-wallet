package com.finance.wallet;

import org.junit.jupiter.api.Test;
import static org.mockito.Mockito.when;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.finance.wallet.entity.User;
import com.finance.wallet.repository.UserRepository;
import com.finance.wallet.service.AuditLogService;
import com.finance.wallet.service.JwtService;

@SpringBootTest
@AutoConfigureMockMvc
class AdminSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtService jwtService;

    @MockitoBean
    private AuditLogService auditLogService;

    @MockitoBean
    private UserRepository userRepository;

    @Test
    void normalUserShouldNotAccessAdminEndpoint() throws Exception {

        User user = new User();
        user.setId(1L);
        user.setRole("ROLE_USER");
        user.setActive(true);

        when(userRepository.findById(1L))
                .thenReturn(java.util.Optional.of(user));

        String token = jwtService.generateToken(
                1L,
                "ROLE_USER"
        );

        mockMvc.perform(
                get("/api/admin/audit-logs")
                        .header(
                                "Authorization",
                                "Bearer " + token
                        )
        )
        .andExpect(status().isForbidden());
    }

    @Test
    void adminShouldAccessAdminEndpoint() throws Exception {

        User admin = new User();
        admin.setId(1L);
        admin.setRole("ROLE_ADMIN");
        admin.setActive(true);

        when(userRepository.findById(1L))
                .thenReturn(java.util.Optional.of(admin));

        String token = jwtService.generateToken(
                1L,
                "ROLE_ADMIN"
        );

        mockMvc.perform(
                get("/api/admin/audit-logs")
                        .header(
                                "Authorization",
                                "Bearer " + token
                        )
        )
        .andExpect(status().isOk());
    }
}