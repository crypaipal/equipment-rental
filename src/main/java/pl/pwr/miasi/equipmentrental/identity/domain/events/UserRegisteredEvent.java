package pl.pwr.miasi.equipmentrental.identity.domain.events;

import pl.pwr.miasi.equipmentrental.identity.domain.Role;
import pl.pwr.miasi.equipmentrental.shared.domain.DomainEvent;

import java.time.Instant;
import java.util.UUID;

public record UserRegisteredEvent(
        UUID eventId,
        Instant occurredAt,
        UUID userId,
        String email,
        Role role
) implements DomainEvent {

    public static UserRegisteredEvent create(UUID userId, String email, Role role) {
        return new UserRegisteredEvent(
                UUID.randomUUID(),
                Instant.now(),
                userId,
                email,
                role
        );
    }

    @Override
    public String eventType() {
        return "UserRegisteredEvent";
    }
}