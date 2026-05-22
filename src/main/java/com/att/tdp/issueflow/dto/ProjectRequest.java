package com.att.tdp.issueflow.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record ProjectRequest(
        @NotBlank String name,
        String description,
        @NotNull Long ownerId
) {}
