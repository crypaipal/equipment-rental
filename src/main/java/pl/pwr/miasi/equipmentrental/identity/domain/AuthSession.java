package pl.pwr.miasi.equipmentrental.identity.domain;

import pl.pwr.miasi.equipmentrental.shared.exception.BusinessException;

import java.time.Instant;
import java.util.UUID;

public class AuthSession {

    private final UUID token;
    private final UUID userId;
    private final Instant createdAt;
    private final Instant expiresAt;

    public AuthSession(UUID token, UUID userId, Instant createdAt, Instant expiresAt) {
        if (token == null) {
            throw new BusinessException("Session token cannot be null");
        }

        if (userId == null) {
            throw new BusinessException("Session user id cannot be null");
        }

        if (createdAt == null) {
            throw new BusinessException("Session creation date cannot be null");
        }

        if (expiresAt == null || !expiresAt.isAfter(createdAt)) {
            throw new BusinessException("Session expiration date must be after creation date");
        }

        this.token = token;
        this.userId = userId;
        this.createdAt = createdAt;
        this.expiresAt = expiresAt;
    }

    public static AuthSession create(UUID userId) {
        Instant now = Instant.now();

        return new AuthSession(
                UUID.randomUUID(),
                userId,
                now,
                now.plusSeconds(2 * 60 * 60)
        );
    }

    public UUID getToken() {
        return token;
    }

    public UUID getUserId() {
        return userId;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getExpiresAt() {
        return expiresAt;
    }
}