package com.finance.wallet.service;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;

import org.mockito.InjectMocks;
import org.mockito.Mock;

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.finance.wallet.entity.User;
import com.finance.wallet.repository.UserRepository;
import com.finance.wallet.repository.WalletRepository;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private WalletRepository walletRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private AuditLogService auditLogService;

    @InjectMocks
    private UserService userService;


    @Test
    void loginUser_shouldRejectSuspendedUser() {

        User user = new User();
        user.setId(2L);
        user.setEmail("receiver@example.com");
        user.setPasswordHash("hashed-password");
        user.setActive(false);

        when(userRepository.findByEmail("receiver@example.com"))
                .thenReturn(Optional.of(user));

        when(passwordEncoder.matches("User12345", "hashed-password"))
                .thenReturn(true);

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> userService.loginUser(
                        "receiver@example.com",
                        "User12345"
                )
        );

        assertEquals(
                "Account is suspended",
                exception.getMessage()
        );

        verify(userRepository)
                .findByEmail("receiver@example.com");

        verify(passwordEncoder)
                .matches(
                        "User12345",
                        "hashed-password"
                );
    }


    @Test
    void loginUser_shouldAllowActiveUser() {

        User user = new User();
        user.setId(1L);
        user.setEmail("student@example.com");
        user.setPasswordHash("hashed-password");
        user.setActive(true);

        when(userRepository.findByEmail("student@example.com"))
                .thenReturn(Optional.of(user));

        when(passwordEncoder.matches(
                "Admin12345",
                "hashed-password"
        )).thenReturn(true);

        User result = userService.loginUser(
                "student@example.com",
                "Admin12345"
        );

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals(
                "student@example.com",
                result.getEmail()
        );
        assertTrue(result.isActive());

        verify(userRepository)
                .findByEmail("student@example.com");

        verify(passwordEncoder)
                .matches(
                        "Admin12345",
                        "hashed-password"
                );
    }


    @Test
    void loginUser_shouldRejectWrongPassword() {

        User user = new User();
        user.setId(1L);
        user.setEmail("student@example.com");
        user.setPasswordHash("hashed-password");
        user.setActive(true);

        when(userRepository.findByEmail("student@example.com"))
                .thenReturn(Optional.of(user));

        when(passwordEncoder.matches(
                "WrongPassword",
                "hashed-password"
        )).thenReturn(false);

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> userService.loginUser(
                        "student@example.com",
                        "WrongPassword"
                )
        );

        assertEquals(
                "Invalid email or password",
                exception.getMessage()
        );

        verify(userRepository)
                .findByEmail("student@example.com");

        verify(passwordEncoder)
                .matches(
                        "WrongPassword",
                        "hashed-password"
                );
    }


    @Test
    void registerUser_shouldCreateUserAndWallet() {

        when(userRepository.existsByEmail("newuser@example.com"))
                .thenReturn(false);

        when(passwordEncoder.encode("Password123"))
                .thenReturn("hashed-password");

        User savedUser = new User();
        savedUser.setId(3L);
        savedUser.setEmail("newuser@example.com");
        savedUser.setFullName("New User");
        savedUser.setPasswordHash("hashed-password");

        when(userRepository.save(any(User.class)))
                .thenReturn(savedUser);

        User result = userService.registerUser(
                "newuser@example.com",
                "Password123",
                "New User"
        );

        assertNotNull(result);
        assertEquals(3L, result.getId());
        assertEquals(
                "newuser@example.com",
                result.getEmail()
        );
        assertEquals(
                "New User",
                result.getFullName()
        );
        assertEquals(
                "hashed-password",
                result.getPasswordHash()
        );

        verify(userRepository)
                .existsByEmail("newuser@example.com");

        verify(passwordEncoder)
                .encode("Password123");

        verify(userRepository)
                .save(any(User.class));

        verify(walletRepository)
                .save(any());

        verify(auditLogService)
                .log(
                        savedUser,
                        "USER_REGISTERED",
                        "User registered successfully"
                );
    }


    @Test
    void registerUser_shouldRejectDuplicateEmail() {

        when(userRepository.existsByEmail("student@example.com"))
                .thenReturn(true);

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> userService.registerUser(
                        "student@example.com",
                        "Password123",
                        "Student"
                )
        );

        assertEquals(
                "Email already registered",
                exception.getMessage()
        );

        verify(userRepository)
                .existsByEmail("student@example.com");

        verify(userRepository, never())
                .save(any(User.class));

        verify(passwordEncoder, never())
                .encode(anyString());

        verify(walletRepository, never())
                .save(any());

        verify(auditLogService, never())
                .log(
                        any(User.class),
                        anyString(),
                        anyString()
                );
    }


    @Test
        void loginUser_shouldRejectUnknownEmail() {

        when(userRepository.findByEmail("unknown@example.com"))
                .thenReturn(Optional.empty());

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> userService.loginUser(
                        "unknown@example.com",
                        "Password123"
                )
        );

        assertEquals(
                "Invalid email or password",
                exception.getMessage()
        );

        verify(userRepository)
                .findByEmail("unknown@example.com");

        verify(passwordEncoder, never())
                .matches(anyString(), anyString());
        }
}