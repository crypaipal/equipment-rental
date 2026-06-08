package pl.pwr.miasi.equipmentrental.identity.infrastructure.security;

import pl.pwr.miasi.equipmentrental.identity.domain.Role;

import java.util.UUID;

public record AuthenticatedUser(
        UUID userId,
        String email,
        Role role
) {
}