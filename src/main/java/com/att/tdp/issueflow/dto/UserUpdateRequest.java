package com.att.tdp.issueflow.dto;

import com.att.tdp.issueflow.model.Role;

public record UserUpdateRequest(String fullName, Role role) {}
