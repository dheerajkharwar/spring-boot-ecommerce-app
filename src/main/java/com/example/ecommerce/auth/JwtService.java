package com.example.ecommerce.auth;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.Base64;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

@Service
public class JwtService {

    private static final String HMAC_ALGORITHM = "HmacSHA256";
    private static final String HEADER = "{\"alg\":\"HS256\",\"typ\":\"JWT\"}";
    private static final Pattern STRING_CLAIM = Pattern.compile("\"%s\"\\s*:\\s*\"([^\"]*)\"");
    private static final Pattern NUMBER_CLAIM = Pattern.compile("\"%s\"\\s*:\\s*(\\d+)");

    private final String secret;
    private final long expirationSeconds;

    public JwtService(@Value("${security.jwt.secret:change-this-development-secret-key}") String secret,
            @Value("${security.jwt.expiration-seconds:86400}") long expirationSeconds) {
        this.secret = secret;
        this.expirationSeconds = expirationSeconds;
    }

    public String generateToken(AppUser user) {
        Instant now = Instant.now();
        String payload = "{"
                + "\"sub\":\"" + escape(user.getUsername()) + "\","
                + "\"role\":\"" + user.getRole().name() + "\","
                + "\"iat\":" + now.getEpochSecond() + ","
                + "\"exp\":" + now.plusSeconds(expirationSeconds).getEpochSecond()
                + "}";

        return encode(HEADER, payload);
    }

    public String extractUsername(String token) {
        return stringClaim(payload(token), "sub");
    }

    public boolean isTokenValid(String token, UserDetails userDetails) {
        String username = extractUsername(token);
        return username.equals(userDetails.getUsername()) && !isExpired(token) && hasValidSignature(token);
    }

    private boolean isExpired(String token) {
        long expiration = numberClaim(payload(token), "exp");
        return Instant.now().getEpochSecond() >= expiration;
    }

    private String payload(String token) {
        try {
            String[] parts = token.split("\\.");
            if (parts.length != 3) {
                throw new IllegalArgumentException("Invalid JWT format");
            }
            byte[] payload = Base64.getUrlDecoder().decode(parts[1]);
            return new String(payload, StandardCharsets.UTF_8);
        } catch (Exception exception) {
            throw new IllegalArgumentException("Invalid JWT token");
        }
    }

    private String stringClaim(String payload, String name) {
        Matcher matcher = Pattern.compile(String.format(STRING_CLAIM.pattern(), name)).matcher(payload);
        if (!matcher.find()) {
            throw new IllegalArgumentException("Invalid JWT token");
        }
        return matcher.group(1);
    }

    private long numberClaim(String payload, String name) {
        Matcher matcher = Pattern.compile(String.format(NUMBER_CLAIM.pattern(), name)).matcher(payload);
        if (!matcher.find()) {
            throw new IllegalArgumentException("Invalid JWT token");
        }
        return Long.parseLong(matcher.group(1));
    }

    private String encode(String value) {
        return Base64.getUrlEncoder().withoutPadding()
                .encodeToString(value.getBytes(StandardCharsets.UTF_8));
    }

    public boolean hasValidSignature(String token) {
        try {
            String[] parts = token.split("\\.");
            if (parts.length != 3) {
                return false;
            }

            String signedContent = parts[0] + "." + parts[1];
            String expectedSignature = sign(signedContent);
            return MessageDigest.isEqual(
                    expectedSignature.getBytes(StandardCharsets.UTF_8),
                    parts[2].getBytes(StandardCharsets.UTF_8)
            );
        } catch (Exception exception) {
            return false;
        }
    }

    private String encode(String header, String claims) {
        String headerPart = encode(header);
        String payloadPart = encode(claims);
        return headerPart + "." + payloadPart + "." + sign(headerPart + "." + payloadPart);
    }

    private String escape(String value) {
        return value.replace("\\", "\\\\").replace("\"", "\\\"");
    }

    private String sign(String content) {
        try {
            Mac mac = Mac.getInstance(HMAC_ALGORITHM);
            mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), HMAC_ALGORITHM));
            return Base64.getUrlEncoder().withoutPadding()
                    .encodeToString(mac.doFinal(content.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception exception) {
            throw new IllegalStateException("Unable to sign JWT token", exception);
        }
    }
}
