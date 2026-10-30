package com.climbers.hub.auth.dto;
import jakarta.validation.constraints.*;

public record SignUpRequest(
        @NotBlank @Pattern(regexp = "[a-z0-9_]{4,50}") String loginId,
        @NotBlank @Size(max = 50) String name,
        @NotBlank @Email @Size(max = 255) String email,
        @NotBlank @Size(min = 8, max = 72) String password) {}
