package com.finance.wallet.controller;

import com.finance.wallet.dto.AuditLogResponse;
import com.finance.wallet.service.AuditLogService;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/audit-logs")
public class AdminAuditLogController {

    private final AuditLogService auditLogService;

    public AdminAuditLogController(AuditLogService auditLogService) {
        this.auditLogService = auditLogService;
    }

    @GetMapping
    public ResponseEntity<List<AuditLogResponse>> getAllAuditLogs() {

        return ResponseEntity.ok(
                auditLogService.getAllAuditLogs()
        );
    }
}