package com.att.tdp.issueflow.security;

import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class TokenBlacklist {
    private final Set<String> tokens = ConcurrentHashMap.newKeySet();

    public void block(String token) {
        if (token != null && !token.isBlank()) {
            tokens.add(token);
        }
    }

    public boolean isBlocked(String token) {
        return token != null && tokens.contains(token);
    }
}
