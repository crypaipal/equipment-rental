package pl.pwr.miasi.equipmentrental.identity.application.result;

import pl.pwr.miasi.equipmentrental.identity.domain.Role;

import java.time.Instant;
import java.util.UUID;

public record UserResult(
        UUID id,
        String firstName,
        String lastName,
        String email,
        Role role,
        Instant lockedUntil,
        String lockReason
) {
}