package com.alonso.salesapp.service.impl;

import com.alonso.salesapp.model.Role;
import com.alonso.salesapp.model.User;
import com.alonso.salesapp.repository.UserRepo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("CustomUserDetailsService Tests")
class CustomUserDetailsServiceTest {

    @Mock
    private UserRepo userRepo;

    @InjectMocks
    private CustomUserDetailsService customUserDetailsService;

    private User user;

    @BeforeEach
    void setUp() {
        Role role = Role.builder()
                .idRole(1)
                .name("ADMIN")
                .enabled(true)
                .build();

        user = User.builder()
                .idUser(1)
                .username("testuser")
                .password("encodedPassword")
                .role(role)
                .enabled(true)
                .build();
    }

    @Test
    @DisplayName("Should load user by username successfully")
    void shouldLoadUserByUsername() {
        // Given
        when(userRepo.findByUsername("testuser")).thenReturn(Optional.of(user));

        // When
        UserDetails userDetails = customUserDetailsService.loadUserByUsername("testuser");

        // Then
        assertNotNull(userDetails);
        assertEquals("testuser", userDetails.getUsername());
        assertEquals("encodedPassword", userDetails.getPassword());
        assertTrue(userDetails.isEnabled());
        assertEquals(1, userDetails.getAuthorities().size());

        verify(userRepo, times(1)).findByUsername("testuser");
    }

    @Test
    @DisplayName("Should throw UsernameNotFoundException when user not found")
    void shouldThrowExceptionWhenUserNotFound() {
        // Given
        when(userRepo.findByUsername(anyString())).thenReturn(Optional.empty());

        // When & Then
        UsernameNotFoundException exception = assertThrows(
                UsernameNotFoundException.class,
                () -> customUserDetailsService.loadUserByUsername("nonexistent")
        );

        assertTrue(exception.getMessage().contains("User not found with username: nonexistent"));
        verify(userRepo, times(1)).findByUsername("nonexistent");
    }
}

