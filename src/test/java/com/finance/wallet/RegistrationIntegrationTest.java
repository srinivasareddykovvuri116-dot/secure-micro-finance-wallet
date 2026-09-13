package com.finance.wallet;

import com.finance.wallet.entity.AuditLog;
import com.finance.wallet.entity.User;
import com.finance.wallet.entity.Wallet;
import com.finance.wallet.repository.AuditLogRepository;
import com.finance.wallet.repository.UserRepository;
import com.finance.wallet.repository.WalletRepository;
import com.finance.wallet.service.UserService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class RegistrationIntegrationTest {

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private WalletRepository walletRepository;

    @Autowired
    private AuditLogRepository auditLogRepository;

    @BeforeEach
    void setUp() {
        auditLogRepository.deleteAll();
        walletRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    void registerUserShouldPersistUserWalletAndAuditLog() {

        User result = userService.registerUser(
                "integration@example.com",
                "Password123",
                "Integration User"
        );

        assertNotNull(result.getId());

        User savedUser = userRepository
                .findByEmail("integration@example.com")
                .orElseThrow();

        assertEquals(
                "integration@example.com",
                savedUser.getEmail()
        );

        assertEquals(
                "Integration User",
                savedUser.getFullName()
        );

        assertNotEquals(
                "Password123",
                savedUser.getPasswordHash()
        );

        Wallet wallet = walletRepository
                .findByUserId(savedUser.getId())
                .orElseThrow();

        assertNotNull(wallet.getId());

        assertEquals(
                "0.00",
                wallet.getBalance().toPlainString()
        );

        List<AuditLog> auditLogs =
                auditLogRepository.findAll();

        assertEquals(1, auditLogs.size());

        AuditLog auditLog = auditLogs.get(0);

        assertEquals(
                savedUser.getId(),
                auditLog.getUser().getId()
        );

        assertEquals(
                "USER_REGISTERED",
                auditLog.getAction()
        );

        assertEquals(
                "User registered successfully",
                auditLog.getDetails()
        );
    }
}