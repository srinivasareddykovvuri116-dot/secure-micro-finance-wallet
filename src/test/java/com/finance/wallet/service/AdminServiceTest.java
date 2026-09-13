package com.finance.wallet.service;

import com.finance.wallet.entity.User;
import com.finance.wallet.exception.ResourceNotFoundException;
import com.finance.wallet.repository.UserRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AdminServiceTest {

    private UserRepository userRepository;
    private AuditLogService auditLogService;
    private AdminService adminService;

    @BeforeEach
    void setUp() {

        userRepository = mock(UserRepository.class);
        auditLogService = mock(AuditLogService.class);

        adminService = new AdminService(
                userRepository,
                auditLogService
        );
    }

    @Test
    void getAllUsers_shouldReturnAllUsers() {

        User user1 = new User();
        user1.setId(1L);

        User user2 = new User();
        user2.setId(2L);

        when(userRepository.findAll())
                .thenReturn(List.of(user1, user2));

        List<User> result = adminService.getAllUsers();

        assertEquals(2, result.size());
        assertEquals(1L, result.get(0).getId());
        assertEquals(2L, result.get(1).getId());

        verify(userRepository).findAll();
    }

    @Test
    void setUserActiveStatus_shouldActivateUser() {

        User user = new User();
        user.setId(2L);
        user.setActive(false);

        when(userRepository.findById(2L))
                .thenReturn(Optional.of(user));

        when(userRepository.save(user))
                .thenReturn(user);

        User result = adminService.setUserActiveStatus(
                2L,
                true,
                1L
        );

        assertTrue(result.isActive());

        verify(userRepository).save(user);

        verify(auditLogService).log(
                user,
                "USER_ACTIVATED",
                "User account activated by administrator"
        );
    }

    @Test
    void setUserActiveStatus_shouldSuspendUser() {

        User user = new User();
        user.setId(2L);
        user.setActive(true);

        when(userRepository.findById(2L))
                .thenReturn(Optional.of(user));

        when(userRepository.save(user))
                .thenReturn(user);

        User result = adminService.setUserActiveStatus(
                2L,
                false,
                1L
        );

        assertFalse(result.isActive());

        verify(userRepository).save(user);

        verify(auditLogService).log(
                user,
                "USER_SUSPENDED",
                "User account suspended by administrator"
        );
    }

    @Test
    void setUserActiveStatus_shouldRejectSelfSuspension() {

        User admin = new User();
        admin.setId(1L);
        admin.setActive(true);

        when(userRepository.findById(1L))
                .thenReturn(Optional.of(admin));

        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> adminService.setUserActiveStatus(
                                1L,
                                false,
                                1L
                        )
                );

        assertEquals(
                "Administrators cannot suspend their own account",
                exception.getMessage()
        );

        verify(userRepository, never()).save(any());
        verifyNoInteractions(auditLogService);
    }

    @Test
    void setUserActiveStatus_shouldThrowWhenUserDoesNotExist() {

        when(userRepository.findById(99L))
                .thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> adminService.setUserActiveStatus(
                        99L,
                        true,
                        1L
                )
        );

        verify(userRepository, never()).save(any());
        verifyNoInteractions(auditLogService);
    }
}