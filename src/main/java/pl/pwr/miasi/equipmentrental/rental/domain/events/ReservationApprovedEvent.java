package pl.pwr.miasi.equipmentrental.rental.domain.events;

import pl.pwr.miasi.equipmentrental.shared.domain.DomainEvent;

import java.time.Instant;
import java.util.UUID;

public record ReservationApprovedEvent(
        UUID eventId,
        Instant occurredAt,
        UUID reservationId,
        UUID userId,
        UUID assetId
) implements DomainEvent {

    public static ReservationApprovedEvent create(UUID reservationId, UUID userId, UUID assetId) {
        return new ReservationApprovedEvent(
                UUID.randomUUID(),
                Instant.now(),
                reservationId,
                userId,
                assetId
        );
    }

    @Override
    public String eventType() {
        return "ReservationApprovedEvent";
    }
}