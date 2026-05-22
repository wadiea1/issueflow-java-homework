package com.att.tdp.issueflow.dto;

import com.att.tdp.issueflow.model.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record UserCreateRequest(
        @NotBlank String username,
        @Email @NotBlank String email,
        @NotBlank String fullName,
        @NotNull Role role,
        String password
) {}
