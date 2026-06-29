package com.stockflow.user;

import com.stockflow.audit.AuditAction;
import com.stockflow.audit.AuditLogService;
import com.stockflow.exception.DuplicateResourceException;
import com.stockflow.exception.ResourceNotFoundException;
import com.stockflow.user.dto.UserCreateRequest;
import com.stockflow.user.dto.UserResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private AuditLogService auditLogService;

    @InjectMocks
    private UserService userService;

    @Test
    void canCreateEmployeeUserWithHashedPassword() {
        UserCreateRequest request = new UserCreateRequest(
                "  Kari Nordmann  ",
                "  KARI@EXAMPLE.COM  ",
                "hemmelig123"
        );
        when(userRepository.existsByEmailIgnoreCase("kari@example.com")).thenReturn(false);
        when(passwordEncoder.encode("hemmelig123")).thenReturn("bcrypt-hash");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            user.setId(1L);
            user.setCreatedAt(LocalDateTime.of(2026, 6, 29, 10, 0));
            user.setUpdatedAt(LocalDateTime.of(2026, 6, 29, 10, 0));
            return user;
        });

        UserResponse response = userService.create(request);

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        User savedUser = userCaptor.getValue();

        assertThat(savedUser.getName()).isEqualTo("Kari Nordmann");
        assertThat(savedUser.getEmail()).isEqualTo("kari@example.com");
        assertThat(savedUser.getPasswordHash()).isEqualTo("bcrypt-hash");
        assertThat(savedUser.getRole()).isEqualTo(UserRole.EMPLOYEE);
        assertThat(response.id()).isEqualTo(1L);
        assertThat(response.role()).isEqualTo(UserRole.EMPLOYEE);
        verify(passwordEncoder).encode("hemmelig123");
        verify(auditLogService).record(
                AuditAction.USER_CREATED,
                "User",
                1L,
                "User created with email kari@example.com"
        );
    }

    @Test
    void cannotCreateUserWithDuplicateEmail() {
        UserCreateRequest request = new UserCreateRequest(
                "Kari Nordmann",
                "Kari@Example.com",
                "hemmelig123"
        );
        when(userRepository.existsByEmailIgnoreCase("kari@example.com")).thenReturn(true);

        assertThatThrownBy(() -> userService.create(request))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("kari@example.com");

        verify(passwordEncoder, never()).encode(any());
        verify(userRepository, never()).save(any());
    }

    @Test
    void canFindAllUsers() {
        when(userRepository.findAllByOrderByCreatedAtDesc()).thenReturn(List.of(user()));

        List<UserResponse> users = userService.findAll();

        assertThat(users).hasSize(1);
        assertThat(users.getFirst().email()).isEqualTo("kari@example.com");
        assertThat(users.getFirst().role()).isEqualTo(UserRole.EMPLOYEE);
    }

    @Test
    void throwsWhenUserDoesNotExist() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.findById(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("User not found");
    }

    private User user() {
        return User.builder()
                .id(1L)
                .name("Kari Nordmann")
                .email("kari@example.com")
                .passwordHash("bcrypt-hash")
                .role(UserRole.EMPLOYEE)
                .createdAt(LocalDateTime.of(2026, 6, 29, 10, 0))
                .updatedAt(LocalDateTime.of(2026, 6, 29, 10, 0))
                .build();
    }
}
