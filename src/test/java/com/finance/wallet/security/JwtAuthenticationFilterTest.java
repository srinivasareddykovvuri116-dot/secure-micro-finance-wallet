package com.finance.wallet.security;

import com.finance.wallet.entity.User;
import com.finance.wallet.repository.UserRepository;
import com.finance.wallet.service.JwtService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.io.IOException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JwtAuthenticationFilterTest {

    @Mock
    private JwtService jwtService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private FilterChain filterChain;

    @InjectMocks
    private JwtAuthenticationFilter filter;

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void shouldContinueWhenAuthorizationHeaderIsMissing()
            throws ServletException, IOException {

        when(request.getHeader("Authorization"))
                .thenReturn(null);

        filter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        verifyNoInteractions(jwtService, userRepository);

        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    void shouldContinueWhenAuthorizationHeaderIsNotBearer()
            throws ServletException, IOException {

        when(request.getHeader("Authorization"))
                .thenReturn("Basic abc123");

        filter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        verifyNoInteractions(jwtService, userRepository);

        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    void shouldContinueWhenTokenIsInvalid()
            throws ServletException, IOException {

        when(request.getHeader("Authorization"))
                .thenReturn("Bearer invalid-token");

        when(jwtService.isTokenValid("invalid-token"))
                .thenReturn(false);

        filter.doFilterInternal(request, response, filterChain);

        verify(jwtService).isTokenValid("invalid-token");
        verify(filterChain).doFilter(request, response);
        verify(userRepository, never()).findById(anyLong());

        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    void shouldContinueWhenUserIsNotFound()
            throws ServletException, IOException {

        when(request.getHeader("Authorization"))
                .thenReturn("Bearer valid-token");

        when(jwtService.isTokenValid("valid-token"))
                .thenReturn(true);

        when(jwtService.extractUserId("valid-token"))
                .thenReturn("10");

        when(userRepository.findById(10L))
                .thenReturn(Optional.empty());

        filter.doFilterInternal(request, response, filterChain);

        verify(userRepository).findById(10L);
        verify(filterChain).doFilter(request, response);

        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    void shouldContinueWhenUserIsInactive()
            throws ServletException, IOException {

        when(request.getHeader("Authorization"))
                .thenReturn("Bearer valid-token");

        when(jwtService.isTokenValid("valid-token"))
                .thenReturn(true);

        when(jwtService.extractUserId("valid-token"))
                .thenReturn("10");

        User user = new User();
        user.setId(10L);
        user.setEmail("user@example.com");
        user.setRole("ROLE_USER");
        user.setActive(false);

        when(userRepository.findById(10L))
                .thenReturn(Optional.of(user));

        filter.doFilterInternal(request, response, filterChain);

        verify(userRepository).findById(10L);
        verify(filterChain).doFilter(request, response);

        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    void shouldAuthenticateActiveUser()
            throws ServletException, IOException {

        when(request.getHeader("Authorization"))
                .thenReturn("Bearer valid-token");

        when(jwtService.isTokenValid("valid-token"))
                .thenReturn(true);

        when(jwtService.extractUserId("valid-token"))
                .thenReturn("10");

        User user = new User();
        user.setId(10L);
        user.setEmail("user@example.com");
        user.setRole("ROLE_USER");
        user.setActive(true);

        when(userRepository.findById(10L))
                .thenReturn(Optional.of(user));

        filter.doFilterInternal(request, response, filterChain);

        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        assertNotNull(authentication);
        assertEquals(10L, authentication.getPrincipal());
        assertEquals("ROLE_USER",
                authentication.getAuthorities()
                        .iterator()
                        .next()
                        .getAuthority());

        verify(userRepository).findById(10L);
        verify(filterChain).doFilter(request, response);
    }
}