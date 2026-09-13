package com.finance.wallet.controller;

import com.finance.wallet.dto.AdminUserResponse;
import com.finance.wallet.entity.User;
import com.finance.wallet.service.AdminService;

import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class AdminControllerTest {

    private final AdminService adminService =
            mock(AdminService.class);

    private final AdminController adminController =
            new AdminController(adminService);

    @Test
    void setUserActiveStatus_shouldReturnActivatedUser() {

        User user = new User();

        user.setId(1L);
        user.setEmail("student@example.com");
        user.setFullName("Student");
        user.setRole("ROLE_ADMIN");
        user.setActive(true);
        user.setPasswordHash("secret-hash");

        Authentication authentication =
                mock(Authentication.class);

        when(authentication.getPrincipal())
                .thenReturn(1L);

        when(adminService.setUserActiveStatus(
                1L,
                true,
                1L
        )).thenReturn(user);

        ResponseEntity<AdminUserResponse> response =
                adminController.setUserActiveStatus(
                        1L,
                        true,
                        authentication
                );

        assertEquals(
                200,
                response.getStatusCode().value()
        );

        AdminUserResponse adminUser =
                response.getBody();

        assertEquals(
                1L,
                adminUser.getId()
        );

        assertEquals(
                "student@example.com",
                adminUser.getEmail()
        );

        assertEquals(
                "Student",
                adminUser.getFullName()
        );

        assertEquals(
                "ROLE_ADMIN",
                adminUser.getRole()
        );

        assertEquals(
                true,
                adminUser.isActive()
        );

        verify(adminService).setUserActiveStatus(
                1L,
                true,
                1L
        );
    }

    @Test
    void setUserActiveStatus_shouldReturnSuspendedUser() {

        User user = new User();

        user.setId(2L);
        user.setEmail("receiver@example.com");
        user.setFullName("Receiver");
        user.setRole("ROLE_USER");
        user.setActive(false);
        user.setPasswordHash("secret-hash");

        Authentication authentication =
                mock(Authentication.class);

        when(authentication.getPrincipal())
                .thenReturn(1L);

        when(adminService.setUserActiveStatus(
                2L,
                false,
                1L
        )).thenReturn(user);

        ResponseEntity<AdminUserResponse> response =
                adminController.setUserActiveStatus(
                        2L,
                        false,
                        authentication
                );

        assertEquals(
                200,
                response.getStatusCode().value()
        );

        AdminUserResponse adminUser =
                response.getBody();

        assertEquals(
                2L,
                adminUser.getId()
        );

        assertEquals(
                "receiver@example.com",
                adminUser.getEmail()
        );

        assertEquals(
                "Receiver",
                adminUser.getFullName()
        );

        assertEquals(
                "ROLE_USER",
                adminUser.getRole()
        );

        assertEquals(
                false,
                adminUser.isActive()
        );

        verify(adminService).setUserActiveStatus(
                2L,
                false,
                1L
        );
    }

    @Test
    void getAllUsers_shouldReturnUserDtos() {

        User admin = new User();

        admin.setId(1L);
        admin.setEmail("student@example.com");
        admin.setFullName("Student");
        admin.setRole("ROLE_ADMIN");
        admin.setActive(true);
        admin.setPasswordHash("secret-hash");

        User user = new User();

        user.setId(2L);
        user.setEmail("receiver@example.com");
        user.setFullName("Receiver");
        user.setRole("ROLE_USER");
        user.setActive(false);
        user.setPasswordHash("another-secret-hash");

        when(adminService.getAllUsers())
                .thenReturn(List.of(admin, user));

        ResponseEntity<List<AdminUserResponse>> response =
                adminController.getAllUsers();

        assertEquals(
                200,
                response.getStatusCode().value()
        );

        List<AdminUserResponse> users =
                response.getBody();

        assertEquals(
                2,
                users.size()
        );

        assertEquals(
                1L,
                users.get(0).getId()
        );

        assertEquals(
                "student@example.com",
                users.get(0).getEmail()
        );

        assertEquals(
                "ROLE_ADMIN",
                users.get(0).getRole()
        );

        assertEquals(
                true,
                users.get(0).isActive()
        );

        assertEquals(
                2L,
                users.get(1).getId()
        );

        assertEquals(
                "receiver@example.com",
                users.get(1).getEmail()
        );

        assertEquals(
                "ROLE_USER",
                users.get(1).getRole()
        );

        assertEquals(
                false,
                users.get(1).isActive()
        );

        verify(adminService).getAllUsers();
    }
}