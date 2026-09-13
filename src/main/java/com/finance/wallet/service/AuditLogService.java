package com.finance.wallet.service;

import com.finance.wallet.dto.AuditLogResponse;
import com.finance.wallet.entity.AuditLog;
import com.finance.wallet.entity.User;
import com.finance.wallet.repository.AuditLogRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class AuditLogService {

    private final AuditLogRepository auditLogRepository;

    public AuditLogService(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

    @Transactional
    public void log(User user, String action, String details) {

        AuditLog auditLog = new AuditLog();

        auditLog.setUser(user);
        auditLog.setAction(action);
        auditLog.setDetails(details);

        auditLogRepository.save(auditLog);
    }

    @Transactional(readOnly = true)
    public List<AuditLogResponse> getAllAuditLogs() {

        return auditLogRepository
                .findAllByOrderByCreatedAtDesc()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    private AuditLogResponse toResponse(AuditLog auditLog) {

        return new AuditLogResponse(
                auditLog.getId(),
                auditLog.getUser() != null
                        ? auditLog.getUser().getId()
                        : null,
                auditLog.getAction(),
                auditLog.getDetails(),
                auditLog.getCreatedAt()
        );
    }
}