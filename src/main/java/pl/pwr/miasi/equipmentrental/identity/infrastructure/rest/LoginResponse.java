package pl.pwr.miasi.equipmentrental.identity.infrastructure.rest;

import pl.pwr.miasi.equipmentrental.identity.domain.Role;

import java.time.Instant;
import java.util.UUID;

public record LoginResponse(
        UUID token,
        UUID userId,
        String email,
        Role role,
        Instant expiresAt
) {
}