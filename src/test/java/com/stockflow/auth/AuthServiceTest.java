package com.stockflow.auth;

import com.stockflow.auth.dto.AuthResponse;
import com.stockflow.auth.dto.LoginRequest;
import com.stockflow.user.User;
import com.stockflow.user.UserRepository;
import com.stockflow.user.UserRole;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private TokenService tokenService;

    @InjectMocks
    private AuthService authService;

    @Test
    void returnsTokenForValidCredentials() {
        User user = user();
        LoginRequest request = new LoginRequest("  ADMIN@EXAMPLE.COM ", "hemmelig123");
        AuthResponse expected = new AuthResponse("jwt", "Bearer", null, null);
        when(userRepository.findByEmailIgnoreCase("admin@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("hemmelig123", "bcrypt-hash")).thenReturn(true);
        when(tokenService.issueToken(user)).thenReturn(expected);

        AuthResponse response = authService.login(request);

        assertThat(response).isSameAs(expected);
        verify(tokenService).issueToken(user);
    }

    @Test
    void rejectsUnknownEmailWithoutCheckingPassword() {
        LoginRequest request = new LoginRequest("unknown@example.com", "hemmelig123");
        when(userRepository.findByEmailIgnoreCase("unknown@example.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(BadCredentialsException.class)
                .hasMessage("Invalid email or password");

        verify(passwordEncoder, never()).matches("hemmelig123", "bcrypt-hash");
        verify(tokenService, never()).issueToken(org.mockito.ArgumentMatchers.any());
    }

    @Test
    void rejectsWrongPasswordWithGenericMessage() {
        User user = user();
        LoginRequest request = new LoginRequest("admin@example.com", "feil-passord");
        when(userRepository.findByEmailIgnoreCase("admin@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("feil-passord", "bcrypt-hash")).thenReturn(false);

        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(BadCredentialsException.class)
                .hasMessage("Invalid email or password");

        verify(tokenService, never()).issueToken(org.mockito.ArgumentMatchers.any());
    }

    private User user() {
        return User.builder()
                .id(1L)
                .name("Admin")
                .email("admin@example.com")
                .passwordHash("bcrypt-hash")
                .role(UserRole.ADMIN)
                .build();
    }
}
