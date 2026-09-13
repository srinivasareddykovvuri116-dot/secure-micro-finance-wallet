package com.finance.wallet.service;

import com.finance.wallet.entity.User;
import com.finance.wallet.exception.ResourceNotFoundException;
import com.finance.wallet.repository.UserRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class AdminService {

    private final UserRepository userRepository;
    private final AuditLogService auditLogService;

    public AdminService(
            UserRepository userRepository,
            AuditLogService auditLogService) {

        this.userRepository = userRepository;
        this.auditLogService = auditLogService;
    }

    @Transactional(readOnly = true)
    public List<User> getAllUsers() {

        return userRepository.findAll();
    }

    @Transactional
    public User setUserActiveStatus(
            Long userId,
            boolean active,
            Long requestingAdminId) {

        User user = userRepository.findById(userId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "User not found"
                        ));

        if (!active && userId.equals(requestingAdminId)) {

            throw new IllegalArgumentException(
                    "Administrators cannot suspend their own account"
            );
        }

        user.setActive(active);

        User savedUser = userRepository.save(user);

        String action = active
                ? "USER_ACTIVATED"
                : "USER_SUSPENDED";

        String details = active
                ? "User account activated by administrator"
                : "User account suspended by administrator";

        auditLogService.log(
                savedUser,
                action,
                details
        );

        return savedUser;
    }
}