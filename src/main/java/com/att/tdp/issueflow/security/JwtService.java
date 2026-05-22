package com.att.tdp.issueflow.security;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.Map;

@Service
public class JwtService {
    private final String secret;
    private final long expirationSeconds;
    private final ObjectMapper mapper = new ObjectMapper();

    public JwtService(
            @Value("${security.jwt.secret:issueflow-secret-key-change-me}") String secret,
            @Value("${security.jwt.expiration-seconds:3600}") long expirationSeconds
    ) {
        this.secret = secret;
        this.expirationSeconds = expirationSeconds;
    }

    public String generateToken(String username) {
        try {
            Map<String, Object> header = new LinkedHashMap<>();
            header.put("alg", "HS256");
            header.put("typ", "JWT");

            Map<String, Object> payload = new LinkedHashMap<>();
            payload.put("sub", username);
            payload.put("exp", Instant.now().plusSeconds(expirationSeconds).getEpochSecond());

            String unsigned = base64(mapper.writeValueAsBytes(header)) + "." + base64(mapper.writeValueAsBytes(payload));
            return unsigned + "." + sign(unsigned);
        } catch (Exception e) {
            throw new IllegalStateException("Could not create JWT token", e);
        }
    }

    public String usernameFromToken(String token) {
        Map<String, Object> payload = payload(token);
        return payload.get("sub") == null ? null : payload.get("sub").toString();
    }

    public boolean isValid(String token) {
        try {
            String[] parts = token.split("\\.");
            if (parts.length != 3) {
                return false;
            }
            String unsigned = parts[0] + "." + parts[1];
            if (!sign(unsigned).equals(parts[2])) {
                return false;
            }
            Map<String, Object> payload = payload(token);
            Object exp = payload.get("exp");
            long expiry = exp instanceof Number ? ((Number) exp).longValue() : Long.parseLong(exp.toString());
            return Instant.now().getEpochSecond() < expiry;
        } catch (Exception e) {
            return false;
        }
    }

    public long expirationSeconds() {
        return expirationSeconds;
    }

    private Map<String, Object> payload(String token) {
        try {
            String[] parts = token.split("\\.");
            byte[] decoded = Base64.getUrlDecoder().decode(parts[1]);
            return mapper.readValue(decoded, new TypeReference<>() {});
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid token");
        }
    }

    private String sign(String unsigned) throws Exception {
        Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
        return base64(mac.doFinal(unsigned.getBytes(StandardCharsets.UTF_8)));
    }

    private String base64(byte[] bytes) {
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }
}
