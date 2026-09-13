package com.finance.wallet.security;

import com.finance.wallet.entity.User;
import com.finance.wallet.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CustomUserDetailsServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private CustomUserDetailsService userDetailsService;

    @Test
    void shouldLoadUserByEmail() {
        User user = new User();
        user.setEmail("user@example.com");
        user.setPasswordHash("hashed-password");
        user.setRole("ROLE_USER");

        when(userRepository.findByEmail("user@example.com"))
                .thenReturn(Optional.of(user));

        UserDetails result =
                userDetailsService.loadUserByUsername("user@example.com");

        assertEquals("user@example.com", result.getUsername());
        assertEquals("hashed-password", result.getPassword());
        assertTrue(result.getAuthorities().stream()
                .anyMatch(authority ->
                        authority.getAuthority().equals("ROLE_USER")));

        verify(userRepository).findByEmail("user@example.com");
    }

    @Test
    void shouldThrowExceptionWhenUserNotFound() {
        when(userRepository.findByEmail("missing@example.com"))
                .thenReturn(Optional.empty());

        UsernameNotFoundException exception = assertThrows(
                UsernameNotFoundException.class,
                () -> userDetailsService.loadUserByUsername("missing@example.com")
        );

        assertEquals("User not found", exception.getMessage());

        verify(userRepository).findByEmail("missing@example.com");
    }
}