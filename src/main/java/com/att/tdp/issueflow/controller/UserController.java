package com.att.tdp.issueflow.controller;

import com.att.tdp.issueflow.dto.*;
import com.att.tdp.issueflow.service.UserService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/users")
public class UserController {
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public List<UserResponse> list() {
        return userService.list();
    }

    @GetMapping("/{userId}")
    public UserResponse get(@PathVariable Long userId) {
        return userService.get(userId);
    }

    @PostMapping
    public UserResponse create(@Valid @RequestBody UserCreateRequest request) {
        return userService.create(request);
    }

    @PostMapping("/update/{userId}")
    public void update(@PathVariable Long userId, @RequestBody UserUpdateRequest request) {
        userService.update(userId, request);
    }

    @DeleteMapping("/{userId}")
    public void delete(@PathVariable Long userId) {
        userService.delete(userId);
    }

    @GetMapping("/{userId}/mentions")
    public MentionPageResponse mentions(@PathVariable Long userId,
                                        @RequestParam(defaultValue = "1") int page,
                                        @RequestParam(defaultValue = "20") int pageSize) {
        return userService.mentions(userId, page, pageSize);
    }
}
