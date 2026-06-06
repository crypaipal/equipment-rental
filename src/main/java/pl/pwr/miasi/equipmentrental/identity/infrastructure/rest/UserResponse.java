package pl.pwr.miasi.equipmentrental.identity.infrastructure.rest;

import pl.pwr.miasi.equipmentrental.identity.domain.Role;

import java.time.Instant;
import java.util.UUID;

public record UserResponse(
        UUID id,
        String firstName,
        String lastName,
        String email,
        Role role,
        Instant lockedUntil,
        String lockReason
) {
}