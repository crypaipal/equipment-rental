package pl.pwr.miasi.equipmentrental.identity.domain.events;

import pl.pwr.miasi.equipmentrental.shared.domain.DomainEvent;

import java.time.Instant;
import java.util.UUID;

public record UserBlockedEvent(
        UUID eventId,
        Instant occurredAt,
        UUID userId,
        Instant lockedUntil,
        String reason
) implements DomainEvent {

    public static UserBlockedEvent create(UUID userId, Instant lockedUntil, String reason) {
        return new UserBlockedEvent(
                UUID.randomUUID(),
                Instant.now(),
                userId,
                lockedUntil,
                reason
        );
    }

    @Override
    public String eventType() {
        return "UserBlockedEvent";
    }
}