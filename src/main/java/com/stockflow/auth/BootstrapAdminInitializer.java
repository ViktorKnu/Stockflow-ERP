package com.stockflow.auth;

import com.stockflow.audit.AuditAction;
import com.stockflow.audit.AuditLogService;
import com.stockflow.config.SecurityProperties;
import com.stockflow.user.User;
import com.stockflow.user.UserRepository;
import com.stockflow.user.UserRole;
import com.stockflow.user.dto.UserCreateRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;
import java.util.Set;

@Component
@RequiredArgsConstructor
@Slf4j
public class BootstrapAdminInitializer implements ApplicationRunner {

    private final SecurityProperties securityProperties;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuditLogService auditLogService;
    private final Validator validator;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        SecurityProperties.BootstrapAdmin config = securityProperties.bootstrapAdmin();
        if (config == null || allBlank(config.email(), config.password())) {
            return;
        }
        if (isBlank(config.email()) || isBlank(config.password())) {
            throw new IllegalStateException(
                    "STOCKFLOW_ADMIN_EMAIL and STOCKFLOW_ADMIN_PASSWORD must either both be set or both be empty"
            );
        }

        UserCreateRequest request = new UserCreateRequest(
                config.name(),
                config.email(),
                config.password(),
                UserRole.ADMIN
        );
        validate(request);

        String email = config.email().trim().toLowerCase(Locale.ROOT);
        User existing = userRepository.findByEmailIgnoreCase(email).orElse(null);
        if (existing != null && existing.getRole() == UserRole.ADMIN) {
            log.info("Bootstrap administrator is already configured for {}", email);
            return;
        }

        User admin = existing == null ? new User() : existing;
        admin.setName(config.name().trim());
        admin.setEmail(email);
        admin.setPasswordHash(passwordEncoder.encode(config.password()));
        admin.setRole(UserRole.ADMIN);

        User savedAdmin = userRepository.save(admin);
        auditLogService.record(
                AuditAction.USER_BOOTSTRAPPED,
                "User",
                savedAdmin.getId(),
                "Administrator bootstrapped with email " + email
        );
        log.info("Bootstrap administrator is ready for {}", email);
    }

    private void validate(UserCreateRequest request) {
        Set<ConstraintViolation<UserCreateRequest>> violations = validator.validate(request);
        if (!violations.isEmpty()) {
            String message = violations.stream()
                    .map(violation -> violation.getPropertyPath() + " " + violation.getMessage())
                    .sorted()
                    .findFirst()
                    .orElse("invalid configuration");
            throw new IllegalStateException("Invalid bootstrap administrator configuration: " + message);
        }
    }

    private boolean allBlank(String first, String second) {
        return isBlank(first) && isBlank(second);
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
