package pl.pwr.miasi.equipmentrental.identity.application.result;

import pl.pwr.miasi.equipmentrental.identity.domain.Role;

import java.time.Instant;
import java.util.UUID;

public record LoginResult(
        UUID token,
        UUID userId,
        String email,
        Role role,
        Instant expiresAt
) {
}