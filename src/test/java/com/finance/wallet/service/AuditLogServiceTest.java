package com.finance.wallet.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.time.Instant;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import com.finance.wallet.dto.AuditLogResponse;
import com.finance.wallet.entity.AuditLog;
import com.finance.wallet.entity.User;
import com.finance.wallet.repository.AuditLogRepository;

@SpringBootTest
class AuditLogServiceTest {

    @MockitoBean
    private AuditLogRepository auditLogRepository;

    @Test
    void shouldSaveAuditLog() {

        User user = new User();
        user.setId(1L);

        AuditLogService auditLogService =
                new AuditLogService(auditLogRepository);

        auditLogService.log(
                user,
                "DEPOSIT",
                "amount=500.00"
        );

        verify(auditLogRepository).save(
                any(AuditLog.class)
        );
    }

    @Test
    void shouldReturnAuditLogsWithUserId() {

        User user = new User();
        user.setId(1L);

        AuditLog auditLog = new AuditLog();
        auditLog.setUser(user);
        auditLog.setAction("DEPOSIT");
        auditLog.setDetails("amount=500.00");

        Instant createdAt = Instant.now();

        when(auditLogRepository.findAllByOrderByCreatedAtDesc())
                .thenReturn(List.of(auditLog));

        // createdAt is normally populated by JPA @PrePersist.
        // For this unit test, we don't need to assert it.
        AuditLogService auditLogService =
                new AuditLogService(auditLogRepository);

        List<AuditLogResponse> result =
                auditLogService.getAllAuditLogs();

        assertEquals(1, result.size());

        AuditLogResponse response = result.get(0);

        assertEquals(1L, response.getUserId());
        assertEquals("DEPOSIT", response.getAction());
        assertEquals("amount=500.00", response.getDetails());

        verify(auditLogRepository)
                .findAllByOrderByCreatedAtDesc();
    }

    @Test
    void shouldReturnNullUserIdWhenAuditLogHasNoUser() {

        AuditLog auditLog = new AuditLog();
        auditLog.setUser(null);
        auditLog.setAction("SYSTEM");
        auditLog.setDetails("System event");

        when(auditLogRepository.findAllByOrderByCreatedAtDesc())
                .thenReturn(List.of(auditLog));

        AuditLogService auditLogService =
                new AuditLogService(auditLogRepository);

        List<AuditLogResponse> result =
                auditLogService.getAllAuditLogs();

        assertEquals(1, result.size());

        AuditLogResponse response = result.get(0);

        assertNull(response.getUserId());
        assertEquals("SYSTEM", response.getAction());
        assertEquals("System event", response.getDetails());
    }

    @Test
    void shouldReturnEmptyListWhenNoAuditLogsExist() {

        when(auditLogRepository.findAllByOrderByCreatedAtDesc())
                .thenReturn(List.of());

        AuditLogService auditLogService =
                new AuditLogService(auditLogRepository);

        List<AuditLogResponse> result =
                auditLogService.getAllAuditLogs();

        assertNotNull(result);
        assertTrue(result.isEmpty());

        verify(auditLogRepository)
                .findAllByOrderByCreatedAtDesc();
    }
}